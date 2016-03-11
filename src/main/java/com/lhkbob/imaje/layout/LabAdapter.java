package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.Lab;
import com.lhkbob.imaje.data.DoubleSource;

import java.util.LinkedHashMap;

/**
 *
 */
public class LabAdapter<T extends Lab> implements PixelAdapter<T> {
  private final DoubleSource a;
  private final DoubleSource b;
  private final DoubleSource l;
  private final Class<T> type;

  public LabAdapter(Class<T> type, DoubleSource l, DoubleSource a, DoubleSource b) {
    this.type = type;
    this.l = l;
    this.a = a;
    this.b = b;
  }

  @Override
  public void get(long pixelIndex, T result) {
    result.l(l.get(pixelIndex));
    result.a(a.get(pixelIndex));
    result.b(b.get(pixelIndex));
  }

  @Override
  public LinkedHashMap<String, DoubleSource> getChannels() {
    LinkedHashMap<String, DoubleSource> channels = new LinkedHashMap<>();
    channels.put(Lab.L, l);
    channels.put(Lab.A, a);
    channels.put(Lab.B, b);
    return channels;
  }

  @Override
  public Class<T> getType() {
    return type;
  }

  @Override
  public void set(long pixelIndex, T value) {
    l.set(pixelIndex, value.l());
    a.set(pixelIndex, value.a());
    b.set(pixelIndex, value.b());
  }
}