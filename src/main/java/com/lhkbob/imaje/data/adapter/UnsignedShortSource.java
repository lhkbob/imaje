package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataType;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.IntSource;
import com.lhkbob.imaje.data.ShortSource;

/**
 *
 */
public class UnsignedShortSource implements IntSource, DataView<ShortSource> {
  private final ShortSource source;

  public UnsignedShortSource(ShortSource source) {
    if (source.getDataType() != DataType.SINT16)
      throw new IllegalArgumentException(
          "Source type must be SINT16 to ensure no undue bit manipulation occurs");
    this.source = source;
  }

  @Override
  public int get(long index) {
    return Short.toUnsignedInt(source.get(index));
  }

  @Override
  public boolean isBigEndian() {
    return source.isBigEndian();
  }

  @Override
  public DataType getDataType() {
    return DataType.UINT16;
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
  public ShortSource getSource() {
    return source;
  }

  @Override
  public void set(long index, int value) {
    source.set(index, (short) value);
  }
}
