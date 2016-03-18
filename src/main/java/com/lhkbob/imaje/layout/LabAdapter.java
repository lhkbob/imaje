package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.Lab;
import com.lhkbob.imaje.data.DoubleSource;

/**
 *
 */
public class LabAdapter<T extends Lab> extends AbstractSingleSource3ComponentAdapter<T> {
  public LabAdapter(
      Class<T> type, PixelLayout layout, DoubleSource data) {
    super(type, layout, false, data);
  }

  @Override
  protected void get(double c1, double c2, double c3, T result) {
    result.l(c1);
    result.a(c2);
    result.b(c3);
  }

  @Override
  protected void set(T value, long i1, long i2, long i3, DoubleSource data) {
    data.set(i1, value.l());
    data.set(i2, value.a());
    data.set(i3, value.b());
  }
}
