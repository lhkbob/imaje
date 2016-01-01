package com.lhkbob.imaje.color.icc.curves;

/**
 *
 */
public class ComposedCurve implements Curve {
  private final Curve f;
  private final Curve g;

  public ComposedCurve(Curve g, Curve f) {
    this.f = f;
    this.g = g;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ComposedCurve)) {
      return false;
    }
    ComposedCurve c = (ComposedCurve) o;
    return c.f.equals(f) && c.g.equals(g);
  }

  @Override
  public double evaluate(double x) {
    double fx = f.evaluate(x);
    if (Double.isNaN(fx)) {
      return Double.NaN;
    } else {
      return g.evaluate(fx);
    }
  }

  @Override
  public double getDomainMax() {
    return f.getDomainMax();
  }

  @Override
  public double getDomainMin() {
    return f.getDomainMin();
  }

  @Override
  public int hashCode() {
    return f.hashCode() ^ g.hashCode();
  }

  @Override
  public Curve inverted() {
    // The inverse (if it exists) is equal to f^-1(g^-1)
    Curve invF = f.inverted();
    Curve invG = g.inverted();
    if (invF == null || invG == null) {
      return null;
    }
    return new ComposedCurve(invF, invG);
  }

  @Override
  public String toString() {
    return String.format("y(x) = g(f(x)), where\n  f: %s\n  g: %s", f, g);
  }
}
