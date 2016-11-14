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

/**
 * Signed64FloatingPointNumber
 * ===========================
 *
 * A BinaryRepresentation implementation for a 64-bit signed floating point number. This is the
 * exact Java/IEEE representation, which has a sign bit, 11 exponent bits, and 52 mantissa bits.
 * This is a separate implementation than {@link SignedFloatingPointNumber} because the logic of
 * that class does not work correctly when at the limit of 64 bits.
 *
 * This class should rarely be used except when it is required to have a BinaryRepresentation for
 * `double`. Otherwise the DataBuffer implementations that natively support `double` should be used
 * instead.
 *
 * @author Michael Ludwig
 */
public class Signed64FloatingPointNumber implements BinaryRepresentation {
  @Override
  public int getBitSize() {
    return 64;
  }

  @Override
  public double getMaxValue() {
    return Double.POSITIVE_INFINITY;
  }

  @Override
  public double getMinValue() {
    return Double.NEGATIVE_INFINITY;
  }

  @Override
  public boolean isFloatingPoint() {
    return true;
  }

  @Override
  public boolean isUnsigned() {
    return false;
  }

  @Override
  public long toBits(double value) {
    return Double.doubleToRawLongBits(value);
  }

  @Override
  public double toNumericValue(long bits) {
    return Double.longBitsToDouble(bits);
  }

  @Override
  public int hashCode() {
    return Signed64FloatingPointNumber.class.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Signed64FloatingPointNumber;
  }

  @Override
  public String toString() {
    return "SFLOAT(64, e: 11, m: 52)";
  }
}
