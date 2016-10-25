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
 *
 */
public interface FloatData extends NumericData<IntData> {
  float get(long index);

  void set(long index, float value);

  @Override
  default int getBitSize() {
    return Float.SIZE;
  }

  default double getValue(long index) {
    return get(index);
  }

  default void setValue(long index, double value) {
    set(index, (float) value);
  }

  @Override
  default IntData asBitData() {
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
