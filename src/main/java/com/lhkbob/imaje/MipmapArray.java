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
public class MipmapArray<T extends Color> implements Image<T> {
  private final List<List<PixelArray>> layers;
  private final Class<T> colorType;

  public MipmapArray(List<Mipmap<T>> layers) {
    Arguments.notEmpty("layers", layers);

    // Since the layers are already grouped as mipmaps, we can assume that each mipmap is
    // mipmap complete, and that all pixel arrays are compatible with color T.
    // The only validation that needs to be performed is that they are array complete.

    List<PixelArray> topLevelImages = new ArrayList<>();
    List<List<PixelArray>> allData = new ArrayList<>();
    for (Mipmap<T> m : layers) {
      topLevelImages.add(m.getPixelArray(0));
      // The returned list is unmodifiable and will not be changed
      allData.add(m.getPixelArrays());
    }

    ImageUtils.checkArrayCompleteness(topLevelImages);

    colorType = layers.get(0).getColorType();
    this.layers = Collections.unmodifiableList(allData);
  }

  public MipmapArray(Class<T> colorType, List<List<PixelArray>> layersOfMipmaps) {
    Arguments.notNull("colorType", colorType);
    Arguments.notEmpty("layersOfMipmaps", layersOfMipmaps);

    // Collect top-level images of all layers while validating each layer's set of mipmaps
    List<PixelArray> topLevelImages = new ArrayList<>();
    // Also collect unmodifiable copies of the mipmap lists, assuming validation succeeds
    List<List<PixelArray>> lockedCopy = new ArrayList<>(layersOfMipmaps.size());
    for (List<PixelArray> mipSet : layersOfMipmaps) {
      Arguments.notEmpty("layer", mipSet);
      ImageUtils.checkMipmapCompleteness(mipSet);
      topLevelImages.add(mipSet.get(0));

      lockedCopy.add(Collections.unmodifiableList(new ArrayList<>(mipSet)));
    }

    // Check that all top level images are array complete, and since every mipmap set was also
    // complete, that means all lower level images will also be array complete for the mip level.
    ImageUtils.checkArrayCompleteness(topLevelImages);
    ImageUtils.checkImageCompatibility(colorType, topLevelImages);

    this.colorType = colorType;
    layers = Collections.unmodifiableList(lockedCopy);
  }

  public double get(int layer, int level, int x, int y, T result) {
    return getPixelArray(layer, level).get(x, y, result.getChannels());
  }

  public double getAlpha(int layer, int level, int x, int y) {
    return getPixelArray(layer, level).getAlpha(x, y);
  }

  public void set(int layer, int level, int x, int y, T value) {
    getPixelArray(layer, level)
        .set(x, y, value.getChannels(), getPixelArray(layer, level).getAlpha(x, y));
  }

  public void set(int layer, int level, int x, int y, T value, double alpha) {
    getPixelArray(layer, level).set(x, y, value.getChannels(), alpha);
  }

  public void setAlpha(int layer, int level, int x, int y, double alpha) {
    getPixelArray(layer, level).setAlpha(x, y, alpha);
  }

  @Override
  public Class<T> getColorType() {
    return colorType;
  }

  @Override
  public int getLayerCount() {
    return layers.size();
  }

  public Mipmap<T> getLayerAsMipmap(int layer) {
    return new Mipmap<>(colorType, getPixelArraysForLayer(layer));
  }

  public RasterArray<T> getMipmapAsArray(int level) {
    return new RasterArray<>(colorType, getPixelArraysForMipmap(level));
  }

  public Raster<T> getLayerMipmapAsRaster(int layer, int level) {
    return new Raster<>(colorType, getPixelArray(layer, level));
  }

  public PixelArray getPixelArray(int layer, int level) {
    return layers.get(layer).get(level);
  }

  public List<PixelArray> getPixelArraysForMipmap(int level) {
    List<PixelArray> forLevel = new ArrayList<>(layers.size());
    for (List<PixelArray> layer : layers) {
      forLevel.add(layer.get(level));
    }
    return forLevel;
  }

  public List<PixelArray> getPixelArraysForLayer(int layer) {
    return layers.get(layer);
  }

  public List<List<PixelArray>> getPixelArrays() {
    return layers;
  }

  @Override
  public int getMipmapCount() {
    return getPixelArraysForLayer(0).size();
  }

  @Override
  public Pixel<T> getPixel(int layer, int mipmapLevel, int... coords) {
    Arguments.equals("coords.length", 2, coords.length);
    return getPixel(layer, mipmapLevel, coords[0], coords[1]);
  }

  public Pixel<T> getPixel(int layer, int mipmapLevel, int x, int y) {
    ArrayBackedPixel<T> p = new ArrayBackedPixel<>(
        colorType, getPixelArray(layer, mipmapLevel), layer, mipmapLevel);
    p.refreshAt(x, y);
    return p;
  }

  @Override
  public boolean hasAlphaChannel() {
    return getPixelArray(0, 0).getFormat().hasAlphaChannel();
  }

  @Override
  public int getDimensionality() {
    return 2;
  }

  @Override
  public int getDimension(int dim) {
    if (dim == 0) {
      return getPixelArray(0, 0).getLayout().getWidth();
    } else if (dim == 1) {
      return getPixelArray(0, 0).getLayout().getHeight();
    } else {
      return 1;
    }
  }

  @Override
  public Iterator<Pixel<T>> iterator() {
    int layerCount = getLayerCount();
    int mipmapCount = getMipmapCount();

    List<Iterator<Pixel<T>>> wrappedLayers = new ArrayList<>(layerCount * mipmapCount);
    for (int i = 0; i < layerCount; i++) {
      for (int j = 0; j < mipmapCount; j++) {
        wrappedLayers.add(ArrayBackedPixel.iterator(colorType, getPixelArray(i, j), i, j));
      }
    }
    return new IteratorChain<>(wrappedLayers);
  }

  @Override
  public Spliterator<Pixel<T>> spliterator() {
    int layerCount = getLayerCount();
    int mipmapCount = getMipmapCount();

    List<Spliterator<Pixel<T>>> wrappedLayers = new ArrayList<>(layerCount * mipmapCount);
    for (int i = 0; i < layerCount; i++) {
      for (int j = 0; j < mipmapCount; j++) {
        wrappedLayers.add(ArrayBackedPixel.spliterator(colorType, getPixelArray(i, j), i, j));
      }
    }
    return new SpliteratorChain<>(wrappedLayers);
  }
}
