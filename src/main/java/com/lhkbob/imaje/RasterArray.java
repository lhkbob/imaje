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
import com.lhkbob.imaje.util.IteratorChain;
import com.lhkbob.imaje.util.SpliteratorChain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;

/**
 */
public class RasterArray<T extends Color> implements Image<T> {
  private final List<PixelArray> layers;
  private final Class<T> colorType;

  public RasterArray(List<Raster<T>> layers) {
    Arguments.notEmpty("layers", layers);

    colorType = layers.get(0).getColorType();
    List<PixelArray> arrays = new ArrayList<>(layers.size());
    for (Raster<T> l : layers) {
      arrays.add(l.getPixelArray());
    }

    Images.checkArrayCompleteness(arrays);
    this.layers = Collections.unmodifiableList(arrays);
  }

  public RasterArray(Class<T> colorType, List<PixelArray> layers) {
    Arguments.notNull("colorType", colorType);
    Arguments.notEmpty("layers", layers);

    Images.checkArrayCompleteness(layers);
    Images.checkImageCompatibility(colorType, layers);

    this.layers = Collections.unmodifiableList(new ArrayList<>(layers));
    this.colorType = colorType;
  }

  public RasterArray<T> getSubImage(int x, int y, int w, int h) {
    return new RasterArray<>(
        colorType, SubImagePixelArray.createSubImagesForArray(layers, x, y, w, h));
  }

  public double get(int layer, int x, int y, T result) {
    return getPixelArray(layer).get(x, y, result.getChannels());
  }

  public double getAlpha(int layer, int x, int y) {
    return getPixelArray(layer).getAlpha(x, y);
  }

  public void set(int layer, int x, int y, T value) {
    getPixelArray(layer).set(x, y, value.getChannels(), getPixelArray(layer).getAlpha(x, y));
  }

  public void set(int layer, int x, int y, T value, double alpha) {
    getPixelArray(layer).set(x, y, value.getChannels(), alpha);
  }

  public void setAlpha(int layer, int x, int y, double alpha) {
    getPixelArray(layer).setAlpha(x, y, alpha);
  }

  @Override
  public Class<T> getColorType() {
    return colorType;
  }

  public Raster<T> getLayerAsRaster(int index) {
    return new Raster<>(colorType, getPixelArray(index));
  }

  public PixelArray getPixelArray(int index) {
    return layers.get(index);
  }

  public Volume<T> getAsVolume() {
    return new Volume<>(colorType, getPixelArrays());
  }

  @Override
  public int getLayerCount() {
    return layers.size();
  }

  @Override
  public int getMipmapCount() {
    return 1;
  }

  @Override
  public Pixel<T> getPixel(int layer, int mipmapLevel, int... coords) {
    Arguments.equals("mipmapLevel", 0, mipmapLevel);
    Arguments.equals("coords.length", 2, coords.length);
    return getPixel(layer, coords[0], coords[1]);
  }

  public List<PixelArray> getPixelArrays() {
    return layers;
  }

  @Override
  public boolean hasAlphaChannel() {
    return layers.get(0).getFormat().hasAlphaChannel();
  }

  @Override
  public int getDimensionality() {
    return 2;
  }

  @Override
  public int getDimension(int dim) {
    if (dim == 0) {
      return getPixelArray(0).getLayout().getWidth();
    } else if (dim == 1) {
      return getPixelArray(0).getLayout().getHeight();
    } else {
      return 1;
    }
  }

  public Pixel<T> getPixel(int layer, int x, int y) {
    ArrayBackedPixel<T> p = new ArrayBackedPixel<>(colorType, getPixelArray(layer), layer, 0);
    p.refreshAt(x, y);
    return p;
  }

  @Override
  public Iterator<Pixel<T>> iterator() {
    // FIXME should these aggregate iterators here and in Mipmap, MipmapArray, Volume, and MipmapVolume
    // be sorted by data offset to improve cache performance?
    List<Iterator<Pixel<T>>> wrappedLayers = new ArrayList<>(getLayerCount());
    for (int i = 0; i < getLayerCount(); i++) {
      wrappedLayers.add(ArrayBackedPixel.iterator(colorType, getPixelArray(i), i, 0));
    }
    return new IteratorChain<>(wrappedLayers);
  }

  @Override
  public Spliterator<Pixel<T>> spliterator() {
    List<Spliterator<Pixel<T>>> wrappedLayers = new ArrayList<>(getLayerCount());
    for (int i = 0; i < getLayerCount(); i++) {
      wrappedLayers.add(ArrayBackedPixel.spliterator(colorType, getPixelArray(i), i, 0));
    }
    return new SpliteratorChain<>(wrappedLayers);
  }
}
