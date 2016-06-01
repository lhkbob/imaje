package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;

/**
 *
 */
public interface Pixel<T extends Color> {
  double get(T result);

  double getAlpha();

  int getLayer();

  int getLevel();

  int getX();

  int getY();

  void set(T value);

  void set(T value, double a);

  void setAlpha(double a);
}
