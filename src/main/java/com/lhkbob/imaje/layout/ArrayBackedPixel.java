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
package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.Images;
import com.lhkbob.imaje.Pixel;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.util.Arguments;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 *
 */
public class ArrayBackedPixel<T extends Color> implements Pixel<T> {
  private int x;
  private int y;

  private final int[] otherCoords;
  private final int layer;
  private final int level;

  private final PixelArray data;
  private final long[] bandOffsets;

  private final T cachedColor;
  private transient double cachedAlpha;

  public ArrayBackedPixel(
      Class<T> colorType, PixelArray data, int layer, int mipmap, int... fixedCoords) {
    Arguments.notNull("colorType", colorType);
    Arguments.notNull("data", data);

    this.data = data;
    this.bandOffsets = new long[data.getBandCount()];
    cachedColor = Color.newInstance(colorType);
    otherCoords = fixedCoords.clone();
    level = mipmap;
    this.layer = layer;

    // Final validation to make sure channel counts are compatible
    Images.checkImageCompatibility(colorType, data);
  }

  @Override
  public T getColor() {
    return cachedColor;
  }

  @Override
  public double getAlpha() {
    return cachedAlpha;
  }

  @Override
  public int getLayerIndex() {
    return layer;
  }

  @Override
  public int getMipmapLevel() {
    return level;
  }

  @Override
  public int getDimensionality() {
    return 2 + otherCoords.length;
  }

  @Override
  public int getCoordinate(int dim) {
    if (dim == 0) {
      return x;
    } else if (dim == 1) {
      return y;
    } else if (dim - 2 < otherCoords.length) {
      return otherCoords[dim - 2];
    } else {
      return 0;
    }
  }


  @Override
  public void setColor(T value) {
    setColor(value, cachedAlpha);
  }

  @Override
  public void setColor(T value, double a) {
    Arguments.notNull("value", value);

    if (value != cachedColor) {
      // Copy the state of value into the internal cachedColor instance so that future calls to
      // getColor are accurate
      cachedColor.set(value.getChannels());
    }

    // Now that cachedColor matches value, persist will correctly store the new value
    persist(a);
  }

  @Override
  public void setAlpha(double a) {
    cachedAlpha = a;
    data.setAlpha(x, y, a);
  }

  @Override
  public void persist() {
    persist(cachedAlpha);
  }

  @Override
  public void persist(double alpha) {
    cachedAlpha = alpha;
    data.set(x, y, cachedColor.getChannels(), alpha, bandOffsets);
  }

  @Override
  public void refresh() {
    refreshAt(x, y);
  }

  public void refreshAt(int x, int y) {
    cachedAlpha = data.get(x, y, cachedColor.getChannels(), bandOffsets);
    // The PixelArray validates x and y, so if code reaches here it was a valid coordinate and we
    // can update the pixel's location
    this.x = x;
    this.y = y;
  }

  // FIXME move the window transforming logic into PixelArrays so it can be shared, and update it
  // so that after each step it is clamped to the dimensions of the next layer (or current layer?)
  // to prevent the circumstance where a virtual array wraps a subimage array that wraps a real array,
  // and the window of the virtual array gets transformed (unclamped) until it is passed to the
  // data layout of the root, which could then access pixel values that are outside of the subimage's
  // window.
  public static <T extends Color> Iterator<Pixel<T>> iterator(
      Class<T> colorType, PixelArray data, ImageWindow window, int layer, int mipmap,
      int... fixedDims) {
    List<PixelArray> transformPath = PixelArrays.getHierarchy(data);
    RootPixelArray root = (RootPixelArray) transformPath.get(transformPath.size() - 1);

    // Convert the image window to the root coordinate space
    ImageWindow rootWindow = window.clone();
    for (PixelArray aTransformPath : transformPath) {
      aTransformPath.toParentWindow(rootWindow);
    }

    return new DefaultIterator<>(
        colorType, root.getLayout().iterator(rootWindow), transformPath, layer, mipmap, fixedDims);
  }

  public static <T extends Color> Spliterator<Pixel<T>> spliterator(
      Class<T> colorType, PixelArray data, ImageWindow window, int layer, int mipmap,
      int... fixedDims) {
    List<PixelArray> transformPath = PixelArrays.getHierarchy(data);
    RootPixelArray root = (RootPixelArray) transformPath.get(transformPath.size() - 1);

    // Convert the image window to the root coordinate space
    ImageWindow rootWindow = window.clone();
    for (PixelArray aTransformPath : transformPath) {
      aTransformPath.toParentWindow(rootWindow);
    }

    return new DefaultSpliterator<>(colorType, root.getLayout().spliterator(rootWindow),
        transformPath, layer, mipmap, fixedDims);
  }

  private static class DefaultIterator<T extends Color> implements Iterator<Pixel<T>> {
    private final Iterator<ImageCoordinate> coords;
    private final ArrayBackedPixel<T> pixel;
    private final List<PixelArray> transformPath;

    DefaultIterator(
        Class<T> colorType, Iterator<ImageCoordinate> iterator, List<PixelArray> transformPath,
        int layer, int mipmap, int... fixedDims) {
      this.transformPath = transformPath;
      coords = iterator;
      pixel = new ArrayBackedPixel<>(colorType, transformPath.get(0), layer, mipmap, fixedDims);
    }

    @Override
    public boolean hasNext() {
      return coords.hasNext();
    }

    @Override
    public Pixel<T> next() {
      ImageCoordinate c = coords.next();
      // Back track to convert from root coordinate space to the top level pixel array
      for (int i = transformPath.size() - 1; i >= 0; i--) {
        transformPath.get(i).fromParentCoordinate(c);
      }
      pixel.refreshAt(c.getX(), c.getY());
      return pixel;
    }
  }

  private static class DefaultSpliterator<T extends Color> implements Spliterator<Pixel<T>> {
    private final Spliterator<ImageCoordinate> coords;
    private final List<PixelArray> transformPath;
    private final ArrayBackedPixel<T> pixel;
    private final Class<T> colorType;

    DefaultSpliterator(
        Class<T> colorType, Spliterator<ImageCoordinate> coords, List<PixelArray> transformPath,
        int layer, int mipmap, int... fixedDims) {
      this.coords = coords;
      this.colorType = colorType;
      this.transformPath = transformPath;
      pixel = new ArrayBackedPixel<>(colorType, transformPath.get(0), layer, mipmap, fixedDims);
    }

    @Override
    public int characteristics() {
      return coords.characteristics();
    }

    @Override
    public long estimateSize() {
      return coords.estimateSize();
    }

    @Override
    public boolean tryAdvance(Consumer<? super Pixel<T>> action) {
      return coords.tryAdvance(coord -> {
        // Back track to convert from root coordinate space to the top level pixel array
        for (int i = transformPath.size() - 1; i >= 0; i--) {
          transformPath.get(i).fromParentCoordinate(coord);
        }

        pixel.refreshAt(coord.getX(), coord.getY());
        action.accept(pixel);
      });
    }

    @Override
    public Spliterator<Pixel<T>> trySplit() {
      Spliterator<ImageCoordinate> split = coords.trySplit();
      if (split == null) {
        return null;
      } else {
        return new DefaultSpliterator<>(
            colorType, split, transformPath, pixel.layer, pixel.level, pixel.otherCoords);
      }
    }
  }
}
