package com.lhkbob.imaje.color.icc.curves;

/**
 *
 */
public final class GammaCurve implements Curve {
  private final double gamma;
  private final double xOffset;
  private final double xScalar;
  private final double yOffset;
  public GammaCurve(
      double gamma, double xScalar, double xOffset, double yOffset) {
    this.gamma = gamma;
    this.xScalar = xScalar;
    this.xOffset = xOffset;
    this.yOffset = yOffset;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof GammaCurve)) {
      return false;
    }
    GammaCurve c = (GammaCurve) o;
    return Double.compare(c.gamma, gamma) == 0 && Double.compare(c.xScalar, xScalar) == 0
        && Double.compare(c.xOffset, xOffset) == 0 && Double.compare(c.yOffset, yOffset) == 0;
  }

  @Override
  public double evaluate(double x) {
    return Math.pow(xScalar * x + xOffset, gamma) + yOffset;
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
    result = 31 * result + Double.hashCode(gamma);
    result = 31 * result + Double.hashCode(xScalar);
    result = 31 * result + Double.hashCode(yOffset);
    result = 31 * result + Double.hashCode(yOffset);
    return result;
  }

  @Override
  public Curve inverted() {
    // Make sure we're not dividing by values that trivialize this function
    if (Math.abs(gamma) < EPS || Math.abs(xScalar) < EPS) {
      return null;
    }

    double invG = 1.0 / gamma;
    double invPowerXScalar = 1.0 / Math.pow(xScalar, gamma);
    double invPowerXOffset = -invPowerXScalar * yOffset;
    double invPowerYOffset = -xOffset / xScalar;

    return new GammaCurve(invG, invPowerXScalar, invPowerXOffset, invPowerYOffset);
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
    String func = String.format("(%s)^%.3f", base, gamma);
    if (Double.compare(yOffset, 0.0) != 0) {
      func = String.format("%s + %.3f", func, yOffset);
    }

    return "y(x) = " + func;
  }
  private static final double EPS = 1e-8;
}
