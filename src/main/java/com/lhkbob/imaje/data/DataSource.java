package com.lhkbob.imaje.data;

/**
 *
 */
public interface DataSource<T extends Number> {
  long getLength();

  T getElement(long index);

  void setElement(long index, T value);

  Class<T> getBoxedElementType();
}
