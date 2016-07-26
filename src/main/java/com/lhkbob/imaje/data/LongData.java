package com.lhkbob.imaje.data;

import com.lhkbob.imaje.util.Arguments;

import java.nio.LongBuffer;

/**
 *
 */
public interface LongData extends BitData {
  class Numeric implements NumericData<LongData>, DataView<LongData> {
    private final LongData source;

    public Numeric(LongData source) {
      Arguments.notNull("source", source);
      this.source = source;
    }

    @Override
    public LongData asBitData() {
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
    public LongData getSource() {
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
      // Clamp to long boundary values so casting roll-over from double isn't surprising
      value = Math.max(Long.MIN_VALUE, Math.min(value, Long.MAX_VALUE));
      source.set(index, Math.round(value));
    }
  }

  long get(long index);

  default void get(long dataIndex, long[] values) {
    get(dataIndex, values, 0, values.length);
  }

  default void get(long dataIndex, long[] values, int offset, int length) {
    Arguments.checkArrayRange("value array", values.length, offset, length);
    Arguments.checkArrayRange("LongData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      values[offset + i] = get(dataIndex + i);
    }
  }

  default void get(long dataIndex, LongBuffer values) {
    get(dataIndex, values, 0, values.capacity());
  }

  default void get(long dataIndex, LongBuffer values, int offset, int length) {
    Arguments.checkArrayRange("value buffer", values.capacity(), offset, length);
    Arguments.checkArrayRange("LongData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      values.put(offset + i, get(dataIndex + i));
    }
  }

  @Override
  default int getBitSize() {
    return Long.SIZE;
  }

  @Override
  default long getBits(long index) {
    return get(index);
  }

  void set(long index, long value);

  default void set(long dataIndex, long[] values) {
    set(dataIndex, values, 0, values.length);
  }

  default void set(long dataIndex, long[] values, int offset, int length) {
    Arguments.checkArrayRange("value array", values.length, offset, length);
    Arguments.checkArrayRange("LongData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      set(dataIndex + i, values[offset + i]);
    }
  }

  default void set(long dataIndex, LongBuffer values) {
    set(dataIndex, values, 0, values.capacity());
  }

  default void set(long dataIndex, LongBuffer values, int offset, int length) {
    Arguments.checkArrayRange("value buffer", values.capacity(), offset, length);
    Arguments.checkArrayRange("LongData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      set(dataIndex + i, values.get(offset + i));
    }
  }

  @Override
  default void setBits(long index, long value) {
    set(index, value);
  }
}
