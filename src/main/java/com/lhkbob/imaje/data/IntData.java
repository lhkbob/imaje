package com.lhkbob.imaje.data;

import com.lhkbob.imaje.util.Arguments;

import java.nio.IntBuffer;

/**
 *
 */
public interface IntData extends BitData {
  class Numeric implements NumericData<IntData>, DataView<IntData> {
    private final IntData source;

    public Numeric(IntData source) {
      Arguments.notNull("source", source);
      this.source = source;
    }

    @Override
    public IntData asBitData() {
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
    public IntData getSource() {
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
      // Clamp to int boundary values so casting roll-over from double isn't surprising
      value = Math.max(Integer.MIN_VALUE, Math.min(value, Integer.MAX_VALUE));
      source.set(index, (int) Math.round(value));
    }
  }

  int get(long index);

  default void get(long dataIndex, int[] values) {
    get(dataIndex, values, 0, values.length);
  }

  default void get(long dataIndex, int[] values, int offset, int length) {
    Arguments.checkArrayRange("value array", values.length, offset, length);
    Arguments.checkArrayRange("IntData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      values[offset + i] = get(dataIndex + i);
    }
  }

  default void get(long dataIndex, IntBuffer values) {
    get(dataIndex, values, 0, values.capacity());
  }

  default void get(long dataIndex, IntBuffer values, int offset, int length) {
    Arguments.checkArrayRange("value buffer", values.capacity(), offset, length);
    Arguments.checkArrayRange("IntData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      values.put(offset + i, get(dataIndex + i));
    }
  }

  @Override
  default int getBitSize() {
    return Integer.SIZE;
  }

  @Override
  default long getBits(long index) {
    return get(index);
  }

  void set(long index, int value);

  default void set(long dataIndex, int[] values) {
    set(dataIndex, values, 0, values.length);
  }

  default void set(long dataIndex, int[] values, int offset, int length) {
    Arguments.checkArrayRange("value array", values.length, offset, length);
    Arguments.checkArrayRange("IntData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      set(dataIndex + i, values[offset + i]);
    }
  }

  default void set(long dataIndex, IntBuffer values) {
    set(dataIndex, values, 0, values.capacity());
  }

  default void set(long dataIndex, IntBuffer values, int offset, int length) {
    Arguments.checkArrayRange("value buffer", values.capacity(), offset, length);
    Arguments.checkArrayRange("IntData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      set(dataIndex + i, values.get(offset + i));
    }
  }

  @Override
  default void setBits(long index, long value) {
    set(index, (int) value);
  }
}
