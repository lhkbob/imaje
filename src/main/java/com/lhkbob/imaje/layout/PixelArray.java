package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.data.DataBuffer;

/**
 *
 */
public interface PixelArray {
  PixelLayout getLayout();

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
