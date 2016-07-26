package com.lhkbob.imaje.data.large;

import com.lhkbob.imaje.data.ByteData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.ByteBuffer;

/**
 */
public class LargeByteData extends AbstractLargeDataSource<ByteData> implements ByteData {
  public LargeByteData(ByteData[] sources) {
    super(sources);
  }

  @Override
  public byte get(long index) {
    return getSource(index).get(getIndexInSource(index));
  }

  @Override
  public void set(long index, byte value) {
    getSource(index).set(getIndexInSource(index), value);
  }

  @Override
  public void set(long dataIndex, byte[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeByteData", getLength(), dataIndex, length);

    bulkOperation(ByteData::set, dataIndex, values, offset, length);
  }

  @Override
  public void set(long dataIndex, ByteBuffer values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values buffer", values.capacity(), offset, length);
    Arguments.checkArrayRange("LargeByteData", getLength(), dataIndex, length);

    bulkOperation(ByteData::set, dataIndex, values, offset, length);
  }

  @Override
  public void get(long dataIndex, byte[] values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("LargeByteData", getLength(), dataIndex, length);

    bulkOperation(ByteData::get, dataIndex, values, offset, length);
  }

  @Override
  public void get(long dataIndex, ByteBuffer values, int offset, int length) {
    // Optimize by calling bulk sets on sub-sources with appropriately updated ranges
    Arguments.checkArrayRange("values buffer", values.capacity(), offset, length);
    Arguments.checkArrayRange("LargeByteData", getLength(), dataIndex, length);

    bulkOperation(ByteData::get, dataIndex, values, offset, length);
  }
}
