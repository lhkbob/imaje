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
 * PackedPixelArray
 * ================
 *
 * PackedPixelArray is a RootPixelArray implementation that represents a pixel's channel values as
 * being packed into a single primitive bit field for each pixel in the image. It uses a {@link
 * DataLayout} to describe how the pixels are mapped from two dimensions to one. Data is stored in a
 * {@link BitData} instance whose bit size matches the total bit size of the array's {@link
 * PixelFormat}.
 *
 * @author Michael Ludwig
 */
public class PackedPixelArray extends RootPixelArray {
  private final BitData data;
  private final long[] fieldMasks;
  private final long[] fieldShifts;
  private final BinaryRepresentation[] fields;
  private final PixelFormat format;
  private final DataLayout layout;

  /**
   * Create a new PackedPixelArray that assumes logical color channel data is described by
   * `format`, mapped from two dimensions to one by `layout`, and stored within `data`.
   *
   * The layout must have a band count of 1 since all data fields of the format are packed into
   * a single primitive element. The total bit size of the format must equal the bit size of `data`.
   * `data` must have a length equal to the required elements specified by `layout`. The format
   * cannot have any custom fields.
   *
   * @param format
   *     The pixel format for the array
   * @param layout
   *     The layout of the array
   * @param data
   *     The data source of the array
   * @throws IllegalArgumentException
   *     if the requirements described above are not met
   */
  public PackedPixelArray(PixelFormat format, DataLayout layout, BitData data) {
    Arguments.equals("layout.getBandCount()", 1, layout.getBandCount());
    Arguments.equals("bit size", format.getBitSize(), data.getBitSize());
    Arguments.equals("data length", layout.getRequiredDataElements(), data.getLength());
    Arguments.equals("custom channel count", 0, format.getCustomChannelCount());

    fields = new BinaryRepresentation[format.getDataFieldCount()];
    fieldMasks = new long[fields.length];
    fieldShifts = new long[fields.length];

    // Count from the back so we can track total shift from right to left
    int shift = 0;
    for (int i = fields.length - 1; i >= 0; i--) {
      fields[i] = getRepresentation(format.getDataFieldType(i), format.getDataFieldBitSize(i));
      fieldMasks[i] = Functions.maskLong(format.getDataFieldBitSize(i));
      fieldShifts[i] = shift;
      shift += format.getDataFieldBitSize(i);
    }

    this.layout = layout;
    this.format = format;
    this.data = data;
  }

  /**
   * Determine if the given pixel format can be used by PackedPixelArray instances. Because
   * PackedPixelArray also requires that any DataLayout has a band count of 1, the structure of a
   * PixelFormat guarantees that the data fields will be contiguous. The only caveat being that the
   * total number of bits required line up with the `byte`, `short`, `int`, or `long` sizes.
   * The format is not supported if it has custom channels.
   *
   * @param format
   *     The pixel format to check
   * @return True if could be used to represent a format packed into a single primitive
   */
  public static boolean isSupported(PixelFormat format) {
    if (format.getCustomChannelCount() != 0) {
      return false;
    }

    // PixelFormat's structure already guarantees the channels will be packed, and any bits that
    // are ignored will be marked as SKIPPED. Thus, the only validation is that the total bit
    // size must equal one of Java's primitive types.
    int bits = format.getBitSize();
    return bits == 8 || bits == 16 || bits == 32 || bits == 64;
  }

  @Override
  public double get(int x, int y, double[] channelValues) {
    long index = layout.getBandOffset(x, y, 0);
    return unpack(data.getBits(index), channelValues);
  }

  @Override
  public double get(int x, int y, double[] channelValues, long[] bandOffsets) {
    layout.getBandOffsets(x, y, bandOffsets);
    return unpack(data.getBits(bandOffsets[0]), channelValues);
  }

  @Override
  public double getAlpha(int x, int y) {
    if (format.hasAlphaChannel()) {
      long index = layout.getBandOffset(x, y, 0);
      return bitFieldToDouble(data.getBits(index), format.getAlphaChannelDataField());
    } else {
      return 1.0;
    }
  }

  @Override
  public BitData getData(int band) {
    Arguments.equals("band", 0, band);
    return data;
  }

  @Override
  public PixelFormat getFormat() {
    return format;
  }

  @Override
  public DataLayout getLayout() {
    return layout;
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a) {
    long index = layout.getBandOffset(x, y, 0);
    data.setBits(index, pack(channelValues, a));
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a, long[] bandOffsets) {
    layout.getBandOffsets(x, y, bandOffsets);
    data.setBits(bandOffsets[0], pack(channelValues, a));
  }

  @Override
  public void setAlpha(int x, int y, double alpha) {
    if (format.hasAlphaChannel()) {
      long index = layout.getBandOffset(x, y, 0);
      long bits = data.getBits(index);

      // Zero out original bit field
      int alphaDataFieldIndex = format.getAlphaChannelDataField();
      long alphaMask = fieldMasks[alphaDataFieldIndex] << fieldShifts[alphaDataFieldIndex];
      bits &= ~alphaMask;

      // Calculate new alpha bit field and or it into the remaining value, store into data source
      data.setBits(index, bits | doubleToBitField(alpha, alphaDataFieldIndex));
    } // else ignore set request
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

  private long pack(double[] values, double a) {
    long bits = 0;
    for (int i = 0; i < format.getColorChannelCount(); i++) {
      int dataChannel = format.getColorChannelDataField(i);
      bits |= doubleToBitField(values[i], dataChannel);
    }
    if (format.hasAlphaChannel()) {
      bits |= doubleToBitField(a, format.getAlphaChannelDataField());
    }

    return bits;
  }

  private double unpack(long bits, double[] values) {
    for (int i = 0; i < format.getColorChannelCount(); i++) {
      int dataChannel = format.getColorChannelDataField(i);
      values[i] = bitFieldToDouble(bits, dataChannel);
    }

    if (format.hasAlphaChannel()) {
      return bitFieldToDouble(bits, format.getAlphaChannelDataField());
    } else {
      return 1.0;
    }
  }

  private static BinaryRepresentation getRepresentation(PixelFormat.Type type, int size) {
    switch (type) {
    case UINT:
    case USCALED:
      return new UnsignedInteger(size);
    case SINT:
    case SSCALED:
      return new SignedInteger(size);
    case UNORM:
      return new UnsignedNormalizedInteger(size);
    case SNORM:
      return new SignedNormalizedInteger(size);
    case SFLOAT:
      if (size == 64) {
        return Data.SFLOAT64;
      } else if (size == 32) {
        return Data.SFLOAT32;
      } else if (size == 16) {
        return Data.SFLOAT16;
      } else {
        throw new UnsupportedOperationException(
            "Unknown bit size for SFLOAT, unsure of mantissa and exponent bit sizes for total of: "
                + size);
      }
    case UFLOAT:
      // FIXME eventually add support for the 10 and 11 bit UFLOATs
      throw new UnsupportedOperationException("UFLOAT channels are currently unsupported");
    default:
      throw new UnsupportedOperationException("Unknown type: " + type);
    }
  }
}
