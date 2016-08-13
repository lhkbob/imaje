package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.data.DataBuffer;

/**
 * FIXME add an isReadOnly method or maybe isCompressed to indicate that storing values can't
 * happen on a per-pixel basis
 */
public interface PixelArray {
  DataLayout getLayout();

  DataBuffer getData();

  PixelFormat getFormat();

  long getDataOffset();

  double get(int x, int y, double[] channelValues);

  double get(int x, int y, double[] channelValues, long[] channels);

  double getAlpha(int x, int y);

  void set(int x, int y, double[] channelValues, double a);

  void set(int x, int y, double[] channelValues, double a, long[] channels);

  void setAlpha(int x, int y, double alpha);
}
