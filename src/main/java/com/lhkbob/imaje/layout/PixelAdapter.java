package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.Color;

/**
 *
 */
public interface PixelAdapter<T extends Color> extends Iterable<ImageCoordinate> {
  int getWidth();

  int getHeight();

  double get(int x, int y, T result);

  double get(int x, int y, T result, long[] channels);

  double getAlpha(int x, int y);

  Class<T> getType();

  void set(int x, int y, T value, double a);

  void set(int x, int y, T value, double a, long[] channels);

  void setAlpha(int x, int y, double a);

  GPUFormat getFormat();

  boolean isGPUCompatible();

  boolean hasAlphaChannel();

  long[] createCompatibleChannelArray();
}
