package com.lhkbob.imaje.data.large;

import com.lhkbob.imaje.data.ShortSource;

import java.util.Arrays;

/**
 *
 */
public class LargeShortSource implements ShortSource {
  private final long repeatedSourceLength;
  private final ShortSource[] subSources;
  private final long totalLength;

  public LargeShortSource(ShortSource[] sources) {
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

  @Override
  public short get(long index) {
    int sourceIndex = (int) (index / repeatedSourceLength);
    long withinSourceIndex = index % repeatedSourceLength;
    return subSources[sourceIndex].get(withinSourceIndex);
  }

  @Override
  public long getLength() {
    return totalLength;
  }

  public ShortSource[] getSources() {
    return Arrays.copyOf(subSources, subSources.length);
  }

  @Override
  public void set(long index, short value) {
    int sourceIndex = (int) (index / repeatedSourceLength);
    long withinSourceIndex = index % repeatedSourceLength;
    subSources[sourceIndex].set(withinSourceIndex, value);
  }
}
