package com.lhkbob.imaje.layout;

import java.util.PrimitiveIterator;
import java.util.function.Consumer;
import java.util.function.ObjLongConsumer;

/**
 *
 */
public final class ImageCoordinate {
  public static class Iterator implements java.util.Iterator<ImageCoordinate> {
    private final PrimitiveIterator.OfLong indexIterator;
    private final ImageCoordinate output;
    private final ObjLongConsumer<ImageCoordinate> updater;

    public Iterator(
        PrimitiveIterator.OfLong baseIterator, ObjLongConsumer<ImageCoordinate> indexToPixel) {
      indexIterator = baseIterator;
      updater = indexToPixel;
      output = new ImageCoordinate();
    }

    @Override
    public boolean hasNext() {
      return indexIterator.hasNext();
    }

    @Override
    public ImageCoordinate next() {
      updater.accept(output, indexIterator.next());
      return output;
    }
  }

  public static class Spliterator implements java.util.Spliterator<ImageCoordinate> {
    private final java.util.Spliterator.OfLong indexSpliterator;
    private final ImageCoordinate output;
    private final ObjLongConsumer<ImageCoordinate> updater;

    public Spliterator(
        java.util.Spliterator.OfLong baseSpliterator,
        ObjLongConsumer<ImageCoordinate> indexToPixel) {
      indexSpliterator = baseSpliterator;
      updater = indexToPixel;
      output = new ImageCoordinate();
    }

    @Override
    public int characteristics() {
      return indexSpliterator.characteristics();
    }

    @Override
    public long estimateSize() {
      return indexSpliterator.estimateSize();
    }

    @Override
    public boolean tryAdvance(
        Consumer<? super ImageCoordinate> action) {
      return indexSpliterator.tryAdvance((long index) -> {
        updater.accept(output, index);
        action.accept(output);
      });
    }

    @Override
    public Spliterator trySplit() {
      java.util.Spliterator.OfLong split = indexSpliterator.trySplit();
      if (split != null) {
        return new Spliterator(split, updater);
      } else {
        return null;
      }
    }
  }
  private int x;
  private int y;

  public ImageCoordinate() {
    x = 0;
    y = 0;
  }

  public ImageCoordinate(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ImageCoordinate)) {
      return false;
    }
    ImageCoordinate c = (ImageCoordinate) o;
    return c.x == x && c.y == y;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  @Override
  public int hashCode() {
    int result = x;
    result = 31 * result + y;
    return result;
  }

  public void setX(int x) {
    this.x = x;
  }

  public void setY(int y) {
    this.y = y;
  }

  @Override
  public String toString() {
    return String.format("(%d, %d)", x, y);
  }
}
