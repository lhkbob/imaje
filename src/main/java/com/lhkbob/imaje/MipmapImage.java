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
public class MipmapImage<T extends Color> implements Image<T> {
  private final List<RasterImage<T>> mipmaps;

  public MipmapImage(List<RasterImage<T>> mipmaps) {
    if (mipmaps.isEmpty()) {
      throw new IllegalArgumentException("Must provide at least one image");
    }

    // Verify that dimensions and properties are as expected
    ImageUtils.checkMultiImageCompatibility(mipmaps);
    ImageUtils.checkMipmapCompleteness(mipmaps);

    this.mipmaps = Collections.unmodifiableList(new ArrayList<>(mipmaps));
  }

  @Override
  public Class<T> getColorType() {
    return mipmaps.get(0).getColorType();
  }

  @Override
  public int getHeight() {
    return mipmaps.get(0).getHeight();
  }

  @Override
  public int getLayerCount() {
    return 1;
  }

  public RasterImage<T> getLevel(int level) {
    return mipmaps.get(level);
  }

  public List<RasterImage<T>> getLevelImages() {
    return mipmaps;
  }

  @Override
  public Map<String, String> getMetadata() {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getMipmapCount() {
    return mipmaps.size();
  }

  public Pixel<T> getPixel(int x, int y, int level) {
    return mipmaps.get(level).getExplicitPixel(x, y, level, 0);
  }

  @Override
  public Pixel<T> getPixel(int x, int y, int level, int layer) {
    if (layer == 0) {
      return getPixel(x, y, level);
    } else {
      throw new IndexOutOfBoundsException("Array layer is illegal: " + layer);
    }
  }

  @Override
  public int getWidth() {
    return mipmaps.get(0).getWidth();
  }

  @Override
  public boolean hasAlphaChannel() {
    return mipmaps.get(0).hasAlphaChannel();
  }

  @Override
  public Iterator<Pixel<T>> iterator() {
    List<Iterator<Pixel<T>>> wrappedLayers = new ArrayList<>(mipmaps.size());
    for (int i = 0; i < mipmaps.size(); i++) {
      wrappedLayers.add(mipmaps.get(i).iterator(i, 0));
    }
    return new IteratorChain<>(wrappedLayers);
  }

  @Override
  public Spliterator<Pixel<T>> spliterator() {
    List<Spliterator<Pixel<T>>> wrappedLayers = new ArrayList<>(mipmaps.size());
    for (int i = 0; i < mipmaps.size(); i++) {
      wrappedLayers.add(mipmaps.get(i).spliterator(i, 0));
    }
    return new SpliteratorChain<>(wrappedLayers);
  }
}
