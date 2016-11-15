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

import com.lhkbob.imaje.util.IndexIterator;
import com.lhkbob.imaje.util.IndexSpliterator;

import java.util.Iterator;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * ImageCoordinate
 * ===============
 *
 * ImageCoordinate is a data type for describing the X and Y components of a pixel's location. It
 * can have negative coordinates or coordinates that are outside an associated image's bounds. This
 * is to facilitate referencing and functioning over a virtual coordinate frame.
 *
 * @author Michael Ludwig
 */
public final class ImageCoordinate implements Cloneable {
  /**
   * FastIterator
   * ============
   *
   * An iterator of ImageCoordinate values within a particular window. It is a fast iterator in the
   * sense that it reuses a single ImageCoordinate instance and updates the reported X and Y
   * coordinates with each iteration step.
   *
   * @author Michael Ludwig
   */
  public static class FastIterator implements Iterator<ImageCoordinate> {
    private final PrimitiveIterator.OfLong indexIterator;
    private final ImageCoordinate output;
    private final int width;
    private final int x;
    private final int y;

    /**
     * Create a new FastIterator that reports all ImageCoordinate values contained within the
     * given `window`.
     *
     * @param window
     *     The window to iterate over
     */
    public FastIterator(ImageWindow window) {
      this(window.getX(), window.getY(), window.getWidth(), window.getHeight());
    }

    /**
     * Create a new FastIterator that reports all ImageCoordinate values contained within the window
     * defined by `x`, `y`, `width`, and `height`.
     *
     * @param x
     *     The x coordinate of the lower left corner of the window
     * @param y
     *     The y coordinate of the lower left corner of the window
     * @param width
     *     The width of the window
     * @param height
     *     The height of the window
     */
    public FastIterator(int x, int y, int width, int height) {
      indexIterator = new IndexIterator(width * height);
      output = new ImageCoordinate();

      this.width = width;
      this.x = x;
      this.y = y;
    }

    @Override
    public boolean hasNext() {
      return indexIterator.hasNext();
    }

    @Override
    public ImageCoordinate next() {
      updateCoordinate(output, indexIterator.next(), x, y, width);
      return output;
    }
  }

  /**
   * FastSpliterator
   * ============
   *
   * A spliterator of ImageCoordinate values within a particular window. It is a fast spliterator in
   * the sense that it reuses a single ImageCoordinate instance and updates the reported X and Y
   * coordinates with each iteration step. This spliterator can be split down to a single row within
   * the window it's iterating over. Each split of the original spliterator instance gets its own
   * ImageCoordinate that is updated in-place so it is still a thread-safe iteration mechanism.
   *
   * @author Michael Ludwig
   */
  public static class FastSpliterator implements Spliterator<ImageCoordinate> {
    private final Spliterator.OfLong indexSpliterator;
    private final ImageCoordinate output;
    private final int width;
    private final int x;
    private final int y;

    /**
     * Create a new FastSpliterator that reports all ImageCoordinate values contained within the
     * given `window`.
     *
     * @param window
     *     The window to iterate over
     */
    public FastSpliterator(ImageWindow window) {
      this(window.getX(), window.getY(), window.getWidth(), window.getHeight());
    }

    /**
     * Create a new FastSpliterator that reports all ImageCoordinate values contained within the
     * window defined by `x`, `y`, `width`, and `height`.
     *
     * @param x
     *     The x coordinate of the lower left corner of the window
     * @param y
     *     The y coordinate of the lower left corner of the window
     * @param width
     *     The width of the window
     * @param height
     *     The height of the window
     */
    public FastSpliterator(int x, int y, int width, int height) {
      this(new IndexSpliterator(width * height, width), x, y, width);
    }

    private FastSpliterator(Spliterator.OfLong spliterator, int x, int y, int width) {
      indexSpliterator = spliterator;
      output = new ImageCoordinate();
      this.width = width;
      this.x = x;
      this.y = y;
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
        updateCoordinate(output, index, x, y, width);
        action.accept(output);
      });
    }

    @Override
    public FastSpliterator trySplit() {
      Spliterator.OfLong split = indexSpliterator.trySplit();
      if (split != null) {
        return new FastSpliterator(split, x, y, width);
      } else {
        return null;
      }
    }
  }

  private int x;
  private int y;

  /**
   * Create a new image coordinate at the origin, `(0, 0)`.
   */
  public ImageCoordinate() {
    x = 0;
    y = 0;
  }

  /**
   * Create a new image coordinate with the specified initial `x` and `y` values.
   *
   * @param x
   *     The x component
   * @param y
   *     The y component
   */
  public ImageCoordinate(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public ImageCoordinate clone() {
    try {
      return (ImageCoordinate) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen", e);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ImageCoordinate)) {
      return false;
    }
    ImageCoordinate c = (ImageCoordinate) o;
    return c.x == x && c.y == y;
  }

  /**
   * @return The x component, may be negative
   */
  public int getX() {
    return x;
  }

  /**
   * @return The y component, may be negative
   */
  public int getY() {
    return y;
  }

  @Override
  public int hashCode() {
    int result = x;
    result = 31 * result + y;
    return result;
  }

  /**
   * Set the new x component of this ImageCoordinate, which can be a negative value.
   *
   * @param x
   *     The new x component
   */
  public void setX(int x) {
    this.x = x;
  }

  /**
   * Set the new y component of this ImageCoordinate, which can be a negative value.
   *
   * @param y
   *     The new y component
   */
  public void setY(int y) {
    this.y = y;
  }

  @Override
  public String toString() {
    return String.format("(%d, %d)", x, y);
  }

  private static void updateCoordinate(
      ImageCoordinate coord, long index, int offsetX, int offsetY, int width) {
    int y = (int) (index / width);
    int x = (int) (index - y * width);
    coord.setX(offsetX + x);
    coord.setY(offsetY + y);
  }
}
