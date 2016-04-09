package com.lhkbob.imaje.data;

/**
 *
 */
public interface BitDataSource extends DataSource {
  long getBits(long index);

  void setBits(long index, long value);
}
