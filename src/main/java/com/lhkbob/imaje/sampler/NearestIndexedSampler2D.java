package com.lhkbob.imaje.sampler;

import com.lhkbob.imaje.RasterArray;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.util.Arguments;

/**
 *
 */
public class NearestIndexedSampler2D<T extends Color> implements IndexedSampler2D<T> {
  private final RasterArray<T> image;
  private final BoundaryStrategy<T> uStrategy;
  private final BoundaryStrategy<T> vStrategy;

  public NearestIndexedSampler2D(
      RasterArray<T> image, BoundaryStrategy<T> uStrategy, BoundaryStrategy<T> vStrategy) {
    Arguments.notNull("image", image);
    Arguments.notNull("uStrategy", uStrategy);
    Arguments.notNull("vStrategy", vStrategy);

    this.image = image;
    this.uStrategy = uStrategy;
    this.vStrategy = vStrategy;
  }

  @Override
  public RasterArray<T> getImage() {
    return image;
  }

  @Override
  public double sample(double u, double v, int index, double lod, T result) {
    Arguments.checkIndex("index", image.getLayerCount(), index);

    // Ignore LOD parameter since there is only one mipmap level
    int i = Samplers.coordToTexel(u, image.getWidth());
    int j = Samplers.coordToTexel(v, image.getHeight());

    if (uStrategy.useBorder(i, image.getWidth())) {
      result.set(uStrategy.getBorderColor().getChannels());
      return uStrategy.getBorderAlpha();
    } else if (vStrategy.useBorder(j, image.getHeight())) {
      result.set(vStrategy.getBorderColor().getChannels());
      return vStrategy.getBorderAlpha();
    } else {
      // Ensure u and v are within the image bounds
      int x = uStrategy.wrap(i, image.getWidth());
      int y = vStrategy.wrap(j, image.getHeight());

      return image.get(index, x, y, result);
    }
  }
}
