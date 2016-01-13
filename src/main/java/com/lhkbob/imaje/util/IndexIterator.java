package com.lhkbob.imaje.util;

import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

/**
 *
 */
public class IndexIterator implements PrimitiveIterator.OfLong {
  private final long size;
  private int nextIndex;

  public IndexIterator(long size) {
    this.size = size;
    nextIndex = 0;
  }

  @Override
  public boolean hasNext() {
    return nextIndex < size;
  }

  @Override
  public long nextLong() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    } else {
      return nextIndex++;
    }
  }
}
