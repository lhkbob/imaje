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

import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.Functions;

/**
 * SignedInteger
 * =============
 *
 * A 2's complement signed integer BinaryRepresentation. This has the same semantics as Java's
 * `int`, `long` etc. but can have an arbitrary number of bits (between 1 and 64). Its most
 * significant bit is the sign bit. This is not a floating point representation. Non-integer values
 * are rounded to the nearest integer. Values outside of representable range of integers are
 * clamped. If it is `N` bits, it can represent values between `-2^(N-1)` and `2^(N-1)-1`. An `N`
 * value of 64 is equivalent to `long`, 32 is equivalent to `int`, 16 is `short`, and 8 is a `byte`,
 * although these particular values should only be used when native types cannot be used directly.
 *
 * @author Michael Ludwig
 */
public class SignedInteger implements BinaryRepresentation {
  private final int bits;
  private final double maxValue;
  private final double minValue;
  private final long negMask;
  private final long posMask;

  /**
   * Create a new SignedInteger with the given number of bits.
   *
   * @param bits
   *     The bit size of the representation
   * @throws IllegalArgumentException
   *     if `bits` is less than 1 or greater than 64
   */
  public SignedInteger(int bits) {
    Arguments.inRangeInclusive("bits", 1, 64, bits);

    this.bits = bits;
    maxValue = Math.pow(2.0, bits - 1) - 1.0;
    minValue = -Math.pow(2.0, bits - 1);
    // This forms a bit field of all 1s in the least signifcant positive 'bits' count bits.
    // It is done this way by using >>> on a full bitfield to work correctly when bits = 64
    posMask = Functions.maskLong(bits - 1);
    negMask = 1L << (bits - 1);
  }

  @Override
  public int hashCode() {
    return Integer.hashCode(bits);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof SignedInteger)) {
      return false;
    }
    return ((SignedInteger) o).bits == bits;
  }

  @Override
  public String toString() {
    return String.format("SINT(%d)", bits);
  }

  @Override
  public int getBitSize() {
    return bits;
  }

  @Override
  public double getMaxValue() {
    return maxValue;
  }

  @Override
  public double getMinValue() {
    return minValue;
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
    value = Functions.clamp(value, minValue, maxValue);
    // In both of the following cases, since value is between the minimum and maximum, there is no
    // need to apply the positive bit mask to the rounded long.
    if (value < 0.0) {
      // Round positive portion to a long and then OR in the sign bit
      value -= minValue;
      return negMask | Math.round(value);
    } else {
      // Just round the positive portion
      return Math.round(value);
    }
  }

  @Override
  public double toNumericValue(long bits) {
    // First lift the positive portion of the bits to a double, and since posMask will have at most
    // 63 bits in it, value will be correct
    double value = bits & posMask;
    if ((bits & negMask) != 0) {
      // Subtract off 2^(bits-1), which is equal to +minValue
      value += minValue;
    }
    return value;
  }
}
