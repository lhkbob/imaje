package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;

/**
 *
 */
public interface Pixel<T extends Color> {
  T getColor();

  double getAlpha();

  int getLayer();

  int getLevel();

  int getX();

  int getY();

  void setColor(T value);

  void setColor(T value, double a);

  void setAlpha(double a);

  void persist();

  void persist(double alpha);

  void refresh();

  // FIXME add some sort of API for moving a pixel's location over the surface of an image?
  // Or perhaps, we have a movablePixel? that adds this functionality to the API and the
  // image class can return a movablepixel for random access, but iterators are exposed as "movable"
}
