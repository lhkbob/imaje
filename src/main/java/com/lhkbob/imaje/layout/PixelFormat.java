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

import com.lhkbob.imaje.util.Functions;

import java.util.Arrays;

/**
 * FIXME will we have to make this a pretty plain interface and then have uncompressed and compressed versions?
 * Or does this go on top of that? I mean it kind of does, but it kind of doesn't. The logical color
 * channel ordering is kind of irrelevant to bit layout and data channel layout and could be
 * separated. But I'm not sure how much that buys us.
 */
public class PixelFormat {
  public static final int ALPHA_CHANNEL = -1;
  public static final int SKIP_CHANNEL = -2;

  public enum Type {
    UINT, SINT, USCALED, SSCALED, UNORM, SNORM, SFLOAT, UFLOAT
  }

  private final int[] bitSize;
  private final Type[] channelType;
  private final int[] colorToDataChannel;
  private final int alphaDataChannel;

  public static PixelFormat createFromMasks(
      long[] colorMasks, int channelCount, long alphaMask, Type type) {
    // First filter masks equal to 0, which is a convenience for image loading code that frequently
    // has a fixed number of channels specified but their presence in data is encoded as a 0'ed mask
    int logicalChannelCount = 0;
    for (int i = 0; i < colorMasks.length; i++) {
      if (colorMasks[i] != 0) {
        logicalChannelCount++;
      }
    }

    // Before including a possible alpha channel, validate the maxChannels argument, which must be
    // less than or equal to the number of non-zero masks.
    if (channelCount > logicalChannelCount) {
      throw new IllegalArgumentException(
          "Insufficient non-zero color masks provided to meet requested channel count");
    }
    // Now compact the colorMasks array to exclude 0s so that indexing data to color channel is
    // easier.
    int filteredCount = 0;
    long[] filteredColorMasks = new long[logicalChannelCount];
    for (int i = 0; i < colorMasks.length; i++) {
      if (colorMasks[i] == 0) {
        continue;
      }
      filteredColorMasks[filteredCount++] = colorMasks[i];
    }

    if (alphaMask != 0) {
      logicalChannelCount++;
    }

    int[] dataChannelMap = new int[logicalChannelCount];
    Type[] dataType = new Type[logicalChannelCount];
    int[] bitSize = new int[logicalChannelCount];

    int expectedShift = 0;
    for (int i = dataChannelMap.length - 1; i >= 0; i--) {
      // Find the color or alpha channel that has a shift by the expected number of bits,
      // which will be monotonically increasing, so as we move left through the channel map
      // we insert the appropriately higher-order masks.
      int size = 0;
      int logicalChannel = 0;
      if (alphaMask != 0 && Long.numberOfTrailingZeros(alphaMask) == expectedShift) {
        logicalChannel = ALPHA_CHANNEL;
        size = Long.bitCount(alphaMask);
      } else {
        // Search for a color mask
        boolean found = false;
        for (int j = 0; j < filteredColorMasks.length; j++) {
          if (Long.numberOfTrailingZeros(filteredColorMasks[j]) == expectedShift) {
            if (j >= channelCount) {
              // Convert channel into a skipped channel
              logicalChannel = SKIP_CHANNEL;
            } else {
              logicalChannel = j;
            }
            size = Long.bitCount(colorMasks[j]);
            found = true;
            break;
          }
        }

        if (!found) {
          throw new IllegalArgumentException("Provided color masks are not contiguous");
        }
      }

      dataChannelMap[i] = logicalChannel;
      bitSize[i] = size;
      dataType[i] = type;
    }

    return new PixelFormat(dataChannelMap, dataType, bitSize);
  }

  public PixelFormat(int[] dataChannelMap, Type[] dataType, int[] bitSize) {
    if (dataChannelMap.length != dataType.length || dataType.length != bitSize.length) {
      throw new IllegalArgumentException("All input arrays must be of the same length");
    }

    // Make sure all types are non-null for non-skipped channels
    for (int i = 0; i < dataType.length; i++) {
      if (dataType[i] == null && dataChannelMap[i] > SKIP_CHANNEL) {
        throw new NullPointerException("Type cannot be null for unskipped channel " + i);
      }
    }

    // Make sure all bit sizes are at least 1 (no empty channels)
    for (int i = 0; i < bitSize.length; i++) {
      if (bitSize[i] < 0) {
        throw new IllegalArgumentException(
            "Bit size cannot be less than 1 (requested size " + bitSize[i] + " for channel " + i
                + ")");
      }
    }

    // Make sure that all referenced logical channels are specified from 0 to N, where N is < data
    // channel count and there are no duplicates (ALPHA cannot be duplicated but SKIP can be). N is
    // the highest value in dataChannelMap and (N+1) is the logical channel count of the pixel
    // format.
    int alphaIndex = -1;
    for (int i = 0; i < dataChannelMap.length; i++) {
      if (dataChannelMap[i] == ALPHA_CHANNEL) {
        if (alphaIndex >= 0) {
          // Duplicate alpha index specified, which is illegal
          throw new IllegalArgumentException(
              "Duplicate alpha channels specified in data to logical map");
        }
        alphaIndex = i;
      }
    }

    int logicalChannelsCount;
    if (alphaIndex < 0) {
      // Has no alpha, so the map length should be color channels max
      logicalChannelsCount = dataChannelMap.length;
    } else {
      // Has an alpha, so the map length should be 1+color channels max
      logicalChannelsCount = dataChannelMap.length - 1;
    }
    // However, to support formats and adapters that want to store a color that needs fewer channels
    // than what the data can hold (e.g. an XY normal map in an RGBA texture), or even to cleverly
    // encode two images into the same data where one is set to RG and the other stores to BA,
    // the dataToLogicalMap can reference fewer logical channels.
    int maxReferencedLogicalChannel = -1;
    for (int aDataToLogicalChannelMap : dataChannelMap) {
      if (aDataToLogicalChannelMap >= logicalChannelsCount) {
        throw new IllegalArgumentException(
            "Cannot reference more logical color channels than available in data map");
      }
      if (aDataToLogicalChannelMap > maxReferencedLogicalChannel) {
        maxReferencedLogicalChannel = aDataToLogicalChannelMap;
      }
    }
    logicalChannelsCount = maxReferencedLogicalChannel + 1;

    // 0 to color channels max - 1 must be present in any order, detect and build up the
    // inverse map simultaneously
    int[] logicalToData = new int[logicalChannelsCount];
    for (int i = 0; i < logicalChannelsCount; i++) {
      // Search for i within dataToLogicalMap. Although not necessary to check for duplicates, it
      // allows for improved error reporting (instead of specifying a different value is missing).
      int index = -1;
      for (int j = 0; j < dataChannelMap.length; j++) {
        if (dataChannelMap[j] == i) {
          if (index >= 0) {
            // Duplicate logical index
            throw new IllegalArgumentException("Duplicate logical color channel provided: " + i);
          }
          index = j;
        }
      }

      if (index < 0) {
        throw new IllegalArgumentException("No data channel maps to logical color channel: " + i);
      }
      logicalToData[i] = index;
    }

    this.bitSize = Arrays.copyOf(bitSize, bitSize.length);
    this.channelType = Arrays.copyOf(dataType, dataType.length);
    // Nullify channel type for skipped channels
    for (int i = 0; i < dataType.length; i++) {
      if (dataChannelMap[i] < ALPHA_CHANNEL) {
        this.channelType[i] = null;
      }
    }

    colorToDataChannel = logicalToData;
    alphaDataChannel = alphaIndex;
  }

  public boolean isLogicallyCompatible(PixelFormat format) {
    // Two pixel formats are compatible if they can store the same color channels and alpha channel,
    // but it is permissible for there to be type changes in the data, making them compatible with
    // the same color types, but potentially lossy. All equivalent formats are compatible.
    if (format == null) {
      return false;
    }
    if (format.getColorChannelCount() != getColorChannelCount()) {
      return false;
    }
    if (format.hasAlphaChannel() != hasAlphaChannel()) {
      return false;
    }
    return true;
  }

  public boolean isLogicallyEquivalent(PixelFormat format) {
    // Two pixel formats are equivalent if they can represent the same logical data (i.e. they
    // have the same number and type of color channels and an alpha channel; the order of the color
    // channels in data does not matter nor if there are skipped data channels in one or the other)
    if (!isLogicallyCompatible(format)) {
      return false;
    }

    for (int i = 0; i < getColorChannelCount(); i++) {
      if (getColorChannelType(i) != format.getColorChannelType(i)) {
        return false;
      }
    }

    return true;
  }

  public int getElementCount() {
    return colorToDataChannel.length + (alphaDataChannel >= 0 ? 1 : 0);
  }

  public int getElementBitSize() {
    int total = 0;
    for (int i = 0; i < bitSize.length; i++) {
      if (channelType[i] != null) {
        total += bitSize[i];
      }
    }
    return total;
  }

  // FIXME is the word channel redundant and of little value?

  public boolean isDataChannelSkipped(int dataIndex) {
    return channelType[dataIndex] == null;
  }

  public Type getColorChannelType(int colorChannel) {
    return channelType[colorToDataChannel[colorChannel]];
  }

  public int getColorChannelBitSize(int colorChannel) {
    return bitSize[colorToDataChannel[colorChannel]];
  }

  public int getColorChannelDataIndex(int colorChannel) {
    return colorToDataChannel[colorChannel];
  }

  public int getTotalBitSize() {
    int total = 0;
    for (int i = 0; i < bitSize.length; i++) {
      total += bitSize[i];
    }
    return total;
  }

  public int getDataChannelCount() {
    return bitSize.length;
  }

  public int getColorChannelCount() {
    return colorToDataChannel.length;
  }

  public boolean hasAlphaChannel() {
    return alphaDataChannel >= 0;
  }

  public int getAlphaChannelDataIndex() {
    return alphaDataChannel;
  }

  public Type getAlphaChannelType() {
    if (hasAlphaChannel()) {
      return channelType[alphaDataChannel];
    } else {
      return null;
    }
  }

  public int getAlphaChannelBitSize() {
    if (hasAlphaChannel()) {
      return bitSize[alphaDataChannel];
    } else {
      return 0;
    }
  }

  public int getDataChannelBitSize(int dataIndex) {
    return bitSize[dataIndex];
  }

  public Type getDataChannelType(int dataIndex) {
    return channelType[dataIndex];
  }

  public int getDataChannelColorIndex(int dataIndex) {
    if (dataIndex == alphaDataChannel) {
      return ALPHA_CHANNEL;
    } else {
      for (int i = 0; i < colorToDataChannel.length; i++) {
        if (colorToDataChannel[i] == dataIndex) {
          return i;
        }
      }

      return SKIP_CHANNEL;
    }
  }

  public long getColorChannelBitMask(int color) {
    return getDataChannelBitMask(getColorChannelDataIndex(color));
  }

  public long getDataChannelBitMask(int dataIndex) {
    long mask = Functions.maskLong(bitSize[dataIndex]);
    long shift = 0;
    for (int i = bitSize.length - 1; i > dataIndex; i--) {
      shift += bitSize[i];
    }
    return mask << shift;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + Arrays.hashCode(bitSize);
    result = 31 * result + Arrays.hashCode(channelType);
    result = 31 * result + Arrays.hashCode(colorToDataChannel);
    result = 31 * result + alphaDataChannel;
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PixelFormat)) {
      return false;
    }
    PixelFormat f = (PixelFormat) o;
    return Arrays.equals(f.colorToDataChannel, colorToDataChannel) && Arrays
        .equals(f.channelType, channelType) && Arrays.equals(f.bitSize, bitSize)
        && f.alphaDataChannel == alphaDataChannel;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("PixelFormat(");
    for (int i = 0; i < getDataChannelCount(); i++) {
      if (i > 0) {
        sb.append(", ");
      }

      String channelName = null; // This will always be initialized but compiler doesn't realize it
      boolean ignoreType = false;
      if (alphaDataChannel == i) {
        // The alpha channel
        channelName = "Alpha";
      } else {
        // Search for color channel
        boolean color = false;
        for (int j = 0; j < colorToDataChannel.length; j++) {
          if (i == colorToDataChannel[j]) {
            channelName = "Ch" + j;
            color = true;
            break;
          }
        }

        if (!color) {
          // A skipped data channel
          channelName = "Skipped";
          ignoreType = true;
        }
      }

      sb.append(channelName).append("(");
      if (!ignoreType) {
        sb.append(channelType[i]).append(", ");
      }
      sb.append(bitSize[i]).append(")");
    }
    sb.append(")");
    return sb.toString();
  }
}
