package com.lhkbob.imaje.data;

/**
 *
 */
public interface IntSource extends DataSource<Integer> {
  int get(long index);

  void set(long index, int value);

  @Override
  default Integer getElement(long index) {
    return get(index);
  }

  @Override
  default void setElement(long index, Integer value) {
    set(index, value);
  }

  @Override
  default Class<Integer> getBoxedElementType() {
    return Integer.class;
  }
}
