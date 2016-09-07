package com.lhkbob.imaje.sampler;

import com.lhkbob.imaje.Image;
import com.lhkbob.imaje.Volume;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.util.Arguments;

/**
 *
 */
public class NearestSampler3D<T extends Color> implements Sampler3D<T> {
  private final Volume<T> image;
  private final BoundaryStrategy<T> uStrategy;
  private final BoundaryStrategy<T> vStrategy;
  private final BoundaryStrategy<T> wStrategy;

  public NearestSampler3D(
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
  }

  @Override
  public Image<T> getImage() {
    return image;
  }

  @Override
  public double sample(double u, double v, double w, double lod, T result) {
    // Ignore LOD parameter since there is only one mipmap level
    int i = Samplers.coordToTexel(u, image.getWidth());
    int j = Samplers.coordToTexel(v, image.getHeight());
    int k = Samplers.coordToTexel(w, image.getDepth());

    if (uStrategy.useBorder(i, image.getWidth())) {
      result.set(uStrategy.getBorderColor().getChannels());
      return uStrategy.getBorderAlpha();
    } else if (vStrategy.useBorder(j, image.getHeight())) {
      result.set(vStrategy.getBorderColor().getChannels());
      return vStrategy.getBorderAlpha();
    } else if (wStrategy.useBorder(k, image.getDepth())) {
      result.set(wStrategy.getBorderColor().getChannels());
      return wStrategy.getBorderAlpha();
    } else {
      // Ensure u, v, and w are within the image bounds
      int x = uStrategy.wrap(i, image.getWidth());
      int y = vStrategy.wrap(j, image.getHeight());
      int z = wStrategy.wrap(k, image.getDepth());

      return image.get(x, y, z, result);
    }
  }
}
