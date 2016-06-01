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
public class ImageArray<T extends Color> implements Image<T> {
  private final List<RasterImage<T>> layers;

  public ImageArray(List<RasterImage<T>> layers) {
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

  @Override
  public int getHeight() {
    return layers.get(0).getHeight();
  }

  public RasterImage<T> getLayer(int index) {
    return layers.get(index);
  }

  @Override
  public int getLayerCount() {
    return layers.size();
  }

  public List<RasterImage<T>> getLayerImages() {
    return layers;
  }

  @Override
  public Map<String, String> getMetadata() {
    throw new UnsupportedOperationException("NOT IMPLEMENTED");
  }

  @Override
  public int getMipmapCount() {
    return 1;
  }

  public Pixel<T> getPixel(int x, int y, int layer) {
    return layers.get(layer).getExplicitPixel(x, y, 0, layer);
  }

  @Override
  public Pixel<T> getPixel(int x, int y, int level, int layer) {
    if (level == 0) {
      return getPixel(x, y, layer);
    } else {
      throw new IndexOutOfBoundsException("Mipmap level is illegal: " + level);
    }
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
    List<Iterator<Pixel<T>>> wrappedLayers = new ArrayList<>(layers.size());
    for (int i = 0; i < layers.size(); i++) {
      wrappedLayers.add(layers.get(i).iterator(0, i));
    }
    return new IteratorChain<>(wrappedLayers);
  }

  @Override
  public Spliterator<Pixel<T>> spliterator() {
    List<Spliterator<Pixel<T>>> wrappedLayers = new ArrayList<>(layers.size());
    for (int i = 0; i < layers.size(); i++) {
      wrappedLayers.add(layers.get(i).spliterator(0, i));
    }
    return new SpliteratorChain<>(wrappedLayers);
  }
}
