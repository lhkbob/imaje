package com.lhkbob.imaje.color.icc.curves;

/**
 *
 */
public final class LinearCurve implements Curve {
  private final double offset;
  private final double slope;
  public LinearCurve(double slope, double offset) {
    this.slope = slope;
    this.offset = offset;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof LinearCurve)) {
      return false;
    }
    LinearCurve c = (LinearCurve) o;
    return Double.compare(c.slope, slope) == 0 && Double.compare(c.offset, offset) == 0;
  }

  @Override
  public double evaluate(double x) {
    return slope * x + offset;
  }

  @Override
  public double getDomainMax() {
    return Double.POSITIVE_INFINITY;
  }

  @Override
  public double getDomainMin() {
    return Double.NEGATIVE_INFINITY;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result += 31 * result + Double.hashCode(slope);
    result += 31 * result + Double.hashCode(offset);
    return result;
  }

  @Override
  public Curve inverted() {
    if (Math.abs(slope) < EPS) {
      return null;
    }

    double invSlope = 1.0 / slope;
    double invOffset = -offset / slope;
    return new LinearCurve(invSlope, invOffset);
  }

  @Override
  public String toString() {
    String base = "x";
    if (Double.compare(slope, 1.0) != 0) {
      base = String.format("%.3f * %s", slope, base);
    }
    if (Double.compare(offset, 0.0) != 0) {
      base = String.format("%s + %.3f", base, offset);
    }

    return "y(x) = " + base;
  }
  private static final double EPS = 1e-8;
}
