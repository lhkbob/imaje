package com.lhkbob.imaje.color.transform.curves;

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
      double alpha = normalized - idx;
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
    int monotonicity = SampledCurve.calculateStrictMonotonicity(values);
    if (monotonicity == 0) {
      // not invertible
      return null;
    } else if (monotonicity > 0) {
      // positively monotonic, so generate a synthetic x axis array before swapping xs and ys
      // and returning a sampled curve (which will properly interpolate between the non-uniform
      // values distribution in this class)
      return new SampledCurve(values, generateXAxis(false), true);
    } else {
      // negatively monotonic, so generate x axis, reverse both arrays and then swap xs and ys
      double[] reversedXs = generateXAxis(true);
      double[] reversedYs = new double[values.length];
      for (int i = 0; i < values.length; i++) {
        reversedYs[values.length - i - 1] = values[i];
      }
      return new SampledCurve(reversedYs, reversedXs, true);
    }
  }

  @Override
  public String toString() {
    return String
        .format("x in [%.3f, %.3f], y(x) = piecewise-linear function from %d uniform samples",
            domainMin, domainMax, values.length);
  }

  private double[] generateXAxis(boolean reverse) {
    double[] xs = new double[values.length];
    for (int i = 0; i < xs.length; i++) {
      double a = (reverse ? xs.length - i - 1 : i) / (xs.length - 1.0);
      xs[i] = a * (domainMax - domainMin) + domainMin;
    }
    return xs;
  }
}
