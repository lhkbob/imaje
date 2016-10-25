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
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 *
 */
public class InvertedLayout implements DataLayout {
  private final boolean invertX;
  private final boolean invertY;
  private final DataLayout original;

  public InvertedLayout(DataLayout original, boolean invertX, boolean invertY) {
    Arguments.notNull("original", original);
    if (!invertX && !invertY) {
      throw new IllegalArgumentException("At least one of invertX and invertY should be true");
    }
    this.original = original;
    this.invertX = invertX;
    this.invertY = invertY;
  }

  public DataLayout getOriginalLayout() {
    return original;
  }

  public boolean isXAxisInverted() {
    return invertX;
  }

  public boolean isYAxisInverted() {
    return invertY;
  }

  @Override
  public int getHeight() {
    return original.getHeight();
  }

  @Override
  public void getChannelIndices(int x, int y, long[] channelIndices) {
    original.getChannelIndices(getX(x), getY(y), channelIndices);
  }

  @Override
  public long getChannelIndex(int x, int y, int channel) {
    return original.getChannelIndex(getX(x), getY(y), channel);
  }

  @Override
  public int getChannelCount() {
    return original.getChannelCount();
  }

  @Override
  public long getRequiredDataElements() {
    return original.getRequiredDataElements();
  }

  @Override
  public boolean isGPUCompatible() {
    // If the X/Y is inverted, that means the image is flipped with respect to what the OpenGL coordinate
    // system expects so this can never be GPU ready. Hypothetically this could be wrapping a raster
    // that already flipped the X, restoring order, but that is tricky to get right. It is much easier to say
    // that composed layouts are not GPU compatible.
    return false;
  }

  @Override
  public int getWidth() {
    return original.getWidth();
  }

  @Override
  public boolean isDataBottomToTop() {
    if (invertY) {
      // Flip vertical ordering
      return !original.isDataBottomToTop();
    } else {
      // Preserve vertical ordering
      return original.isDataBottomToTop();
    }
  }

  @Override
  public boolean isDataLeftToRight() {
    if (invertX) {
      // Flip horizontal ordering
      return !original.isDataLeftToRight();
    } else {
      // Preserve vertical ordering
      return original.isDataLeftToRight();
    }
  }

  @Override
  public Iterator<ImageCoordinate> iterator() {
    return new InvertingIterator(original.iterator());
  }

  @Override
  public Spliterator<ImageCoordinate> spliterator() {
    return new InvertingSpliterator(original.spliterator());
  }

  private int getY(int y) {
    if (invertY)
      return getHeight() - y - 1;
    else
      return y;
  }

  private int getX(int x) {
    if (invertX)
      return getWidth() - x - 1;
    else
      return x;
  }

  private class InvertingIterator implements Iterator<ImageCoordinate> {
    private final Iterator<ImageCoordinate> base;

    InvertingIterator(Iterator<ImageCoordinate> base) {
      this.base = base;
    }

    @Override
    public boolean hasNext() {
      return base.hasNext();
    }

    @Override
    public ImageCoordinate next() {
      ImageCoordinate ic = base.next();
      ic.setX(getX(ic.getX()));
      ic.setY(getY(ic.getY()));
      return ic;
    }
  }

  private class InvertingSpliterator implements Spliterator<ImageCoordinate> {
    private final Spliterator<ImageCoordinate> base;

    InvertingSpliterator(Spliterator<ImageCoordinate> base) {
      this.base = base;
    }

    @Override
    public boolean tryAdvance(
        Consumer<? super ImageCoordinate> action) {
      return base.tryAdvance(ic -> {
        ic.setX(getX(ic.getX()));
        ic.setY(getY(ic.getY()));
        action.accept(ic);
      });
    }

    @Override
    public Spliterator<ImageCoordinate> trySplit() {
      Spliterator<ImageCoordinate> baseSplit = base.trySplit();
      if (baseSplit != null) {
        return new InvertingSpliterator(baseSplit);
      } else {
        return null;
      }
    }

    @Override
    public long estimateSize() {
      return base.estimateSize();
    }

    @Override
    public long getExactSizeIfKnown() {
      return base.getExactSizeIfKnown();
    }

    @Override
    public int characteristics() {
      // Make sure it's not SORTED; if some base spliterator somehow does sort image coordinates,
      // the X/Y inversion is likely to mangle that ordering.
      return base.characteristics() & ~Spliterator.SORTED;
    }
  }
}
