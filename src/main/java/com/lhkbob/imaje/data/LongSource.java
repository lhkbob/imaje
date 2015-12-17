package com.lhkbob.imaje.data;

/**
 *
 */
public interface LongSource extends DataSource<Long> {
  long get(long index);

  void set(long index, long value);

  @Override
  default Long getElement(long index) {
    return get(index);
  }

  @Override
  default void setElement(long index, Long value) {
    set(index, value);
  }

  @Override
  default Class<Long> getBoxedElementType() {
    return Long.class;
  }
}
