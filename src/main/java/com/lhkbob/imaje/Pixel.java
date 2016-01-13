package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;

/**
 *
 */
public interface Pixel<T extends Color> {
  void get(T result);

  double getAlpha();

  int getLayer();

  int getLevel();

  int getX();

  int getY();

  void set(T value);

  void setAlpha(double a);
}
