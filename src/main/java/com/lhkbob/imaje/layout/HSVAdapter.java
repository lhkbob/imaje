package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.HSV;
import com.lhkbob.imaje.data.DoubleSource;

/**
 *
 */
public class HSVAdapter extends AbstractSingleSource3ComponentAdapter<HSV> {

  public HSVAdapter(PixelLayout layout, DoubleSource data) {
    super(HSV.class, layout, false, data);
  }

  @Override
  protected void get(double c1, double c2, double c3, HSV result) {
    result.h(c1);
    result.s(c2);
    result.v(c3);
  }

  @Override
  protected void set(HSV value, long i1, long i2, long i3, DoubleSource data) {
    data.set(i1, value.h());
    data.set(i2, value.s());
    data.set(i3, value.v());
  }
}
