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

import com.lhkbob.imaje.data.ByteData;
import com.lhkbob.imaje.data.DoubleData;
import com.lhkbob.imaje.data.FloatData;
import com.lhkbob.imaje.data.IntData;
import com.lhkbob.imaje.data.LongData;
import com.lhkbob.imaje.data.NumericData;
import com.lhkbob.imaje.data.ShortData;
import com.lhkbob.imaje.data.types.CustomBinaryData;
import com.lhkbob.imaje.data.types.SignedInteger;
import com.lhkbob.imaje.data.types.SignedNormalizedInteger;
import com.lhkbob.imaje.data.types.UnsignedInteger;
import com.lhkbob.imaje.data.types.UnsignedNormalizedInteger;
import com.lhkbob.imaje.util.Arguments;

/**
 *
 */
public class UnpackedPixelArray implements PixelArray {
  private final DataLayout layout;
  private final PixelFormat format;
  private final NumericData<?> data;

  // Returns true if all data bit sizes are the same size, all bit sizes equal 8, 16, 32, or 64.
  // And all types are the same.
  public static boolean isSupported(PixelFormat format) {
    int reqBits = -1;
    PixelFormat.Type reqType = null;
    for (int i = 0; i < format.getDataChannelCount(); i++) {
      if (reqBits < 0) {
        reqBits = format.getDataChannelBitSize(i);
        // Validate bit size, without a specific data buffer assume a Java primitive bit size
        if (reqBits != 8 && reqBits != 16 && reqBits != 32 && reqBits != 64) {
          // Channel cannot be fully stored in a single primitive
          return false;
        }
      } else if (reqBits != format.getDataChannelBitSize(i)) {
        // Channel does not have the same size
        return false;
      }

      PixelFormat.Type curType = format.getDataChannelType(i);
      if (reqType == null) {
        // This could still be null if the data channel is skipped
        reqType = curType;
      } else if (curType != null && reqType != curType) {
        // Unskipped channel differs from another unskipped channel
        return false;
      }
    }

    return true;
  }

  public UnpackedPixelArray(PixelFormat format, DataLayout layout, NumericData<?> data) {
    Arguments.equals("channel count", format.getDataChannelCount(), layout.getChannelCount());
    Arguments.checkArrayRange("data length", data.getLength(), 0, layout.getRequiredDataElements());

    // Now verify that the format fits the mold expected of unpacked types
    // i.e. bit size for each channel is the same and type of each channel is the same.
    // - because data access goes directly to the numeric data source, this checks all data channels
    //   even those that are skipped by the format (although only bit size is validated then).

    // There's always at least one color channel, which has a bit size and type
    // (unlike with randomly accessing a data channel, which could have a null type if skipped)
    int channelBitSize = format.getColorChannelBitSize(0);
    PixelFormat.Type channelType = format.getColorChannelType(0);
    // NOTE: this performs the equivalent validation as isSupported() but is also targeted directly
    // with the bit size of the data buffer, so is more efficient and potentially more flexible if
    // someone implemented a non-Java primitive sized numeric data source.
    for (int i = 0; i < format.getDataChannelCount(); i++) {
      if (format.getDataChannelType(i) != null && format.getDataChannelType(i) != channelType) {
        throw new IllegalArgumentException(
            "Data channel " + i + " has incorrect type for an unpacked encoder, expected "
                + channelType + " but was " + format.getDataChannelType(i));
      }
      if (format.getDataChannelBitSize(i) != channelBitSize) {
        throw new IllegalArgumentException(
            "Data channel " + i + " has incorrect bit size for an unpacked encoder, expected "
                + channelBitSize + " but was " + format.getDataChannelBitSize(i));
      }
    }

    // At this point the format is self-consistent with an unpacked format but make sure the
    // numeric data source provides a compatible type to the pixel format
    if (channelBitSize != data.getBitSize()) {
      throw new IllegalArgumentException(
          "Channel bit size is incompatible with data source bit size, expected " + channelBitSize
              + " but was " + data.getBitSize());
    }

    boolean badType = true;
    switch (channelType) {
    case UINT:
    case USCALED:
      // Must go through a known type with UINT semantics
      if (data instanceof CustomBinaryData && ((CustomBinaryData) data)
          .getBinaryRepresentation() instanceof UnsignedInteger) {
        badType = false;
      }
    case SINT:
    case SSCALED:
      // Native short, int, long, byte is preferred
      if (data instanceof ByteData || data instanceof ShortData || data instanceof IntData
          || data instanceof LongData) {
        badType = false;
      } else if (data instanceof CustomBinaryData && ((CustomBinaryData) data)
          .getBinaryRepresentation() instanceof SignedInteger) {
        badType = false;
      }
      break;
    case UNORM:
      // Must go through a known type with UNORM semantics
      if (data instanceof CustomBinaryData && ((CustomBinaryData) data)
          .getBinaryRepresentation() instanceof UnsignedNormalizedInteger) {
        badType = false;
      }
      break;
    case SNORM:
      // Must go through a known type with SNORM semantics
      if (data instanceof CustomBinaryData && ((CustomBinaryData) data)
          .getBinaryRepresentation() instanceof SignedNormalizedInteger) {
        badType = false;
      }
      break;
    case SFLOAT:
      // Native float or double is preferred
      if (data instanceof FloatData || data instanceof DoubleData) {
        badType = false;
      } else if (data instanceof CustomBinaryData) {
        // Only other valid source type is a BinaryNumericSource with one of the known SFLOAT types
        CustomBinaryData d = (CustomBinaryData) data;
        if (d.getBinaryRepresentation().isFloatingPoint() && !d.getBinaryRepresentation()
            .isUnsigned()) {
          badType = false;
        }
      }
      break;
    case UFLOAT:
      if (data instanceof CustomBinaryData) {
        CustomBinaryData d = (CustomBinaryData) data;
        if (d.getBinaryRepresentation().isFloatingPoint() && d.getBinaryRepresentation()
            .isUnsigned()) {
          badType = false;
        }
      }
      break;
    }

    if (badType) {
      throw new IllegalArgumentException(
          "Unsupported DataSource type (" + data + ") for type " + channelType);
    }

    // Validation complete
    this.data = data;
    this.format = format;
    this.layout = layout;
  }

  @Override
  public DataLayout getLayout() {
    return layout;
  }

  @Override
  public PixelFormat getFormat() {
    return format;
  }

  @Override
  public NumericData<?> getData() {
    return data;
  }

  @Override
  public double get(int x, int y, double[] channelValues) {
    for (int i = 0; i < format.getColorChannelCount(); i++) {
      int dataChannel = format.getColorChannelDataIndex(i);
      channelValues[i] = data.getValue(layout.getChannelIndex(x, y, dataChannel));
    }

    if (format.hasAlphaChannel()) {
      return data.getValue(layout.getChannelIndex(x, y, format.getAlphaChannelDataIndex()));
    } else {
      return 1.0;
    }
  }

  @Override
  public double get(int x, int y, double[] channelValues, long[] channels) {
    layout.getChannelIndices(x, y, channels);
    for (int i = 0; i < format.getColorChannelCount(); i++) {
      int dataChannel = format.getColorChannelDataIndex(i);
      channelValues[i] = data.getValue(channels[dataChannel]);
    }

    if (format.hasAlphaChannel()) {
      return data.getValue(channels[format.getAlphaChannelDataIndex()]);
    } else {
      return 1.0;
    }
  }

  @Override
  public double getAlpha(int x, int y) {
    if (format.hasAlphaChannel()) {
      return data.getValue(layout.getChannelIndex(x, y, format.getAlphaChannelDataIndex()));
    } else {
      // No alpha channel
      return 1.0;
    }
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a) {
    for (int i = 0; i < format.getColorChannelCount(); i++) {
      int dataChannel = format.getColorChannelDataIndex(i);
      data.setValue(layout.getChannelIndex(x, y, dataChannel), channelValues[i]);
    }

    if (format.hasAlphaChannel()) {
      data.setValue(layout.getChannelIndex(x, y, format.getAlphaChannelDataIndex()), a);
    } // otherwise no alpha channel so ignore the set request
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a, long[] channels) {
    layout.getChannelIndices(x, y, channels);
    for (int i = 0; i < format.getColorChannelCount(); i++) {
      int dataChannel = format.getColorChannelDataIndex(i);
      data.setValue(channels[dataChannel], channelValues[i]);
    }

    if (format.hasAlphaChannel()) {
      data.setValue(channels[format.getAlphaChannelDataIndex()], a);
    } // otherwise no alpha channel so ignore the set request
  }

  @Override
  public void setAlpha(int x, int y, double alpha) {
    if (format.hasAlphaChannel()) {
      data.setValue(layout.getChannelIndex(x, y, format.getAlphaChannelDataIndex()), alpha);
    } // otherwise no alpha channel so ignore the set request
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }
}
