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

/**
 * ComposedCurve
 * =============
 *
 * A curve that represents the functional composition of two curves, `g` and `f`: for a given
 * input `x` it returns `g(f(x))`.
 *
 * @author Michael Ludwig
 */
public class ComposedCurve implements Curve {
  private final Curve f;
  private final Curve g;

  /**
   * Create a new ComposedCurve based on the two functions, where `g` is the outer function invoked
   * with the result of `f`'s evaluation on an input `x`, e.g. this function represents `g(f(x))`.
   *
   * @param g
   *     The outer function of the composition
   * @param f
   *     The inner function of the composition
   */
  public ComposedCurve(Curve g, Curve f) {
    Arguments.notNull("g", g);
    Arguments.notNull("f", f);

    this.f = f;
    this.g = g;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }

    if (!(o instanceof ComposedCurve)) {
      return false;
    }
    ComposedCurve c = (ComposedCurve) o;
    return Objects.equals(c.f, f) && Objects.equals(c.g, g);
  }

  @Override
  public double evaluate(double x) {
    double fx = f.evaluate(x);
    if (Double.isNaN(fx)) {
      return Double.NaN;
    } else {
      return g.evaluate(fx);
    }
  }

  @Override
  public double getDomainMax() {
    return f.getDomainMax();
  }

  @Override
  public double getDomainMin() {
    return f.getDomainMin();
  }

  @Override
  public int hashCode() {
    return f.hashCode() ^ g.hashCode();
  }

  @Override
  public Optional<Curve> inverted() {
    // The inverse (if it exists) is equal to f^-1(g^-1)
    Optional<Curve> invF = f.inverted();
    Optional<Curve> invG = g.inverted();

    if (invF.isPresent() && invG.isPresent()) {
      return Optional.of(new ComposedCurve(invF.get(), invG.get()));
    } else {
      return Optional.empty();
    }
  }

  @Override
  public String toString() {
    return String.format("y(x) = g(f(x)), where\n  f: %s\n  g: %s", f, g);
  }
}
