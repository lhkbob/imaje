package com.lhkbob.imaje.color.transform.curves;

/**
 * xScalar = a
 * xOffset = x_o
 * yScalar = b
 * yOffset = y_o
 *
 * Then given an f, we define
 * g(x) = b * f(a * x + x_o) + y_o
 *
 * x_f_min = a * x_min + x_o -> x_min = (x_f_min - x_o) / a
 * x_f_max = (x_f_max - x_o) / a
 * y_min = b * f_min + y_o
 * y_max = b * f_max + y_o
 *
 * inverse of g is:
 * y = g(x) -> y = b * f(a * x + x_o) + y_o
 * (y - y_o) / b = f(a * x + x_o)
 * f^-1((y - y_o) / b) = a * x + x_o
 * f^-1((y - y_o) / b) - x_o = a * x
 * f^-1((y - y_o) / b) / a - x_o / a = x(y)
 */
public final class TransformedCurve implements Curve {
  private final Curve f;
  private final double xOffset;
  private final double xScalar;
  private final double yOffset;
  private final double yScalar;

  public TransformedCurve(Curve f, double xScalar, double xOffset, double yScalar, double yOffset) {
    this.f = f;
    this.xScalar = xScalar;
    this.xOffset = xOffset;
    this.yScalar = yScalar;
    this.yOffset = yOffset;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof TransformedCurve)) {
      return false;
    }

    TransformedCurve c = (TransformedCurve) o;
    return c.f.equals(f) && Double.compare(c.xScalar, xScalar) == 0
        && Double.compare(c.xOffset, xOffset) == 0 && Double.compare(c.yScalar, yScalar) == 0
        && Double.compare(c.yOffset, yOffset) == 0;
  }

  @Override
  public double evaluate(double x) {
    if (x < getDomainMin() || x > getDomainMax()) {
      return Double.NaN;
    }
    return yScalar * f.evaluate(xScalar * x + xOffset) + yOffset;
  }

  @Override
  public double getDomainMax() {
    return (f.getDomainMax() - xOffset) / xScalar;
  }

  @Override
  public double getDomainMin() {
    return (f.getDomainMin() - xOffset) / xScalar;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + f.hashCode();
    result = 31 * result + Double.hashCode(xScalar);
    result = 31 * result + Double.hashCode(xOffset);
    result = 31 * result + Double.hashCode(yScalar);
    result = 31 * result + Double.hashCode(yOffset);
    return result;
  }

  @Override
  public Curve inverted() {
    // Make sure we're not dividing by values that trivialize this function
    if (Math.abs(yScalar) < EPS || Math.abs(xScalar) < EPS) {
      return null;
    }

    return new TransformedCurve(
        f.inverted(), 1.0 / yScalar, -yOffset / yScalar, 1.0 / xScalar, -xOffset / xScalar);
  }

  @Override
  public String toString() {
    String base = "x";
    if (Double.compare(xScalar, 1.0) != 0) {
      base = String.format("%.3f * %s", xScalar, base);
    }
    if (Double.compare(xOffset, 0.0) != 0) {
      base = String.format("%s + %.3f", base, xOffset);
    }
    String func = "f(" + base + ")";
    if (Double.compare(yScalar, 1.0) != 0) {
      func = String.format("%.3f * %s", yScalar, func);
    }
    if (Double.compare(yOffset, 0.0) != 0) {
      func = String.format("%s + %.3f", func, yOffset);
    }

    return String
        .format("x in [%.3f, %.3f], y(x) = %s where\n f: %s", getDomainMin(), getDomainMax(), func,
            f);
  }

  private static final double EPS = 1e-8;
}
