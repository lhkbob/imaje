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

import com.lhkbob.imaje.data.BitData;
import com.lhkbob.imaje.data.DataBuffer;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.NumericData;
import com.lhkbob.imaje.util.Arguments;

import java.util.Objects;

/**
 * CustomBinaryData
 * ================
 *
 * A NumericData implementation that wraps a BitData buffer and combines those bit fields with a
 * particular {@link BinaryRepresentation} that handles the actual conversion logic to-and-from
 * numeric values.
 *
 * @author Michael Ludwig
 */
public class CustomBinaryData<T extends BitData> extends NumericData<T> implements DataView<T> {
  private final BinaryRepresentation converter;
  private final T source;

  /**
   * Create a new CustomBinaryData that uses `bitRep` for its numeric value conversions, and
   * stores the bit data in `source`. Changes made directly to `source` are reflected in the state
   * of this DataBuffer, and modifications made via this buffer are reflected in `source`.
   *
   * @param bitRep
   *     The binary representation used to encode numeric values in `source`
   * @param source
   *     The bit data source to wrap
   * @throws IllegalArgumentException
   *     if the bit size of `bitRep` and `source` are not equal
   */
  public CustomBinaryData(BinaryRepresentation bitRep, T source) {
    Arguments.equals("bit size", bitRep.getBitSize(), source.getBitSize());
    this.source = source;
    converter = bitRep;
  }

  @Override
  public T asBitData() {
    return source;
  }

  /**
   * @return The binary representation used for value conversion with this data bufferf
   */
  public BinaryRepresentation getBinaryRepresentation() {
    return converter;
  }

  @Override
  public int getBitSize() {
    return source.getBitSize();
  }

  @Override
  public long getLength() {
    return source.getLength();
  }

  @Override
  public T getSource() {
    return source;
  }

  @Override
  public double getValue(long index) {
    long bits = source.getBits(index);
    return converter.toNumericValue(bits);
  }

  @Override
  public boolean isBigEndian() {
    return source.isBigEndian();
  }

  @Override
  public boolean isGPUAccessible() {
    return source.isGPUAccessible();
  }

  @Override
  public void set(long writeIndex, DataBuffer data, long readIndex, long length) {
    if (data instanceof CustomBinaryData) {
      CustomBinaryData<?> custom = (CustomBinaryData<?>) data;
      if (Objects.equals(custom.converter, converter)) {
        // These are compatible representations so instead of fallback back to the
        // element-by-element numeric conversion implementation, do a copy directly between this and
        // data's underlying bit sources.
        source.set(writeIndex, custom.source, readIndex, length);
        return;
      }
    }

    // Otherwise just fallback to the default implementation that breaks down the copying
    // based on more concrete data buffer implementations irrespective of this buffer's type.
    super.set(writeIndex, data, readIndex, length);
  }

  @Override
  public void setValue(long index, double value) {
    // This assumes the converter handles any clamping to the range of allowable values for the
    // representation
    long bits = converter.toBits(value);
    source.setBits(index, bits);
  }
}
