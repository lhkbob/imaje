package com.lhkbob.imaje.data;

/**
 *
 */
public interface IntSource extends DataSource<Integer> {
  int get(long index);

  @Override
  default Class<Integer> getBoxedElementType() {
    return Integer.class;
  }

  @Override
  default Integer getElement(long index) {
    return get(index);
  }

  void set(long index, int value);

  @Override
  default void setElement(long index, Integer value) {
    set(index, value);
  }
}
