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
package com.lhkbob.imaje.data;

/**
 * DoubleData
 * ==========
 *
 * DoubleData is a specialization of NumericData that stores numeric values as Java `double`s. The
 * actual type used for bulk storage is unspecified and can be a `double[]`, `DoubleBuffer` or
 * something else. However, the key point is that float values are natively represented and do not
 * need to be further interpreted.
 *
 * This class provides a view of the double values by using {@link Double#doubleToLongBits(double)}
 * and its inverse operation, {@link Double#longBitsToDouble(long)}. It also adds direct getters
 * and setters for `double`s that avoid casting and widening to `double`.
 *
 * @author Michael Ludwig
 */
public abstract class DoubleData extends NumericData<LongData> {
  @Override
  public LongData asBitData() {
    return new LongData() {
      @Override
      public long get(long index) {
        return Double.doubleToLongBits(DoubleData.this.get(index));
      }

      @Override
      public long getLength() {
        return DoubleData.this.getLength();
      }

      @Override
      public boolean isBigEndian() {
        return DoubleData.this.isBigEndian();
      }

      @Override
      public boolean isGPUAccessible() {
        return DoubleData.this.isGPUAccessible();
      }

      @Override
      public void set(long index, long value) {
        DoubleData.this.set(index, Double.longBitsToDouble(value));
      }
    };
  }

  /**
   * Get the `double` stored at `index` in this data buffer. This returns the directly
   * represented floating-point value without any modifications.
   *
   * @param index
   *     The index to access
   * @return The value as a `double`
   *
   * @throws IndexOutOfBoundsException
   *     if `index` is out of bounds
   */
  public abstract double get(long index);

  @Override
  public final int getBitSize() {
    return Double.SIZE;
  }

  @Override
  public final double getValue(long index) {
    return get(index);
  }

  /**
   * Set the value at `index` in this buffer to the specified `double` value. This stores the
   * floating-point value as-is without any other type conversions.
   *
   * @param index
   *     The index to modify
   * @param value
   *     The new value
   * @throws IndexOutOfBoundsException
   *     if `index` is out of bounds
   */
  public abstract void set(long index, double value);

  @Override
  public final void setValue(long index, double value) {
    set(index, value);
  }
}
