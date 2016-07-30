package com.lhkbob.imaje.data.large;

import com.lhkbob.imaje.data.LongData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.LongBuffer;

/**
 *
 */
public class LargeLongData extends AbstractLargeDataSource<LongData> implements LongData {
  public LargeLongData(LongData[] sources) {
    super(sources);
  }

  @Override
  public long get(long index) {
    return getSource(index).get(getIndexInSource(index));
  }

  @Override
  public void get(long dataIndex, long[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeLongData", getLength(), dataIndex, length);

    bulkOperation(LongData::get, dataIndex, values, offset, length);
  }

  @Override
  public void get(long dataIndex, LongBuffer values) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("LargeLongData", getLength(), dataIndex, values.remaining());

    int limit = values.limit();
    bulkOperation(this::getSubSource, dataIndex, values, values.position(), values.remaining());
    // Make sure the entire buffer looks consumed
    values.limit(limit).position(limit);  }

  @Override
  public void set(long index, long value) {
    getSource(index).set(getIndexInSource(index), value);
  }

  @Override
  public void set(long dataIndex, LongBuffer values) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("LargeLongData", getLength(), dataIndex, values.remaining());

    int limit = values.limit();
    bulkOperation(this::setSubSource, dataIndex, values, values.position(), values.remaining());
    // Make sure the entire buffer looks consumed
    values.limit(limit).position(limit);
  }

  @Override
  public void set(long dataIndex, long[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeLongData", getLength(), dataIndex, length);

    bulkOperation(LongData::set, dataIndex, values, offset, length);
  }

  private void getSubSource(
      LongData source, long srcOffset, LongBuffer get, int getOffset, int getLength) {
    get.limit(getOffset + getLength).position(getOffset);
    source.get(srcOffset, get);
  }

  private void setSubSource(
      LongData source, long srcOffset, LongBuffer values, int valuesOffset, int valuesLength) {
    // Configure the position and limit of values for the given sub range
    values.limit(valuesOffset + valuesLength).position(valuesOffset);
    source.set(srcOffset, values);
  }
}
