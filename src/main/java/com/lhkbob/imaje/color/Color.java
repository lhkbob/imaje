package com.lhkbob.imaje.color;

/**
 *
 */
public interface Color extends Cloneable {
  Color clone();

  void fromArray(double[] array);

  double get(int channel);

  int getChannelCount();

  void toArray(double[] array);
}
