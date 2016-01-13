package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.util.IteratorChain;
import com.lhkbob.imaje.util.SpliteratorChain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 *
 */
public class DefaultMipmapImageArray<T extends Color> implements MipmapImageArray<T> {
  private final Class<T> colorType;
  private final boolean hasAlpha;
  private final int height;
  private final List<MipmapImage<T>> layers;
  private final List<ImageArray<T>> levels; // Built up based on provided layers
  private final int width;

  public DefaultMipmapImageArray(List<MipmapImage<T>> layers) {
    if (layers.isEmpty()) {
      throw new IllegalArgumentException("Must provide at least one layer");
    }
    this.layers = Collections.unmodifiableList(new ArrayList<>(layers));

    width = layers.get(0).getWidth();
    height = layers.get(0).getHeight();
    hasAlpha = layers.get(0).hasAlphaChannel();
    colorType = layers.get(0).getColorType();

    for (int i = 1; i < layers.size(); i++) {
      if (layers.get(i).getWidth() != width || layers.get(i).getHeight() != height) {
        throw new IllegalArgumentException(String
            .format("All images must be the same size (%d x %d) vs (%d x %d)", width, height,
                layers.get(i).getWidth(), layers.get(i).getHeight()));
      }
      if (hasAlpha != layers.get(i).hasAlphaChannel()) {
        throw new IllegalArgumentException(String
            .format("All images must have equivalent alpha channel state (expected %s, but was %s)",
                hasAlpha, layers.get(i).hasAlphaChannel()));
      }
      if (!colorType.equals(layers.get(i).getColorType())) {
        throw new IllegalArgumentException(String
            .format("All images must have the same color type (expected %s, but was %s)", colorType,
                layers.get(i).getColorType()));
      }
    }

    // Since all layer mipmap images have the same top-level dimension they will have the same
    // level count as well.
    int levelCount = layers.get(0).getMipmapCount();
    List<ImageArray<T>> levels = new ArrayList<>(levelCount);
    for (int i = 0; i < levelCount; i++) {
      List<RasterImage<T>> levelImages = new ArrayList<>(layers.size());
      for (MipmapImage<T> layer : layers) {
        levelImages.add(layer.getLevel(i));
      }
      levels.add(new DefaultImageArray<>(levelImages));
    }
    this.levels = Collections.unmodifiableList(levels);
  }

  @Override
  public Class<T> getColorType() {
    return colorType;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public MipmapImage<T> getLayer(int index) {
    return layers.get(index);
  }

  @Override
  public int getLayerCount() {
    return layers.size();
  }

  @Override
  public List<MipmapImage<T>> getLayerImages() {
    return layers;
  }

  @Override
  public ImageArray<T> getLevel(int level) {
    return levels.get(level);
  }

  @Override
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
  public Pixel<T> getPixel(int x, int y, int level, int index) {
    ArrayPixel p = new ArrayPixel();
    p.fromLayer = getLayer(index).getPixel(x, y, level);
    p.layer = index;
    return p;
  }

  @Override
  public RasterImage<T> getRaster(int level, int index) {
    return layers.get(index).getLevel(level);
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public boolean hasAlphaChannel() {
    return hasAlpha;
  }

  @Override
  public Iterator<Pixel<T>> iterator() {
    List<ArrayIterator> wrappedLayers = new ArrayList<>(layers.size());
    for (int i = 0; i < layers.size(); i++) {
      wrappedLayers.add(new ArrayIterator(i, layers.get(i).iterator()));
    }
    return new IteratorChain<>(wrappedLayers);
  }

  @Override
  public Spliterator<Pixel<T>> spliterator() {
    List<ArraySpliterator> wrappedLayers = new ArrayList<>(layers.size());
    for (int i = 0; i < layers.size(); i++) {
      wrappedLayers.add(new ArraySpliterator(i, layers.get(i).spliterator()));
    }
    return new SpliteratorChain<>(wrappedLayers);
  }

  private class ArrayIterator implements Iterator<Pixel<T>> {
    private final Iterator<Pixel<T>> layerIterator;
    private final ArrayPixel pixel;

    public ArrayIterator(int layer, Iterator<Pixel<T>> layerIterator) {
      this.layerIterator = layerIterator;
      pixel = new ArrayPixel();
      pixel.layer = layer;
    }

    @Override
    public boolean hasNext() {
      return layerIterator.hasNext();
    }

    @Override
    public Pixel<T> next() {
      pixel.fromLayer = layerIterator.next();
      return pixel;
    }

    @Override
    public void remove() {
      layerIterator.remove();
    }
  }

  private class ArrayPixel implements Pixel<T> {
    private Pixel<T> fromLayer;
    private int layer;

    @Override
    public void get(T result) {
      fromLayer.get(result);
    }

    @Override
    public double getAlpha() {
      return fromLayer.getAlpha();
    }

    @Override
    public int getLayer() {
      return layer;
    }

    @Override
    public int getLevel() {
      return fromLayer.getLevel();
    }

    @Override
    public int getX() {
      return fromLayer.getX();
    }

    @Override
    public int getY() {
      return fromLayer.getY();
    }

    @Override
    public void set(T value) {
      fromLayer.set(value);
    }

    @Override
    public void setAlpha(double a) {
      fromLayer.setAlpha(a);
    }
  }

  private class ArraySpliterator implements Spliterator<Pixel<T>> {
    private final Spliterator<Pixel<T>> layerSpliterator;
    private final ArrayPixel pixel;

    public ArraySpliterator(int layer, Spliterator<Pixel<T>> layerSpliterator) {
      this.layerSpliterator = layerSpliterator;
      pixel = new ArrayPixel();
      pixel.layer = layer;
    }

    @Override
    public int characteristics() {
      return layerSpliterator.characteristics();
    }

    @Override
    public long estimateSize() {
      return layerSpliterator.estimateSize();
    }

    @Override
    public boolean tryAdvance(Consumer<? super Pixel<T>> action) {
      return layerSpliterator.tryAdvance(toWrap -> {
        pixel.fromLayer = toWrap;
        action.accept(pixel);
      });
    }

    @Override
    public Spliterator<Pixel<T>> trySplit() {
      Spliterator<Pixel<T>> split = layerSpliterator.trySplit();
      if (split != null) {
        return new ArraySpliterator(pixel.layer, split);
      } else {
        return null;
      }
    }
  }
}
