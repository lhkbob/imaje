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

  public static <T> SpliteratorChain<T> newSpliteratorChain(Collection<? extends Iterable<T>> splitSources) {
    SpliteratorChain<T> split = new SpliteratorChain<>();
    for (Iterable<T> source : splitSources) {
      split.spliterators.add(source.spliterator());
    }
    return split;
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

    while(!spliterators.isEmpty()) {
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
    if (current == null && spliterators.isEmpty())
      return null;

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
    if (size == Long.MAX_VALUE)
      return null;

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
    while(spliterators.size() > 1) {
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

  @Override
  public long estimateSize() {
    long size = 0;
    for (Spliterator<T> s : spliterators) {
      long subSize = s.estimateSize();
      // Prevent overflow if a sub-spliterator is unable to estimate the size or has an infinite
      // number of elements. In that case this spliterator should report the same
      if (subSize == Long.MAX_VALUE)
        return Long.MAX_VALUE;
      size += subSize;
    }
    return size;
  }

  @Override
  public int characteristics() {
    int caps = ~0;
    for (Spliterator<T> s: spliterators) {
      caps &= s.characteristics();
    }
    return caps;
  }
}
