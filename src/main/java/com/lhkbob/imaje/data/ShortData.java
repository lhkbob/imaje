package com.lhkbob.imaje.data;

import com.lhkbob.imaje.util.Arguments;

import java.nio.ShortBuffer;

/**
 *
 */
public interface ShortData extends BitData {
  class Numeric implements NumericData<ShortData>, DataView<ShortData> {
    private final ShortData source;

    public Numeric(ShortData source) {
      Arguments.notNull("source", source);
      this.source = source;
    }

    @Override
    public ShortData asBitData() {
      return source;
    }

    @Override
    public int getBitSize() {
      return source.getBitSize();
    }

    @Override
    public long getLength() {
      return source.getLength();
    }

    @Override
    public ShortData getSource() {
      return source;
    }

    @Override
    public double getValue(long index) {
      return source.get(index);
    }

    @Override
    public boolean isBigEndian() {
      return source.isBigEndian();
    }

    @Override
    public boolean isGPUAccessible() {
      return source.isGPUAccessible();
    }

    @Override
    public void setValue(long index, double value) {
      // Clamp to short boundary values so casting roll-over from double isn't surprising
      value = Math.max(Short.MIN_VALUE, Math.min(value, Short.MAX_VALUE));
      source.set(index, (short) Math.round(value));
    }
  }

  short get(long index);

  default void get(long dataIndex, short[] values) {
    get(dataIndex, values, 0, values.length);
  }

  default void get(long dataIndex, short[] values, int offset, int length) {
    Arguments.checkArrayRange("value array", values.length, offset, length);
    Arguments.checkArrayRange("ShortData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      values[offset + i] = get(dataIndex + i);
    }
  }

  default void get(long dataIndex, ShortBuffer values) {
    Arguments.checkArrayRange("ShortData", getLength(), dataIndex, values.remaining());

    int rem = values.remaining();
    int off = values.position();

    for (int i = 0; i < rem; i++) {
      values.put(off + i, get(dataIndex + i));
    }
    values.position(values.limit());  }

  @Override
  default int getBitSize() {
    return Short.SIZE;
  }

  @Override
  default long getBits(long index) {
    return get(index);
  }

  void set(long index, short value);

  default void set(long dataIndex, short[] values) {
    set(dataIndex, values, 0, values.length);
  }

  default void set(long dataIndex, short[] values, int offset, int length) {
    Arguments.checkArrayRange("value array", values.length, offset, length);
    Arguments.checkArrayRange("ShortData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      set(dataIndex + i, values[offset + i]);
    }
  }

  default void set(long dataIndex, ShortBuffer values) {
    Arguments.checkArrayRange("ShortData", getLength(), dataIndex, values.remaining());

    int rem = values.remaining();
    int off = values.position();

    for (int i = 0; i < rem; i++) {
      set(dataIndex + i, values.get(off + i));
    }
    values.position(values.limit());
  }

  @Override
  default void setBits(long index, long value) {
    set(index, (short) value);
  }
}
