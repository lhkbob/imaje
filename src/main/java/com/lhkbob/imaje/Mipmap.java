package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.layout.ArrayBackedPixel;
import com.lhkbob.imaje.layout.PixelArray;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.ImageUtils;
import com.lhkbob.imaje.util.IteratorChain;
import com.lhkbob.imaje.util.SpliteratorChain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;

/**
 */
public class Mipmap<T extends Color> implements Image<T> {
  private final List<PixelArray> mipmaps;
  private final Class<T> colorType;

  public Mipmap(List<Raster<T>> mipmaps) {
    Arguments.notEmpty("mipmaps", mipmaps);

    colorType = mipmaps.get(0).getColorType();
    List<PixelArray> arrays = new ArrayList<>(mipmaps.size());
    for (Raster<T> l : mipmaps) {
      arrays.add(l.getPixelArray());
    }

    // Verify that dimensions and properties are as expected
    ImageUtils.checkMipmapCompleteness(arrays);
    this.mipmaps = Collections.unmodifiableList(arrays);
  }

  public Mipmap(Class<T> colorType, List<PixelArray> mipmaps) {
    Arguments.notNull("colorType", colorType);
    Arguments.notEmpty("mipmaps", mipmaps);

    ImageUtils.checkMipmapCompleteness(mipmaps);
    ImageUtils.checkImageCompatibility(colorType, mipmaps);

    this.colorType = colorType;
    this.mipmaps = Collections.unmodifiableList(new ArrayList<>(mipmaps));
  }

  public double get(int level, int x, int y, T result) {
    return getPixelArray(level).get(x, y, result.getChannels());
  }

  public double getAlpha(int level, int x, int y) {
    return getPixelArray(level).getAlpha(x, y);
  }

  public void set(int level, int x, int y, T value) {
    getPixelArray(level).set(x, y, value.getChannels(), getPixelArray(level).getAlpha(x, y));
  }

  public void set(int level, int x, int y, T value, double alpha) {
    getPixelArray(level).set(x, y, value.getChannels(), alpha);
  }

  public void setAlpha(int level, int x, int y, double alpha) {
    getPixelArray(level).setAlpha(x, y, alpha);
  }

  @Override
  public Class<T> getColorType() {
    return colorType;
  }


  public Raster<T> getMipmapAsRaster(int level) {
    return new Raster<>(colorType, getPixelArray(level));
  }

  public PixelArray getPixelArray(int level) {
    return mipmaps.get(level);
  }

  public List<PixelArray> getPixelArrays() {
    return mipmaps;
  }

  @Override
  public int getLayerCount() {
    return 1;
  }

  @Override
  public int getMipmapCount() {
    return mipmaps.size();
  }

  @Override
  public Pixel<T> getPixel(int layer, int mipmapLevel, int... coords) {
    Arguments.equals("layer", 0, layer);
    Arguments.equals("coords.length", 2, coords.length);
    return getPixel(mipmapLevel, coords[0], coords[1]);
  }

  @Override
  public boolean hasAlphaChannel() {
    return getPixelArray(0).getFormat().hasAlphaChannel();
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

  public Pixel<T> getPixel(int level, int x, int y) {
    ArrayBackedPixel<T> p = new ArrayBackedPixel<>(colorType, getPixelArray(level), 0, level);
    p.refreshAt(x, y);
    return p;
  }

  @Override
  public Iterator<Pixel<T>> iterator() {
    List<Iterator<Pixel<T>>> wrappedLayers = new ArrayList<>(getMipmapCount());
    for (int i = 0; i < getMipmapCount(); i++) {
      wrappedLayers.add(ArrayBackedPixel.iterator(colorType, getPixelArray(i), 0, i));
    }
    return new IteratorChain<>(wrappedLayers);
  }

  @Override
  public Spliterator<Pixel<T>> spliterator() {
    List<Spliterator<Pixel<T>>> wrappedLayers = new ArrayList<>(getMipmapCount());
    for (int i = 0; i < getMipmapCount(); i++) {
      wrappedLayers.add(ArrayBackedPixel.spliterator(colorType, getPixelArray(i), 0, i));
    }
    return new SpliteratorChain<>(wrappedLayers);
  }
}
