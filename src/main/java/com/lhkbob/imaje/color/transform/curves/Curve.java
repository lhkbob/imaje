package com.lhkbob.imaje.color.transform.curves;

/**
 *
 */
public interface Curve {
  double evaluate(double x);

  double getDomainMax();

  double getDomainMin();

  Curve inverted();
}
