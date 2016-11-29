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
package com.lhkbob.imaje.color.transform;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.transform.curves.Curve;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.Functions;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CurveTransform<SI extends ColorSpace<I, SI>, I extends Color<I, SI>, SO extends ColorSpace<O, SO>, O extends Color<O, SO>> implements ColorTransform<SI, I, SO, O> {
  private final SI inputSpace;
  private final SO outputSpace;

  private final List<Curve> curves;
  private final CurveTransform<SO, O, SI, I> inverse;

  public CurveTransform(SI inputSpace, SO outputSpace, List<? extends Curve> curves) {
    Arguments.equals("inputSpace.getChannelCount()", curves.size(), inputSpace.getChannelCount());
    Arguments.equals("outputSpace.getChannelCount()", curves.size(), outputSpace.getChannelCount());
    this.inputSpace = inputSpace;
    this.outputSpace = outputSpace;
    this.curves = new ArrayList<>(curves);
    inverse = new CurveTransform<>(this);
  }

  private CurveTransform(CurveTransform<SO, O, SI, I> inverse) {
    this.inverse = inverse;
    inputSpace = inverse.getOutputSpace();
    outputSpace = inverse.getInputSpace();

    curves = new ArrayList<>(inverse.curves.size());
    for (Curve c : inverse.curves) {
      Curve invC = (c == null ? null : c.inverted());
      curves.add(invC);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof CurveTransform)) {
      return false;
    }

    CurveTransform<?, ?, ?, ?> c = (CurveTransform<?, ?, ?, ?>) o;
    return c.inputSpace.equals(inputSpace) && c.outputSpace.equals(outputSpace) && c.curves
        .equals(curves);
  }

  @Override
  public int hashCode() {
    int result = inputSpace.hashCode();
    result += result * 31 + outputSpace.hashCode();
    result += result * 31 + curves.hashCode();
    return result;
  }

  @Override
  public CurveTransform<SO, O, SI, I> inverse() {
    return inverse;
  }

  @Override
  public SI getInputSpace() {
    return inputSpace;
  }

  @Override
  public SO getOutputSpace() {
    return outputSpace;
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    Arguments.equals("input.length", inputSpace.getChannelCount(), input.length);
    Arguments.equals("output.length", outputSpace.getChannelCount(), output.length);

    for (int i = 0; i < input.length; i++) {
      Curve c = curves.get(i);
      if (c != null) {
        double inDomain = Functions.clamp(input[i], c.getDomainMin(), c.getDomainMax());
        output[i] = c.evaluate(inDomain);
      } else {
        output[i] = input[i];
      }
    }
    return true;
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
}
