package com.lhkbob.imaje.data.large;

import com.lhkbob.imaje.data.ShortData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.ShortBuffer;

/**
 *
 */
public class LargeShortData extends AbstractLargeDataSource<ShortData> implements ShortData {
  public LargeShortData(ShortData[] sources) {
    super(sources);
  }

  @Override
  public short get(long index) {
    return getSource(index).get(getIndexInSource(index));
  }

  @Override
  public void get(long dataIndex, short[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeShortData", getLength(), dataIndex, length);

    bulkOperation(ShortData::get, dataIndex, values, offset, length);
  }

  @Override
  public void get(long dataIndex, ShortBuffer values) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("LargeShortData", getLength(), dataIndex, values.remaining());

    int limit = values.limit();
    bulkOperation(this::getSubSource, dataIndex, values, values.position(), values.remaining());
    // Make sure the entire buffer looks consumed
    values.limit(limit).position(limit);  }

  @Override
  public void set(long index, short value) {
    getSource(index).set(getIndexInSource(index), value);
  }

  @Override
  public void set(long dataIndex, ShortBuffer values) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("LargeShortData", getLength(), dataIndex, values.remaining());

    int limit = values.limit();
    bulkOperation(this::setSubSource, dataIndex, values, values.position(), values.remaining());
    // Make sure the entire buffer looks consumed
    values.limit(limit).position(limit);
  }

  @Override
  public void set(long dataIndex, short[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeShortData", getLength(), dataIndex, length);

    bulkOperation(ShortData::set, dataIndex, values, offset, length);
  }

  private void getSubSource(
      ShortData source, long srcOffset, ShortBuffer get, int getOffset, int getLength) {
    get.limit(getOffset + getLength).position(getOffset);
    source.get(srcOffset, get);
  }

  private void setSubSource(
      ShortData source, long srcOffset, ShortBuffer values, int valuesOffset, int valuesLength) {
    // Configure the position and limit of values for the given sub range
    values.limit(valuesOffset + valuesLength).position(valuesOffset);
    source.set(srcOffset, values);
  }
}
