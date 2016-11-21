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
import com.lhkbob.imaje.data.types.UnsignedSharedExponent;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.Functions;

/**
 * SharedExponentArray
 * ===================
 *
 * SharedExponentArray is a packed RootPixelArray that requires a custom channel for a shared
 * exponent, while the color channels represent unsigned mantissas. This maps the color channels and
 * exponent field to {@link UnsignedSharedExponent} to produce the vector of color values per pixel.
 * The PixelFormat for arrays of this type cannot have an alpha channel and must provide a {@link
 * #EXPONENT_CHANNEL}.
 *
 * @author Michael Ludwig
 */
public class SharedExponentArray extends RootPixelArray {
  /**
   * Custom channel label for the shared exponent in the packed pixel.
   */
  public static final int EXPONENT_CHANNEL = PixelFormat.CUSTOM_DATA_CHANNEL;

  private final PixelFormat format;
  private final DataLayout layout;
  private final BitData data;

  private final UnsignedSharedExponent exp;

  /**
   * Create a new SharedExponentArray that assumes logical color channel data is described by
   * `format`, mapped from two dimensions to one by `layout`, and stored within `data`.
   *
   * The layout must have a band count of 1 since all data fields of the format are packed into
   * a single primitive element. The total bit size of the format must equal the bit size of `data`.
   * `data` must have a length equal to the required elements specified by `layout`.
   *
   * Additionally, the format cannot have an alpha channel and it it must have at least two
   * color channels. The format must also include the custom {@link #EXPONENT_CHANNEL}.
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
  public SharedExponentArray(PixelFormat format, DataLayout layout, BitData data) {
    Arguments.equals("layout.getBandCount()", 1, layout.getBandCount());
    Arguments.equals("bit size", format.getBitSize(), data.getBitSize());
    Arguments.equals("data length", layout.getRequiredDataElements(), data.getLength());
    Arguments.equals("custom channel count", 1, format.getCustomChannelCount());

    if (!format.hasCustomChannel(EXPONENT_CHANNEL)) {
      throw new IllegalArgumentException("Must provide an exponent channel in format");
    }
    if (format.hasAlphaChannel()) {
      throw new IllegalArgumentException(
          "Alpha channels are not supported for shared exponent formats");
    }
    if (format.getColorChannelCount() < 2) {
      throw new IllegalArgumentException("Must have at least 2 color channels.");
    }

    long exponentMask = 0L;
    long[] mantissaMasks = new long[format.getColorChannelCount() - 1];

    // Count from the back so we can track total shift from right to left
    int shift = 0;
    for (int i = format.getDataFieldCount() - 1; i >= 0; i--) {
      if (!format.isDataFieldSkipped(i)) {
        long mask = Functions.maskLong(format.getDataFieldBitSize(i)) << shift;
        int channel = format.getDataFieldChannel(i);
        if (channel == EXPONENT_CHANNEL) {
          // This is the exponent
          exponentMask = mask;
        } else {
          mantissaMasks[channel] = mask;
        }
      }

      shift += format.getDataFieldBitSize(i);
    }

    exp = new UnsignedSharedExponent(exponentMask, mantissaMasks);
    this.format = format;
    this.layout = layout;
    this.data = data;
  }

  @Override
  public DataLayout getLayout() {
    return layout;
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

  /**
   * Get the type converter that is configured based on this array's PixelFormat.
   * @return The configured UnsignedSharedExponent type mapper
   */
  public UnsignedSharedExponent getTypeConverter() {
    return exp;
  }

  @Override
  public double get(int x, int y, double[] channelValues) {
    long bits = data.getBits(layout.getBandOffset(x, y, 0));
    // Expand all floating point values from bits into the given channel array
    exp.toNumericValues(bits, channelValues);

    // Always return 1.0 since there is never an alpha channel
    return 1.0;
  }

  @Override
  public double get(int x, int y, double[] channelValues, long[] bandOffsets) {
    layout.getBandOffsets(x, y, bandOffsets);
    // Expand all floating point values from bits into the given channel array
    exp.toNumericValues(data.getBits(bandOffsets[0]), channelValues);

    // Always return 1.0 since there is never an alpha channel
    return 1.0;
  }

  @Override
  public double getAlpha(int x, int y) {
    // Always return 1.0 since there is never an alpha channel
    return 1.0;
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a) {
    // Ignore alpha value
    long encodedBits = exp.toBits(channelValues);
    data.setBits(layout.getBandOffset(x, y, 0), encodedBits);
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a, long[] bandOffsets) {
    // Ignore alpha value
    long encodedBits = exp.toBits(channelValues);
    layout.getBandOffsets(x, y, bandOffsets);
    data.setBits(bandOffsets[0], encodedBits);
  }

  @Override
  public void setAlpha(int x, int y, double alpha) {
    // Do nothing, there is never an alpha channel
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }
}
