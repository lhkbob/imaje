package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;

/**
 *
 */
public interface Pixel<T extends Color> {
  T getColor();

  double getAlpha();

  int getLayerIndex();

  int getMipmapLevel();

  default int getX() {
    return getCoordinate(0);
  }

  default int getY() {
    if (getDimensionality() > 1) {
      return getCoordinate(1);
    } else {
      return 0;
    }
  }

  default int getZ() {
    if (getDimensionality() > 2) {
      return getCoordinate(2);
    } else {
      return 0;
    }
  }

  int getDimensionality();

  int getCoordinate(int dim);

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
