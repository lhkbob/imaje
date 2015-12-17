package com.lhkbob.imaje.data;

/**
 *
 */
public interface FloatSource extends DataSource<Float> {
  float get(long index);

  void set(long index, float value);

  @Override
  default Float getElement(long index) {
    return get(index);
  }

  @Override
  default void setElement(long index, Float value) {
    set(index, value);
  }

  @Override
  default Class<Float> getBoxedElementType() {
    return Float.class;
  }
}
