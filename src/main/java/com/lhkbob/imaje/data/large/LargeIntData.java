package com.lhkbob.imaje.data.large;

import com.lhkbob.imaje.data.IntData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.IntBuffer;

/**
 *
 */
public class LargeIntData extends AbstractLargeDataSource<IntData> implements IntData {
  public LargeIntData(IntData[] sources) {
    super(sources);
  }

  @Override
  public int get(long index) {
    return getSource(index).get(getIndexInSource(index));
  }

  @Override
  public void get(long dataIndex, int[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeIntData", getLength(), dataIndex, length);

    bulkOperation(IntData::get, dataIndex, values, offset, length);
  }

  @Override
  public void get(long dataIndex, IntBuffer values) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("LargeIntData", getLength(), dataIndex, values.remaining());

    int limit = values.limit();
    bulkOperation(this::getSubSource, dataIndex, values, values.position(), values.remaining());
    // Make sure the entire buffer looks consumed
    values.limit(limit).position(limit);  }

  @Override
  public void set(long index, int value) {
    getSource(index).set(getIndexInSource(index), value);
  }

  @Override
  public void set(long dataIndex, IntBuffer values) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("LargeIntData", getLength(), dataIndex, values.remaining());

    int limit = values.limit();
    bulkOperation(this::setSubSource, dataIndex, values, values.position(), values.remaining());
    // Make sure the entire buffer looks consumed
    values.limit(limit).position(limit);
  }

  @Override
  public void set(long dataIndex, int[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeIntData", getLength(), dataIndex, length);

    bulkOperation(IntData::set, dataIndex, values, offset, length);
  }

  private void getSubSource(
      IntData source, long srcOffset, IntBuffer get, int getOffset, int getLength) {
    get.limit(getOffset + getLength).position(getOffset);
    source.get(srcOffset, get);
  }

  private void setSubSource(
      IntData source, long srcOffset, IntBuffer values, int valuesOffset, int valuesLength) {
    // Configure the position and limit of values for the given sub range
    values.limit(valuesOffset + valuesLength).position(valuesOffset);
    source.set(srcOffset, values);
  }
}
