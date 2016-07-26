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
public class MipmapArray<T extends Color> implements Image<T> {
  private final List<Mipmap<T>> layers;
  private final List<RasterArray<T>> levels;

  public MipmapArray(List<Mipmap<T>> layers) {
    if (layers.isEmpty()) {
      throw new IllegalArgumentException("Must provide at least one layer");
    }

    ImageUtils.checkMultiImageCompatibility(layers);

    this.layers = Collections.unmodifiableList(new ArrayList<>(layers));

    // Since all layer mipmap images have the same top-level dimension they will have the same
    // level count as well.
    int levelCount = layers.get(0).getMipmapCount();
    List<RasterArray<T>> levels = new ArrayList<>(levelCount);
    for (int i = 0; i < levelCount; i++) {
      List<Raster<T>> levelImages = new ArrayList<>(layers.size());
      for (Mipmap<T> layer : layers) {
        levelImages.add(layer.getMipmap(i));
      }
      levels.add(new RasterArray<>(levelImages));
    }
    this.levels = Collections.unmodifiableList(levels);
  }

  @Override
  public Class<T> getColorType() {
    return layers.get(0).getColorType();
  }

  public Mipmap<T> getLayer(int index) {
    return layers.get(index);
  }

  @Override
  public int getLayerCount() {
    return layers.size();
  }

  public List<Mipmap<T>> getLayers() {
    return layers;
  }

  public RasterArray<T> getMipmap(int level) {
    return levels.get(level);
  }

  public List<RasterArray<T>> getMipmaps() {
    return levels;
  }

  @Override
  public int getMipmapCount() {
    return levels.size();
  }

  public Raster<T> getRaster(int level, int layer) {
    return levels.get(level).getLayer(layer);
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

  @Override
  public Pixel<T> getPixel(int x, int y, int level, int layer) {
    return layers.get(layer).getMipmap(level).getPixelForMipmapArray(x, y, level, layer);
  }

  @Override
  public Iterator<Pixel<T>> iterator() {
    List<Iterator<Pixel<T>>> wrappedLayers = new ArrayList<>(layers.size() * levels.size());
    for (int i = 0; i < layers.size(); i++) {
      for (int j = 0; j < levels.size(); j++) {
        wrappedLayers.add(layers.get(i).getMipmap(j).iteratorForMipmapArray(j, i));
      }
    }
    return new IteratorChain<>(wrappedLayers);
  }

  @Override
  public Spliterator<Pixel<T>> spliterator() {
    List<Spliterator<Pixel<T>>> wrappedLayers = new ArrayList<>(layers.size() * levels.size());
    for (int i = 0; i < layers.size(); i++) {
      for (int j = 0; j < levels.size(); j++) {
        wrappedLayers.add(layers.get(i).getMipmap(j).spliteratorForMipmapArray(j, i));
      }
    }
    return new SpliteratorChain<>(wrappedLayers);
  }
}
