package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataType;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.IntSource;
import com.lhkbob.imaje.data.LongSource;

/**
 *
 */
public class UnsignedIntSource implements LongSource, DataView<IntSource> {
  private final IntSource source;

  public UnsignedIntSource(IntSource source) {
    if (source.getDataType() != DataType.SINT32)
      throw new IllegalArgumentException(
          "Source type must be SINT32 to ensure no undue bit manipulation occurs");
    this.source = source;
  }

  @Override
  public long get(long index) {
    return Integer.toUnsignedLong(source.get(index));
  }

  @Override
  public boolean isBigEndian() {
    return source.isBigEndian();
  }

  @Override
  public DataType getDataType() {
    return DataType.UINT32;
  }

  @Override
  public boolean isGPUAccessible() {
    return source.isGPUAccessible();
  }

  @Override
  public long getLength() {
    return source.getLength();
  }

  @Override
  public IntSource getSource() {
    return source;
  }

  @Override
  public void set(long index, long value) {
    source.set(index, (int) value);
  }
}
