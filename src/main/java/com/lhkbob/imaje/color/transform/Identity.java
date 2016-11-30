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
import com.lhkbob.imaje.util.Arguments;

/**
 *
 */
public class Identity<SA extends ColorSpace<A, SA>, A extends Color<A, SA>, SB extends ColorSpace<B, SB>, B extends Color<B, SB>> implements ColorTransform<SA, A, SB, B> {
  private final SA inSpace;
  private final SB outSpace;
  private final Identity<SB, B, SA, A> inverse;

  public Identity(SA inSpace, SB outSpace) {
    Arguments.equals("channel count", inSpace.getChannelCount(), outSpace.getChannelCount());
    this.inSpace = inSpace;
    this.outSpace = outSpace;
    inverse = new Identity<>(this);
  }

  private Identity(Identity<SB, B, SA, A> inverse) {
    inSpace = inverse.getOutputSpace();
    outSpace = inverse.getInputSpace();
    this.inverse = inverse;
  }

  @Override
  public Identity<SB, B, SA, A> inverse() {
    return inverse;
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

    // Copy input to output
    System.arraycopy(input, 0, output, 0, input.length);
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
    return i.inSpace.equals(inSpace) && i.outSpace.equals(outSpace);
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
