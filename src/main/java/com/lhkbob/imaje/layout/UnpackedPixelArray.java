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

import com.lhkbob.imaje.data.DataBuffer;
import com.lhkbob.imaje.data.NumericData;
import com.lhkbob.imaje.util.Arguments;

/**
 * UnpackedPixelArray
 * ================
 *
 * UnpackedPixelArray is a RootPixelArray implementation that represents each channel value of a
 * pixel as its own numeric value, for every pixel in the image. It uses a {@link DataLayout} to
 * describe how the pixels are mapped from two dimensions to one. Data is stored in a {@link
 * NumericData} instance whose bit size matches the bit size of each data field defined in its
 * {@link PixelFormat}. The implementation and characteristics of the numeric data buffer must
 * correspond with the formats' field types.
 *
 * @author Michael Ludwig
 */
public class UnpackedPixelArray extends RootPixelArray {
  private final NumericData<?> data;
  private final PixelFormat format;
  private final DataLayout layout;

  /**
   * Create a new UnpackedPixelArray that assumes logical color channel data is described by
   * `format`, mapped from two dimensions to one by `layout`, and stored within `data`.
   *
   * The layout must have a band count equal to the data field count of the format, since each data
   * field of the format correspond to a band in the data layout (even if the field is marked as
   * skipped). Every data field bit size must be the same and all unskipped fields must have the
   * same type. This bit size and type must be compatible with `data`, which holds the pixel data
   * for every single band. Compatibility is determined by {@link
   * PixelArrays#checkBufferCompatible(DataBuffer, PixelFormat.Type, int)}. `data` must have a
   * length equal to the required elements specified by `layout`.
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
  public UnpackedPixelArray(PixelFormat format, DataLayout layout, NumericData<?> data) {
    Arguments.equals("channel count", format.getDataFieldCount(), layout.getBandCount());
    Arguments.equals("data length", layout.getRequiredDataElements(), data.getLength());

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
    for (int i = 0; i < format.getDataFieldCount(); i++) {
      if (format.getDataFieldType(i) != null && format.getDataFieldType(i) != channelType) {
        throw new IllegalArgumentException(
            "Data channel " + i + " has incorrect type for an unpacked encoder, expected "
                + channelType + " but was " + format.getDataFieldType(i));
      }
      if (format.getDataFieldBitSize(i) != channelBitSize) {
        throw new IllegalArgumentException(
            "Data channel " + i + " has incorrect bit size for an unpacked encoder, expected "
                + channelBitSize + " but was " + format.getDataFieldBitSize(i));
      }
    }

    PixelArrays.checkBufferCompatible(data, channelType, channelBitSize);

    // Validation complete
    this.data = data;
    this.format = format;
    this.layout = layout;
  }

  /**
   * Determine if the given pixel format can be used by UnpackedPixelArray instances. To be
   * compatible, every data field of the format (even if skipped) must have the same bit size. This
   * bit size must be an exact match with one of the Java primitive types (i.e. 8, 16, 32, or 64).
   * Every unskipped data field must also have the same type semantics.
   *
   * @param format
   *     The pixel format to check
   * @return True if could be used to represent a format unpacked across multiple primitives
   */
  public static boolean isSupported(PixelFormat format) {
    int reqBits = -1;
    PixelFormat.Type reqType = null;
    for (int i = 0; i < format.getDataFieldCount(); i++) {
      if (reqBits < 0) {
        reqBits = format.getDataFieldBitSize(i);
        // Validate bit size, without a specific data buffer assume a Java primitive bit size
        if (reqBits != 8 && reqBits != 16 && reqBits != 32 && reqBits != 64) {
          // Channel cannot be fully stored in a single primitive
          return false;
        }
      } else if (reqBits != format.getDataFieldBitSize(i)) {
        // Channel does not have the same size
        return false;
      }

      PixelFormat.Type curType = format.getDataFieldType(i);
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

  @Override
  public double get(int x, int y, double[] channelValues) {
    for (int i = 0; i < format.getColorChannelCount(); i++) {
      int dataChannel = format.getColorChannelDataField(i);
      channelValues[i] = data.getValue(layout.getBandOffset(x, y, dataChannel));
    }

    if (format.hasAlphaChannel()) {
      return data.getValue(layout.getBandOffset(x, y, format.getAlphaChannelDataField()));
    } else {
      return 1.0;
    }
  }

  @Override
  public double get(int x, int y, double[] channelValues, long[] bandOffsets) {
    layout.getBandOffsets(x, y, bandOffsets);
    for (int i = 0; i < format.getColorChannelCount(); i++) {
      int dataChannel = format.getColorChannelDataField(i);
      channelValues[i] = data.getValue(bandOffsets[dataChannel]);
    }

    if (format.hasAlphaChannel()) {
      return data.getValue(bandOffsets[format.getAlphaChannelDataField()]);
    } else {
      return 1.0;
    }
  }

  @Override
  public double getAlpha(int x, int y) {
    if (format.hasAlphaChannel()) {
      return data.getValue(layout.getBandOffset(x, y, format.getAlphaChannelDataField()));
    } else {
      // No alpha channel
      return 1.0;
    }
  }

  @Override
  public NumericData<?> getData(int band) {
    Arguments.checkIndex("band", format.getDataFieldCount(), band);
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
    for (int i = 0; i < format.getColorChannelCount(); i++) {
      int dataChannel = format.getColorChannelDataField(i);
      data.setValue(layout.getBandOffset(x, y, dataChannel), channelValues[i]);
    }

    if (format.hasAlphaChannel()) {
      data.setValue(layout.getBandOffset(x, y, format.getAlphaChannelDataField()), a);
    } // otherwise no alpha channel so ignore the set request
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a, long[] bandOffsets) {
    layout.getBandOffsets(x, y, bandOffsets);
    for (int i = 0; i < format.getColorChannelCount(); i++) {
      int dataChannel = format.getColorChannelDataField(i);
      data.setValue(bandOffsets[dataChannel], channelValues[i]);
    }

    if (format.hasAlphaChannel()) {
      data.setValue(bandOffsets[format.getAlphaChannelDataField()], a);
    } // otherwise no alpha channel so ignore the set request
  }

  @Override
  public void setAlpha(int x, int y, double alpha) {
    if (format.hasAlphaChannel()) {
      data.setValue(layout.getBandOffset(x, y, format.getAlphaChannelDataField()), alpha);
    } // otherwise no alpha channel so ignore the set request
  }
}
