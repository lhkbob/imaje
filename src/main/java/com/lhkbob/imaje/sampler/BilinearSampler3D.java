package com.lhkbob.imaje.sampler;

import com.lhkbob.imaje.Volume;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.op.ColorOps;
import com.lhkbob.imaje.util.Arguments;

/**
 *
 */
public class BilinearSampler3D<T extends Color> implements Sampler3D<T> {
  private final Volume<T> image;
  private final BoundaryStrategy<T> uStrategy;
  private final BoundaryStrategy<T> vStrategy;
  private final BoundaryStrategy<T> wStrategy;

  private final T sample;

  public BilinearSampler3D(
      Volume<T> image, BoundaryStrategy<T> uStrategy, BoundaryStrategy<T> vStrategy,
      BoundaryStrategy<T> wStrategy) {
    Arguments.notNull("image", image);
    Arguments.notNull("uStrategy", uStrategy);
    Arguments.notNull("vStrategy", vStrategy);
    Arguments.notNull("wStrategy", wStrategy);

    this.image = image;
    this.uStrategy = uStrategy;
    this.vStrategy = vStrategy;
    this.wStrategy = wStrategy;

    sample = Color.newInstance(image.getColorType());
  }

  @Override
  public Volume<T> getImage() {
    return image;
  }

  private double sample(int i, int j, int k) {
    if (uStrategy.useBorder(i, image.getWidth())) {
      sample.set(uStrategy.getBorderColor().getChannels());
      return uStrategy.getBorderAlpha();
    } else if (vStrategy.useBorder(j, image.getHeight())) {
      sample.set(vStrategy.getBorderColor().getChannels());
      return vStrategy.getBorderAlpha();
    } else if (wStrategy.useBorder(k, image.getDepth())) {
      sample.set(wStrategy.getBorderColor().getChannels());
      return wStrategy.getBorderAlpha();
    } else {
      int x = uStrategy.wrap(i, image.getWidth());
      int y = vStrategy.wrap(j, image.getHeight());
      int z = wStrategy.wrap(k, image.getDepth());
      return image.get(x, y, z, sample);
    }
  }

  @Override
  public double sample(double u, double v, double w, double lod, T result) {
    // Ignore LOD value
    int i0 = Samplers.sampleToTexel(u, image.getWidth());
    int i1 = i0 + 1;
    int j0 = Samplers.sampleToTexel(v, image.getHeight());
    int j1 = j0 + 1;
    int k0 = Samplers.sampleToTexel(w, image.getDepth());
    int k1 = k0 + 1;

    double alpha = Samplers.sampleWeight(u, image.getWidth());
    double beta = Samplers.sampleWeight(v, image.getHeight());
    double gamma = Samplers.sampleWeight(w, image.getDepth());

    // Collect the eight corners (u0, v0, w0), (u1, v0, w0), (u0, v1, w0), (u1, v1, w0),
    // (u0, v0, w1), (u1, v0, w1), (u0, v1, w1), (u1, v1, w1) and weighte add them into result.
    double weight000 = (1.0 - alpha) * (1.0 - beta) * (1.0 - gamma);
    double blendedAlpha = weight000 * sample(i0, j0, k0);
    ColorOps.mul(sample, weight000, result);

    double weight100 = alpha * (1.0 - beta) * (1.0 - gamma);
    blendedAlpha += weight100 * sample(i1, j0, k0);
    ColorOps.addScaled(result, sample, weight100, result);

    double weight010 = (1.0 - alpha) * beta * (1.0 - gamma);
    blendedAlpha += weight010 * sample(i0, j1, k0);
    ColorOps.addScaled(result, sample, weight010, result);

    double weight110 = alpha * beta * (1.0 - gamma);
    blendedAlpha += weight110 * sample(i1, j1, k0);
    ColorOps.addScaled(result, sample, weight110, result);

    double weight001 = (1.0 - alpha) * (1.0 - beta) * gamma;
    blendedAlpha += weight001 * sample(i0, j0, k1);
    ColorOps.mul(sample, weight001, result);

    double weight101 = alpha * (1.0 - beta) * gamma;
    blendedAlpha += weight101 * sample(i1, j0, k1);
    ColorOps.addScaled(result, sample, weight101, result);

    double weight011 = (1.0 - alpha) * beta * gamma;
    blendedAlpha += weight011 * sample(i0, j1, k1);
    ColorOps.addScaled(result, sample, weight011, result);

    double weight111 = alpha * beta * gamma;
    blendedAlpha += weight111 * sample(i1, j1, k1);
    ColorOps.addScaled(result, sample, weight111, result);

    return blendedAlpha;
  }
}
