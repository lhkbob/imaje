package com.lhkbob.imaje.util;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.layout.PixelArray;
import com.lhkbob.imaje.layout.PixelFormat;

import java.util.Collection;
import java.util.List;

/**
 *
 */
public final class ImageUtils {
  private ImageUtils() {

  }

  public static long getUncompressedImageSize(int[] dimensions, boolean mipmapped) {
    long size = 0;
    int mipmaps = mipmapped ? getMipmapCount(dimensions) : 1;
    for (int i = 0; i < mipmaps; i++) {
      int forLevel = 1;
      for (int j = 0; j < dimensions.length; j++) {
        forLevel *= getMipmapDimension(dimensions[j], i);
      }
      size += forLevel;
    }
    return size;
  }

  public static int getMipmapCount(int maxDimension) {
    return (int) Math.floor(Math.log(maxDimension) / Math.log(2.0)) + 1;
  }

  public static int getMipmapCount(int... dimensions) {
    int max = 0;
    for (int i = 0; i < dimensions.length; i++) {
      if (dimensions[i] > max) {
        max = dimensions[i];
      }
    }

    return getMipmapCount(max);
  }

  public static int getMipmapCount(int width, int height) {
    return getMipmapCount(Math.max(width, height));
  }

  public static int getMipmapDimension(int topLevelDimension, int level) {
    return Math.max(topLevelDimension >> level, 1);
  }

  public static int[] getMipmapDimensions(int[] topLevelDimensions, int level) {
    int[] mip = new int[topLevelDimensions.length];
    for (int i = 0; i < mip.length; i++) {
      mip[i] = getMipmapDimension(topLevelDimensions[i], level);
    }
    return mip;
  }

  public static void checkArrayCompleteness(Collection<PixelArray> images) {
    int width = 0;
    int height = 0;
    PixelFormat format = null;

    for (PixelArray img : images) {
      if (format == null) {
        format = img.getFormat();
        width = img.getLayout().getWidth();
        height = img.getLayout().getHeight();
      } else {
        if (!format.equals(img.getFormat())) {
          throw new IllegalArgumentException("Images differ in format, expected " + format+ " but was " + img.getFormat());
        }
        if (width != img.getLayout().getWidth() || height != img.getLayout().getHeight()) {
          throw new IllegalArgumentException("Image dimensions differ, expected (" + width + ", " + height + ") but was (" + img.getLayout().getWidth() + ", " + img.getLayout().getHeight() + ")");
        }
      }
    }
  }

  public static void checkImageCompatibility(Class<? extends Color> colorType, Collection<PixelArray> images) {
    for (PixelArray p : images) {
      checkImageCompatibility(colorType, p);
    }
  }

  public static void checkImageCompatibility(Class<? extends Color> colorType, PixelArray data) {
    Arguments.equals("channel count", Color.getChannelCount(colorType),
        data.getFormat().getColorChannelCount());
  }

  public static void checkMipmapCompleteness(List<PixelArray> levels) {
    int baseWidth = levels.get(0).getLayout().getWidth();
    int baseHeight = levels.get(0).getLayout().getHeight();
    PixelFormat baseFormat = levels.get(0).getFormat();

    int mipmapCount = getMipmapCount(baseWidth, baseHeight);
    if (levels.size() != mipmapCount) {
      throw new IllegalArgumentException("Incorrect number of level images provided, expected " + mipmapCount + " but was " + levels.size());
    }
    for (int i = 0; i < mipmapCount; i++) {
      int w = getMipmapDimension(baseWidth, i);
      int h = getMipmapDimension(baseHeight, i);
      if (w != levels.get(i).getLayout().getWidth() || h != levels.get(i).getLayout().getHeight()) {
        throw new IllegalArgumentException("Mipmap level " + i + " image has incorrect dimensions, expected (" + w + ", " + h + ") but was (" + levels.get(i).getLayout().getWidth() + ", " + levels.get(i).getLayout().getHeight() + ")");
      }
      if (levels.get(i).getFormat().equals(baseFormat)) {
        throw new IllegalArgumentException("Mipmap level " + i + " image has different format, expected " + baseFormat + ", but was " + levels.get(i).getFormat());
      }
    }
  }
}
