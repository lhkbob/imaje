package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.DepthStencil;
import com.lhkbob.imaje.data.DataSource;
import com.lhkbob.imaje.data.DoubleSource;
import com.lhkbob.imaje.data.IntSource;

import java.util.LinkedHashMap;

/**
 *
 */
public class DepthStencilAdapter<T extends DepthStencil> implements PixelAdapter<T> {
  private final DoubleSource d;
  private final IntSource s;
  private final Class<T> type;

  public DepthStencilAdapter(Class<T> type, DoubleSource d, IntSource s) {
    this.type = type;
    this.d = d;
    this.s = s;
  }

  @Override
  public void get(long pixelIndex, T result) {
    result.z(d.get(pixelIndex));
    result.stencil(s.get(pixelIndex));
  }

  @Override
  public LinkedHashMap<String, DataSource<?>> getChannels() {
    LinkedHashMap<String, DataSource<?>> channels = new LinkedHashMap<>();
    channels.put(DepthStencil.Z, d);
    channels.put(DepthStencil.STENCIL_MASK, s);
    return channels;
  }

  @Override
  public Class<T> getType() {
    return type;
  }

  @Override
  public void set(long pixelIndex, T value) {
    d.set(pixelIndex, value.z());
    s.set(pixelIndex, value.stencil());
  }
}
