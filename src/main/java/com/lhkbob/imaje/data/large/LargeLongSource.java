package com.lhkbob.imaje.data.large;

import com.lhkbob.imaje.data.LongSource;

import java.util.Arrays;

/**
 *
 */
public class LargeLongSource implements LongSource {
  private final LongSource[] subSources;
  private final long totalLength;
  private final long repeatedSourceLength;

  public LargeLongSource(LongSource[] sources) {
    subSources = Arrays.copyOf(sources, sources.length);
    repeatedSourceLength = sources[0].getLength();
    long total = repeatedSourceLength;
    for (int i = 1; i < sources.length; i++) {
      if (i < sources.length - 1 && sources[i].getLength() != repeatedSourceLength) {
        throw new IllegalArgumentException(
            "All but last source must have the same size, expected: " + repeatedSourceLength);
      }
      total += sources[i].getLength();
    }
    totalLength = total;
  }

  public LongSource[] getSources() {
    return Arrays.copyOf(subSources, subSources.length);
  }

  @Override
  public long getLength() {
    return totalLength;
  }

  @Override
  public long get(long index) {
    int sourceIndex = (int) (index / repeatedSourceLength);
    long withinSourceIndex = index % repeatedSourceLength;
    return subSources[sourceIndex].get(withinSourceIndex);
  }

  @Override
  public void set(long index, long value) {
    int sourceIndex = (int) (index / repeatedSourceLength);
    long withinSourceIndex = index % repeatedSourceLength;
    subSources[sourceIndex].set(withinSourceIndex, value);
  }
}
