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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 *
 */
public class SpliteratorChain<T> implements Spliterator<T> {
  private final Queue<Spliterator<T>> spliterators;
  private Spliterator<T> current;

  public SpliteratorChain(Collection<? extends Spliterator<T>> spliterators) {
    this();
    this.spliterators.addAll(spliterators);
  }

  private SpliteratorChain() {
    this.spliterators = new ArrayDeque<>();
    current = null;
  }

  public static <T> SpliteratorChain<T> newSpliteratorChain(
      Collection<? extends Iterable<T>> splitSources) {
    SpliteratorChain<T> split = new SpliteratorChain<>();
    for (Iterable<T> source : splitSources) {
      split.spliterators.add(source.spliterator());
    }
    return split;
  }

  @Override
  public int characteristics() {
    int caps = ~0;
    for (Spliterator<T> s : spliterators) {
      caps &= s.characteristics();
    }
    return caps;
  }

  @Override
  public long estimateSize() {
    long size = 0;
    for (Spliterator<T> s : spliterators) {
      long subSize = s.estimateSize();
      // Prevent overflow if a sub-spliterator is unable to estimate the size or has an infinite
      // number of elements. In that case this spliterator should report the same
      if (subSize == Long.MAX_VALUE) {
        return Long.MAX_VALUE;
      }
      size += subSize;
    }
    return size;
  }

  @Override
  public boolean tryAdvance(Consumer<? super T> action) {
    if (current != null) {
      // Try using the current spliterator
      if (current.tryAdvance(action)) {
        // accepted
        return true;
      } else {
        // No element left in the current spliterator so move on
        current = null;
      }
    }

    while (!spliterators.isEmpty()) {
      current = spliterators.poll();
      if (current.tryAdvance(action)) {
        // Found a spliterator with elements
        return true;
      }
    }

    // No elements left in any spliterator
    current = null;
    return false;
  }

  @Override
  public Spliterator<T> trySplit() {
    // Fast path optimization for when the spliterator is empty
    if (current == null && spliterators.isEmpty()) {
      return null;
    }

    // If this spliterator is on the last subspliterator, try splitting the current one directly
    // (and since current is properly updated internally to be the postfix then this has the
    //  correct behavior for the overall spliterator too)
    if (current != null && spliterators.isEmpty()) {
      return current.trySplit();
    }

    // There are spliterators remaining, so split based on the current and queued sub-spliterators,
    // with the goal of having approximately half the elements in the returned split.

    // However, if there are infinite elements, or unknown elements, then don't split
    long size = estimateSize();
    if (size == Long.MAX_VALUE) {
      return null;
    }

    long splitSize = 0;
    SpliteratorChain<T> split = new SpliteratorChain<>();
    if (current != null) {
      // Must include the current spliterator in the prefix
      split.spliterators.add(current);
      splitSize += current.estimateSize();
      current = null; // this spliterator doesn't get to keep the current
    }

    // Move sub-spliterators over until about half the elements are covered, but make sure to
    // leave at least one spliterator in this one
    while (spliterators.size() > 1) {
      Spliterator<T> next = spliterators.peek();
      long nextSize = next.estimateSize();
      // FIXME update this logic to always take at least one (e.g. if current == null we ought
      // to add the first even if it's > 1/2 the rest)
      if (splitSize + nextSize < size / 2) {
        // Can include the spliterator in the prefix
        split.spliterators.add(spliterators.poll());
      } else {
        // Achieved a semi-good split of elements, so keep next in this spliterators queue
        break;
      }
    }

    if (split.spliterators.isEmpty()) {
      // No split available
      return null;
    } else if (split.spliterators.size() == 1) {
      // Optimization to prevent wrapping with an unnecessary spliterator chain
      return split.spliterators.poll();
    } else {
      return split;
    }
  }
}
