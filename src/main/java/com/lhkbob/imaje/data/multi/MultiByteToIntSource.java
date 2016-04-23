package com.lhkbob.imaje.data.multi;

import com.lhkbob.imaje.data.ByteSource;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.IntSource;

/**
 *
 */
public class MultiByteToIntSource implements IntSource, DataView<ByteSource> {
  private final ByteSource source;
  private boolean bigEndian;

  public MultiByteToIntSource(ByteSource source, boolean bigEndian) {
    this.source = source;
    this.bigEndian = bigEndian;
  }

  @Override
  public int get(long index) {
    // Java is big Endian so combination is always the same, just the order in which we look them
    // up from the byte source changes.
    long bidx = byteSourceIndex(index);

    byte b0, b1, b2, b3;
    if (bigEndian) {
      b3 = source.get(bidx);
      b2 = source.get(bidx + 1);
      b1 = source.get(bidx + 2);
      b0 = source.get(bidx + 3);
    } else {
      b0 = source.get(bidx);
      b1 = source.get(bidx + 1);
      b2 = source.get(bidx + 2);
      b3 = source.get(bidx + 3);
    }

    return ((b3 << 24) | ((0xff & b2) << 16) | ((0xff & b1) << 8) | (0xff & b0));
  }

  @Override
  public long getLength() {
    return source.getLength() >> 2;
  }

  @Override
  public ByteSource getSource() {
    return source;
  }

  @Override
  public boolean isBigEndian() {
    return bigEndian;
  }

  @Override
  public boolean isGPUAccessible() {
    return source.isGPUAccessible() && Data.isNativeEndian(this);
  }

  @Override
  public void set(long index, int value) {
    // Java is big Endian so separation is always the same, just the order in which we place them
    // into the byte source changes.
    long bidx = byteSourceIndex(index);

    byte b0 = (byte) (value);
    byte b1 = (byte) (value >> 8);
    byte b2 = (byte) (value >> 16);
    byte b3 = (byte) (value >> 24);

    if (bigEndian) {
      source.set(bidx, b3);
      source.set(bidx + 1, b2);
      source.set(bidx + 2, b1);
      source.set(bidx + 3, b0);
    } else {
      source.set(bidx, b0);
      source.set(bidx + 1, b1);
      source.set(bidx + 2, b2);
      source.set(bidx + 3, b3);
    }
  }

  private static long byteSourceIndex(long index) {
    return index << 2;
  }
}
