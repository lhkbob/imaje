package com.lhkbob.imaje.color.icc.curves;

/**
 *
 */
public interface Curve {
  double evaluate(double x);

  double getDomainMax();

  double getDomainMin();

  Curve inverted();
}
