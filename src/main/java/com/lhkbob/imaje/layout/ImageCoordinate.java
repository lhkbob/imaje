package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.util.Arguments;

import java.util.Iterator;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.ObjLongConsumer;

/**
 *
 */
public final class ImageCoordinate {
  public static class FastIterator implements Iterator<ImageCoordinate> {
    private final PrimitiveIterator.OfLong indexIterator;
    private final ImageCoordinate output;
    private final ObjLongConsumer<ImageCoordinate> updater;

    public FastIterator(
        PrimitiveIterator.OfLong baseIterator, ObjLongConsumer<ImageCoordinate> indexToPixel) {
      Arguments.notNull("baseIterator", baseIterator);
      Arguments.notNull("indexToPixel", indexToPixel);

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

  public static class FastSpliterator implements Spliterator<ImageCoordinate> {
    private final Spliterator.OfLong indexSpliterator;
    private final ImageCoordinate output;
    private final ObjLongConsumer<ImageCoordinate> updater;

    public FastSpliterator(
        Spliterator.OfLong baseSpliterator,
        ObjLongConsumer<ImageCoordinate> indexToPixel) {
      Arguments.notNull("baseSpliterator", baseSpliterator);
      Arguments.notNull("indexToPixel", indexToPixel);

      indexSpliterator = baseSpliterator;
      updater = indexToPixel;
      output = new ImageCoordinate();
    }

    @Override
    public int characteristics() {
      int base = indexSpliterator.characteristics();
      // Remove SORTED since although the underlying spliterator may be sorted in 1D, there is no
      // defined natural comparison or other comparison defined for ImageCoordinate.
      return base & ~Spliterator.SORTED;
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
    public FastSpliterator trySplit() {
      Spliterator.OfLong split = indexSpliterator.trySplit();
      if (split != null) {
        return new FastSpliterator(split, updater);
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
