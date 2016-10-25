/*
 * BSD 3-Clause License - imaJe
 *
 * Copyright (c) 2016, Michael Ludwig
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.layout.PixelArray;
import com.lhkbob.imaje.layout.PixelFormat;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.Functions;

import java.util.Collection;
import java.util.List;

/**
 *
 */
public final class Images {
  private Images() {

  }

  public static <T extends Color> ImageBuilder.OfRaster<T> newRaster(Class<T> colorType) {
    return new ImageBuilder.OfRaster<>(colorType);
  }

  public static <T extends Color> ImageBuilder.OfMipmap<T> newMipmap(Class<T> colorType) {
    return new ImageBuilder.OfMipmap<>(colorType);
  }

  public static <T extends Color> ImageBuilder.OfRasterArray<T> newRasterArray(Class<T> colorType) {
    return new ImageBuilder.OfRasterArray<>(colorType);
  }

  public static <T extends Color> ImageBuilder.OfMipmapArray<T> newMipmapArray(Class<T> colorType) {
    return new ImageBuilder.OfMipmapArray<>(colorType);
  }

  public static <T extends Color> ImageBuilder.OfVolume<T> newVolume(Class<T> colorType) {
    return new ImageBuilder.OfVolume<>(colorType);
  }

  public static <T extends Color> ImageBuilder.OfMipmapVolume<T> newMipmapVolume(Class<T> colorType) {
    return new ImageBuilder.OfMipmapVolume<>(colorType);
  }

  public static long getUncompressedImageSize(int... dimensions) {
    long size = 0;
    for (int j = 0; j < dimensions.length; j++) {
      size *= dimensions[j];
    }
    return size;
  }

  public static int getMaxMipmaps(int maxDimension) {
    return Functions.floorLog2(maxDimension) + 1;
  }

  public static int getMaxMipmaps(int... dimensions) {
    int max = 0;
    for (int i = 0; i < dimensions.length; i++) {
      if (dimensions[i] > max) {
        max = dimensions[i];
      }
    }

    return getMaxMipmaps(max);
  }

  public static int getMaxMipmaps(int width, int height) {
    return getMaxMipmaps(Math.max(width, height));
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
        if (!format.isLogicallyEquivalent(img.getFormat())) {
          throw new IllegalArgumentException(
              "Images differ in format, expected " + format + " but was " + img.getFormat());
        }
        if (width != img.getLayout().getWidth() || height != img.getLayout().getHeight()) {
          throw new IllegalArgumentException(
              "Image dimensions differ, expected (" + width + ", " + height + ") but was (" + img
                  .getLayout().getWidth() + ", " + img.getLayout().getHeight() + ")");
        }
      }
    }
  }

  public static void checkImageCompatibility(
      Class<? extends Color> colorType, Collection<PixelArray> images) {
    for (PixelArray p : images) {
      checkImageCompatibility(colorType, p);
    }
  }

  public static void checkImageCompatibility(Class<? extends Color> colorType, PixelArray data) {
    Arguments.equals("channel count", Color.getChannelCount(colorType),
        data.getColorChannelCount());
  }

  public static void checkMipmapCompleteness(List<PixelArray> levels) {
    int baseWidth = levels.get(0).getLayout().getWidth();
    int baseHeight = levels.get(0).getLayout().getHeight();
    PixelFormat baseFormat = levels.get(0).getFormat();

    int mipmapCount = getMaxMipmaps(baseWidth, baseHeight);
    if (levels.size() != mipmapCount) {
      throw new IllegalArgumentException(
          "Incorrect number of level images provided, expected " + mipmapCount + " but was "
              + levels.size());
    }
    for (int i = 0; i < mipmapCount; i++) {
      int w = getMipmapDimension(baseWidth, i);
      int h = getMipmapDimension(baseHeight, i);
      if (w != levels.get(i).getLayout().getWidth() || h != levels.get(i).getLayout().getHeight()) {
        throw new IllegalArgumentException(
            "Mipmap level " + i + " image has incorrect dimensions, expected (" + w + ", " + h
                + ") but was (" + levels.get(i).getLayout().getWidth() + ", " + levels.get(i)
                .getLayout().getHeight() + ")");
      }
      if (levels.get(i).getFormat().isLogicallyEquivalent(baseFormat)) {
        throw new IllegalArgumentException(
            "Mipmap level " + i + " image has different format, expected " + baseFormat
                + ", but was " + levels.get(i).getFormat());
      }
    }
  }
}
