package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;

/**
 *
 */
public interface Pixel<T extends Color> {
  int getX();

  int getY();

  int getLevel();

  int getLayer();

  void get(T result);

  void set(T value);

  double getAlpha();

  void setAlpha(double a);
}
