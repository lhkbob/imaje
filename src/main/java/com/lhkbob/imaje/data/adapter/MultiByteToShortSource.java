package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.ByteSource;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.ShortSource;

/**
 *
 */
public class MultiByteToShortSource implements ShortSource, DataView<ByteSource> {
  private final ByteSource source;
  private boolean bigEndian;

  public MultiByteToShortSource(ByteSource source, boolean bigEndian) {
    this.source = source;
    this.bigEndian = bigEndian;
  }

  @Override
  public long getLength() {
    return source.getLength() >> 1;
  }

  @Override
  public short get(long index) {
    // Java is big Endian so combination is always the same, just the order in which we look them
    // up from the byte source changes.
    long bidx = byteSourceIndex(index);

    byte b0, b1;
    if (bigEndian) {
      b1 = source.get(bidx);
      b0 = source.get(bidx + 1);
    } else {
      b0 = source.get(bidx);
      b1 = source.get(bidx + 1);
    }

    return (short) ((b1 << 8) | (0xff & b0));
  }

  @Override
  public void set(long index, short value) {
    // Java is big Endian so separation is always the same, just the order in which we place them
    // into the byte source changes.
    long bidx = byteSourceIndex(index);

    byte b0 = (byte) (value);
    byte b1 = (byte) (value >> 8);

    if (bigEndian) {
      source.set(bidx, b1);
      source.set(bidx + 1, b0);
    } else {
      source.set(bidx, b0);
      source.set(bidx + 1, b1);
    }
  }

  public boolean isBigEndian() {
    return bigEndian;
  }

  @Override
  public ByteSource getSource() {
    return source;
  }

  private static long byteSourceIndex(long index) {
    return index << 1;
  }
}
