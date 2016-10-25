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
 *
 */
public class SharedExponentArray implements PixelArray {
  private final PixelFormat format;
  private final DataLayout layout;
  private final BitData data;

  private final UnsignedSharedExponent exp;

  public SharedExponentArray(PixelFormat format, DataLayout layout, BitData data) {
    Arguments.equals("layout.getChannelCount()", 1, layout.getChannelCount());
    Arguments.equals("bit size", format.getTotalBitSize(), data.getBitSize());
    Arguments
        .checkArrayRange("data length", data.getLength(), 0, layout.getRequiredDataElements());

    if (format.hasAlphaChannel()) {
      throw new IllegalArgumentException(
          "Alpha channels are not supported for shared exponent formats");
    }
    if (format.getColorChannelCount() < 3) {
      throw new IllegalArgumentException(
          "Must have at least 3 color channels, where one is reserved for the exponent field");
    }

    long exponentMask = 0L;
    long[] mantissaMasks = new long[format.getColorChannelCount() - 1];

    // Count from the back so we can track total shift from right to left
    int shift = 0;
    for (int i = format.getDataChannelCount() - 1; i >= 0; i--) {
      if (!format.isDataChannelSkipped(i)) {
        long mask = Functions.maskLong(format.getDataChannelBitSize(i)) << shift;
        int channel = format.getDataChannelColorIndex(i);
        if (channel == mantissaMasks.length) {
          // This is the exponent
          exponentMask = mask;
        } else {
          mantissaMasks[channel] = mask;
        }
      }

      shift += format.getDataChannelBitSize(i);
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
  public BitData getData() {
    return data;
  }

  @Override
  public PixelFormat getFormat() {
    return format;
  }

  public UnsignedSharedExponent getTypeConverter() {
    return exp;
  }

  @Override
  public double get(int x, int y, double[] channelValues) {
    long bits = data.getBits(layout.getChannelIndex(x, y, 0));
    // Expand all floating point values from bits into the given channel array
    exp.toNumericValues(bits, channelValues);

    // Always return 1.0 since there is never an alpha channel
    return 1.0;
  }

  @Override
  public double get(int x, int y, double[] channelValues, long[] channels) {
    layout.getChannelIndices(x, y, channels);
    // Expand all floating point values from bits into the given channel array
    exp.toNumericValues(data.getBits(channels[0]), channelValues);

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
    data.setBits(layout.getChannelIndex(x, y, 0), encodedBits);
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a, long[] channels) {
    // Ignore alpha value
    long encodedBits = exp.toBits(channelValues);
    layout.getChannelIndices(x, y, channels);
    data.setBits(channels[0], encodedBits);
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
