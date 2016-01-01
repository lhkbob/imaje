package com.lhkbob.imaje.color.icc.curves;

import java.util.Arrays;

/**
 *
 */
public class UniformlySampledCurve implements Curve {
  private final double domainMax;
  private final double domainMin;
  private final double[] values;

  public UniformlySampledCurve(double domainMin, double domainMax, double[] values) {
    if (domainMin > domainMax) {
      throw new IllegalArgumentException("Domain min must be less than or equal to max");
    }
    if (values.length < 2) {
      throw new IllegalArgumentException("Values array length must be at least 2");
    }
    this.domainMax = domainMax;
    this.domainMin = domainMin;
    this.values = Arrays.copyOf(values, values.length);

    double min = Double.POSITIVE_INFINITY;
    double max = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < values.length; i++) {
      min = Math.min(min, values[i]);
      max = Math.max(max, values[i]);
    }
  }

  @Override
  public boolean equals(Object o) {
    return this == o;
  }

  @Override
  public double evaluate(double x) {
    if (x < getDomainMin() || x > getDomainMax()) {
      return Double.NaN;
    }

    double normalized = (values.length - 1) * (x - domainMin) / (domainMax - domainMin);
    int idx = (int) Math.floor(normalized);
    if (idx == values.length - 1) {
      // If we're exactly at the end of the array, no need to interpolation
      return values[idx];
    } else {
      double alpha = normalized - idx * (values.length - 1);
      return alpha * values[idx + 1] + (1.0 - alpha) * values[idx];
    }
  }

  @Override
  public double getDomainMax() {
    return domainMax;
  }

  @Override
  public double getDomainMin() {
    return domainMin;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public Curve inverted() {
    throw new UnsupportedOperationException("NOT IMPLEMENTED");
  }

  @Override
  public String toString() {
    return String
        .format("x in [%.3f, %.3f], y(x) = piecewise-linear function from %d uniform samples",
            domainMin, domainMax, values.length);
  }
}
