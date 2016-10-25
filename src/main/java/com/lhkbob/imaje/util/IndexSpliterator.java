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
