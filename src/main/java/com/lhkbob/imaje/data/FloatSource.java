package com.lhkbob.imaje.data;

/**
 *
 */
public interface FloatSource extends DataSource<Float> {
  interface Primitive extends FloatSource {

  }

  float get(long index);

  @Override
  default Class<Float> getBoxedElementType() {
    return Float.class;
  }

  @Override
  default Float getElement(long index) {
    return get(index);
  }

  void set(long index, float value);

  @Override
  default void setElement(long index, Float value) {
    set(index, value);
  }
}
