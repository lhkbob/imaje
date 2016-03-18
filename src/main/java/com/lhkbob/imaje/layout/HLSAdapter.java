package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.HLS;
import com.lhkbob.imaje.data.DoubleSource;

/**
 *
 */
public class HLSAdapter extends AbstractSingleSource3ComponentAdapter<HLS> {
  public HLSAdapter(PixelLayout layout, DoubleSource data) {
    super(HLS.class, layout, false, data);
  }

  @Override
  protected void get(double c1, double c2, double c3, HLS result) {
    result.h(c1);
    result.l(c2);
    result.s(c3);
  }

  @Override
  protected void set(HLS value, long i1, long i2, long i3, DoubleSource data) {
    data.set(i1, value.h());
    data.set(i2, value.l());
    data.set(i3, value.s());
  }
}
