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
package com.lhkbob.imaje.color.transform.general;

import com.lhkbob.imaje.color.transform.curves.Curve;
import com.lhkbob.imaje.util.Arguments;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Curves implements Transform {
  private final List<Curve> curves;

  public Curves(List<? extends Curve> curves) {
    Arguments.notEmpty("curves", curves);
    this.curves = new ArrayList<>(curves);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Curves)) {
      return false;
    }
    return ((Curves) o).curves.equals(curves);
  }

  @Override
  public int getInputChannels() {
    return curves.size();
  }

  @Override
  public Curves getLocallySafeInstance() {
    // This is purely functional (curve list is constant, and a curve is meant to be a constant
    // thread-safe function) so the instance can be used by any thread
    return this;
  }

  @Override
  public int getOutputChannels() {
    return curves.size();
  }

  @Override
  public int hashCode() {
    return curves.hashCode();
  }

  @Override
  public Curves inverted() {
    List<Curve> inverted = new ArrayList<>();
    for (Curve c : curves) {
      Curve invC = c.inverted();
      if (invC == null) {
        return null;
      } else {
        inverted.add(invC);
      }
    }
    return new Curves(inverted);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Curve Transform (dim: ").append(curves.size())
        .append("):");
    for (int i = 0; i < curves.size(); i++) {
      sb.append("\n  channel ").append(i + 1).append(": ").append(curves.get(i));
    }
    return sb.toString();
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    for (int i = 0; i < input.length; i++) {
      Curve c = curves.get(i);
      double inDomain = Math.max(c.getDomainMin(), Math.min(input[i], c.getDomainMax()));
      output[i] = c.evaluate(inDomain);
    }
  }
}
