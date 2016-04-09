package com.lhkbob.imaje.color;

/**
 * Document that all Color implementations must provide a public default constructor.
 */
public interface Color extends Cloneable {
  Color clone();

  void fromArray(double[] array, int offset);

  double get(int channel);

  int getChannelCount();

  void toArray(double[] array, int offset);

  default void set(double... values) {
    fromArray(values, 0);
  }
}
