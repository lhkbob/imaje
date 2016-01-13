package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.HSV;
import com.lhkbob.imaje.data.DoubleSource;

import java.util.LinkedHashMap;

/**
 *
 */
public class HSVAdapter implements PixelAdapter<HSV> {
  private final DoubleSource h;
  private final DoubleSource s;
  private final DoubleSource v;

  public HSVAdapter(DoubleSource h, DoubleSource s, DoubleSource v) {
    this.h = h;
    this.s = s;
    this.v = v;
  }

  @Override
  public void get(long pixelIndex, HSV result) {
    result.h(h.get(pixelIndex));
    result.s(s.get(pixelIndex));
    result.v(v.get(pixelIndex));
  }

  @Override
  public LinkedHashMap<String, DoubleSource> getChannels() {
    LinkedHashMap<String, DoubleSource> channels = new LinkedHashMap<>();
    channels.put(HSV.HUE, h);
    channels.put(HSV.SATURATION, s);
    channels.put(HSV.VALUE, v);
    return channels;
  }

  @Override
  public Class<HSV> getType() {
    return HSV.class;
  }

  @Override
  public void set(long pixelIndex, HSV value) {
    h.set(pixelIndex, value.h());
    s.set(pixelIndex, value.s());
    v.set(pixelIndex, value.v());
  }
}
