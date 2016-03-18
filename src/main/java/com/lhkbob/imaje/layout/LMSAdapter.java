package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.LMS;
import com.lhkbob.imaje.data.DoubleSource;

/**
 *
 */
public class LMSAdapter extends AbstractSingleSource3ComponentAdapter<LMS> {

  public LMSAdapter(PixelLayout layout, DoubleSource data) {
    super(LMS.class, layout, false, data);
  }

  @Override
  protected void get(double c1, double c2, double c3, LMS result) {
    result.l(c1);
    result.m(c2);
    result.s(c3);
  }

  @Override
  protected void set(LMS value, long i1, long i2, long i3, DoubleSource data) {
    data.set(i1, value.l());
    data.set(i2, value.m());
    data.set(i3, value.s());
  }
}
