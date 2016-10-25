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
import com.lhkbob.imaje.layout.ArrayBackedPixel;
import com.lhkbob.imaje.layout.PixelArray;
import com.lhkbob.imaje.layout.SubImagePixelArray;
import com.lhkbob.imaje.util.Arguments;

import java.util.Iterator;
import java.util.Spliterator;

/**
 *
 */
public class Raster<T extends Color> implements Image<T> {
  private final PixelArray data;
  private final Class<T> colorType;

  public Raster(Class<T> colorType, PixelArray data) {
    Images.checkImageCompatibility(colorType, data);

    this.colorType = colorType;
    this.data = data;
  }

  public double get(int x, int y, T result) {
    return data.get(x, y, result.getChannels());
  }

  public double getAlpha(int x, int y) {
    return data.getAlpha(x, y);
  }

  public Raster<T> getSubImage(int x, int y, int w, int h) {
    return new Raster<>(colorType, SubImagePixelArray.createSubImageForRaster(data, x, y, w, h));
  }

  @Override
  public int getLayerCount() {
    return 1;
  }

  @Override
  public int getMipmapCount() {
    return 1;
  }

  @Override
  public Pixel<T> getPixel(int layer, int mipmapLevel, int... coords) {
    Arguments.equals("mipmapLevel", 0, mipmapLevel);
    Arguments.equals("layer", 0, layer);
    Arguments.equals("coords.length", 2, coords.length);
    return getPixel(coords[0], coords[1]);
  }

  @Override
  public Class<T> getColorType() {
    return colorType;
  }

  public Pixel<T> getPixel(int x, int y) {
    ArrayBackedPixel<T> p = new ArrayBackedPixel<>(colorType, data, 0, 0);
    p.refreshAt(x, y);
    return p;
  }

  @Override
  public boolean hasAlphaChannel() {
    return data.getFormat().hasAlphaChannel();
  }

  @Override
  public int getDimensionality() {
    return 2;
  }

  @Override
  public int getDimension(int dim) {
    if (dim == 0) {
      return data.getLayout().getWidth();
    } else if (dim == 1) {
      return data.getLayout().getHeight();
    } else {
      return 1;
    }
  }

  @Override
  public Iterator<Pixel<T>> iterator() {
    return ArrayBackedPixel.iterator(colorType, data, 0, 0);
  }

  public void set(int x, int y, T value) {
    data.set(x, y, value.getChannels(), data.getAlpha(x, y));
  }

  public void set(int x, int y, T value, double alpha) {
    data.set(x, y, value.getChannels(), alpha);
  }

  public void setAlpha(int x, int y, double alpha) {
    data.setAlpha(x, y, alpha);
  }

  @Override
  public Spliterator<Pixel<T>> spliterator() {
    return ArrayBackedPixel.spliterator(colorType, data, 0, 0);
  }

  public PixelArray getPixelArray() {
    return data;
  }
}
