package com.lhkbob.imaje.color;

/**
 *
 */
public interface Color extends Cloneable {
  int getChannelCount();

  double get(int channel);

  void toArray(double[] array);

  void fromArray(double[] array);

  Color clone();
}
