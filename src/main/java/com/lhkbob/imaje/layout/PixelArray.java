package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.data.DataSource;

import java.util.function.Predicate;

/**
 *
 */
public interface PixelArray {
  PixelLayout getLayout();

  DataSource getData();

  PixelFormat getFormat();

  double get(int x, int y, double[] channelValues, int offset);

  double get(int x, int y, double[] channelValues, int offset, long[] channels);

  double getAlpha(int x, int y);

  void set(int x, int y, double[] channelValues, int offset, double a);

  void set(int x, int y, double[] channelValues, int offset, double a, long[] channels);

  void setAlpha(int x, int y, double alpha);

  Predicate<GPUFormat> getGPUFormatFilter();
}
