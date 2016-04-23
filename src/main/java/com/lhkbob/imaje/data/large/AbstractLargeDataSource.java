package com.lhkbob.imaje.data.large;

import com.lhkbob.imaje.data.DataSource;

import java.util.Arrays;

/**
 *
 */
public abstract class AbstractLargeDataSource<S extends DataSource> implements DataSource {
  private final S[] sources;
  private final long totalLength;
  private final long repeatedLength;
  private final boolean bigEndian;

  public AbstractLargeDataSource(S[] sources) {
    this.sources = Arrays.copyOf(sources, sources.length);
    repeatedLength = sources[0].getLength();
    bigEndian = sources[0].isBigEndian();

    long total = repeatedLength;
    for (int i = 1; i < sources.length; i++) {
      if (i < sources.length - 1) {
        if (sources[i].getLength() != repeatedLength) {
          throw new IllegalArgumentException(
              "All but last source must have the same size, expected: " + repeatedLength + ", but was " + sources[i].getLength());
        }
      } else {
        if (sources[i].getLength() > repeatedLength) {
          throw new IllegalArgumentException("Last source can have at most size " + repeatedLength + ", but was " + sources[i].getLength());
        }
      }

      if (bigEndian != sources[i].isBigEndian()) {
        throw new IllegalArgumentException("Endianness of sources are not all the same");
      }
      total += sources[i].getLength();
    }
    totalLength = total;
  }

  public S[] getSources() {
    return Arrays.copyOf(sources, sources.length);
  }

  @Override
  public long getLength() {
    return totalLength;
  }

  @Override
  public boolean isBigEndian() {
    return bigEndian;
  }

  @Override
  public boolean isGPUAccessible() {
    // Although the GPU might be able to support data sets that have more than a 32 bit index,
    // because Java can't allocate a contiguous array that long there is no way to have such a long
    // data source represented by a single pointer; thus this form of large data source cannot be
    // GPU accessible
    return false;
  }

  protected long getIndexInSource(long index) {
    return index % repeatedLength;
  }

  protected S getSource(long index) {
    return sources[(int) (index / repeatedLength)];
  }
}
