package com.lhkbob.imaje.data.large;

import com.lhkbob.imaje.data.FloatData;
import com.lhkbob.imaje.data.IntData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

/**
 *
 */
public class LargeFloatData extends AbstractLargeDataSource<FloatData> implements FloatData {
  public LargeFloatData(FloatData[] sources) {
    super(sources);
  }

  @Override
  public float get(long index) {
    return getSource(index).get(getIndexInSource(index));
  }

  @Override
  public void set(long index, float value) {
    getSource(index).set(getIndexInSource(index), value);
  }

  @Override
  public IntData asBitData() {
    FloatData[] sources = getSources();
    IntData[] bits = new IntData[sources.length];
    for (int i = 0; i < sources.length; i++) {
      bits[i] = sources[i].asBitData();
    }

    return new LargeIntData(bits);
  }

  @Override
  public void setValues(long dataIndex, double[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeFloatData", getLength(), dataIndex, length);

    bulkOperation(FloatData::setValues, dataIndex, values, offset, length);
  }

  @Override
  public void getValues(long dataIndex, double[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeFloatData", getLength(), dataIndex, length);

    bulkOperation(FloatData::getValues, dataIndex, values, offset, length);
  }

  @Override
  public void setValues(long dataIndex, float[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeFloatData", getLength(), dataIndex, length);

    bulkOperation(FloatData::setValues, dataIndex, values, offset, length);
  }

  @Override
  public void getValues(long dataIndex, float[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeFloatData", getLength(), dataIndex, length);

    bulkOperation(FloatData::getValues, dataIndex, values, offset, length);
  }

  @Override
  public void setValues(long dataIndex, DoubleBuffer values) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("LargeDoubleData", getLength(), dataIndex, values.remaining());

    int limit = values.limit();
    bulkOperation(this::setSubSource, dataIndex, values, values.position(), values.remaining());
    // Make sure the entire buffer looks consumed
    values.limit(limit).position(limit);
  }

  @Override
  public void setValues(long dataIndex, FloatBuffer values) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("LargeDoubleData", getLength(), dataIndex, values.remaining());

    int limit = values.limit();
    bulkOperation(this::setSubSource, dataIndex, values, values.position(), values.remaining());
    // Make sure the entire buffer looks consumed
    values.limit(limit).position(limit);
  }

  @Override
  public void getValues(long dataIndex, DoubleBuffer values) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("LargeDoubleData", getLength(), dataIndex, values.remaining());

    int limit = values.limit();
    bulkOperation(this::getSubSource, dataIndex, values, values.position(), values.remaining());
    // Make sure the entire buffer looks consumed
    values.limit(limit).position(limit);
  }

  @Override
  public void getValues(long dataIndex, FloatBuffer values) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("LargeDoubleData", getLength(), dataIndex, values.remaining());

    int limit = values.limit();
    bulkOperation(this::getSubSource, dataIndex, values, values.position(), values.remaining());
    // Make sure the entire buffer looks consumed
    values.limit(limit).position(limit);
  }

  private void setSubSource(
      FloatData source, long srcOffset, DoubleBuffer values, int valuesOffset, int valuesLength) {
    // Configure the position and limit of values for the given sub range
    values.limit(valuesOffset + valuesLength).position(valuesOffset);
    source.setValues(srcOffset, values);
  }

  private void setSubSource(
      FloatData source, long srcOffset, FloatBuffer values, int valuesOffset, int valuesLength) {
    // Configure the position and limit of values for the given sub range
    values.limit(valuesOffset + valuesLength).position(valuesOffset);
    source.setValues(srcOffset, values);
  }

  private void getSubSource(
      FloatData source, long srcOffset, DoubleBuffer get, int getOffset, int getLength) {
    get.limit(getOffset + getLength).position(getOffset);
    source.getValues(srcOffset, get);
  }

  private void getSubSource(
      FloatData source, long srcOffset, FloatBuffer get, int getOffset, int getLength) {
    get.limit(getOffset + getLength).position(getOffset);
    source.getValues(srcOffset, get);
  }
}
