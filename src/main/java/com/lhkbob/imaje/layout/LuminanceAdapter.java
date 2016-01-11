package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.Luminance;
import com.lhkbob.imaje.data.DoubleSource;

import java.util.LinkedHashMap;

/**
 *
 */
public class LuminanceAdapter implements PixelAdapter<Luminance> {
  private final DoubleSource l;

  public LuminanceAdapter(DoubleSource l) {
    this.l = l;
  }

  @Override
  public void get(long pixelIndex, Luminance result) {
    result.l(l.get(pixelIndex));
  }

  @Override
  public void set(long pixelIndex, Luminance value) {
    l.set(pixelIndex, value.l());
  }

  @Override
  public LinkedHashMap<String, DoubleSource> getChannels() {
    LinkedHashMap<String, DoubleSource> channels = new LinkedHashMap<>();
    channels.put(Luminance.LUMINANCE, l);
    return channels;
  }
}
