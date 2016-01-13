package com.lhkbob.imaje.data;

/**
 *
 */
public interface DoubleSource extends DataSource<Double> {
  double get(long index);

  @Override
  default Class<Double> getBoxedElementType() {
    return Double.class;
  }

  @Override
  default Double getElement(long index) {
    return get(index);
  }

  void set(long index, double value);

  @Override
  default void setElement(long index, Double value) {
    set(index, value);
  }
}
