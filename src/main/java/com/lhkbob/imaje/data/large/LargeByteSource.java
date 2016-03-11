package com.lhkbob.imaje.data.large;

import com.lhkbob.imaje.data.ByteSource;
import com.lhkbob.imaje.data.DataType;

/**
 *
 */
public class LargeByteSource extends AbstractLargeDataSource<Byte, ByteSource> implements ByteSource {
  public LargeByteSource(ByteSource[] sources) {
    super(sources);
  }

  @Override
  public byte get(long index) {
    return getSource(index).get(getIndexInSource(index));
  }

  @Override
  public DataType getDataType() {
    return DataType.SINT8;
  }

  @Override
  public void set(long index, byte value) {
    getSource(index).set(getIndexInSource(index), value);
  }
}
