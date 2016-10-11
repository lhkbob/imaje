package com.lhkbob.imaje.util;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.LongConsumer;

/**
 *
 */
public class IndexSpliterator implements Spliterator.OfLong {
  private final long minimumSplit;
  private final long indexFence;
  private long nextIndex;

  public IndexSpliterator(long size, long minimumSplit) {
    this(0, size, minimumSplit);
  }

  private IndexSpliterator(long nextIndex, long indexFence, long minimumSplit) {
    this.minimumSplit = minimumSplit;

    this.indexFence = indexFence;
    this.nextIndex = nextIndex;
  }

  @Override
  public int characteristics() {
    return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.IMMUTABLE | Spliterator.SORTED
        | Spliterator.ORDERED;
  }

  @Override
  public long estimateSize() {
    return indexFence - nextIndex;
  }

  @Override
  public boolean tryAdvance(LongConsumer action) {
    if (nextIndex < indexFence) {
      long report = nextIndex++;
      action.accept(report);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public OfLong trySplit() {
    if (minimumSplit < 1 || estimateSize() <= minimumSplit) {
      return null;
    }

    long split = (nextIndex + indexFence) / 2;
    // The returned spliterator is from [nextIndex, split) and this spliterator
    // is updated to be from [split, indexFence)
    OfLong prefix = new IndexSpliterator(nextIndex, split, minimumSplit);
    nextIndex = split;
    return prefix;
  }

  @Override
  public Comparator<Long> getComparator() {
    // Elements are returned in the natural order of longs, so a null comparator should be returned.
    // The default implementation throws illegal state exception.
    return null;
  }
}
