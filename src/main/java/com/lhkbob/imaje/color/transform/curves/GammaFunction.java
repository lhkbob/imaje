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
 * GammaFunction
 * =============
 *
 * A curve that defines a polynomial function based on several constants, of the form:
 *
 * ```
 * f(x) = (a * x + b) ^ gamma + c
 * ```
 *
 * The curve has no domain restrictions.
 *
 * @author Michael Ludwig
 */
public final class GammaFunction implements Curve {
  private final double gamma;
  private final double xOffset;
  private final double xScalar;
  private final double yOffset;

  /**
   * Create a new GammaFunction with the given constants.
   *
   * @param gamma
   *     The exponent constant
   * @param xScalar
   *     The scale factor to `x` in the base, e.g. `a` in the main example
   * @param xOffset
   *     The offset to `x` in the base, e.g. `b` in the main example
   * @param yOffset
   *     The constant added to the polynomial, e.g. `c` in the main example
   */
  public GammaFunction(
      double gamma, double xScalar, double xOffset, double yOffset) {
    this.gamma = gamma;
    this.xScalar = xScalar;
    this.xOffset = xOffset;
    this.yOffset = yOffset;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof GammaFunction)) {
      return false;
    }
    GammaFunction c = (GammaFunction) o;
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
  public Optional<Curve> inverse() {
    // Make sure we're not dividing by values that trivialize this function
    if (Math.abs(gamma) < EPS || Math.abs(xScalar) < EPS) {
      return Optional.empty();
    }

    double invG = 1.0 / gamma;
    double invPowerXScalar = 1.0 / Math.pow(xScalar, gamma);
    double invPowerXOffset = -invPowerXScalar * yOffset;
    double invPowerYOffset = -xOffset / xScalar;

    return Optional.of(new GammaFunction(invG, invPowerXScalar, invPowerXOffset, invPowerYOffset));
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
