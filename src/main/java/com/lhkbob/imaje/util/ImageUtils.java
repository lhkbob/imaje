package com.lhkbob.imaje.util;

import com.lhkbob.imaje.Image;
import com.lhkbob.imaje.color.Color;

import java.util.Collection;
import java.util.List;

/**
 *
 */
public final class ImageUtils {
  private ImageUtils() {

  }

  public static long getImageSize(int width, int height, int channels) {
    return getImageSize(width, height, channels, false, 1);
  }

  public static long getMipmapImageSize(int width, int height, int channels) {
    return getImageSize(width, height, channels, true, 1);
  }

  public static long getImageArraySize(int width, int height, int channels, int layers) {
    return getImageSize(width, height, channels, false, layers);
  }

  public static long getImageSize(int width, int height, int channels, boolean mipmapped, int layers) {
    long size = 0;
    int mipmaps = mipmapped ? getMipmapCount(width, height) : 1;
    for (int i = 0; i < mipmaps; i++) {
      size += getMipmapDimension(width, i) * getMipmapDimension(height, i);
    }
    return size * channels * layers;
  }

  public static int getMipmapCount(int maxDimension) {
    return (int) Math.floor(Math.log(maxDimension) / Math.log(2.0)) + 1;
  }

  public static int getMipmapCount(int width, int height) {
    return getMipmapCount(Math.max(width, height));
  }

  public static int getMipmapDimension(int topLevelDimension, int level) {
    return Math.max(topLevelDimension >> level, 1);
  }

  public static void checkMultiImageCompatibility(Collection<? extends Image<?>> images) {
    Class<? extends Color> colorType = null;
    boolean hasAlpha = false;
    int width = 0;
    int height = 0;
    int layers = 0;
    int levels = 0;

    for (Image<?> img : images) {
      if (colorType == null) {
        colorType = img.getColorType();
        width = img.getWidth();
        height = img.getHeight();
        hasAlpha = img.hasAlphaChannel();
        layers = img.getLayerCount();
        levels = img.getMipmapCount();
      } else {
        if (!colorType.equals(img.getColorType())) {
          throw new IllegalArgumentException("Images differ in color type, expected " + colorType + " but was " + img.getColorType());
        }
        if (width != img.getWidth() || height != img.getHeight()) {
          throw new IllegalArgumentException("Image dimensions differ, expected (" + width + ", " + height + ") but was (" + img.getWidth() + ", " + img.getHeight() + ")");
        }
        if (hasAlpha != img.hasAlphaChannel()) {
          throw new IllegalArgumentException("Images have alpha channel mismatch, expected " + hasAlpha + " but was " + img.hasAlphaChannel());
        }
        if (layers != img.getLayerCount()) {
          throw new IllegalArgumentException("Images have different layer counts, expected " + layers + " but was " + img.getLayerCount());
        }
        if (levels != img.getMipmapCount()) {
          throw new IllegalArgumentException("Images have different mipmap counts, expected " + levels + " but was " + img.getMipmapCount());
        }
      }
    }
  }

  public static void checkMipmapCompleteness(List<? extends Image<?>> levels) {
    int baseWidth = levels.get(0).getWidth();
    int baseHeight = levels.get(0).getHeight();

    int mipmapCount = getMipmapCount(baseWidth, baseHeight);
    if (levels.size() != mipmapCount) {
      throw new IllegalArgumentException("Incorrect number of level images provided, expected " + mipmapCount + " but was " + levels.size());
    }
    for (int i = 0; i < mipmapCount; i++) {
      int w = getMipmapDimension(baseWidth, i);
      int h = getMipmapDimension(baseHeight, i);
      if (w != levels.get(i).getWidth() || h != levels.get(i).getHeight()) {
        throw new IllegalArgumentException("Mipmap level " + i + " image has incorrect dimensions, expected (" + w + ", " + h + ") but was (" + levels.get(i).getWidth() + ", " + levels.get(i).getHeight() + ")");
      }
    }
  }
}
