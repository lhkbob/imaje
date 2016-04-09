package com.lhkbob.imaje.data;

/**
 *
 */
public interface NumericDataSource extends DataSource {
  double getValue(long index);

  void setValue(long index, double value);
}
