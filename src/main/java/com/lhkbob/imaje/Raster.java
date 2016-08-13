package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.layout.ArrayBackedPixel;
import com.lhkbob.imaje.layout.PixelArray;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.ImageUtils;

import java.util.Iterator;
import java.util.Spliterator;

/**
 *
 */
public class Raster<T extends Color> implements Image<T> {
  private final PixelArray data;
  private final Class<T> colorType;

  public Raster(Class<T> colorType, PixelArray data) {
    ImageUtils.checkImageCompatibility(colorType, data);

    this.colorType = colorType;
    this.data = data;
  }

  public double get(int x, int y, T result) {
    return data.get(x, y, result.getChannels());
  }

  public double getAlpha(int x, int y) {
    return data.getAlpha(x, y);
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
