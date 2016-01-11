package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.Yyx;
import com.lhkbob.imaje.data.DoubleSource;

import java.util.LinkedHashMap;

/**
 *
 */
public class YyxAdapter implements PixelAdapter<Yyx> {
  private final DoubleSource l; // Y
  private final DoubleSource x; // x
  private final DoubleSource y; // y

  public YyxAdapter(DoubleSource l, DoubleSource x, DoubleSource y) {
    this.l = l;
    this.x = x;
    this.y = y;
  }

  @Override
  public void get(long pixelIndex, Yyx result) {
    result.lum(l.get(pixelIndex));
    result.x(x.get(pixelIndex));
    result.y(y.get(pixelIndex));
  }

  @Override
  public void set(long pixelIndex, Yyx value) {
    l.set(pixelIndex, value.lum());
    x.set(pixelIndex, value.x());
    y.set(pixelIndex, value.y());
  }

  @Override
  public LinkedHashMap<String, DoubleSource> getChannels() {
    LinkedHashMap<String, DoubleSource> channels = new LinkedHashMap<>();
    channels.put(Yyx.Y, l);
    channels.put(Yyx.X_CHROMATICITY, x);
    channels.put(Yyx.Y_CHROMATICITY, y);
    return channels;
  }
}
