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

import java.util.Optional;

/**
 * Transform
 * =========
 *
 * Transform is a multidimensional vector function from one {@link VectorSpace} to another. When the
 * two vector spaces are {@link com.lhkbob.imaje.color.ColorSpace ColorSpaces}, then the function
 * represents a color transformation. Implementations should provide inverses when they exist, or if
 * there are reasonable approximations to the true inverse.
 *
 * @author Michael Ludwig
 */
public interface Transform<I extends Vector<I, SI>, SI extends VectorSpace<I, SI>, O extends Vector<O, SO>, SO extends VectorSpace<O, SO>> {
  /**
   * Get the transform that represents the inverse of this transform, if it exists. Unlike {@link
   * Curve#inverse()}, this inverse does not need to be a strictly mathematical inverse. This is
   * especially true when dealing with color spaces where some transformations are lossy, or involve
   * clamping of channel values. Others, such as transformations to and from spectral values,
   * technically are not invertible because multiple spectrums can have the same tristimulus value.
   * In these cases, the transform can choose a "best" option for its inverse.
   *
   * @return The inverse transform, or null if no inverse is available
   */
  Optional<? extends Transform<O, SO, I, SI>> inverse();

  /**
   * @return The input vector or color space
   */
  SI getInputSpace();

  /**
   * @return The output vector or color space
   */
  SO getOutputSpace();

  /**
   * Transform the `input` vector into the output space and store it in `output`. The length of
   * `input` must equal the channel count of the input space, and the length of `output` must equal
   * the channel count of the output space. It is assumed that the values in the input array
   * represent a vector in the input space used by the transformation.
   *
   * Besides using unchecked arrays for the input and output values, this behaves the same as the
   * type-checked {@link #apply(Vector, Vector)}. Thus, it should return `false` if the transformed
   * value is out of gamut, and `true` otherwise.
   *
   * The `input` and `output` arrays should not be the same instance.
   *
   * @param input
   *     The input values to transform
   * @param output
   *     The array to hold the transformed output
   * @return True if the transformed value is in gamut
   *
   * @throws NullPointerException
   *     if `input` or `output` are null
   * @throws IllegalArgumentException
   *     if the array lengths don't match the channel counts of the respective vector spaces
   */
  boolean applyUnchecked(double[] input, double[] output);

  /**
   * Transform the given `input` vector or color and store the output value into the provided
   * `output` instance. If the transformation of the input value is outside the gamut for the output
   * space, then `false` is returned. If possible the transformation should still provide the
   * out-of-gamut value in `output`. If in-gamut, or if the transformation does not have a limited
   * gamut, then `true` is returned.
   *
   * The `input` and `output` vectors should not be the same instance.
   *
   * @param input
   *     The input value to transform
   * @param output
   *     The instance to hold the output of the transformation
   * @return True if the transformed value is in gamut
   *
   * @throws NullPointerException
   *     if `input` or `output` are null
   */
  default boolean apply(I input, O output) {
    return applyUnchecked(input.getChannels(), output.getChannels());
  }

  /**
   * Transform the given `input` vector or color into the output space, based on the specifics of
   * the transformation. The input vector is not modified, and a new instance of `O` is returned
   * with the output value.
   *
   * @param input
   *     The value to transform
   * @return The transformed value, in a new instance
   */
  default O apply(I input) {
    O res = getOutputSpace().newValue();
    apply(input, res);
    return res;
  }
}
