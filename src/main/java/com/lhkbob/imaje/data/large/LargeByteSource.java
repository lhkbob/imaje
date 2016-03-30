package com.lhkbob.imaje.data.large;

import com.lhkbob.imaje.data.ByteSource;

/**
 *
 */
public class LargeByteSource extends AbstractLargeDataSource<Byte, ByteSource.Primitive> implements ByteSource.Primitive {
  public LargeByteSource(ByteSource.Primitive[] sources) {
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
}
