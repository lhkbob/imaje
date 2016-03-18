package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.Color;

/**
 *
 */
public interface PixelAdapter<T extends Color> {
  double get(int x, int y, T result);

  double get(int x, int y, T result, long[] channels);

  Class<T> getType();

  void set(int x, int y, T value, double a);

  void set(int x, int y, T value, double a, long[] channels);

  GPUFormat getFormat();

  boolean isGPUCompatible();

  boolean hasAlphaChannel();

  long[] createCompatibleChannelArray();
}
