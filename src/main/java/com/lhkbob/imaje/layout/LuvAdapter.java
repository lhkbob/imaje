package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.Luv;
import com.lhkbob.imaje.data.DoubleSource;

/**
 *
 */
public class LuvAdapter extends AbstractSingleSource3ComponentAdapter<Luv> {

  public LuvAdapter(PixelLayout layout, DoubleSource data) {
    super(Luv.class, layout, false, data);
  }

  @Override
  protected void get(double c1, double c2, double c3, Luv result) {
    result.l(c1);
    result.u(c2);
    result.v(c3);
  }

  @Override
  protected void set(Luv value, long i1, long i2, long i3, DoubleSource data) {
    data.set(i1, value.l());
    data.set(i2, value.u());
    data.set(i3, value.v());
  }
}
