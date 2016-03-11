package com.lhkbob.imaje.data;

/**
 *
 */
public interface DataSource<T extends Number> {
  Class<T> getBoxedElementType();

  T getElement(long index);

  long getLength();

  void setElement(long index, T value);

  boolean isBigEndian();

  DataType getDataType();

  boolean isGPUAccessible();
}
