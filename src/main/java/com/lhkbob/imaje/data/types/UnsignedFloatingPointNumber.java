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
 * UnsignedFloatingPointNumber
 * ===========================
 *
 * UnsignedFloatingPointNumber is a flexible floating-point binary representation that has an
 * exponent and mantissa (like conventional floating point numbers), but no sign bit. This is
 * achieved by wrapping a {@link SignedFloatingPointNumber} and masking off its sign bit. However,
 * this does mean that it can only represent up to 63-bit floats.
 *
 * This representation can store values from 0 to positive infinity.
 *
 * @author Michael Ludwig
 */
public class UnsignedFloatingPointNumber implements BinaryRepresentation {
  private final SignedFloatingPointNumber base;
  private final long unsignedMask;

  /**
   * Create a new UnsignedFloatingPointNumber representation that has the given `exponentBits` and
   * `mantissaBits` count configuration. This will use a lookup table if the total number of bits
   * of the representation is less than or equal to 16.
   *
   * @param exponentBits
   *     The number of exponent bits to allocate
   * @param mantissaBits
   *     The number of mantissa bits to allocate
   * @throws UnsupportedOperationException
   *     if `exponentBits` is greater than or equal to 11, or if `mantissaBits` is greater than or
   *     equal to 52
   * @throws IllegalArgumentException
   *     if `exponentBits` or `mantissaBits` is not positive
   */
  public UnsignedFloatingPointNumber(int exponentBits, int mantissaBits) {
    base = new SignedFloatingPointNumber(exponentBits, mantissaBits);
    unsignedMask = Functions.maskLong(exponentBits + mantissaBits);
  }

  /**
   * Create a new UnsignedFloatingPointNumber representation that has the given `exponentBits` and
   * `mantissaBits` count configuration. If `useLUT` this will calculate lookup tables for
   * the exponent conversion and binary to `double` conversion processes.
   *
   * @param exponentBits
   *     The number of exponent bits to allocate
   * @param mantissaBits
   *     The number of mantissa bits to allocate
   * @param useLUT
   *     Whether or not to create exponent and to-double look-up-tables
   * @throws UnsupportedOperationException
   *     if `exponentBits` is greater than or equal to 11, or if `mantissaBits` is greater than or
   *     equal to 52
   * @throws IllegalArgumentException
   *     if `exponentBits` or `mantissaBits` is not positive
   */
  public UnsignedFloatingPointNumber(int exponentBits, int mantissaBits, boolean useLUT) {
    base = new SignedFloatingPointNumber(exponentBits, mantissaBits, useLUT);
    unsignedMask = Functions.maskLong(exponentBits + mantissaBits);
  }

  /**
   * Create a new UnsignedFloatingPointNumber representation that has the given `exponentBits` and
   * `mantissaBits` count configuration. If `useExponentLUT` this will calculate lookup tables from
   * the standard `double` exponent to the custom exponent. If `useToDoubleLUT`, every single custom
   * binary value will be pre-computed and stored in a look-up-table to its corresponding `double`.
   * This makes bit to double conversions constant time but at the cost of increased data storage.
   *
   * @param exponentBits
   *     The number of exponent bits to allocate
   * @param mantissaBits
   *     The number of mantissa bits to allocate
   * @param useExponentLUT
   *     True if the exponent conversion table (double -> bit) should be created
   * @param useToDoubleLUT
   *     True if the bit -> double look-up-table should be created
   * @throws UnsupportedOperationException
   *     if `exponentBits` is greater than or equal to 11, or if `mantissaBits` is greater than or
   *     equal to 52
   * @throws IllegalArgumentException
   *     if `exponentBits` or `mantissaBits` is not positive
   */
  public UnsignedFloatingPointNumber(
      int exponentBits, int mantissaBits, boolean useExponentLUT, boolean useToDoubleLUT) {
    base = new SignedFloatingPointNumber(
        exponentBits, mantissaBits, useExponentLUT, useToDoubleLUT);
    unsignedMask = Functions.maskLong(exponentBits + mantissaBits);
  }

  @Override
  public int getBitSize() {
    // Remove the sign bit from the reported count
    return base.getBitSize() - 1;
  }

  @Override
  public double getMaxValue() {
    return Double.POSITIVE_INFINITY;
  }

  @Override
  public double getMinValue() {
    return 0.0;
  }

  @Override
  public boolean isFloatingPoint() {
    return true;
  }

  @Override
  public boolean isUnsigned() {
    return true;
  }

  @Override
  public long toBits(double value) {
    // First wrap the double value to be positive or zero; then any conversion to the signed
    // floating point format with the extra sign bit is equivalent to the unsigned format by just
    // ignoring the sign bit
    value = Functions.clamp(value, 0.0, Double.POSITIVE_INFINITY);
    return unsignedMask & base.toBits(value);
  }

  @Override
  public double toNumericValue(long bits) {
    // Make sure to chop off any higher bits in case the signed float misinterprets it
    return base.toNumericValue(bits & unsignedMask);
  }

  @Override
  public int hashCode() {
    return base.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof UnsignedFloatingPointNumber)) {
      return false;
    }
    return Objects.equals(((UnsignedFloatingPointNumber) o).base, base);
  }

  @Override
  public String toString() {
    return String.format("UFLOAT(%d, e: %d, m: %d)", getBitSize(), base.getExponentBits(),
        base.getMantissaBits());
  }
}
