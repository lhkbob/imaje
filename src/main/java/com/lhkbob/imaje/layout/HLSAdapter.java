package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.HLS;
import com.lhkbob.imaje.data.DoubleSource;

import java.util.LinkedHashMap;

/**
 *
 */
public class HLSAdapter implements PixelAdapter<HLS> {
  private final DoubleSource h;
  private final DoubleSource l;
  private final DoubleSource s;

  public HLSAdapter(DoubleSource h, DoubleSource l, DoubleSource s) {
    this.h = h;
    this.l = l;
    this.s = s;
  }

  @Override
  public void get(long pixelIndex, HLS result) {
    result.h(h.get(pixelIndex));
    result.l(l.get(pixelIndex));
    result.s(s.get(pixelIndex));
  }

  @Override
  public void set(long pixelIndex, HLS value) {
    h.set(pixelIndex, value.h());
    l.set(pixelIndex, value.l());
    s.set(pixelIndex, value.s());
  }

  @Override
  public LinkedHashMap<String, DoubleSource> getChannels() {
    LinkedHashMap<String, DoubleSource> channels = new LinkedHashMap<>();
    channels.put(HLS.HUE, h);
    channels.put(HLS.LIGHTNESS, l);
    channels.put(HLS.SATURATION, s);
    return channels;
  }

  @Override
  public Class<HLS> getType() {
    return HLS.class;
  }
}
