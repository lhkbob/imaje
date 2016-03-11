package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataType;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.LongSource;

/**
 *
 */
public class UnsignedLongSource implements LongSource, DataView<LongSource> {
  private final LongSource source;

  public UnsignedLongSource(LongSource source) {
    if (source.getDataType() != DataType.SINT64) {
      throw new IllegalArgumentException("Source type must be SINT64 to ensure no undo bit manipulations occur");
    }
    this.source = source;
  }

  @Override
  public long get(long index) {
    return source.get(index);
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
    return DataType.UINT64;
  }

  @Override
  public boolean isGPUAccessible() {
    return source.isGPUAccessible();
  }

  @Override
  public void set(long index, long value) {
    source.set(index, value);
  }

  @Override
  public LongSource getSource() {
    return source;
  }
}
