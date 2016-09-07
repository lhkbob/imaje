package com.lhkbob.imaje.sampler;

import com.lhkbob.imaje.Raster;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.util.Arguments;

/**
 *
 */
public class NearestSampler2D<T extends Color> implements Sampler2D<T> {
  private final Raster<T> image;
  private final BoundaryStrategy<T> uStrategy;
  private final BoundaryStrategy<T> vStrategy;

  public NearestSampler2D(
      Raster<T> image, BoundaryStrategy<T> uStrategy, BoundaryStrategy<T> vStrategy) {
    Arguments.notNull("image", image);
    Arguments.notNull("uStrategy", uStrategy);
    Arguments.notNull("vStrategy", vStrategy);

    this.image = image;
    this.uStrategy = uStrategy;
    this.vStrategy = vStrategy;
  }

  @Override
  public Raster<T> getImage() {
    return image;
  }

  @Override
  public double sample(double u, double v, double lod, T result) {
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

      return image.get(x, y, result);
    }
  }
}
