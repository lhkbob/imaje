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
public class DefaultImageArray<T extends Color> implements ImageArray<T> {
  private final List<RasterImage<T>> layers;

  private final int width;
  private final int height;
  private final Class<T> colorType;
  private final boolean hasAlpha;

  public DefaultImageArray(List<RasterImage<T>> layers) {
    if (layers.isEmpty()) {
      throw new IllegalArgumentException("Must provide at least one layer");
    }
    this.layers = Collections.unmodifiableList(new ArrayList<>(layers));

    width = layers.get(0).getWidth();
    height = layers.get(0).getHeight();
    hasAlpha = layers.get(0).hasAlphaChannel();
    colorType = layers.get(0).getColorType();

    for (int i = 1; i < layers.size(); i++) {
      if (layers.get(i).getWidth() != width || layers.get(i).getHeight() != height)
        throw new IllegalArgumentException(String.format("All images must be the same size (%d x %d) vs (%d x %d)", width, height, layers.get(i).getWidth(), layers.get(i).getHeight()));
      if (hasAlpha != layers.get(i).hasAlphaChannel())
        throw new IllegalArgumentException(String.format("All images must have equivalent alpha channel state (expected %s, but was %s)", hasAlpha, layers.get(i).hasAlphaChannel()));
      if (!colorType.equals(layers.get(i).getColorType())) {
        throw new IllegalArgumentException(String.format("All images must have the same color type (expected %s, but was %s)", colorType, layers.get(i).getColorType()));
      }
    }
  }

  @Override
  public RasterImage<T> getLayer(int index) {
    return layers.get(index);
  }

  @Override
  public List<RasterImage<T>> getLayerImages() {
    return layers;
  }

  @Override
  public Pixel<T> getPixel(int x, int y, int layer) {
    ArrayPixel p = new ArrayPixel();
    p.fromLayer = layers.get(layer).getPixel(x, y);
    p.layer = layer;
    return p;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
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
  public boolean hasAlphaChannel() {
    return hasAlpha;
  }

  @Override
  public Class<T> getColorType() {
    return colorType;
  }

  @Override
  public Map<String, String> getMetadata() {
    throw new UnsupportedOperationException("NOT IMPLEMENTED");
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

  private class ArraySpliterator implements Spliterator<Pixel<T>> {
    private final ArrayPixel pixel;
    private final Spliterator<Pixel<T>> layerSpliterator;

    public ArraySpliterator(int layer, Spliterator<Pixel<T>> layerSpliterator) {
      this.layerSpliterator = layerSpliterator;
      pixel = new ArrayPixel();
      pixel.layer = layer;
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

    @Override
    public long estimateSize() {
      return layerSpliterator.estimateSize();
    }

    @Override
    public int characteristics() {
      return layerSpliterator.characteristics();
    }
  }

  private class ArrayPixel implements Pixel<T> {
    private Pixel<T> fromLayer;
    private int layer;

    @Override
    public int getX() {
      return fromLayer.getX();
    }

    @Override
    public int getY() {
      return fromLayer.getY();
    }

    @Override
    public int getLevel() {
      return fromLayer.getLevel();
    }

    @Override
    public int getLayer() {
      return layer;
    }

    @Override
    public void get(T result) {
      fromLayer.get(result);
    }

    @Override
    public void set(T value) {
      fromLayer.set(value);
    }

    @Override
    public double getAlpha() {
      return fromLayer.getAlpha();
    }

    @Override
    public void setAlpha(double a) {
      fromLayer.setAlpha(a);
    }
  }
}
