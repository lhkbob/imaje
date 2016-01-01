package com.lhkbob.imaje.color.icc.curves;

/**
 *
 */
public final class Constant implements Curve {
  private final double domainMax;
  private final double domainMin;
  private final double value;

  public Constant(double value) {
    this(value, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
  }

  public Constant(double value, double domainMin, double domainMax) {
    if (domainMax < domainMax) {
      throw new IllegalArgumentException("Domain max must be greater than or equal to domain min");
    }
    this.domainMin = domainMin;
    this.domainMax = domainMax;
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Constant)) {
      return false;
    }
    Constant c = (Constant) o;
    return Double.compare(c.domainMax, domainMax) == 0
        && Double.compare(c.domainMin, domainMin) == 0 && Double.compare(c.value, value) == 0;
  }

  @Override
  public double evaluate(double x) {
    if (x < getDomainMin() || x > getDomainMax()) {
      return Double.NaN;
    }

    return value;
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
    int result = 17;
    result = 31 * result + Double.hashCode(value);
    result = 31 * result + Double.hashCode(domainMin);
    result = 31 * result + Double.hashCode(domainMax);
    return result;
  }

  @Override
  public Curve inverted() {
    return null;
  }

  @Override
  public String toString() {
    return String.format("x in [%.3f, %.3f], y(x) = %.3f", domainMin, domainMax, value);
  }
}
