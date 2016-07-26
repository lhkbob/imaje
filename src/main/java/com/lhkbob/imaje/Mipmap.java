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
 */
public class Mipmap<T extends Color> implements Image<T> {
  private final List<Raster<T>> mipmaps;

  public Mipmap(List<Raster<T>> mipmaps) {
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


  public Raster<T> getMipmap(int level) {
    return mipmaps.get(level);
  }

  public List<Raster<T>> getMipmaps() {
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
  public boolean hasAlphaChannel() {
    return mipmaps.get(0).hasAlphaChannel();
  }

  @Override
  public int getWidth() {
    return mipmaps.get(0).getWidth();
  }

  @Override
  public int getHeight() {
    return mipmaps.get(0).getHeight();
  }

  @Override
  public Pixel<T> getPixel(int x, int y, int level, int layer) {
    if (layer != 0) {
      throw new IllegalArgumentException(
          "Image is not multilayered, layer must be 0, not: " + layer);
    }
    return getPixel(x, y, level);
  }

  public Pixel<T> getPixel(int x, int y, int level) {
    return mipmaps.get(level).getPixelForMipmapArray(x, y, level, 0);
  }

  @Override
  public Iterator<Pixel<T>> iterator() {
    List<Iterator<Pixel<T>>> wrappedLayers = new ArrayList<>(mipmaps.size());
    for (int i = 0; i < mipmaps.size(); i++) {
      wrappedLayers.add(mipmaps.get(i).iteratorForMipmapArray(i, 0));
    }
    return new IteratorChain<>(wrappedLayers);
  }

  @Override
  public Spliterator<Pixel<T>> spliterator() {
    List<Spliterator<Pixel<T>>> wrappedLayers = new ArrayList<>(mipmaps.size());
    for (int i = 0; i < mipmaps.size(); i++) {
      wrappedLayers.add(mipmaps.get(i).spliteratorForMipmapArray(i, 0));
    }
    return new SpliteratorChain<>(wrappedLayers);
  }
}
