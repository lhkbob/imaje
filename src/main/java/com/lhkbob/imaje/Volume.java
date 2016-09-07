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
 *
 */
public class Volume<T extends Color> implements Image<T> {
  private final List<PixelArray> zData;
  private final Class<T> colorType;

  public Volume(List<Raster<T>> zData) {
    Arguments.notEmpty("zData", zData);

    colorType = zData.get(0).getColorType();
    List<PixelArray> arrays = new ArrayList<>(zData.size());
    for (Raster<T> l : zData) {
      arrays.add(l.getPixelArray());
    }

    ImageUtils.checkArrayCompleteness(arrays);
    this.zData = Collections.unmodifiableList(arrays);
  }

  public Volume(Class<T> colorType, List<PixelArray> zData) {
    Arguments.notNull("colorType", colorType);
    Arguments.notEmpty("zData", zData);

    ImageUtils.checkArrayCompleteness(zData);
    ImageUtils.checkImageCompatibility(colorType, zData);

    this.colorType = colorType;
    this.zData = Collections.unmodifiableList(new ArrayList<>(zData));
  }

  public PixelArray getPixelArray(int z) {
    return zData.get(z);
  }

  public List<PixelArray> getPixelArrays() {
    return zData;
  }

  public Raster<T> getDepthSliceAsRaster(int z) {
    return new Raster<>(colorType, getPixelArray(z));
  }

  public RasterArray<T> getAsRasterArray() {
    return new RasterArray<>(colorType, getPixelArrays());
  }

  public Pixel<T> getPixel(int x, int y, int z) {
    ArrayBackedPixel<T> p = new ArrayBackedPixel<>(colorType, getPixelArray(z), 0, 0, z);
    p.refreshAt(x, y);
    return p;
  }

  public double get(int x, int y, int z, T result) {
    return getPixelArray(z).get(x, y, result.getChannels());
  }

  public double getAlpha(int x, int y, int z) {
    return getPixelArray(z).getAlpha(x, y);
  }

  public void set(int x, int y, int z, T value) {
    getPixelArray(z).set(x, y, value.getChannels(), getPixelArray(z).getAlpha(x, y));
  }

  public void set(int x, int y, int z, T value, double alpha) {
    getPixelArray(z).set(x, y, value.getChannels(), alpha);
  }

  public void setAlpha(int x, int y, int z, double alpha) {
    getPixelArray(z).setAlpha(x, y, alpha);
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
    Arguments.equals("layer", 0, layer);
    Arguments.equals("mipmapLevel", 0, mipmapLevel);
    Arguments.equals("coords.length", 3, coords.length);

    return getPixel(coords[0], coords[1], coords[2]);
  }

  @Override
  public Class<T> getColorType() {
    return colorType;
  }

  @Override
  public boolean hasAlphaChannel() {
    return getPixelArray(0).getFormat().hasAlphaChannel();
  }

  @Override
  public int getDimensionality() {
    return 3;
  }

  @Override
  public int getDimension(int dim) {
    if (dim == 0) {
      return getPixelArray(0).getLayout().getWidth();
    } else if (dim == 1) {
      return getPixelArray(0).getLayout().getHeight();
    } else if (dim == 2) {
      return zData.size();
    } else {
      return 1;
    }
  }

  @Override
  public Iterator<Pixel<T>> iterator() {
    int depth = getDepth();

    List<Iterator<Pixel<T>>> wrappedLayers = new ArrayList<>(depth);
    for (int i = 0; i < depth; i++) {
      wrappedLayers.add(ArrayBackedPixel.iterator(colorType, getPixelArray(i), 0, 0, i));
    }
    return new IteratorChain<>(wrappedLayers);
  }

  @Override
  public Spliterator<Pixel<T>> spliterator() {
    int depth = getDepth();

    List<Spliterator<Pixel<T>>> wrappedLayers = new ArrayList<>(depth);
    for (int i = 0; i < depth; i++) {
      wrappedLayers.add(ArrayBackedPixel.spliterator(colorType, getPixelArray(i), 0, 0, i));
    }
    return new SpliteratorChain<>(wrappedLayers);
  }
}
