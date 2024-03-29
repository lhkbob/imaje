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

import java.util.Objects;
import java.util.Optional;

/**
 * DomainWindow
 * ============
 *
 * A curve implementation that restricts another function to a specific domain range, but otherwise
 * does not modify the behavior of the function.
 *
 * @author Michael Ludwig
 */
public final class DomainWindow implements Curve {
  private final double domainMax;
  private final double domainMin;
  private final Curve f;

  /**
   * Create a new DomainWindow that wraps the curve `f` and restricts the domain to
   * be within `domainMin` and `domainMax`, which must be inside `f`'s own declared domain.
   *
   * @param f
   *     The curve function to wrap
   * @param domainMin
   *     The new domain lower boundary
   * @param domainMax
   *     The new domain upper boundary
   * @throws IllegalArgumentException
   *     if `domainMin` is less than `f`'s domain minimum or if `domainMax` is less than `f`'s
   *     domain maximum
   */
  public DomainWindow(Curve f, double domainMin, double domainMax) {
    if (domainMin < f.getDomainMin() || domainMax > f.getDomainMax()) {
      throw new IllegalArgumentException("Domain window does not restrict curve's domain");
    }

    this.f = f;
    this.domainMax = domainMax;
    this.domainMin = domainMin;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof DomainWindow)) {
      return false;
    }
    DomainWindow c = (DomainWindow) o;
    return Double.compare(c.domainMax, domainMax) == 0
        && Double.compare(c.domainMin, domainMin) == 0 && Objects.equals(c.f, f);
  }

  @Override
  public double evaluate(double x) {
    if (x < getDomainMin() || x > getDomainMax()) {
      return Double.NaN;
    }
    return f.evaluate(x);
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
    int result = 17;
    result = 31 * result + Double.hashCode(domainMax);
    result = 31 * result + Double.hashCode(domainMin);
    result = 31 * result + f.hashCode();
    return result;
  }

  @Override
  public Optional<Curve> inverse() {
    Optional<Curve> invF = f.inverse();
    if (!invF.isPresent()) {
      return Optional.empty();
    }

    // If invF is present, then the inverse's domain will be the evaluation of this window's domain
    double invDomainMin = f.evaluate(domainMin);
    double invDomainMax = f.evaluate(domainMax);

    return Optional.of(new DomainWindow(invF.get(), Math.min(invDomainMin, invDomainMax),
        Math.max(invDomainMin, invDomainMax)));
  }

  @Override
  public String toString() {
    return String.format("x in [%.3f, %.3f], f: %s", domainMin, domainMax, f);
  }
}
