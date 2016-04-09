package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.ByteSource;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.LongSource;

/**
 *
 */
public class MultiByteToLongSource implements LongSource, DataView<ByteSource> {
  private final ByteSource source;
  private boolean bigEndian;

  public MultiByteToLongSource(ByteSource source, boolean bigEndian) {
    this.source = source;
    this.bigEndian = bigEndian;
  }

  @Override
  public long get(long index) {
    // Java is big Endian so combination is always the same, just the order in which we look them
    // up from the byte source changes.
    long bidx = byteSourceIndex(index);

    byte b0, b1, b2, b3, b4, b5, b6, b7;
    if (bigEndian) {
      b7 = source.get(bidx);
      b6 = source.get(bidx + 1);
      b5 = source.get(bidx + 2);
      b4 = source.get(bidx + 3);
      b3 = source.get(bidx + 4);
      b2 = source.get(bidx + 5);
      b1 = source.get(bidx + 6);
      b0 = source.get(bidx + 7);
    } else {
      b0 = source.get(bidx);
      b1 = source.get(bidx + 1);
      b2 = source.get(bidx + 2);
      b3 = source.get(bidx + 3);
      b4 = source.get(bidx + 4);
      b5 = source.get(bidx + 5);
      b6 = source.get(bidx + 6);
      b7 = source.get(bidx + 7);
    }

    return (((0xffL & b7) << 56) | ((0xffL & b6) << 48) | ((0xffL & b5) << 40) | ((0xffL & b4)
        << 32) | ((0xffL & b3) << 24) | ((0xffL & b2) << 16) | ((0xffL & b1) << 8) | (0xffL & b0));
  }

  @Override
  public long getLength() {
    return source.getLength() >> 3;
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
  public void set(long index, long value) {
    // Java is big Endian so separation is always the same, just the order in which we place them
    // into the byte source changes.
    long bidx = byteSourceIndex(index);

    byte b0 = (byte) (value);
    byte b1 = (byte) (value >> 8);
    byte b2 = (byte) (value >> 16);
    byte b3 = (byte) (value >> 24);
    byte b4 = (byte) (value >> 32);
    byte b5 = (byte) (value >> 40);
    byte b6 = (byte) (value >> 48);
    byte b7 = (byte) (value >> 56);

    if (bigEndian) {
      source.set(bidx, b7);
      source.set(bidx + 1, b6);
      source.set(bidx + 2, b5);
      source.set(bidx + 3, b4);
      source.set(bidx + 4, b3);
      source.set(bidx + 5, b2);
      source.set(bidx + 6, b1);
      source.set(bidx + 7, b0);
    } else {
      source.set(bidx, b0);
      source.set(bidx + 1, b1);
      source.set(bidx + 2, b2);
      source.set(bidx + 3, b3);
      source.set(bidx + 4, b4);
      source.set(bidx + 5, b5);
      source.set(bidx + 6, b6);
      source.set(bidx + 7, b7);
    }
  }

  private static long byteSourceIndex(long index) {
    return index << 3;
  }
}
