package com.lhkbob.imaje.layout;

import java.util.Arrays;

/**
 *
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

    // Make sure that all referenced logical channels are specified from 0 to N, where N is < data channel count
    // and there are no duplicates (ALPHA cannot be duplicated but SKIP can be).
    // N is the highest value in dataChannelMap and (N+1) is the logical channel count of the pixel format.
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
