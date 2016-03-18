package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.data.DoubleSource;

/**
 *
 */
public class XYZAdapter extends AbstractSingleSource3ComponentAdapter<XYZ> {
  public XYZAdapter(PixelLayout layout, DoubleSource data) {
    super(XYZ.class, layout, false, data);
  }

  @Override
  protected void get(double c1, double c2, double c3, XYZ result) {
    result.x(c1);
    result.y(c2);
    result.z(c3);
  }

  @Override
  protected void set(XYZ value, long i1, long i2, long i3, DoubleSource data) {
    data.set(i1, value.x());
    data.set(i2, value.y());
    data.set(i3, value.z());
  }
}
