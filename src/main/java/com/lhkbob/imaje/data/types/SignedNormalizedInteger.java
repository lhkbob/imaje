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
package com.lhkbob.imaje.data.types;

import com.lhkbob.imaje.util.Functions;

import java.util.Objects;

/**
 * SignedNormalizedInteger
 * =======================
 *
 * A fixed-point BinaryRepresentation that normalizes a signed integer to the real-value range `[-1,
 * 1]`. The normalization is based on the signed integer representation's maximum values. As an
 * example, an 8-bit signed integer maps -127 to -1, 127 to 1 (a -128 value is clamped to -1 as
 * well). This is done so that the representation is balanced around 0.
 *
 * @author Michael Ludwig
 */
public class SignedNormalizedInteger implements BinaryRepresentation {
  private final SignedInteger unnormalized;

  /**
   * Create a SignedNormalizedInteger with the given number of bits.
   *
   * @param bits
   *     The bit size of the representation
   * @throws IllegalArgumentException
   *     if `bits` is less than 1 or greater than 64
   */
  public SignedNormalizedInteger(int bits) {
    unnormalized = new SignedInteger(bits);
  }

  @Override
  public int getBitSize() {
    return unnormalized.getBitSize();
  }

  @Override
  public double getMaxValue() {
    return 1.0;
  }

  @Override
  public double getMinValue() {
    return -1.0;
  }

  @Override
  public boolean isFloatingPoint() {
    return false;
  }

  @Override
  public boolean isUnsigned() {
    return false;
  }

  @Override
  public long toBits(double value) {
    value = Functions.clamp(value, -1.0, 1.0);
    return unnormalized.toBits(value * unnormalized.getMaxValue());
  }

  @Override
  public double toNumericValue(long bits) {
    // Clamp the scaled values since the distribution about 0 is uneven between positive
    // and negative axis.
    return Functions
        .clamp(unnormalized.toNumericValue(bits) / unnormalized.getMaxValue(), -1.0, 1.0);
  }

  @Override
  public int hashCode() {
    return unnormalized.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SignedNormalizedInteger)) {
      return false;
    }
    return Objects.equals(((SignedNormalizedInteger) o).unnormalized, unnormalized);
  }

  @Override
  public String toString() {
    return String.format("SNORM(%d)", unnormalized.getBitSize());
  }
}
