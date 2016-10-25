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

import java.util.Arrays;

/**
 *
 */
public class SampledCurve implements Curve {
  private final double[] xs;
  private final double[] ys;

  public SampledCurve(double[] xs, double[] ys) {
    this(xs, ys, false);
  }

  SampledCurve(double[] xs, double[] ys, boolean owned) {
    Arguments.equals("array lengths", xs.length, ys.length);
    if (xs.length < 2) {
      throw new IllegalArgumentException("Must provide at least 2 samples");
    }

    if (owned) {
      // Trust internal code properly sorted the xs, no need for a defensive copy
      this.xs = xs;
      this.ys = ys;
    } else {
      // Confirm that the x axis is sorted
      for (int i = 1; i < xs.length; i++) {
        if (xs[i] >= xs[i + 1]) {
          throw new IllegalArgumentException("X axis values are not sorted");
        }
      }

      // defensive copy
      this.xs = Arrays.copyOf(xs, xs.length);
      this.ys = Arrays.copyOf(ys, ys.length);
    }
  }

  public static int calculateStrictMonotonicity(double[] ys) {
    if (ys.length < 2) {
      return 0;
    }
    boolean positive = ys[1] > ys[0];
    boolean negative = ys[1] < ys[0];

    // If the first segment is neither positive or negatively sloped, then it was
    // flat so it's not monotonic
    if (!positive && !negative) {
      return 0;
    }

    for (int i = 2; i < ys.length; i++) {
      if (positive && ys[i] <= ys[i - 1]) {
        // positive monotonicity hypothesis is incorrect
        return 0;
      } else if (negative && ys[i] >= ys[i - 1]) {
        // negative monotonicity hypothesis is incorrect
        return 0;
      }
    }

    return (positive ? 1 : -1);
  }

  @Override
  public boolean equals(Object o) {
    return o == this;
  }

  @Override
  public double evaluate(double x) {
    int idx = Arrays.binarySearch(xs, x);
    if (idx >= 0) {
      // Highly unlikely, but the input is exactly at a sample point
      return ys[idx];
    } else {
      // idx = -insert - 1 -> insert = -(idx + 1)
      int insert = -(idx + 1);
      // insert represents the first sample > x so we interpolate between
      // (insert - 1) and insert. But if insert == 0 or insert == sample count
      // then x is outside of the domain of the function.
      if (insert <= 0 || insert >= xs.length) {
        return Double.NaN;
      }

      // Linearly interpolate between insert - 1 and insert, normalizing
      // based on the difference between x-axis sample points
      double alpha = (x - xs[insert - 1]) / (xs[insert] - xs[insert - 1]);
      return alpha * ys[insert] + (1.0 - alpha) * ys[insert - 1];
    }
  }

  @Override
  public double getDomainMax() {
    return xs[xs.length - 1];
  }

  @Override
  public double getDomainMin() {
    return xs[0];
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public Curve inverted() {
    // Check if the y values are monotonically increasing or decreasing
    int monotonicity = calculateStrictMonotonicity(ys);
    if (monotonicity == 0) {
      // not invertible
      return null;
    } else if (monotonicity > 0) {
      // Positively monotonic, so we can simply swap xs and ys variables to invert
      return new SampledCurve(ys, xs, true);
    } else {
      // Negatively monotonic, so swap xs/ys and then reverse the xs/ys arrays to meet
      // requirement that the new xs array is positively ordered
      double[] reversedXs = new double[xs.length];
      double[] reversedYs = new double[ys.length];
      for (int i = 0; i < xs.length; i++) {
        reversedXs[xs.length - i - 1] = xs[i];
        reversedYs[ys.length - i - 1] = ys[i];
      }

      return new SampledCurve(reversedYs, reversedXs, true);
    }
  }

  @Override
  public String toString() {
    return String.format("x in [%.3f, %.3f], y(x) = piecewise-linear function from %d samples",
        getDomainMin(), getDomainMax(), ys.length);
  }
}
