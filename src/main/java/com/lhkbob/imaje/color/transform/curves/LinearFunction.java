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
 * LinearFunction
 * ==============
 *
 * A curve that defines a linear function based on several constants, of the form:
 *
 * ```
 * f(x) = a * x + b
 * ```
 *
 * The curve has no domain restrictions.
 *
 * @author Michael Ludwig
 */
public final class LinearFunction implements Curve {
  private final double offset;
  private final double slope;

  /**
   * Create a new LinearFunction with the given constant parameters.
   *
   * @param slope
   *     The slope of the function, e.g. `a` in the main example
   * @param offset
   *     The constant added to the function, e.g. `b` in the main example
   */
  public LinearFunction(double slope, double offset) {
    this.slope = slope;
    this.offset = offset;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof LinearFunction)) {
      return false;
    }
    LinearFunction c = (LinearFunction) o;
    return Double.compare(c.slope, slope) == 0 && Double.compare(c.offset, offset) == 0;
  }

  @Override
  public double evaluate(double x) {
    return slope * x + offset;
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
    result = 31 * result + Double.hashCode(slope);
    result = 31 * result + Double.hashCode(offset);
    return result;
  }

  @Override
  public Optional<Curve> inverse() {
    if (Math.abs(slope) < EPS) {
      return Optional.empty();
    }

    double invSlope = 1.0 / slope;
    double invOffset = -offset / slope;
    return Optional.of(new LinearFunction(invSlope, invOffset));
  }

  @Override
  public String toString() {
    String base = "x";
    if (Double.compare(slope, 1.0) != 0) {
      base = String.format("%.3f * %s", slope, base);
    }
    if (Double.compare(offset, 0.0) != 0) {
      base = String.format("%s + %.3f", base, offset);
    }

    return "y(x) = " + base;
  }

  private static final double EPS = 1e-8;
}
