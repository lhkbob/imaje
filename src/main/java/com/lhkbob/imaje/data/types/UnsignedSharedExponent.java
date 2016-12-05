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

import java.util.Arrays;

/**
 * UnsignedSharedExponent
 * ======================
 *
 * This is a special binary representation that compactly packs multiple numeric values into a
 * single bit field. Multiple mantissas rely on a single shared exponent to calculate the actual
 * numeric value for each. Unlike {@link SignedFloatingPointNumber} and {@link
 * UnsignedFloatingPointNumber}, these mantissas do not have an implicit leading bit.
 *
 * The shared exponent representation has an exponent mask and several mantissa masks. The exponent
 * is combined with each mantissa to form a vector of numeric values with dimensionality equal to
 * the number of mantissa masks provided (at least 2). All constructors and operations work with
 * `long` bit fields, however, there is no requirement that the exponent and mantissa masks fill out
 * all 64 bits. Lower bit-count shared exponent representations can be easily represented by using
 * masks that use a particular number of lower-significance bits.
 *
 * While this shares much of the same logic as the single-valued binary representations, and can be
 * used in many of the same overarching scenarios, it requires an entirely different interface
 * because it operates on a vector of numeric values.
 *
 * @author Michael Ludwig
 */
public class UnsignedSharedExponent {
  private final long exponentBias;
  private final long exponentMask;
  private final int exponentShift;
  private final int mantissaBits;
  private final long[] mantissaMasks;
  private final int[] mantissaShifts;
  private final double maxComponentValues;

  /**
   * Create a new UnsignedSharedExponent representation where the shared mask is stored in
   * `exponentMask` and `mantissaMasks` contains the mantissa mask for each vector component.
   * The mantissa masks are ordered in the array logically, corresponding to how values are
   * stored in the argument to {@link #toBits(double[])}.
   *
   * If `N` is the number of set bits in `exponentMask`, then the configured exponent bias
   * is equal to `2^(N-1)-1` and the maximum biased exponent is `2^N-1`.
   *
   * @param exponentMask
   *     The exponent mask that is shared
   * @param mantissaMasks
   *     The mantissa mask for each value in the packed field, ordered by logical index used to
   *     refer to the values
   * @throws IllegalArgumentException
   *     if the length of `mantissaMasks` is less than 2, or if the number of bits in each mantissa
   *     mask are not the same
   */
  public UnsignedSharedExponent(long exponentMask, long[] mantissaMasks) {
    this(exponentMask, mantissaMasks, (1 << (Long.bitCount(exponentMask) - 1)) - 1);
  }

  /**
   * Create a new UnsignedSharedExponent representation where the shared mask is stored in
   * `exponentMask` and `mantissaMasks` contains the mantissa mask for each vector component.
   * The mantissa masks are ordered in the array logically, corresponding to how values are
   * stored in the argument to {@link #toBits(double[])}.
   *
   * This constructor allows a custom exponent bias to be provided. If `N` is the number of set bits
   * in `exponentMask`, then the maximum biased exponent is `2^N-1`.
   *
   * @param exponentMask
   *     The exponent mask that is shared
   * @param mantissaMasks
   *     The mantissa mask for each value in the packed field, ordered by logical index used to
   *     refer to the values
   * @param exponentBias
   *     The custom exponent bias to subtract from the exponent before calculating the final real
   *     value
   * @throws IllegalArgumentException
   *     if the length of `mantissaMasks` is less than 2, or if the number of bits in each mantissa
   *     mask are not the same
   */
  public UnsignedSharedExponent(long exponentMask, long[] mantissaMasks, long exponentBias) {
    this(exponentMask, mantissaMasks, exponentBias, (1 << Long.bitCount(exponentMask)) - 1);
  }

  /**
   * Create a new UnsignedSharedExponent representation where the shared mask is stored in
   * `exponentMask` and `mantissaMasks` contains the mantissa mask for each vector component.
   * The mantissa masks are ordered in the array logically, corresponding to how values are
   * stored in the argument to {@link #toBits(double[])}.
   *
   * This constructor enables a custom exponent bias and max biased exponent. The max biased
   * exponent defines the maximum value that can be represented by this UnsignedSharedExponent.
   *
   * @param exponentMask
   *     The exponent mask that is shared
   * @param mantissaMasks
   *     The mantissa mask for each value in the packed field, ordered by logical index used to
   *     refer to the values
   * @param exponentBias
   *     The custom exponent bias to subtract from the exponent before calculating the final real
   *     value
   * @param maxBiasedExponent
   *     The maximum exponent (after being biased by `exponentBias`) that is valid in this
   *     representation, which determins the maximum value representable
   * @throws IllegalArgumentException
   *     if the length of `mantissaMasks` is less than 2, or if the number of bits in each mantissa
   *     mask are not the same
   */
  public UnsignedSharedExponent(
      long exponentMask, long[] mantissaMasks, long exponentBias, long maxBiasedExponent) {
    if (mantissaMasks.length <= 1) {
      throw new IllegalArgumentException("At least two mantissa fields must be provided");
    }

    int mBits = 0;
    mantissaShifts = new int[mantissaMasks.length];
    for (int i = 0; i < mantissaMasks.length; i++) {
      if (mBits == 0) {
        mBits = Long.bitCount(mantissaMasks[i]);
      } else if (mBits != Long.bitCount(mantissaMasks[i])) {
        throw new IllegalArgumentException("All mantissa's must be the same bit size");
      }

      mantissaShifts[i] = Long.numberOfTrailingZeros(mantissaMasks[i]);
    }

    mantissaBits = mBits;
    maxComponentValues =
        ((1L << mBits) - 1) / (double) (1L << mBits) * (1L << (maxBiasedExponent - exponentBias));
    exponentShift = Long.numberOfTrailingZeros(exponentMask);

    this.mantissaMasks = Arrays.copyOf(mantissaMasks, mantissaMasks.length);
    this.exponentBias = exponentBias;
    this.exponentMask = exponentMask;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + Long.hashCode(exponentMask);
    result = 31 * result + Long.hashCode(exponentBias);
    result = 31 * result + Double.hashCode(maxComponentValues);
    for (int i = 0; i < mantissaMasks.length; i++) {
      result = 31 * result + Long.hashCode(mantissaMasks[i]);
    }
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof UnsignedSharedExponent)) {
      return false;
    }
    UnsignedSharedExponent e = (UnsignedSharedExponent) o;
    return e.exponentMask == exponentMask && e.exponentBias == exponentBias
        && Math.abs(e.maxComponentValues - maxComponentValues) < 1e-8 && Arrays
        .equals(e.mantissaMasks, mantissaMasks);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("UnsignedSharedExponent(");
    sb.append(getBitCount()).append(", e: ");
    sb.append(Long.toHexString(exponentMask));
    for (int i = 0; i < mantissaMasks.length; i++) {
      sb.append(", m").append(i).append(": ");
      sb.append(Long.toHexString(mantissaMasks[i]));
    }
    sb.append(")");
    return sb.toString();
  }

  /**
   * @return The total bit count required by this representation
   */
  public int getBitCount() {
    return getExponentBitCount() + getValueCount() * getMantissaBitCount();
  }

  /**
   * Get a BinaryRepresentation that exposes a particular vector `component` as the numeric value of
   * the returned BinaryRepresentation. The bit field still represents the entire shared exponent
   * vector.
   *
   * Calling {@link BinaryRepresentation#toNumericValue(long)} is equivalent to calling
   * `toNumericValues(bits, result)` and then returning `result[component]`.
   *
   * Calling {@link BinaryRepresentation#toBits(double)} uses the given `double` value for every
   * component.
   *
   * @param component
   *     The component of the numeric vector linked to the returned BinaryRepresentation
   * @return BinaryRepresentation wrapper over a particular component of the vector
   */
  public BinaryRepresentation getComponentRepresentation(int component) {
    Arguments.checkIndex("component", getValueCount(), component);
    return new ComponentRepresentation(component);
  }

  /**
   * @return The bias subtracted from the initial unsigned exponent value when converting to numeric
   * values
   */
  public long getExponentBias() {
    return exponentBias;
  }

  /**
   * @return The number of bits in the exponent mask
   */
  public int getExponentBitCount() {
    return Long.bitCount(exponentMask);
  }

  /**
   * @return The bit mask for the exponent field
   */
  public long getExponentMask() {
    return exponentMask;
  }

  /**
   * @return The number of bits in each mantissa mask (not the total number of bits of all
   * mantissas)
   */
  public int getMantissaBitCount() {
    return mantissaBits;
  }

  /**
   * @param component
   *     The value component to access, must be between 0 and `getValueCount() - 1`
   * @return The mantissa mask for the given `component` of the vector this representation can
   * encode
   */
  public long getMantissaMask(int component) {
    return mantissaMasks[component];
  }

  /**
   * @return A bitmask that is the union of the exponent mask and all mantissa masks
   */
  public long getMask() {
    long mask = exponentMask;
    for (long mantissaMask : mantissaMasks) {
      mask |= mantissaMask;
    }
    return mask;
  }

  /**
   * @return The maximum component value storable in this representation, all real numbers will be
   * clamped to be less than this.
   */
  public double getMaxValue() {
    return maxComponentValues;
  }

  /**
   * @return The minimum value, which is 0, since this is an unsigned representation.
   */
  public double getMinValue() {
    return 0.0;
  }

  /**
   * @return The number of numeric values this format represents
   */
  public int getValueCount() {
    return mantissaMasks.length;
  }

  /**
   * Convert the vector of numeric values stored in the `values` array to a bit field. Bits that
   * are not used by the exponent or mantissa masks are set to 0. This conversion from `double`
   * to a much smaller bit representation is lossy but will attempt to preserve the value as best
   * as possible. Values outside of the minimum and maximum range of this representation will
   * be clamped.
   *
   * @param values
   *     The numeric values to convert
   * @return The bit field representation of `values`
   *
   * @throws IllegalArgumentException
   *     if `values`'s length is not equal to the value count of this representation
   */
  public long toBits(double[] values) {
    Arguments.equals("values.length", mantissaMasks.length, values.length);

    // Calculate largest component value
    double maxValue = 0.0;
    for (int i = 0; i < values.length; i++) {
      double v = Functions.clamp(values[i], 0.0, maxComponentValues);
      if (v > maxValue) {
        maxValue = v;
      }
    }

    // Preliminary shared exponent
    long prelimExponent =
        Math.max(-exponentBias - 1, (long) Math.floor(Functions.log2(maxValue))) + 1 + exponentBias;
    long maxBits = Math
        .round(maxValue / Math.pow(2.0, prelimExponent - exponentBias - mantissaBits));
    long exponent = (maxBits < (1L << mantissaBits) ? prelimExponent : prelimExponent + 1);

    // Initialize bit field with shared exponent bits
    long bitField = exponentMask & (exponent << exponentShift);

    double scale = Math.pow(2.0, exponent - exponentBias - mantissaBits);
    for (int i = 0; i < mantissaMasks.length; i++) {
      double v = Functions.clamp(values[i], 0.0, maxComponentValues);
      long valueBits = mantissaMasks[i] & (Math.round(v / scale) << mantissaShifts[i]);
      bitField |= valueBits;
    }

    return bitField;
  }

  /**
   * Convert the shared exponent values stored in `bits` to numeric values and store these
   * into the `result` array. `result`'s length must be equal to {@link #getValueCount()}.
   * Bits in `bits` that are not used by the exponent or mantissa masks are ignored.
   *
   * @param bits
   *     The bit field to convert to numeric values
   * @param result
   *     The storage for the output vector
   * @throws IllegalArgumentException
   *     if `result` length does not equal the value count of this shared exponent representation
   */
  public void toNumericValues(long bits, double[] result) {
    Arguments.equals("result.length", mantissaMasks.length, result.length);

    // Extract biased exponent from bit pattern
    long exponent = (bits & exponentMask) >> exponentShift;
    double scale = Math.pow(2.0, exponent - exponentBias - mantissaBits);
    for (int i = 0; i < mantissaMasks.length; i++) {
      // Extract and shift unnormalized mantissa from bit pattern
      long mantissa = (bits & mantissaMasks[i]) >>> mantissaShifts[i];
      // Scale by exponent with bias, and divide by 2^mantissaBits to convert from an unsigned int
      // into a double value between 0 and 1.
      result[i] = mantissa * scale;
    }
  }

  private class ComponentRepresentation implements BinaryRepresentation {
    private final int component;

    ComponentRepresentation(int component) {
      this.component = component;
    }

    @Override
    public int getBitSize() {
      return getBitCount();
    }

    @Override
    public double getMaxValue() {
      return UnsignedSharedExponent.this.getMaxValue();
    }

    @Override
    public double getMinValue() {
      return UnsignedSharedExponent.this.getMinValue();
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
      double[] vector = new double[getValueCount()];
      Arrays.fill(vector, value);
      return UnsignedSharedExponent.this.toBits(vector);
    }

    @Override
    public double toNumericValue(long bits) {
      double[] vector = new double[getValueCount()];
      toNumericValues(bits, vector);
      return vector[component];
    }
  }
}
