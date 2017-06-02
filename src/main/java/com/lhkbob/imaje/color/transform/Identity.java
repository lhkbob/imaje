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
import com.lhkbob.imaje.util.Arguments;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Identity
 * ========
 *
 * The Identity transform returns the input channel values without modification, although it does
 * allow changing the vector space for the output space. If this is done, this transformation acts
 * much like a type-casting operation.
 *
 * The identity transform has several modes of operation depending on the dimensionality of the
 * input and output spaces. If both spaces have the same dimensionality, then the input values are
 * passed through unmodified. If the input space is 1D, but the output space has more, then the
 * input value is used for every output channel value. If the output space is 1D, but the input
 * space has a higher dimensionality, then the output value is set to the average of the input
 * channels.
 *
 * @author Michael Ludwig
 */
public class Identity<A extends Vector<A, SA>, SA extends VectorSpace<A, SA>, B extends Vector<B, SB>, SB extends VectorSpace<B, SB>> implements Transform<A, SA, B, SB> {
  private final SA inSpace;
  private final SB outSpace;
  private final Identity<B, SB, A, SA> inverse;

  /**
   * Create a new Identity transform that goes between the given input and output vector spaces.
   * If both spaces have dimensionality above one, then they must have the same dimensionality.
   * If either space has a dimensionality of one, then the identity transformation behaves in
   * a modified form, as described above.
   *
   * @param inSpace The input space
   * @param outSpace The output space
   */
  public Identity(SA inSpace, SB outSpace) {
    if (inSpace.getChannelCount() != 1 && outSpace.getChannelCount() != 1) {
      Arguments.equals("channel count", inSpace.getChannelCount(), outSpace.getChannelCount());
    }
    // else if either space only has a single channel that will be duplicated or averaged

    this.inSpace = inSpace;
    this.outSpace = outSpace;
    inverse = new Identity<>(this);
  }

  private Identity(Identity<B, SB, A, SA> inverse) {
    inSpace = inverse.getOutputSpace();
    outSpace = inverse.getInputSpace();
    this.inverse = inverse;
  }

  @Override
  public Optional<Identity<B, SB, A, SA>> inverse() {
    return Optional.of(inverse);
  }

  @Override
  public SA getInputSpace() {
    return inSpace;
  }

  @Override
  public SB getOutputSpace() {
    return outSpace;
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    Arguments.equals("input.length", inSpace.getChannelCount(), input.length);
    Arguments.equals("output.length", outSpace.getChannelCount(), output.length);

    if (input.length == output.length) {
      // Copy input to output
      System.arraycopy(input, 0, output, 0, input.length);
    } else if (input.length == 1) {
      // Set every channel in output equal to input[0]
      Arrays.fill(output, input[0]);
    } else {
      // Set single output channel to average of input
      double v = 0;
      for (int i = 0; i < input.length; i++) {
        v += input[i];
      }
      output[0] = v / input.length;
    }
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Identity)) {
      return false;
    }
    Identity i = (Identity) o;
    return Objects.equals(i.inSpace, inSpace) && Objects.equals(i.outSpace, outSpace);
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + inSpace.hashCode();
    result = 31 * result + outSpace.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return String.format("Identity %s -> %s", inSpace, outSpace);
  }
}
