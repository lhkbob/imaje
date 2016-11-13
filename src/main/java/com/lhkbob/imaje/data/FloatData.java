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
 * FloatData
 * =========
 *
 * FloatData is a specialization of NumericData that stores numeric values as Java `float`s. The
 * actual type used for bulk storage is unspecified and can be a `float[]`, `FloatBuffer` or
 * something else. However, the key point is that float values are natively represented and do not
 * need to be further interpreted.
 *
 * This class provides a view of the float values by using {@link Float#floatToIntBits(float)}
 * and its inverse operation, {@link Float#intBitsToFloat(int)}. It also adds direct getters
 * and setters for `float`s that avoid casting and widening to `double`.
 *
 * @author Michael Ludwig
 */
public abstract class FloatData extends NumericData<IntData> {
  /**
   * Get the `float` stored at `index` in this data buffer. This returns the directly
   * represented floating-point value without any modifications.
   *
   * @param index
   *     The index to access
   * @return The value as a `float`
   *
   * @throws IndexOutOfBoundsException
   *     if `index` is out of bounds
   */
  public abstract float get(long index);

  /**
   * Set the value at `index` in this buffer to the specified `float` value. This stores the
   * floating-point value as-is without any other type conversions.
   *
   * @param index
   *     The index to modify
   * @param value
   *     The new value
   * @throws IndexOutOfBoundsException
   *     if `index` is out of bounds
   */
  public abstract void set(long index, float value);

  @Override
  public final int getBitSize() {
    return Float.SIZE;
  }

  @Override
  public final double getValue(long index) {
    return get(index);
  }

  @Override
  public final void setValue(long index, double value) {
    set(index, (float) value);
  }

  @Override
  public IntData asBitData() {
    return new IntData() {
      @Override
      public int get(long index) {
        return Float.floatToIntBits(FloatData.this.get(index));
      }

      @Override
      public void set(long index, int value) {
        FloatData.this.set(index, Float.intBitsToFloat(value));
      }

      @Override
      public long getLength() {
        return FloatData.this.getLength();
      }

      @Override
      public boolean isBigEndian() {
        return FloatData.this.isBigEndian();
      }

      @Override
      public boolean isGPUAccessible() {
        return FloatData.this.isGPUAccessible();
      }
    };
  }
}
