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

import com.lhkbob.imaje.util.Arguments;

import java.util.Objects;
import java.util.Optional;

/*
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

/**
 * TransformedCurve
 * ================
 *
 * A curve that represents a linear transformation of another curve, `f`:
 *
 * ```
 * g(x) = a * f(b * x + c) + d
 * ```
 *
 * The domain of this function is the transformed domain of the function `f`.
 *
 * @author Michael Ludwig
 */
public final class TransformedCurve implements Curve {
  private final Curve f;
  private final double xOffset;
  private final double xScalar;
  private final double yOffset;
  private final double yScalar;

  /**
   * Create a new TransformedCurve based on the defined constants and function to wrap.
   *
   * @param f
   *     The function that is transformed
   * @param xScalar
   *     The scalar applied to `x` before evaluating `f`, e.g. `b` in the main example
   * @param xOffset
   *     The offset added to `x` before evaluating `f`, e.g. `c` in the main example
   * @param yScalar
   *     The scalar applied to the result of `f`, e.g. `a` in the main example
   * @param yOffset
   *     The constant added to the result of `f`, e.g. `d` in the main example
   */
  public TransformedCurve(Curve f, double xScalar, double xOffset, double yScalar, double yOffset) {
    Arguments.notNull("f", f);

    this.f = f;
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
    if (!(o instanceof TransformedCurve)) {
      return false;
    }

    TransformedCurve c = (TransformedCurve) o;
    return Objects.equals(c.f, f) && Double.compare(c.xScalar, xScalar) == 0
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
  public Optional<Curve> inverted() {
    // Make sure we're not dividing by values that trivialize this function
    if (Math.abs(yScalar) < EPS || Math.abs(xScalar) < EPS) {
      return Optional.empty();
    }

    Optional<Curve> invF = f.inverted();
    if (!invF.isPresent()) {
      return Optional.empty();
    }

    return Optional
        .of(new TransformedCurve(invF.get(), 1.0 / yScalar, -yOffset / yScalar, 1.0 / xScalar,
            -xOffset / xScalar));
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
