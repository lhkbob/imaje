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
public class DefaultMipmapImage<T extends Color> implements MipmapImage<T> {
  private final Class<T> colorType;
  private final boolean hasAlpha;
  private final int height;
  private final List<RasterImage<T>> mipmaps;
  private final int width;

  public DefaultMipmapImage(List<RasterImage<T>> mipmaps) {
    if (mipmaps.isEmpty()) {
      throw new IllegalArgumentException("Must provide at least one image");
    }
    width = mipmaps.get(0).getWidth();
    height = mipmaps.get(0).getHeight();
    hasAlpha = mipmaps.get(0).hasAlphaChannel();
    colorType = mipmaps.get(0).getColorType();

    // Verify that dimensions and properties are as expected
    int mipmapCount = Image.getMipmapCount(width, height);
    if (mipmaps.size() != mipmapCount) {
      throw new IllegalArgumentException("Must provide a full set of mipmap images");
    }
    for (int i = 1; i < mipmapCount; i++) {
      RasterImage<T> mipmap = mipmaps.get(i);
      int mipWidth = Image.getMipmapDimension(width, i);
      int mipHeight = Image.getMipmapDimension(height, i);

      if (mipmap.getWidth() != mipWidth || mipmap.getHeight() != mipHeight) {
        throw new IllegalArgumentException(String
            .format("Level %d has incorrect dimensions, expected (%d x %d) vs (%d x %d)", i,
                mipWidth, mipHeight, mipmap.getWidth(), mipmap.getHeight()));
      }

      if (hasAlpha != mipmap.hasAlphaChannel()) {
        throw new IllegalArgumentException(String
            .format("All images must have equivalent alpha channel state (expected %s, but was %s)",
                hasAlpha, mipmap.hasAlphaChannel()));
      }
      if (!colorType.equals(mipmap.getColorType())) {
        throw new IllegalArgumentException(String
            .format("All images must have the same color type (expected %s, but was %s)", colorType,
                mipmap.getColorType()));
      }
    }

    this.mipmaps = Collections.unmodifiableList(new ArrayList<>(mipmaps));
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
  public int getLayerCount() {
    return 1;
  }

  @Override
  public RasterImage<T> getLevel(int level) {
    return mipmaps.get(level);
  }

  @Override
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

  @Override
  public Pixel<T> getPixel(int x, int y, int level) {
    MipmapPixel p = new MipmapPixel();
    p.fromLevel = mipmaps.get(level).getPixel(x, y);
    p.level = level;
    return p;
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
    return width;
  }

  @Override
  public boolean hasAlphaChannel() {
    return hasAlpha;
  }

  @Override
  public Iterator<Pixel<T>> iterator() {
    List<MipmapIterator> wrappedLayers = new ArrayList<>(mipmaps.size());
    for (int i = 0; i < mipmaps.size(); i++) {
      wrappedLayers.add(new MipmapIterator(i, mipmaps.get(i).iterator()));
    }
    return new IteratorChain<>(wrappedLayers);
  }

  @Override
  public Spliterator<Pixel<T>> spliterator() {
    List<MipmapSpliterator> wrappedLayers = new ArrayList<>(mipmaps.size());
    for (int i = 0; i < mipmaps.size(); i++) {
      wrappedLayers.add(new MipmapSpliterator(i, mipmaps.get(i).spliterator()));
    }
    return new SpliteratorChain<>(wrappedLayers);
  }

  private class MipmapIterator implements Iterator<Pixel<T>> {
    private final Iterator<Pixel<T>> levelIterator;
    private final MipmapPixel pixel;

    public MipmapIterator(int level, Iterator<Pixel<T>> levelIterator) {
      this.levelIterator = levelIterator;
      pixel = new MipmapPixel();
      pixel.level = level;
    }

    @Override
    public boolean hasNext() {
      return levelIterator.hasNext();
    }

    @Override
    public Pixel<T> next() {
      pixel.fromLevel = levelIterator.next();
      return pixel;
    }

    @Override
    public void remove() {
      levelIterator.remove();
    }
  }

  private class MipmapPixel implements Pixel<T> {
    private Pixel<T> fromLevel;
    private int level;

    @Override
    public void get(T result) {
      fromLevel.get(result);
    }

    @Override
    public double getAlpha() {
      return fromLevel.getAlpha();
    }

    @Override
    public int getLayer() {
      return fromLevel.getLayer();
    }

    @Override
    public int getLevel() {
      return level;
    }

    @Override
    public int getX() {
      return fromLevel.getX();
    }

    @Override
    public int getY() {
      return fromLevel.getY();
    }

    @Override
    public void set(T value) {
      fromLevel.set(value);
    }

    @Override
    public void setAlpha(double a) {
      fromLevel.setAlpha(a);
    }
  }

  private class MipmapSpliterator implements Spliterator<Pixel<T>> {
    private final Spliterator<Pixel<T>> levelSpliterator;
    private final MipmapPixel pixel;

    public MipmapSpliterator(int level, Spliterator<Pixel<T>> levelSpliterator) {
      this.levelSpliterator = levelSpliterator;
      pixel = new MipmapPixel();
      pixel.level = level;
    }

    @Override
    public int characteristics() {
      return levelSpliterator.characteristics();
    }

    @Override
    public long estimateSize() {
      return levelSpliterator.estimateSize();
    }

    @Override
    public boolean tryAdvance(Consumer<? super Pixel<T>> action) {
      return levelSpliterator.tryAdvance(toWrap -> {
        pixel.fromLevel = toWrap;
        action.accept(pixel);
      });
    }

    @Override
    public Spliterator<Pixel<T>> trySplit() {
      Spliterator<Pixel<T>> split = levelSpliterator.trySplit();
      if (split != null) {
        return new MipmapSpliterator(pixel.level, split);
      } else {
        return null;
      }
    }
  }
}
