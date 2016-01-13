package com.lhkbob.imaje.data.large;

import com.lhkbob.imaje.data.FloatSource;

import java.util.Arrays;

/**
 *
 */
public class LargeFloatSource implements FloatSource {
  private final long repeatedSourceLength;
  private final FloatSource[] subSources;
  private final long totalLength;

  public LargeFloatSource(FloatSource[] sources) {
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
  public float get(long index) {
    int sourceIndex = (int) (index / repeatedSourceLength);
    long withinSourceIndex = index % repeatedSourceLength;
    return subSources[sourceIndex].get(withinSourceIndex);
  }

  @Override
  public long getLength() {
    return totalLength;
  }

  public FloatSource[] getSources() {
    return Arrays.copyOf(subSources, subSources.length);
  }

  @Override
  public void set(long index, float value) {
    int sourceIndex = (int) (index / repeatedSourceLength);
    long withinSourceIndex = index % repeatedSourceLength;
    subSources[sourceIndex].set(withinSourceIndex, value);
  }
}
