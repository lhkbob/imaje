package com.lhkbob.imaje.sampler;

import com.lhkbob.imaje.MipmapArray;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.Images;

/**
 *
 */
public class MipmapNearestIndexedSampler2D<T extends Color> implements IndexedSampler2D<T> {
  private final MipmapArray<T> image;
  private final BoundaryStrategy<T> uStrategy;
  private final BoundaryStrategy<T> vStrategy;

  private final int levelBase;
  private final int levelMax;

  public MipmapNearestIndexedSampler2D(
      MipmapArray<T> image, BoundaryStrategy<T> uStrategy, BoundaryStrategy<T> vStrategy,
      int levelBase, int levelMax) {
    Arguments.notNull("image", image);
    Arguments.notNull("uStrategy", uStrategy);
    Arguments.notNull("vStrategy", vStrategy);
    Arguments
        .checkArrayRange("mipmap levels", image.getMipmapCount(), levelBase, levelMax - levelBase);

    this.image = image;
    this.uStrategy = uStrategy;
    this.vStrategy = vStrategy;

    this.levelBase = levelBase;
    this.levelMax = levelMax;
  }

  @Override
  public MipmapArray<T> getImage() {
    return image;
  }

  @Override
  public double sample(double u, double v, int index, double lod, T result) {
    Arguments.checkIndex("index", image.getLayerCount(), index);
    int mipmap = Samplers.lodToMipmap(lod, levelBase, levelMax);

    // Get dimensions for the selected mipmap
    int w = Images.getMipmapDimension(image.getWidth(), mipmap);
    int h = Images.getMipmapDimension(image.getHeight(), mipmap);

    int i = Samplers.coordToTexel(u, w);
    int j = Samplers.coordToTexel(v, h);

    if (uStrategy.useBorder(i, w)) {
      result.set(uStrategy.getBorderColor().getChannels());
      return uStrategy.getBorderAlpha();
    } else if (vStrategy.useBorder(j, h)) {
      result.set(vStrategy.getBorderColor().getChannels());
      return vStrategy.getBorderAlpha();
    } else {
      // Ensure u and v are within the image bounds
      int x = uStrategy.wrap(i, w);
      int y = vStrategy.wrap(j, h);

      return image.get(index, mipmap, x, y, result);
    }
  }
}
