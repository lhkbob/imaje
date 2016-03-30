package com.lhkbob.imaje.data;

/**
 *
 */
public interface ShortSource extends DataSource<Short> {
  interface Primitive extends ShortSource {

  }

  short get(long index);

  @Override
  default Class<Short> getBoxedElementType() {
    return Short.class;
  }

  @Override
  default Short getElement(long index) {
    return get(index);
  }

  void set(long index, short value);

  @Override
  default void setElement(long index, Short value) {
    set(index, value);
  }
}
