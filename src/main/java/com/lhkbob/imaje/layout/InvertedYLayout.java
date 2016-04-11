package com.lhkbob.imaje.layout;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 *
 */
public class InvertedYLayout implements PixelLayout {
  private final PixelLayout original;

  public InvertedYLayout(PixelLayout toInvert) {
    original = toInvert;
  }

  @Override
  public int getHeight() {
    return original.getHeight();
  }

  @Override
  public void getChannelIndices(int x, int y, long[] channelIndices) {
    original.getChannelIndices(x, invertY(y), channelIndices);
  }

  @Override
  public long getChannelIndex(int x, int y, int channel) {
    return original.getChannelIndex(x, invertY(y), channel);
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
    // If the Y is inverted, that means the image is flipped with respect to what the OpenGL coordinate
    // system expects so this can never be GPU ready
    return false;
  }

  @Override
  public int getWidth() {
    return original.getWidth();
  }

  @Override
  public Iterator<ImageCoordinate> iterator() {
    return new InvertingIterator(original.iterator());
  }

  @Override
  public Spliterator<ImageCoordinate> spliterator() {
    return new InvertingSpliterator(original.spliterator());
  }

  private int invertY(int y) {
    return getHeight() - y - 1;
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
      ic.setY(invertY(ic.getY()));
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
      return base.tryAdvance(imageCoordinate -> {
        imageCoordinate.setY(invertY(imageCoordinate.getY()));
        action.accept(imageCoordinate);
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
      // the Y inversion is likely to mangle that ordering.
      return base.characteristics() & ~Spliterator.SORTED;
    }
  }
}
