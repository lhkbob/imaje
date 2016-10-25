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
package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.data.BitData;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.types.BinaryRepresentation;
import com.lhkbob.imaje.data.types.SignedInteger;
import com.lhkbob.imaje.data.types.SignedNormalizedInteger;
import com.lhkbob.imaje.data.types.UnsignedInteger;
import com.lhkbob.imaje.data.types.UnsignedNormalizedInteger;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.Functions;

/**
 *
 */
public class PackedPixelArray implements PixelArray {
  private final DataLayout layout;
  private final PixelFormat format;
  private final BitData data;

  private final BinaryRepresentation[] fields;
  private final long[] fieldMasks;
  private final long[] fieldShifts;

  public static boolean isSupported(PixelFormat format) {
    // PixelFormat's structure already guarantees the channels will be packed, and any bits that
    // are ignored will be marked as SKIPPED. Thus, the only validation is that the total bit
    // size must equal one of Java's primitive types.
    int bits = format.getTotalBitSize();
    return bits == 8 || bits == 16 || bits == 32 || bits == 64;
  }

  public PackedPixelArray(PixelFormat format, DataLayout layout, BitData data) {
    Arguments.equals("layout.getChannelCount()", 1, layout.getChannelCount());
    Arguments.equals("bit size", format.getTotalBitSize(), data.getBitSize());
    Arguments.checkArrayRange("data length", data.getLength(), 0, layout.getRequiredDataElements());

    fields = new BinaryRepresentation[format.getDataChannelCount()];
    fieldMasks = new long[fields.length];
    fieldShifts = new long[fields.length];

    // Count from the back so we can track total shift from right to left
    int shift = 0;
    for (int i = fields.length - 1; i >= 0; i--) {
      fields[i] = getRepresentation(format.getDataChannelType(i), format.getDataChannelBitSize(i));
      fieldMasks[i] = Functions.maskLong(format.getDataChannelBitSize(i));
      fieldShifts[i] = shift;
      shift += format.getDataChannelBitSize(i);
    }

    this.layout = layout;
    this.format = format;
    this.data = data;
  }

  private static BinaryRepresentation getRepresentation(PixelFormat.Type type, int size) {
    switch(type) {
    case UINT: case USCALED:
      return new UnsignedInteger(size);
    case SINT:
    case SSCALED:
      return new SignedInteger(size);
    case UNORM:
      return new UnsignedNormalizedInteger(size);
    case SNORM:
      return new SignedNormalizedInteger(size);
    case SFLOAT:
      if (size == 64)
        return Data.SFLOAT64;
      else if (size == 32)
        return Data.SFLOAT32;
      else if (size == 16)
        return Data.SFLOAT16;
      else
        throw new UnsupportedOperationException("Unexpected bit size for SFLOAT, unsure of mantissa and exponent bit allocations for size: " + size);
    case UFLOAT:
      // FIXME eventually add support for the 10 and 11 bit UFLOATs
      throw new UnsupportedOperationException("UFLOAT channels are currently unsupported");
    default:
      throw new UnsupportedOperationException("Unknown type: " + type);
    }
  }

  @Override
  public DataLayout getLayout() {
    return layout;
  }

  @Override
  public BitData getData() {
    return data;
  }

  @Override
  public PixelFormat getFormat() {
    return format;
  }

  @Override
  public double get(int x, int y, double[] channelValues) {
    long index = layout.getChannelIndex(x, y, 0);
    return unpack(data.getBits(index), channelValues);
  }

  @Override
  public double get(int x, int y, double[] channelValues, long[] channels) {
    layout.getChannelIndices(x, y, channels);
    return unpack(data.getBits(channels[0]), channelValues);
  }

  @Override
  public double getAlpha(int x, int y) {
    if (format.hasAlphaChannel()) {
      long index = layout.getChannelIndex(x, y, 0);
      return bitFieldToDouble(data.getBits(index), format.getAlphaChannelDataIndex());
    } else {
      return 1.0;
    }
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a) {
    long index = layout.getChannelIndex(x, y, 0);
    data.setBits(index, pack(channelValues, a));
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a, long[] channels) {
    layout.getChannelIndices(x, y, channels);
    data.setBits(channels[0], pack(channelValues, a));
  }

  @Override
  public void setAlpha(int x, int y, double alpha) {
    if (format.hasAlphaChannel()) {
      long index = layout.getChannelIndex(x, y, 0);
      long bits = data.getBits(index);

      // Zero out original bit field
      int alphaDataChannelIndex = format.getAlphaChannelDataIndex();
      long alphaMask = fieldMasks[alphaDataChannelIndex] << fieldShifts[alphaDataChannelIndex];
      bits &= ~alphaMask;

      // Calculate new alpha bit field and or it into the remaining value, store into data source
      data.setBits(index, bits | doubleToBitField(alpha, alphaDataChannelIndex));
    } // else ignore set request
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  private long pack(double[] values, double a) {
    long bits = 0;
    for (int i = 0; i < format.getColorChannelCount(); i++) {
      int dataChannel = format.getColorChannelDataIndex(i);
      bits |= doubleToBitField(values[i], dataChannel);
    }
    if (format.hasAlphaChannel()) {
      bits |= doubleToBitField(a, format.getAlphaChannelDataIndex());
    }

    return bits;
  }

  private double unpack(long bits, double[] values) {
    for (int i = 0; i < format.getColorChannelCount(); i++) {
      int dataChannel = format.getColorChannelDataIndex(i);
      values[i] = bitFieldToDouble(bits, dataChannel);
    }

    if (format.hasAlphaChannel()) {
      return bitFieldToDouble(bits, format.getAlphaChannelDataIndex());
    } else {
      return 1.0;
    }
  }

  private double bitFieldToDouble(long bits, int field) {
    // First shift everything over so the LSB of the field is at bit 0
    bits >>= fieldShifts[field];
    // Next mask off everything to the right of the MSB of the field
    bits &= fieldMasks[field];

    return fields[field].toNumericValue(bits);
  }

  private long doubleToBitField(double value, int field) {
    long bits = fields[field].toBits(value);
    // Shift to the left so the bit field is aligned with the rest of the packed pixel
    // (Masking is not necessary assuming the binary representation contract is correct)
    return bits << fieldShifts[field];
  }
}
