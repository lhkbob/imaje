package com.lhkbob.imaje.color.icc.curves;

/**
 *
 */
public final class DomainWindow implements Curve {
  private final double domainMax;
  private final double domainMin;
  private final Curve f;

  public DomainWindow(Curve f, double domainMin, double domainMax) {
    if (domainMin < f.getDomainMin() || domainMax > f.getDomainMax()) {
      throw new IllegalArgumentException("Domain window does not restrict curve's domain");
    }
    this.f = f;
    this.domainMax = domainMax;
    this.domainMin = domainMin;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof DomainWindow)) {
      return false;
    }
    DomainWindow c = (DomainWindow) o;
    return Double.compare(c.domainMax, domainMax) == 0 &&
        Double.compare(c.domainMin, domainMin) == 0 && c.f.equals(f);
  }

  @Override
  public double evaluate(double x) {
    if (x < getDomainMin() || x > getDomainMax()) {
      return Double.NaN;
    }
    return f.evaluate(x);
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
    result = 31 * result + Double.hashCode(domainMax);
    result = 31 * result + Double.hashCode(domainMin);
    result = 31 * result + f.hashCode();
    return result;
  }

  @Override
  public Curve inverted() {
    Curve invF = f.inverted();
    if (invF == null) {
      return null;
    }
    // If invF is not null, then the inverse's domain will be the evaluation of this window's domain
    double invDomainMin = f.evaluate(domainMin);
    double invDomainMax = f.evaluate(domainMax);

    return new DomainWindow(invF, Math.min(invDomainMin, invDomainMax),
        Math.max(invDomainMin, invDomainMax));
  }

  @Override
  public String toString() {
    return String.format("x in [%.3f, %.3f], f: %s", domainMin, domainMax, f);
  }
}
