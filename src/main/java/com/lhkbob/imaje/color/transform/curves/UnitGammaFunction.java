/*
 * BSD 3-Clause License - imaJe
 *
 * Copyright (c) 2016, Michael Ludwig
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.lhkbob.imaje.color.transform.curves;

import java.util.Optional;

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
 *
 * UnitGammaFunction
 * =================
 *
 * This curve is an optimized piecewise curve combining a linear function when `x` is near zero
 * and becomes a gamma curve beyond that. The domain is restricted to `[0, 1]` and is assumed
 * that the range of the function is within this unit range as well. This specialization is
 * defined because it models the majority of non-linear transformations applied in the many RGB
 * color space definitions. The exact form of this function is:
 *
 * ```
 * f(x) = x > d ? (a * x + b)^gamma + c
 * : e * x + f
 * ```
 *
 * @author Michael Ludwig
 */
public class UnitGammaFunction implements Curve {
  private final double gamma;
  private final double linearThreshold;
  private final double linearXScalar;
  private final double linearYOffset;
  private final double powerXOffset;
  private final double powerXScalar;
  private final double powerYOffset;

  /**
   * Create a new UnitGammaFunction with the given constant parameters.
   *
   * @param gamma
   *     The exponent of the polynomial term
   * @param powerXScalar
   *     The scale factor applied to `x` in polynomial, e.g. `a` in the main example
   * @param powerXOffset
   *     The offset added to `x` in polynomial, e.g. `b` in the main example
   * @param powerYOffset
   *     The constant added to the polynomial, e.g. `c` in the main example
   * @param linearXScalar
   *     The slope of the linear piece, e.g. `e` in the main example
   * @param linearYOffset
   *     The constant added to the linear piece, e.g. `f` in the main example
   * @param linearThreshold
   *     The threshold for determining linear or gamma function, e.g. `d` in the main example
   */
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

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
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
  public Optional<Curve> inverted() {
    // Make sure we're not dividing by values that trivialize this function
    if (Math.abs(gamma) < EPS || Math.abs(powerXScalar) < EPS) {
      return Optional.empty();
    }

    double invG = 1.0 / gamma;
    double invPowerXScalar = 1.0 / Math.pow(powerXScalar, gamma);
    double invPowerXOffset = -invPowerXScalar * powerYOffset;
    double invPowerYOffset = -powerXOffset / powerXScalar;

    if (Double.compare(linearThreshold, 0.0) == 0) {
      // No linear threshold to worry about
      return Optional
          .of(new UnitGammaFunction(invG, invPowerXScalar, invPowerXOffset, invPowerYOffset, 0.0,
              0.0, 0.0));
    } else {
      // Things are invertible if the linear portion has a positive slop and connects to the
      // gamma curve. If it doesn't connect, it's not continuous. If it's negative sloped, it could
      // technically still be invertible but it would require swapping the inequality direction.
      if (Math.abs(power(linearThreshold) - linear(linearThreshold)) >= CONNECTIVITY_EPS) {
        // Not continuous
        return Optional.empty();
      }
      if (linearXScalar <= 0.0) {
        // Not a positive slope
        return Optional.empty();
      }

      // Same inverse of the gamma curve as above, plus an inverted linear term
      // - the new threshold is the y coordinate of this function's linear threshold
      return Optional
          .of(new UnitGammaFunction(invG, invPowerXScalar, invPowerXOffset, invPowerYOffset,
              1.0 / linearXScalar, -linearYOffset / linearXScalar, linear(linearThreshold)));
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

  private static final double CONNECTIVITY_EPS = 1e-2; // Be more forgiving due to precision and decimal issues in specs.
  private static final double EPS = 1e-8;
}
