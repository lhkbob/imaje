package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.ByteSource;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.IntSource;

/**
 *
 */
public class UnsignedByteSource implements IntSource, DataView<ByteSource> {
  private final ByteSource source;

  public UnsignedByteSource(ByteSource source) {
    this.source = source;
  }

  @Override
  public long getLength() {
    return source.getLength();
  }

  @Override
  public int get(long index) {
    return (short) Byte.toUnsignedInt(source.get(index));
  }

  @Override
  public void set(long index, int value) {
    source.set(index, (byte) value);
  }

  @Override
  public ByteSource getSource() {
    return source;
  }
}
