package com.lhkbob.imaje.sampler;

import com.lhkbob.imaje.RasterArray;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.op.ColorOps;
import com.lhkbob.imaje.util.Arguments;

/**
 *
 */
public class BilinearIndexedSampler2D<T extends Color> implements IndexedSampler2D<T> {
  private final RasterArray<T> image;
  private final BoundaryStrategy<T> uStrategy;
  private final BoundaryStrategy<T> vStrategy;

  private final T sample;

  public BilinearIndexedSampler2D(
      RasterArray<T> image, BoundaryStrategy<T> uStrategy, BoundaryStrategy<T> vStrategy) {
    Arguments.notNull("image", image);
    Arguments.notNull("uStrategy", uStrategy);
    Arguments.notNull("vStrategy", vStrategy);

    this.image = image;
    this.uStrategy = uStrategy;
    this.vStrategy = vStrategy;

    sample = Color.newInstance(image.getColorType());
  }

  @Override
  public RasterArray<T> getImage() {
    return image;
  }

  private double sample(int i, int j, int index) {
    if (uStrategy.useBorder(i, image.getWidth())) {
      sample.set(uStrategy.getBorderColor().getChannels());
      return uStrategy.getBorderAlpha();
    } else if (vStrategy.useBorder(j, image.getHeight())) {
      sample.set(vStrategy.getBorderColor().getChannels());
      return vStrategy.getBorderAlpha();
    } else {
      int x = uStrategy.wrap(i, image.getWidth());
      int y = vStrategy.wrap(j, image.getHeight());
      return image.get(index, x, y, sample);
    }
  }

  @Override
  public double sample(double u, double v, int index, double lod, T result) {
    Arguments.checkIndex("index", image.getLayerCount(), index);

    // Ignore LOD value
    int i0 = Samplers.sampleToTexel(u, image.getWidth());
    int i1 = i0 + 1;
    int j0 = Samplers.sampleToTexel(v, image.getHeight());
    int j1 = j0 + 1;

    double alpha = Samplers.sampleWeight(u, image.getWidth());
    double beta = Samplers.sampleWeight(v, image.getHeight());

    // Collect the four corners (u0, v0), (u1, v0), (u0, v1), (u1, v1) and weighted
    // add them into result.
    double weight00 = (1.0 - alpha) * (1.0 - beta);
    double blendedAlpha = weight00 * sample(i0, i0, index);
    ColorOps.mul(sample, weight00, result);

    double weight10 = alpha * (1.0 - beta);
    blendedAlpha += weight10 * sample(i1, i0, index);
    ColorOps.addScaled(result, sample, weight10, result);

    double weight01 = (1.0 - alpha) * beta;
    blendedAlpha += weight01 * sample(i0, i1, index);
    ColorOps.addScaled(result, sample, weight01, result);

    double weight11 = alpha * beta;
    blendedAlpha += weight11 * sample(i1, i1, index);
    ColorOps.addScaled(result, sample, weight11, result);

    return blendedAlpha;
  }
}
