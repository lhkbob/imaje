package com.lhkbob.imaje.color.transform.curves;

/**
 * if no linear threshold (e.g. threshold == 0):
 * y = (a * x + b)^g + c
 * y - c = (a * x + b)^g
 * (y - c)^(1/g) = a * x + b
 * ((y - c)^(1/g) - b) / a = x
 * ((y - c) / a^g)^(1/g) - b / a = x
 *
 * if linear threshold, then the linear portion's inverse is:
 * (y - f) / e = x
 *
 * and the new threshold between the two is y(d)
 */
public class UnitGammaFunction implements Curve {
  private final double gamma;
  private final double linearThreshold;
  private final double linearXScalar;
  private final double linearYOffset;
  private final double powerXOffset;
  private final double powerXScalar;
  private final double powerYOffset;
  public UnitGammaFunction(
      double gamma, double powerXScalar, double powerXOffset, double powerYOffset,
      double linearXScalar, double linearYOffset, double linearThreshold) {
    this.gamma = gamma;
    this.powerXScalar = powerXScalar;
    this.powerXOffset = powerXOffset;
    this.powerYOffset = powerYOffset;
    this.linearXScalar = linearXScalar;
    this.linearYOffset = linearYOffset;
    this.linearThreshold = linearThreshold;
  }

  // FIXME fix naming of these to not rely on underscores for clarity
  public static UnitGammaFunction newCIE122_1996Curve(double gamma, double a, double b) {
    return new UnitGammaFunction(gamma, a, b, 0.0, 0.0, 0.0, -b / a);
  }

  public static UnitGammaFunction newIEC61966_2_1Curve(
      double gamma, double a, double b, double c, double d) {
    return new UnitGammaFunction(gamma, a, b, 0.0, c, 0.0, d);
  }

  public static UnitGammaFunction newIEC61966_3Curve(double gamma, double a, double b, double c) {
    return new UnitGammaFunction(gamma, a, b, c, 0.0, c, -b / a);
  }

  public static UnitGammaFunction newSimpleCurve(double gamma) {
    return new UnitGammaFunction(gamma, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof UnitGammaFunction)) {
      return false;
    }
    UnitGammaFunction c = (UnitGammaFunction) o;
    return Double.compare(c.gamma, gamma) == 0 && Double.compare(c.powerXScalar, powerXScalar) == 0
        && Double.compare(c.powerXOffset, powerXOffset) == 0
        && Double.compare(c.powerYOffset, powerYOffset) == 0
        && Double.compare(c.linearThreshold, linearThreshold) == 0
        && Double.compare(c.linearXScalar, linearXScalar) == 0
        && Double.compare(c.linearYOffset, linearYOffset) == 0;
  }

  @Override
  public double evaluate(double x) {
    if (x < getDomainMin() || x > getDomainMax()) {
      return Double.NaN;
    }

    if (x >= linearThreshold) {
      return power(x);
    } else {
      return linear(x);
    }
  }

  @Override
  public double getDomainMax() {
    return 1.0;
  }

  @Override
  public double getDomainMin() {
    return 0.0;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + Double.hashCode(gamma);
    result = 31 * result + Double.hashCode(powerXScalar);
    result = 31 * result + Double.hashCode(powerXOffset);
    result = 31 * result + Double.hashCode(powerYOffset);
    result = 31 * result + Double.hashCode(linearThreshold);
    result = 31 * result + Double.hashCode(linearXScalar);
    result = 31 * result + Double.hashCode(linearYOffset);
    return result;
  }

  @Override
  public Curve inverted() {
    // Make sure we're not dividing by values that trivialize this function
    if (Math.abs(gamma) < EPS || Math.abs(powerXScalar) < EPS) {
      return null;
    }

    double invG = 1.0 / gamma;
    double invPowerXScalar = 1.0 / Math.pow(powerXScalar, gamma);
    double invPowerXOffset = -invPowerXScalar * powerYOffset;
    double invPowerYOffset = -powerXOffset / powerXScalar;

    if (Double.compare(linearThreshold, 0.0) == 0) {
      // No linear threshold to worry about
      return new UnitGammaFunction(
          invG, invPowerXScalar, invPowerXOffset, invPowerYOffset, 0.0, 0.0, 0.0);
    } else {
      // Things are invertible if the linear portion has a positive slop and connects to the
      // gamma curve. If it doesn't connect, it's not continuous. If it's negative sloped, it could
      // technically still be invertible but it would require swapping the inequality direction.
      if (Math.abs(power(linearThreshold) - linear(linearThreshold)) >= EPS) {
        // Not continuous
        return null;
      }
      if (linearXScalar <= 0.0) {
        // Not a positive slope
        return null;
      }

      // Same inverse of the gamma curve as above, plus an inverted linear term
      // - the new threshold is the y coordinate of this function's linear threshold
      return new UnitGammaFunction(invG, invPowerXScalar, invPowerXOffset, invPowerYOffset,
          1.0 / linearXScalar, -linearYOffset / linearXScalar, linear(linearThreshold));
    }
  }

  @Override
  public String toString() {
    if (linearThreshold > 0.0) {
      return String.format("x in [0.0, 1.0], y(x) = \n  for x >= %.3f: %s\n  for x < %.3f: %s",
          linearThreshold, powerToString(), linearThreshold, linearToString());
    } else {
      return String.format("x in [0.0, 1.0], y(x) = %s", powerToString());
    }
  }

  private double linear(double x) {
    return linearXScalar * x + linearYOffset;
  }

  private String linearToString() {
    if (Double.compare(linearXScalar, 0.0) == 0) {
      return String.format("%.3f", linearYOffset);
    } else if (Double.compare(linearYOffset, 0.0) == 0) {
      return String.format("%.3f * x", linearXScalar);
    } else {
      return String.format("%.3f * x + %.3f", linearXScalar, linearYOffset);
    }
  }

  private double power(double x) {
    return Math.pow(powerXScalar * x + powerXOffset, gamma) + powerYOffset;
  }

  private String powerToString() {
    String base = "x";
    if (Double.compare(powerXScalar, 1.0) != 0) {
      base = String.format("%.3f * %s", powerXScalar, base);
    }
    if (Double.compare(powerXOffset, 0.0) != 0) {
      base = String.format("%s + %.3f", base, powerXOffset);
    }
    String power = String.format("(%s)^%.3f", base, gamma);
    if (Double.compare(powerYOffset, 0.0) != 0) {
      power = String.format("%s + %.3f", power, powerYOffset);
    }
    return power;
  }
  private static final double EPS = 1e-8;
}
