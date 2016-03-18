package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.YUV;
import com.lhkbob.imaje.data.DoubleSource;

/**
 *
 */
public class YUVAdapter<T extends YUV> extends AbstractSingleSource3ComponentAdapter<T> {
  public YUVAdapter(
      Class<T> type, PixelLayout layout, DoubleSource data) {
    super(type, layout, false, data);
  }

  @Override
  protected void get(double c1, double c2, double c3, T result) {
    result.y(c1);
    result.u(c2);
    result.v(c3);
  }

  @Override
  protected void set(T value, long i1, long i2, long i3, DoubleSource data) {
    data.set(i1, value.y());
    data.set(i2, value.u());
    data.set(i3, value.v());
  }
}
