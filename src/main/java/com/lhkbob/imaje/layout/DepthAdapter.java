package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.Depth;
import com.lhkbob.imaje.data.DoubleSource;

import java.util.LinkedHashMap;

/**
 *
 */
public class DepthAdapter implements PixelAdapter<Depth> {
  private final DoubleSource d;

  public DepthAdapter(DoubleSource d) {
    this.d = d;
  }

  @Override
  public void get(long pixelIndex, Depth result) {
    result.z(d.get(pixelIndex));
  }

  @Override
  public void set(long pixelIndex, Depth value) {
    d.set(pixelIndex, value.z());
  }

  @Override
  public LinkedHashMap<String, DoubleSource> getChannels() {
    LinkedHashMap<String, DoubleSource> channels = new LinkedHashMap<>();
    channels.put(Depth.Z, d);
    return channels;
  }
}
