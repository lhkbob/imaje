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

import java.util.Arrays;

/**
 */
public class PixelFormatBuilder implements Cloneable {
  private static final int DEFAULT_CHANNEL = PixelFormat.SKIP_CHANNEL;
  private static final int DEFAULT_BITSIZE = 8;
  private static final PixelFormat.Type DEFAULT_TYPE = PixelFormat.Type.UNORM;

  private static final PixelFormat.Type INHERIT_TYPE = null;
  private static final int INHERIT_BITSIZE = 0;

  // These arrays will always have the same length
  private int[] dataChannelMap;
  private int[] bitSizes;
  private PixelFormat.Type[] types;

  public PixelFormatBuilder() {
    dataChannelMap = new int[0];
    bitSizes = new int[0];
    types = new PixelFormat.Type[0];
  }

  @Override
  public PixelFormatBuilder clone() {
    try {
      PixelFormatBuilder b = (PixelFormatBuilder) super.clone();
      // Update array references to be a deep clone
      b.dataChannelMap = Arrays.copyOf(b.dataChannelMap, b.dataChannelMap.length);
      b.bitSizes = Arrays.copyOf(b.bitSizes, b.bitSizes.length);
      b.types = Arrays.copyOf(b.types, b.types.length);
      return b;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen", e);
    }
  }

  /*
   * Truncates the array to newLength if shorter, or expands it to newLength by filling in new values
   * with the previous last element, or the default if there were no prior values.
   */
  private static int[] getResizedBits(int[] originalBitSizes, int newLength) {
    int oldLength = originalBitSizes.length;
    int[] newBitSizes = Arrays.copyOf(originalBitSizes, newLength);
    int defaultBitSize = oldLength > 0 ? originalBitSizes[oldLength - 1] : DEFAULT_BITSIZE;

    for (int i = oldLength; i < newLength; i++) {
      newBitSizes[i] = defaultBitSize;
    }
    return newBitSizes;
  }

  /*
   * Truncates or expands the channel array, filling in new values with the default channel (skip).
   */
  private static int[] getResizedChannels(int[] originalChannels, int newLength) {
    int oldLength = originalChannels.length;
    int[] newChannels = Arrays.copyOf(originalChannels, newLength);
    for (int i = oldLength; i < newLength; i++) {
      newChannels[i] = DEFAULT_CHANNEL;
    }
    return newChannels;
  }

  /*
   * Truncates or expands the type array, filling in new values with the previous last type or the
   * default type if the old type array was empty.
   */
  private static PixelFormat.Type[] getResizedTypes(
      PixelFormat.Type[] originalTypes, int newLength) {
    int oldLength = originalTypes.length;
    PixelFormat.Type[] newTypes = Arrays.copyOf(originalTypes, newLength);
    PixelFormat.Type defaultType = oldLength > 0 ? originalTypes[oldLength - 1] : DEFAULT_TYPE;

    for (int i = oldLength; i < newLength; i++) {
      newTypes[i] = defaultType;
    }
    return newTypes;
  }

  public PixelFormat build() {
    return new PixelFormat(dataChannelMap, types, bitSizes);
  }

  public PixelFormatBuilder compatibleWith(PixelFormat format) {
    reset();
    for (int i = 0; i < format.getDataFieldCount(); i++) {
      addChannel(
          format.getDataFieldChannel(i), format.getDataFieldBitSize(i), format.getDataFieldType(i));
    }
    return this;
  }

  public PixelFormatBuilder reset() {
    dataChannelMap = new int[0];
    bitSizes = new int[0];
    types = new PixelFormat.Type[0];

    return this;
  }

  public PixelFormatBuilder channels(int... channels) {
    if (channels.length != dataChannelMap.length) {
      // Resize everything, but just create an exact copy of the specified channels array
      // to avoid duplication
      dataChannelMap = Arrays.copyOf(channels, channels.length);
      bitSizes = getResizedBits(bitSizes, channels.length);
      types = getResizedTypes(types, channels.length);
    } else {
      // Just copy the new channels definition into the correctly sized map; type and bit
      // sizes are left alone.
      System.arraycopy(channels, 0, dataChannelMap, 0, channels.length);
    }

    return this;
  }

  public PixelFormatBuilder bits(int... bitSizes) {
    if (bitSizes.length > dataChannelMap.length) {
      // Must expand the other arrays to match
      dataChannelMap = getResizedChannels(dataChannelMap, bitSizes.length);
      types = getResizedTypes(types, bitSizes.length);
      // And then copy in the new sizes
      this.bitSizes = Arrays.copyOf(bitSizes, bitSizes.length);
    } else {
      // Expand the new bit sizes to the current size of the channel array, which is equivalent
      // to an array copy if they are the same length.
      this.bitSizes = getResizedBits(bitSizes, dataChannelMap.length);
    }

    return this;
  }

  public PixelFormatBuilder types(PixelFormat.Type... types) {
    if (types.length > dataChannelMap.length) {
      // Must expand the other arrays to match
      dataChannelMap = getResizedChannels(dataChannelMap, types.length);
      bitSizes = getResizedBits(bitSizes, types.length);
      // And then copy the new types
      this.types = Arrays.copyOf(types, types.length);
    } else {
      // Expand the new types to the current size of the channel array, which is equivalent
      // to an array copy if they are the same length
      this.types = getResizedTypes(types, dataChannelMap.length);
    }

    return this;
  }

  public PixelFormatBuilder addChannel(int channel) {
    return addChannel(channel, INHERIT_BITSIZE, INHERIT_TYPE);
  }

  public PixelFormatBuilder addChannel(int channel, int bits) {
    return addChannel(channel, bits, INHERIT_TYPE);
  }

  public PixelFormatBuilder addChannel(int channel, PixelFormat.Type type) {
    return addChannel(channel, INHERIT_BITSIZE, type);
  }

  public PixelFormatBuilder addChannel(int channel, int bits, PixelFormat.Type type) {
    // Increment size of the channel data, which automatically fills in the new channel size
    // and type with the inherited or default as available.
    dataChannelMap = getResizedChannels(dataChannelMap, dataChannelMap.length + 1);
    bitSizes = getResizedBits(bitSizes, bitSizes.length + 1);
    types = getResizedTypes(types, types.length + 1);

    if (bits > INHERIT_BITSIZE) {
      // Store the actual bit size
      bitSizes[bitSizes.length - 1] = bits;
    }
    if (type != INHERIT_TYPE) {
      // Store the actual type
      types[types.length - 1] = type;
    }

    // Always store the channel mapping, there is no way this can be inherited from prior channels
    dataChannelMap[dataChannelMap.length - 1] = channel;

    return this;
  }
}
