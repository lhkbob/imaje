package com.lhkbob.imaje.data.large;

import com.lhkbob.imaje.data.DoubleData;
import com.lhkbob.imaje.data.LongData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

/**
 *
 */
public class LargeDoubleData extends AbstractLargeDataSource<DoubleData> implements DoubleData {
  public LargeDoubleData(DoubleData[] sources) {
    super(sources);
  }

  @Override
  public double get(long index) {
    return getSource(index).get(getIndexInSource(index));
  }

  @Override
  public void set(long index, double value) {
    getSource(index).set(getIndexInSource(index), value);
  }

  @Override
  public LongData asBitData() {
    DoubleData[] sources = getSources();
    LongData[] bits = new LongData[sources.length];
    for (int i = 0; i < sources.length; i++) {
      bits[i] = sources[i].asBitData();
    }

    return new LargeLongData(bits);
  }

  @Override
  public void setValues(long dataIndex, double[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeDoubleData", getLength(), dataIndex, length);

    bulkOperation(DoubleData::setValues, dataIndex, values, offset, length);
  }

  @Override
  public void setValues(long dataIndex, DoubleBuffer values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values buffer", values.capacity(), offset, length);
    Arguments.checkArrayRange("LargeByteData", getLength(), dataIndex, length);

    bulkOperation(DoubleData::setValues, dataIndex, values, offset, length);
  }

  @Override
  public void getValues(long dataIndex, double[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeDoubleData", getLength(), dataIndex, length);

    bulkOperation(DoubleData::getValues, dataIndex, values, offset, length);
  }

  @Override
  public void getValues(long dataIndex, DoubleBuffer values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values buffer", values.capacity(), offset, length);
    Arguments.checkArrayRange("LargeDoubleData", getLength(), dataIndex, length);

    bulkOperation(DoubleData::getValues, dataIndex, values, offset, length);
  }

  @Override
  public void setValues(long dataIndex, float[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeDoubleData", getLength(), dataIndex, length);

    bulkOperation(DoubleData::setValues, dataIndex, values, offset, length);
  }

  @Override
  public void setValues(long dataIndex, FloatBuffer values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values buffer", values.capacity(), offset, length);
    Arguments.checkArrayRange("LargeByteData", getLength(), dataIndex, length);

    bulkOperation(DoubleData::setValues, dataIndex, values, offset, length);
  }

  @Override
  public void getValues(long dataIndex, float[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeDoubleData", getLength(), dataIndex, length);

    bulkOperation(DoubleData::getValues, dataIndex, values, offset, length);
  }

  @Override
  public void getValues(long dataIndex, FloatBuffer values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values buffer", values.capacity(), offset, length);
    Arguments.checkArrayRange("LargeDoubleData", getLength(), dataIndex, length);

    bulkOperation(DoubleData::getValues, dataIndex, values, offset, length);
  }
}
