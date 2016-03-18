package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.Yyx;
import com.lhkbob.imaje.data.DoubleSource;

/**
 *
 */
public class YyxAdapter extends AbstractSingleSource3ComponentAdapter<Yyx> {
  private boolean yyx; // if false, order is xyY

  private YyxAdapter(PixelLayout layout, boolean yyx, DoubleSource data) {
    super(Yyx.class, layout, false, data);
    this.yyx = yyx;
  }

  public static YyxAdapter newYyxAdapter(PixelLayout layout, DoubleSource data) {
    return new YyxAdapter(layout, true, data);
  }

  public static YyxAdapter newxyYAdapter(PixelLayout layout, DoubleSource data) {
    return new YyxAdapter(layout, false, data);
  }

  @Override
  protected void get(double c1, double c2, double c3, Yyx result) {
    if (yyx) {
      result.lum(c1);
      result.y(c2);
      result.x(c3);
    } else {
      result.lum(c3);
      result.y(c2);
      result.x(c1);
    }
  }

  @Override
  protected void set(Yyx value, long i1, long i2, long i3, DoubleSource data) {
    if (yyx) {
      data.set(i1, value.lum());
      data.set(i2, value.y());
      data.set(i3, value.x());
    } else {
      data.set(i1, value.x());
      data.set(i2, value.y());
      data.set(i3, value.lum());
    }
  }
}
