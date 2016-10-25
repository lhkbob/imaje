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
 *
 */
public class UnsignedSharedExponent {
  private final long[] mantissaMasks;
  private final int[] mantissaShifts;
  private final int mantissaBits;

  private final long exponentMask;
  private final int exponentShift;

  private final long exponentBias;

  private final double maxComponentValues;

  public UnsignedSharedExponent(long exponentMask, long[] mantissaMasks) {
    this(exponentMask, mantissaMasks, (1 << (Long.bitCount(exponentMask) - 1)) - 1);
  }

  public UnsignedSharedExponent(long exponentMask, long[] mantissaMasks, long exponentBias) {
    this(exponentMask, mantissaMasks, exponentBias, (1 << Long.bitCount(exponentMask)) - 1);
  }

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

  public int getValueCount() {
    return mantissaMasks.length;
  }
}
