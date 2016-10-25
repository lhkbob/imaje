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

/**
 *
 */
public class LogGammaFunction implements Curve {
  private final double gamma;
  private final double xOffset;
  private final double xScalar;
  private final double yOffset;
  private final double yScalar;

  public LogGammaFunction(
      double gamma, double xScalar, double xOffset, double yScalar, double yOffset) {
    this.gamma = gamma;
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
    if (!(o instanceof LogGammaFunction)) {
      return false;
    }
    LogGammaFunction c = (LogGammaFunction) o;
    return Double.compare(c.gamma, gamma) == 0 && Double.compare(c.xScalar, xScalar) == 0
        && Double.compare(c.xOffset, xOffset) == 0 && Double.compare(c.yScalar, yScalar) == 0
        && Double.compare(c.yOffset, yOffset) == 0;
  }

  @Override
  public double evaluate(double x) {
    return yScalar * Math.log10(xScalar * Math.pow(x, gamma) + xOffset) + yOffset;
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
    result = 31 * result + Double.hashCode(xOffset);
    result = 31 * result + Double.hashCode(yScalar);
    result = 31 * result + Double.hashCode(yOffset);
    return result;
  }

  @Override
  public Curve inverted() {
    // Make sure we're not dividing by values that trivialize this function
    if (Math.abs(gamma) < EPS || Math.abs(xScalar) < EPS || Math.abs(yScalar) < EPS) {
      return null;
    }

    if (Double.compare(gamma, 1.0) == 0) {
      // Can be represented as just an exponential curve object
      return new ExponentialFunction(
          10.0, 1.0 / yScalar, -yOffset / yScalar, 1.0 / xScalar, -xOffset / xScalar);
    } else if (Double.compare(xOffset, 0.0) == 0) {
      // Can also be an exponential curve
      return new ExponentialFunction(10.0, 1.0 / (yScalar * gamma), -yOffset / (yScalar * gamma),
          1.0 / Math.pow(xScalar, 1.0 / gamma), 0.0);
    } else {
      // Must compose an exponential curve with a gamma curve
      Curve exp = new ExponentialFunction(
          10.0, 1.0 / yScalar, -yOffset / yScalar, 1.0 / xScalar, -xOffset / xScalar);
      Curve gam = new GammaFunction(1.0 / gamma, 1.0, 0.0, 0.0);
      return new ComposedCurve(gam, exp);
    }
  }

  @Override
  public String toString() {
    String base = String.format("x^%.3f", gamma);
    if (Double.compare(xScalar, 1.0) != 0) {
      base = String.format("%.3f * %s", xScalar, base);
    }
    if (Double.compare(xOffset, 0.0) != 0) {
      base = String.format("%s + %.3f", base, xOffset);
    }
    String func = "log_10(" + base + ")";
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
