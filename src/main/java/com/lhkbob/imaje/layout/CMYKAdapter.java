package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.CMYK;
import com.lhkbob.imaje.data.DoubleSource;

import java.util.LinkedHashMap;

/**
 *
 */
public class CMYKAdapter implements PixelAdapter<CMYK> {
  private final DoubleSource c;
  private final DoubleSource m;
  private final DoubleSource y;
  private final DoubleSource k;

  public CMYKAdapter(DoubleSource c, DoubleSource m, DoubleSource y, DoubleSource k) {
    this.c = c;
    this.m = m;
    this.y = y;
    this.k = k;
  }

  @Override
  public void get(long pixelIndex, CMYK result) {
    result.c(c.get(pixelIndex));
    result.m(m.get(pixelIndex));
    result.y(y.get(pixelIndex));
    result.k(k.get(pixelIndex));
  }

  @Override
  public void set(long pixelIndex, CMYK value) {
    c.set(pixelIndex, value.c());
    m.set(pixelIndex, value.m());
    y.set(pixelIndex, value.y());
    k.set(pixelIndex, value.k());
  }

  @Override
  public LinkedHashMap<String, DoubleSource> getChannels() {
    LinkedHashMap<String, DoubleSource> channels = new LinkedHashMap<>();
    channels.put(CMYK.CYAN, c);
    channels.put(CMYK.MAGENTA, m);
    channels.put(CMYK.YELLOW, y);
    channels.put(CMYK.KEY, k);
    return channels;
  }

  @Override
  public Class<CMYK> getType() {
    return CMYK.class;
  }
}
