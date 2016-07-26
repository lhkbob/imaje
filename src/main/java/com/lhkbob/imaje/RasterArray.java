package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;
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
public class RasterArray<T extends Color> implements Image<T> {
  private final List<Raster<T>> layers;

  public RasterArray(List<Raster<T>> layers) {
    if (layers.isEmpty()) {
      throw new IllegalArgumentException("Must provide at least one layer");
    }

    ImageUtils.checkMultiImageCompatibility(layers);

    this.layers = Collections.unmodifiableList(new ArrayList<>(layers));
  }

  @Override
  public Class<T> getColorType() {
    return layers.get(0).getColorType();
  }

  public Raster<T> getLayer(int index) {
    return layers.get(index);
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
  public Pixel<T> getPixel(int x, int y, int mipmapLevel, int layer) {
    if (mipmapLevel != 0)
      throw new IllegalArgumentException("Image is not mipmapped, expected mipmap level of 0, not: " + mipmapLevel);
    return getPixel(x, y, layer);
  }

  public List<Raster<T>> getLayers() {
    return layers;
  }

  @Override
  public boolean hasAlphaChannel() {
    return layers.get(0).hasAlphaChannel();
  }

  @Override
  public int getWidth() {
    return layers.get(0).getWidth();
  }

  @Override
  public int getHeight() {
    return layers.get(0).getHeight();
  }

  public Pixel<T> getPixel(int x, int y, int layer) {
    return layers.get(layer).getPixelForMipmapArray(x, y, 0, layer);
  }

  @Override
  public Iterator<Pixel<T>> iterator() {
    List<Iterator<Pixel<T>>> wrappedLayers = new ArrayList<>(layers.size());
    for (int i = 0; i < layers.size(); i++) {
      wrappedLayers.add(layers.get(i).iteratorForMipmapArray(0, i));
    }
    return new IteratorChain<>(wrappedLayers);
  }

  @Override
  public Spliterator<Pixel<T>> spliterator() {
    List<Spliterator<Pixel<T>>> wrappedLayers = new ArrayList<>(layers.size());
    for (int i = 0; i < layers.size(); i++) {
      wrappedLayers.add(layers.get(i).spliteratorForMipmapArray(0, i));
    }
    return new SpliteratorChain<>(wrappedLayers);
  }
}
