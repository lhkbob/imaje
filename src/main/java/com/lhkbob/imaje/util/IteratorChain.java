package com.lhkbob.imaje.util;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 *
 */
public class IteratorChain<T> implements Iterator<T> {
  private final Queue<Iterator<T>> iterators;
  private Iterator<T> current;
  private Iterator<T> removeFrom;

  public IteratorChain(Collection<? extends Iterator<T>> iterators) {
    this();
    this.iterators.addAll(iterators);
  }

  private IteratorChain() {
    iterators = new ArrayDeque<>();
    current = null;
    removeFrom = null;
  }

  public static <T> IteratorChain<T> newIteratorChain(Collection<? extends Iterable<T>> iteratorSources) {
    IteratorChain<T> chain = new IteratorChain<>();
    for (Iterable<T> source: iteratorSources) {
      chain.iterators.add(source.iterator());
    }
    return chain;
  }

  @Override
  public boolean hasNext() {
    if (current != null) {
      // There is an active iterator, see if it has any elements left
      if (current.hasNext()) {
        return true;
      } else {
        // No more elements left so clear current and fall through to logic that looks for the
        // next iterator with elements
        current = null;
      }
    }

    // No current iterator, so pull the next iterator from the queue until it reports it has elements
    while(!iterators.isEmpty()) {
      current = iterators.poll();
      if (current.hasNext())
        return true;
    }

    // Exhausted all iterators, so clear references for GC (can't clear removeFrom since it could
    // still have remove() invoked, that is a valid possibility until next() is called)
    current = null;
    return false;
  }

  @Override
  public T next() {
    if (!hasNext())
      throw new NoSuchElementException();
    T value = current.next();
    removeFrom = current;
    return value;
  }

  @Override
  public void remove() {
    if (removeFrom == null)
      throw new IllegalStateException("Must call next() first");
    removeFrom.remove();
    removeFrom = null;
  }
}
