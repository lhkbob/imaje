package com.lhkbob.imaje.data;

import com.lhkbob.imaje.util.Arguments;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

/**
 *
 */
public interface NumericData<T extends BitData> extends DataBuffer {
  double getValue(long index);

  void setValue(long index, double value);

  T asBitData();

  default void setValues(long dataIndex, float[] values) {
    setValues(dataIndex, values, 0, values.length);
  }

  default void setValues(long dataIndex, double[] values) {
    setValues(dataIndex, values, 0, values.length);
  }

  default void setValues(long dataIndex, float[] values, int offset, int length) {
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      setValue(dataIndex + i, values[offset + i]);
    }
  }

  default void setValues(long dataIndex, double[] values, int offset, int length) {
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      setValue(dataIndex + i, values[offset + i]);
    }
  }

  default void setValues(long dataIndex, FloatBuffer values) {
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, values.remaining());

    int rem = values.remaining();
    int off = values.position();

    for (int i = 0; i < rem; i++) {
      setValue(dataIndex + i, values.get(off + i));
    }
    values.position(values.limit());
  }

  default void setValues(long dataIndex, DoubleBuffer values) {
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, values.remaining());

    int rem = values.remaining();
    int off = values.position();

    for (int i = 0; i < rem; i++) {
      setValue(dataIndex + i, values.get(off + i));
    }
    values.position(values.limit());
  }

  default void getValues(long dataIndex, float[] values) {
    getValues(dataIndex, values, 0, values.length);
  }

  default void getValues(long dataIndex, double[] values) {
    getValues(dataIndex, values, 0, values.length);
  }

  default void getValues(long dataIndex, float[] values, int offset, int length) {
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      values[offset + i] = (float) getValue(dataIndex + i);
    }
  }

  default void getValues(long dataIndex, double[] values, int offset, int length) {
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, length);

    for (int i = 0; i < length; i++) {
      values[offset + i] = getValue(dataIndex + i);
    }
  }

  default void getValues(long dataIndex, FloatBuffer values) {
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, values.remaining());

    int rem = values.remaining();
    int off = values.position();

    for (int i = 0; i < rem; i++) {
      values.put(off + i, (float) getValue(dataIndex + i));
    }
    values.position(values.limit());
  }

  default void getValues(long dataIndex, DoubleBuffer values) {
    Arguments.checkArrayRange("NumericData", getLength(), dataIndex, values.remaining());

    int rem = values.remaining();
    int off = values.position();

    for (int i = 0; i < rem; i++) {
      values.put(off + i, getValue(dataIndex + i));
    }
    values.position(values.limit());
  }
}
