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
 * BinaryRepresentation
 * ====================
 *
 * A binary representation is an interface for mapping a bit format of fixed bit size (up to 64
 * bits) to a Java `double` so that it can be used in calculations effectively before being turned
 * back into the particular bit representation. The conversion to and from bits to `double` can be
 * lossy and should preserve as much value and correctness as possible. This is similar to the loss
 * that occurs when casting between a `double` and the other primitive types like `float` or `int`.
 *
 * Some binary representations have minimum and maximum values, in which case `double` values
 * outside of that range will be clamped to a valid value before being converted into a bit pattern.
 *
 * BinaryRepresentation implementations should be immutable and thread-safe.
 *
 * @author Michael Ludwig
 */
public interface BinaryRepresentation {
  /**
   * @return The number of bits required to represent the number
   */
  int getBitSize();

  /**
   * @return The maximum value representable by this instance
   */
  double getMaxValue();

  /**
   * @return The minimum value representable by this instance
   */
  double getMinValue();

  /**
   * Get whether or not this representation is a floating point representation that has an exponent
   * and mantissa instead of a fixed point representation that uses a constant scale for
   * normalization. Floating point representations have different decimal resolutions depending on
   * the magnitude of the value being represented.
   *
   * @return True if the representation is floating point
   */
  boolean isFloatingPoint();

  /**
   * @return Whether or not the representation only stores unsigned values, i.e. {@link
   * #getMinValue()} returns 0
   */
  boolean isUnsigned();

  /**
   * Convert the given real number to the closest representable value as its bit pattern. The
   * returned bit field will have representation-dependent bits between `0` and `getBitSize() - 1`,
   * and all higher bits will be set to 0. If `value` is outside the range defined by {@link
   * #getMaxValue()} and {@link #getMinValue()} then the value is clamped to that range before being
   * converted into a bit field.
   *
   * @param value
   *     The number to convert to bit representation
   * @return The binary representation closest to value
   */
  long toBits(double value);

  /**
   * Convert the given bit field to a Java `double` based on the binary representation this instance
   * describes. The bits from `0` to `getBitSize() - 1` are used, higher bits are ignored.
   *
   * @param bits
   *     The bit field to convert to a real number
   * @return The real number closes to the value represented by `bits` in this representation
   */
  double toNumericValue(long bits);
}
