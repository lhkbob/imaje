package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.ByteSource;
import com.lhkbob.imaje.data.DataType;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.IntSource;

/**
 *
 */
public class UnsignedByteSource implements IntSource, DataView<ByteSource> {
  private final ByteSource source;

  public UnsignedByteSource(ByteSource source) {
    if (source.getDataType() != DataType.SINT8)
      throw new IllegalArgumentException(
          "Source type must be SINT8 to ensure no undue bit manipulation occurs");
    this.source = source;
  }

  @Override
  public int get(long index) {
    return (short) Byte.toUnsignedInt(source.get(index));
  }

  @Override
  public long getLength() {
    return source.getLength();
  }

  @Override
  public boolean isBigEndian() {
    return source.isBigEndian();
  }

  @Override
  public DataType getDataType() {
    return DataType.UINT8;
  }

  @Override
  public boolean isGPUAccessible() {
    return source.isGPUAccessible();
  }

  @Override
  public ByteSource getSource() {
    return source;
  }

  @Override
  public void set(long index, int value) {
    source.set(index, (byte) value);
  }
}
