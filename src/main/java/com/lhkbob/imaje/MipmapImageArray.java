package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.util.ImageUtils;
import com.lhkbob.imaje.util.IteratorChain;
import com.lhkbob.imaje.util.SpliteratorChain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;

/**
 *
 */
public class MipmapImageArray<T extends Color> implements Image<T> {
  private final List<MipmapImage<T>> layers;
  private final List<ImageArray<T>> levels;

  public MipmapImageArray(List<MipmapImage<T>> layers) {
    if (layers.isEmpty()) {
      throw new IllegalArgumentException("Must provide at least one layer");
    }

    ImageUtils.checkMultiImageCompatibility(layers);

    this.layers = Collections.unmodifiableList(new ArrayList<>(layers));

    // Since all layer mipmap images have the same top-level dimension they will have the same
    // level count as well.
    int levelCount = layers.get(0).getMipmapCount();
    List<ImageArray<T>> levels = new ArrayList<>(levelCount);
    for (int i = 0; i < levelCount; i++) {
      List<RasterImage<T>> levelImages = new ArrayList<>(layers.size());
      for (MipmapImage<T> layer : layers) {
        levelImages.add(layer.getLevel(i));
      }
      levels.add(new ImageArray<>(levelImages));
    }
    this.levels = Collections.unmodifiableList(levels);
  }

  @Override
  public Class<T> getColorType() {
    return layers.get(0).getColorType();
  }

  @Override
  public int getHeight() {
    return layers.get(0).getHeight();
  }

  public MipmapImage<T> getLayer(int index) {
    return layers.get(index);
  }

  @Override
  public int getLayerCount() {
    return layers.size();
  }

  public List<MipmapImage<T>> getLayerImages() {
    return layers;
  }

  public ImageArray<T> getLevel(int level) {
    return levels.get(level);
  }

  public List<ImageArray<T>> getLevelImages() {
    return levels;
  }

  @Override
  public Map<String, String> getMetadata() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getMipmapCount() {
    return levels.size();
  }

  @Override
  public Pixel<T> getPixel(int x, int y, int level, int layer) {
    return levels.get(level).getLayer(layer).getExplicitPixel(x, y, level, layer);
  }

  public RasterImage<T> getRaster(int level, int layer) {
    return levels.get(level).getLayer(layer);
  }

  @Override
  public int getWidth() {
    return layers.get(0).getWidth();
  }

  @Override
  public boolean hasAlphaChannel() {
    return layers.get(0).hasAlphaChannel();
  }

  @Override
  public Iterator<Pixel<T>> iterator() {
    List<Iterator<Pixel<T>>> wrappedLayers = new ArrayList<>(layers.size() * levels.size());
    for (int i = 0; i < layers.size(); i++) {
      for (int j = 0; j < levels.size(); j++) {
        wrappedLayers.add(layers.get(i).getLevel(j).iterator(j, i));
      }
    }
    return new IteratorChain<>(wrappedLayers);
  }

  @Override
  public Spliterator<Pixel<T>> spliterator() {
    List<Spliterator<Pixel<T>>> wrappedLayers = new ArrayList<>(layers.size() * levels.size());
    for (int i = 0; i < layers.size(); i++) {
      for (int j = 0 ; j < levels.size(); j++) {
        wrappedLayers.add(layers.get(i).getLevel(j).spliterator(j, i));
      }
    }
    return new SpliteratorChain<>(wrappedLayers);
  }
}
