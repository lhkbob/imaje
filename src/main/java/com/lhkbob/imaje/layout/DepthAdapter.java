package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.Depth;
import com.lhkbob.imaje.data.DoubleSource;

import java.util.LinkedHashMap;

/**
 * FIXME this is valid for 32-bit linear depths, but for the normalized depths they
 * are frequently stored in 24-bit precision; perhaps that just demands a custom data source?
 */
public class DepthAdapter<T extends Depth> implements PixelAdapter<T> {
  private final Class<T> type;
  private final DoubleSource d;

  public DepthAdapter(Class<T> type, DoubleSource d) {
    this.type = type;
    this.d = d;
  }

  @Override
  public void get(long pixelIndex, T result) {
    result.z(d.get(pixelIndex));
  }

  @Override
  public void set(long pixelIndex, T value) {
    d.set(pixelIndex, value.z());
  }

  @Override
  public LinkedHashMap<String, DoubleSource> getChannels() {
    LinkedHashMap<String, DoubleSource> channels = new LinkedHashMap<>();
    channels.put(Depth.Z, d);
    return channels;
  }

  @Override
  public Class<T> getType() {
    return type;
  }
}
