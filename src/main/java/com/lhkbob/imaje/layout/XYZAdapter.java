package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.data.DoubleSource;

import java.util.LinkedHashMap;

/**
 *
 */
public class XYZAdapter implements PixelAdapter<XYZ> {
  private final DoubleSource x;
  private final DoubleSource y;
  private final DoubleSource z;

  public XYZAdapter(DoubleSource x, DoubleSource y, DoubleSource z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  @Override
  public void get(long pixelIndex, XYZ result) {
    result.x(x.get(pixelIndex));
    result.y(y.get(pixelIndex));
    result.z(z.get(pixelIndex));
  }

  @Override
  public void set(long pixelIndex, XYZ value) {
    x.set(pixelIndex, value.x());
    y.set(pixelIndex, value.y());
    z.set(pixelIndex, value.z());
  }

  @Override
  public LinkedHashMap<String, DoubleSource> getChannels() {
    LinkedHashMap<String, DoubleSource> channels = new LinkedHashMap<>();
    channels.put(XYZ.X, x);
    channels.put(XYZ.Y, y);
    channels.put(XYZ.Z, z);
    return channels;
  }
}
