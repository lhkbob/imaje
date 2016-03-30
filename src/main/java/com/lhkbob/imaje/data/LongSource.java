package com.lhkbob.imaje.data;

/**
 *
 */
public interface LongSource extends DataSource<Long> {
  interface Primitive extends LongSource {

  }

  long get(long index);

  @Override
  default Class<Long> getBoxedElementType() {
    return Long.class;
  }

  @Override
  default Long getElement(long index) {
    return get(index);
  }

  void set(long index, long value);

  @Override
  default void setElement(long index, Long value) {
    set(index, value);
  }
}
