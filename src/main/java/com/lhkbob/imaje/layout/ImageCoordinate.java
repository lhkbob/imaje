/*
 * BSD 3-Clause License - imaJe
 *
 * Copyright (c) 2016, Michael Ludwig
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
