package com.lhkbob.imaje.data;

/**
 *
 */
public interface BitData extends DataBuffer {
  long getBits(long index);

  void setBits(long index, long value);
}
