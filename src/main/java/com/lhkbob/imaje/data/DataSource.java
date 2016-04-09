package com.lhkbob.imaje.data;

/**
 *
 */
public interface DataSource {
  long getLength();

  boolean isBigEndian();

  boolean isGPUAccessible();

  int getBitSize();
}
