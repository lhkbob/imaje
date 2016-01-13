package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.LMS;
import com.lhkbob.imaje.data.DoubleSource;

import java.util.LinkedHashMap;

/**
 *
 */
public class LMSAdapter implements PixelAdapter<LMS> {
  private final DoubleSource l;
  private final DoubleSource m;
  private final DoubleSource s;

  public LMSAdapter(DoubleSource l, DoubleSource m, DoubleSource s) {
    this.l = l;
    this.m = m;
    this.s = s;
  }

  @Override
  public void get(long pixelIndex, LMS result) {
    result.l(l.get(pixelIndex));
    result.m(m.get(pixelIndex));
    result.s(s.get(pixelIndex));
  }

  @Override
  public LinkedHashMap<String, DoubleSource> getChannels() {
    LinkedHashMap<String, DoubleSource> channels = new LinkedHashMap<>();
    channels.put(LMS.LONG, l);
    channels.put(LMS.MEDIUM, m);
    channels.put(LMS.SHORT, s);
    return channels;
  }

  @Override
  public Class<LMS> getType() {
    return LMS.class;
  }

  @Override
  public void set(long pixelIndex, LMS value) {
    l.set(pixelIndex, value.l());
    m.set(pixelIndex, value.m());
    s.set(pixelIndex, value.s());
  }
}
