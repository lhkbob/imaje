package com.lhkbob.imaje.color.transform.curves;

/**
 *
 */
public final class ExponentialFunction implements Curve {
  private final double base;
  private final double xOffset;
  private final double xScalar;
  private final double yOffset;
  private final double yScalar;

  public ExponentialFunction(
      double base, double xScalar, double xOffset, double yScalar, double yOffset) {
    this.base = base;
    this.xScalar = xScalar;
    this.xOffset = xOffset;
    this.yScalar = yScalar;
    this.yOffset = yOffset;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ExponentialFunction)) {
      return false;
    }

    ExponentialFunction c = (ExponentialFunction) o;
    return Double.compare(c.base, base) == 0 && Double.compare(c.xScalar, xScalar) == 0
        && Double.compare(c.xOffset, xOffset) == 0 && Double.compare(c.yScalar, yScalar) == 0
        && Double.compare(c.yOffset, yOffset) == 0;
  }

  @Override
  public double evaluate(double x) {
    return yScalar * Math.pow(base, xScalar * x + xOffset) + yOffset;
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
    result = 31 * result + Double.hashCode(base);
    result = 31 * result + Double.hashCode(xScalar);
    result = 31 * result + Double.hashCode(xOffset);
    result = 31 * result + Double.hashCode(yScalar);
    result = 31 * result + Double.hashCode(yOffset);
    return result;
  }

  @Override
  public Curve inverted() {
    // Make sure we're not dividing by values that trivialize this function
    if (Math.abs(yScalar) < EPS || Math.abs(xScalar) < EPS || base <= 0.0) {
      return null;
    }
    double invXScalar = 1.0 / yScalar;
    double invXOffset = -yOffset / yScalar;
    double invYScalar = 1.0 / (xScalar * Math.log10(base));
    double invYOffset = -xOffset / xScalar;

    return new LogGammaFunction(1.0, invXScalar, invXOffset, invYScalar, invYOffset);
  }

  @Override
  public String toString() {
    String power = "x";
    if (Double.compare(xScalar, 1.0) != 0) {
      power = String.format("%.3f * %s", xScalar, power);
    }
    if (Double.compare(xOffset, 0.0) != 0) {
      power = String.format("%s + %.3f", power, xOffset);
    }
    String func = String.format("%.3f^(%s)", base, power);
    if (Double.compare(yScalar, 1.0) != 0) {
      func = String.format("%.3f * %s", yScalar, func);
    }
    if (Double.compare(yOffset, 0.0) != 0) {
      func = String.format("%s + %.3f", func, yOffset);
    }
    return "y(x) = " + func;
  }

  private static final double EPS = 1e-8;
}
