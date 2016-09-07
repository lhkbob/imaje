package com.lhkbob.imaje.sampler;

import com.lhkbob.imaje.MipmapVolume;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.ImageUtils;

/**
 *
 */
public class MipmapNearestSampler3D<T extends Color> implements Sampler3D<T> {
  private final MipmapVolume<T> image;
  private final BoundaryStrategy<T> uStrategy;
  private final BoundaryStrategy<T> vStrategy;
  private final BoundaryStrategy<T> wStrategy;

  private final int levelBase;
  private final int levelMax;

  public MipmapNearestSampler3D(
      MipmapVolume<T> image, BoundaryStrategy<T> uStrategy, BoundaryStrategy<T> vStrategy,
      BoundaryStrategy<T> wStrategy, int levelBase, int levelMax) {
    Arguments.notNull("image", image);
    Arguments.notNull("uStrategy", uStrategy);
    Arguments.notNull("vStrategy", vStrategy);
    Arguments.notNull("wStrategy", wStrategy);
    Arguments
        .checkArrayRange("mipmap levels", image.getMipmapCount(), levelBase, levelMax - levelBase);

    this.image = image;
    this.uStrategy = uStrategy;
    this.vStrategy = vStrategy;
    this.wStrategy = wStrategy;

    this.levelBase = levelBase;
    this.levelMax = levelMax;
  }

  @Override
  public MipmapVolume<T> getImage() {
    return image;
  }

  @Override
  public double sample(double u, double v, double w, double lod, T result) {
    int mipmap = Samplers.lodToMipmap(lod, levelBase, levelMax);

    // Get dimensions for the selected mipmap
    int width = ImageUtils.getMipmapDimension(image.getWidth(), mipmap);
    int height = ImageUtils.getMipmapDimension(image.getHeight(), mipmap);
    int depth = ImageUtils.getMipmapDimension(image.getDepth(), mipmap);

    int i = Samplers.coordToTexel(u, width);
    int j = Samplers.coordToTexel(v, height);
    int k = Samplers.coordToTexel(w, depth);

    if (uStrategy.useBorder(i, width)) {
      result.set(uStrategy.getBorderColor().getChannels());
      return uStrategy.getBorderAlpha();
    } else if (vStrategy.useBorder(j, height)) {
      result.set(vStrategy.getBorderColor().getChannels());
      return vStrategy.getBorderAlpha();
    } else if (wStrategy.useBorder(k, depth)) {
      result.set(wStrategy.getBorderColor().getChannels());
      return wStrategy.getBorderAlpha();
    } else {
      // Ensure u, v, and w are within the image bounds
      int x = uStrategy.wrap(i, width);
      int y = vStrategy.wrap(j, height);
      int z = wStrategy.wrap(k, depth);

      return image.get(mipmap, x, y, z, result);
    }
  }
}
