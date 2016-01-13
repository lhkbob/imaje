package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.data.DoubleSource;

import java.util.LinkedHashMap;

/**
 *
 */
public class RGBAdapter<T extends RGB> implements PixelAdapter<T> {
  private final Class<T> type;
  private final DoubleSource r;
  private final DoubleSource g;
  private final DoubleSource b;

  public RGBAdapter(Class<T> type, DoubleSource r, DoubleSource g, DoubleSource b) {
    this.type = type;
    this.r = r;
    this.g = g;
    this.b = b;
  }

  @Override
  public void get(long pixelIndex, T result) {
    result.r(r.get(pixelIndex));
    result.g(g.get(pixelIndex));
    result.b(b.get(pixelIndex));
  }

  @Override
  public void set(long pixelIndex, T value) {
    r.set(pixelIndex, value.r());
    g.set(pixelIndex, value.g());
    b.set(pixelIndex, value.b());
  }

  @Override
  public LinkedHashMap<String, DoubleSource> getChannels() {
    LinkedHashMap<String, DoubleSource> channels = new LinkedHashMap<>();
    channels.put(RGB.RED, r);
    channels.put(RGB.GREEN, g);
    channels.put(RGB.BLUE, b);
    return channels;
  }

  @Override
  public Class<T> getType() {
    return type;
  }
}
