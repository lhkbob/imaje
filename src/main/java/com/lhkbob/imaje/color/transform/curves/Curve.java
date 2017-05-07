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
import java.util.function.DoubleUnaryOperator;

/**
 * Curve
 * =====
 *
 * Curve is an interface for a potentially-domain restricted one-dimensional function that provides
 * an inverse, if the function is invertible. Curve extends {@link DoubleUnaryOperator} so that it
 * can be easily used as a functional interface. It provides two methods for invoking the function:
 * {@link #evaluate(double)} and {@link #applyAsDouble(double)}. The former is domain aware and
 * returns `NaN` if the input is outside of the domain range. The latter is not domain aware in that
 * it returns `0.0` for all values outside of the domain range.
 *
 * Curve instances must be immutable, thread-safe, and provide reasonable implementations for
 * `hashCode()`, `equals()`, and `toString()`.
 *
 * @author Michael Ludwig
 */
public interface Curve extends DoubleUnaryOperator {
  /**
   * Evaluate the function described this instance for the given argument, `x`. If `x` is outside of
   * the domain range specified by {@link #getDomainMin()} and {@link #getDomainMax()} then {@link
   * Double#NaN} must be returned.
   *
   * @param x
   *     The input argument
   * @return The value of the function at `x`, or `NaN` if `x` was outside the domain.
   */
  double evaluate(double x);

  /**
   * @return The upper bound of the domain, inclusive, or {@link Double#POSITIVE_INFINITY} if
   * unbounded from above
   */
  double getDomainMax();

  /**
   * @return The lower bound of the domain, inclusive, or {@link Double#NEGATIVE_INFINITY} if
   * unbounded from below
   */
  double getDomainMin();

  /**
   * Get the inverse of this Curve as another Curve. If the function is invertible an appropriate
   * Curve should be calculated and returned. Often, analytic inverses can be calculated very
   * quickly for some types of curves. If the function is not invertible, or if it cannot be easily
   * calculated (such as with a sampled or data-driven curve), then an empty optional is returned.
   *
   * @return An Optional containing the inverse, or no result if the inverse could not be calculated
   */
  Optional<Curve> inverse();

  /**
   * Evaluates the function, via {@link #evaluate(double)} if `x` is within the domain range,
   * otherwise `0.0` is returned.
   *
   * @param x
   *     The input argument
   * @return The value of the function, or `0` if `x` was outside the domain.
   */
  @Override
  default double applyAsDouble(double x) {
    if (x < getDomainMin() || x > getDomainMax()) {
      return 0.0;
    } else {
      return evaluate(x);
    }
  }
}
