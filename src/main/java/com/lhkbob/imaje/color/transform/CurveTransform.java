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

import com.lhkbob.imaje.color.Vector;
import com.lhkbob.imaje.color.VectorSpace;
import com.lhkbob.imaje.color.transform.curves.Curve;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.Functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * CurveTransform
 * ==============
 *
 * CurveTransform is a Transform implementation that creates a multidimensional function by using
 * a Curve for each dimension. Each dimension or channel can have its own separate Curve function,
 * or curves can be reused to transform each channel the same way.
 *
 * @author Michael Ludwig
 */
public class CurveTransform<I extends Vector<I, SI>, SI extends VectorSpace<I, SI>, O extends Vector<O, SO>, SO extends VectorSpace<O, SO>> implements Transform<I, SI, O, SO> {
  private final SI inputSpace;
  private final SO outputSpace;

  private final List<Curve> curves;
  private final CurveTransform<O, SO, I, SI> inverse;

  /**
   * Create a new CurveTransform that goes between `inputSpace` and `outputSpace` by using a
   * provided {@link Curve} for each channel, specified in `curves`. The channel count for the input
   * and output spaces must be the same length as `curves`. Each Curve in `curves` is the
   * transformation applies to that specific channel. If the curve for a given channel is `null`,
   * then it is assumed the identity transform is used for that channel.
   *
   * The `curves` list is copied so the created instance is properly immutable.
   *
   * @param inputSpace
   *     The input vector space of the transform
   * @param outputSpace
   *     The output vector space of the transform
   * @param curves
   *     The curve transformations for each channel
   * @throws IllegalArgumentException
   *     if the channel counts of the input and output spaces do not equal the length of the
   *     `curves` list
   */
  public CurveTransform(SI inputSpace, SO outputSpace, List<? extends Curve> curves) {
    Arguments.equals("inputSpace.getChannelCount()", curves.size(), inputSpace.getChannelCount());
    Arguments.equals("outputSpace.getChannelCount()", curves.size(), outputSpace.getChannelCount());
    this.inputSpace = inputSpace;
    this.outputSpace = outputSpace;
    this.curves = new ArrayList<>(curves);

    CurveTransform<O, SO, I, SI> inverse = null;
    List<Curve> inverseCurves = new ArrayList<>(curves.size());
    for (Curve c : curves) {
      if (c == null) {
        // Pass through on this channel
        inverseCurves.add(null);
      } else {
        Optional<Curve> invC = c.inverse();
        if (invC.isPresent()) {
          inverseCurves.add(invC.get());
        } else {
          // Can't calculate an inverse for each channel
          break;
        }
      }
    }

    if (inverseCurves.size() == curves.size()) {
      // All channels were inverted properly
      inverse = new CurveTransform<>(inverseCurves, this);
    }

    this.inverse = inverse;
  }

  private CurveTransform(List<Curve> curves, CurveTransform<O, SO, I, SI> inverse) {
    this.inverse = inverse;
    inputSpace = inverse.getOutputSpace();
    outputSpace = inverse.getInputSpace();

    this.curves = curves;
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
    return Objects.equals(c.inputSpace, inputSpace) && Objects.equals(c.outputSpace, outputSpace)
        && Objects.equals(c.curves, curves);
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + inputSpace.hashCode();
    result = 31 * result + outputSpace.hashCode();
    result = 31 * result + curves.hashCode();
    return result;
  }

  @Override
  public Optional<CurveTransform<O, SO, I, SI>> inverse() {
    return Optional.ofNullable(inverse);
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
