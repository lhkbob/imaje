package com.lhkbob.imaje.color.icc.curves;

import java.util.Arrays;

/**
 *
 */
public class SampledCurve implements Curve {
  private final double[] xs;
  private final double[] ys;

  public SampledCurve(double[] xs, double[] ys) {
    if (xs.length != ys.length) {
      throw new IllegalArgumentException(
          "x and y arrays must be of the same length: " + xs.length + " vs. " + ys.length);
    }
    if (xs.length < 2) {
      throw new IllegalArgumentException("Must provide at least 2 samples");
    }

    // Confirm that the x axis is sorted
    for (int i = 1; i < xs.length; i++) {
      if (xs[i] >= xs[i + 1]) {
        throw new IllegalArgumentException("X axis values are not sorted");
      }
    }

    this.xs = Arrays.copyOf(xs, xs.length);
    this.ys = Arrays.copyOf(ys, ys.length);
  }

  @Override
  public boolean equals(Object o) {
    return o == this;
  }

  @Override
  public double evaluate(double x) {
    int idx = Arrays.binarySearch(xs, x);
    if (idx >= 0) {
      // Highly unlikely, but the input is exactly at a sample point
      return ys[idx];
    } else {
      // idx = -(insert - 1) -> insert = -idx + 1
      int insert = -idx + 1;
      // insert represents the first sample > x so we interpolate between
      // (insert - 1) and insert. But if insert == 0 or insert == sample count
      // then x is outside of the domain of the function.
      if (insert <= 0 || insert >= xs.length) {
        return Double.NaN;
      }

      // Linearly interpolate between insert - 1 and insert, normalizing
      // based on the difference between x-axis sample points
      double alpha = (x - xs[insert - 1]) / (xs[insert] - xs[insert - 1]);
      return alpha * ys[insert] + (1.0 - alpha) * ys[insert - 1];
    }
  }

  @Override
  public double getDomainMax() {
    return xs[xs.length - 1];
  }

  @Override
  public double getDomainMin() {
    return xs[0];
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
    return String.format("x in [%.3f, %.3f], y(x) = piecewise-linear function from %d samples",
        getDomainMin(), getDomainMax(), ys.length);
  }
}
