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

import java.util.Arrays;
import java.util.Optional;

/**
 * UniformlySampledCurve
 * =====================
 *
 * A data-driven curve similar to {@link SampledCurve} except that it is assumed that the input
 * arguments are sampled uniformly between a specified domain range, making it unnecessary to store
 * or search through an array of inputs when evaluating the function. If requirements are met,
 * UniformlySampledCurve is a more efficient alternative to SampledCurve.
 *
 * @author Michael Ludwig
 */
public class UniformlySampledCurve implements Curve {
  private final double domainMax;
  private final double domainMin;
  private final double[] values;

  /**
   * Create a new UniformlySampledCurve on the given domain range `[domainMin, domainMax]`.
   * `values` is an array of output values for the function that will be linearly interpolated
   * over the domain. `value[0]` corresponds to the function value at `x = domainMin` and
   * `value[value.length - 1]` corresponds to the function value at `x = domainMax`.
   *
   * The `values` array is cloned so the created curve is immutable.
   *
   * @param domainMin
   *     The lower bound of the domain, inclusive
   * @param domainMax
   *     The upper bound of the domain, inclusive
   * @param values
   *     The sampled function values
   * @throws IllegalArgumentException
   *     if `domainMin >= domainMax` or if `values.length < 2`
   */
  public UniformlySampledCurve(double domainMin, double domainMax, double[] values) {
    if (domainMin >= domainMax) {
      throw new IllegalArgumentException("Domain min must be less than max");
    }
    if (values.length < 2) {
      throw new IllegalArgumentException("Values array length must be at least 2");
    }
    this.domainMax = domainMax;
    this.domainMin = domainMin;
    this.values = Arrays.copyOf(values, values.length);

    double min = Double.POSITIVE_INFINITY;
    double max = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < values.length; i++) {
      min = Math.min(min, values[i]);
      max = Math.max(max, values[i]);
    }
  }

  @Override
  public boolean equals(Object o) {
    return this == o;
  }

  @Override
  public double evaluate(double x) {
    if (x < getDomainMin() || x > getDomainMax()) {
      return Double.NaN;
    }

    double normalized = (values.length - 1) * (x - domainMin) / (domainMax - domainMin);
    int idx = (int) Math.floor(normalized);
    if (idx == values.length - 1) {
      // If we're exactly at the end of the array, no need to interpolation
      return values[idx];
    } else {
      double alpha = normalized - idx;
      return alpha * values[idx + 1] + (1.0 - alpha) * values[idx];
    }
  }

  @Override
  public double getDomainMax() {
    return domainMax;
  }

  @Override
  public double getDomainMin() {
    return domainMin;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public Optional<Curve> inverted() {
    int monotonicity = SampledCurve.calculateStrictMonotonicity(values);
    if (monotonicity == 0) {
      // not invertible
      return Optional.empty();
    } else if (monotonicity > 0) {
      // positively monotonic, so generate a synthetic x axis array before swapping xs and ys
      // and returning a sampled curve (which will properly interpolate between the non-uniform
      // values distribution in this class)
      return Optional.of(new SampledCurve(values, generateXAxis(false), true));
    } else {
      // negatively monotonic, so generate x axis, reverse both arrays and then swap xs and ys
      double[] reversedXs = generateXAxis(true);
      double[] reversedYs = new double[values.length];
      for (int i = 0; i < values.length; i++) {
        reversedYs[values.length - i - 1] = values[i];
      }
      return Optional.of(new SampledCurve(reversedYs, reversedXs, true));
    }
  }

  @Override
  public String toString() {
    return String
        .format("x in [%.3f, %.3f], y(x) = piecewise-linear function from %d uniform samples",
            domainMin, domainMax, values.length);
  }

  private double[] generateXAxis(boolean reverse) {
    double[] xs = new double[values.length];
    for (int i = 0; i < xs.length; i++) {
      double a = (reverse ? xs.length - i - 1 : i) / (xs.length - 1.0);
      xs[i] = a * (domainMax - domainMin) + domainMin;
    }
    return xs;
  }
}
