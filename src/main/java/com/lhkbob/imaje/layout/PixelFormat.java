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
 * PixelFormat
 * ===========
 *
 * PixelFormat describes how logical color channels (ordered from 0 to `N` consistent with color
 * definitions in the {@link com.lhkbob.imaje.color color} package) and an optional alpha channel
 * map to data fields. Data fields are almost equivalent to the bands described in {@link
 * DataLayout} except that they operate on a slightly higher level.
 *
 * Data fields can be packed together into a single primitive and extracted by an appropriately
 * implemented PixelArray. In this scenario, the layout has only one band but many fields. Another
 * option is an unpacked format where each field corresponds to a band, in which case a field and
 * band are the same thing. The connection between field and band is entirely dependent on the
 * PixelArray implementation that relies on the PixelFormat, and as part of this may impose
 * restrictions on the formats it works with.
 *
 * PixelFormats can be used to describe formats that reorder color channels, such as storing red,
 * green, and blue channels as a BGR image instead of RGB. It can also be used for packed formats,
 * cleanly describing a 16-bit format that splits the bit field into 5 bits for red, 6 for green,
 * and the last 5 for blue.
 *
 * {@link PixelFormatBuilder} and {@link #newBuilder()} can be used to fluently build up formats,
 * although its primary constructor is fairly readable on its own.
 *
 * @author Michael Ludwig
 */
// FIXME how will this be updated to handle compressed formats?
public class PixelFormat {
  public static final int ALPHA_CHANNEL = -1;
  public static final int SKIP_CHANNEL = -2;

  /**
   * Type
   * ====
   *
   * Type is an enum that describes how a data field in a pixel format should be interpreted. It
   * does not include the bit count associated with a type since that is flexible for each type
   * (e.g. a format could use an 8-bit or a 12-bit unsigned integer). These types correspond with
   * binary representations that are implemented in the {@link com.lhkbob.imaje.data.types types}
   * package. For the floating-point versions that technically require both a mantissa and exponent
   * count specification, commonly defined mantissa and exponent counts are selected based on the
   * desired total bit count. If there is no such common layout then the type cannot be used
   * currently (without implementing another {@link PixelArray} that is aware of it).
   *
   * @author Michael Ludwig
   */
  public enum Type {
    /**
     * An unsigned integer value, as described by {@link
     * com.lhkbob.imaje.data.types.UnsignedInteger}.
     */
    UINT,
    /**
     * A signed integer value, as described by {@link com.lhkbob.imaje.data.types.SignedInteger}.
     */
    SINT,
    /**
     * Technically very similar to UINT except for how data should be interpreted when passed to the
     * GPU. UINT specifies integer color values for shader code, whereas USCALED produces integral
     * values that are converted to floating point types in shader code.
     */
    USCALED,
    /**
     * Technically very similar to SINT except for how data should be interpreted when passed to the
     * GPU. UINT specifies integer color values for shader code, whereas SSCALED produces integral
     * values that are converted to floating point types in shader code.
     */
    SSCALED,
    /**
     * An unsigned integer that is normalized to `[0, 1]`, as described by
     * {@link com.lhkbob.imaje.data.types.UnsignedNormalizedInteger}.
     */
    UNORM,
    /**
     * A signed integer that is normalized to `[-1, 1]`, as described by
     * {@link com.lhkbob.imaje.data.types.SignedNormalizedInteger}.
     */
    SNORM,
    /**
     * A signed floating point number, as described by {@link
     * com.lhkbob.imaje.data.types.SignedFloatingPointNumber}. Currently, existing PixelArray
     * implementations can handle standard 16-bit, 32-bit, and 64-bit floating point specifications
     * (see {@link com.lhkbob.imaje.data.Data}.
     */
    SFLOAT,
    /**
     * An unsigned floating point number, as described by {@link
     * com.lhkbob.imaje.data.types.UnsignedFloatingPointNumber}. Standardized bit counts that are
     * supported are for 10-bit and 11-bit unsigned numbers.
     */
    UFLOAT
  }
  private final int alphaDataField;
  private final int[] bitSize;
  private final Type[] channelType;
  private final int[] colorToDataField;

  /**
   * Create a new PixelFormat whose data fields are described by the three parallel array maps,
   * `dataChannelMap`, `dataType`, and `bitSize`. Each array must have the same length. The position
   * within the array map represents the data field index. The corresponding value in
   * `dataChannelMap` defines the channel associated with that field. A positive value represents a
   * logical color channel, ordered to line up with the color definitions in the {@link
   * com.lhkbob.imaje.color color} package. {@link #ALPHA_CHANNEL} and {@link #SKIP_CHANNEL} may
   * also be used to mark the field as holding alpha data or as ignorable padding. A channel may
   * only be assigned to one field. All logical color channels (up to maximum positive channel
   * provided in `dataChannelMap`) must be present in the mapping.
   *
   * `dataType` defines the type semantics for the data field. It must be non-null for any field
   * that is not declared as a skipped channel. The bit size of each data field is specified
   * in `bitSize`. Each field must have a bit size of at least 1.
   *
   * @param dataChannelMap
   *     The data field to channel assignment
   * @param dataType
   *     The data field type specification
   * @param bitSize
   *     The data field bit size specification
   * @throws IllegalArgumentException
   *     if the mapping would not create a valid format
   * @throws NullPointerException
   *     if a unskipped field has a null type
   */
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
      if (bitSize[i] < 1) {
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

    colorToDataField = logicalToData;
    alphaDataField = alphaIndex;
  }

  /**
   * Create a PixelFormat from a mask specification. This is an alternate way of specifying a format
   * where each color channel and optionally an alpha channel is described by a bit mask that
   * contains the bits for that channel.
   *
   * The color masks are specified in `colorMasks`, which can contain masks for more channels than
   * are actually used in the final format. Masks that are equal to 0 are silently ignored. This is
   * done because many image formats have a fixed number of masks that can be easily submitted to
   * this function, and the format specifies that optional channels have masks equal to 0.
   *
   * `colorMasks` is first filtered to remove masks equal to 0, and the resulting ordered list
   * defines the logical channel for each mask. If there are more non-zero masks than
   * `channelCount`, channels beyond that count are marked as skipped channels.
   *
   * If `alphaMask` is non-zero, the pixel format will contain an alpha channel, otherwise it will
   * not. All data fields will have the type specified by `type`, while each fields' bit size is
   * determined by the number of set bits in its mask. The bit masks must form a continuous bit
   * field that does not overlap across fields or leave empty gaps between fields. The masks must be
   * defined from bit 0 up to some number of bits less than or equal to 64.
   *
   * @param colorMasks
   *     The masks for each logical channel, where a mask can be set to 0 to mark it as being
   *     filtered out (and subsequently shifting the index of remaining channels)
   * @param channelCount
   *     The number of color channels in the created format, which must be less than or equal to the
   *     number of non-zero masks in `colorMasks`
   * @param alphaMask
   *     The alpha mask, or 0 for no alpha
   * @param type
   *     The data type for each field
   * @return A pixel format that matches the given mask definition
   */
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

  /**
   * @return A new builder in the default initial configuration to build a PixelFormat
   */
  public static PixelFormatBuilder newBuilder() {
    return new PixelFormatBuilder();
  }

  /**
   * Get whether or not the color and alpha channels of this format are compatible with `format`.
   * Two pixel formats are compatible if they store the same number of color channels and have
   * equivalent alpha channel presence (i.e. both have an alpha channel or both have no alpha). It
   * is permissible for color channels to have different types and bit sizes, between the two
   * formats. The ordering or mapping from color channel to data field does not need to be the same
   * for two formats to be compatible.
   *
   * If potential loss of resolution or precision is acceptable, then returning true from this
   * method indicates that the two formats can store the same type of color information.
   *
   * @param format
   *     The format to compare with this format
   * @return True if channels are logically compatible
   */
  public boolean areChannelsCompatible(PixelFormat format) {
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

  /**
   * Get whether or not the color and alpha channels of this format are equivalent with `format`.
   * Two pixel formats have equivalent channels if they are {@link
   * #areChannelsCompatible(PixelFormat) compatible} and have matching types and bit sizes for each
   * channel. However, the ordering or mapping from color channel to data field does not need to be
   * the same for two formats to be compatible.
   *
   * Equivalent channels implies that moving pixel data from one format to the other does not
   * lose any information, and is purely a reordering of the channels in the underlying data.
   *
   * @param format
   *     The format to compare to this format
   * @return True if the channels are logically equivalent
   */
  public boolean areChannelsEquivalent(PixelFormat format) {
    // Two pixel formats are equivalent if they can represent the same logical data (i.e. they
    // have the same number and type of color channels and an alpha channel; the order of the color
    // channels in data does not matter nor if there are skipped data channels in one or the other)
    if (!areChannelsCompatible(format)) {
      return false;
    }

    for (int i = 0; i < getColorChannelCount(); i++) {
      // Make sure each color channel has the same type and bit size
      if (getColorChannelType(i) != format.getColorChannelType(i)) {
        return false;
      }
      if (getColorChannelBitSize(i) != format.getColorChannelBitSize(i)) {
        return false;
      }
    }

    if (hasAlphaChannel()) {
      // Since the channels are compatible, format also has an alpha channel
      if (getAlphaChannelType() != format.getAlphaChannelType()
          || getAlphaChannelBitSize() != format.getAlphaChannelBitSize()) {
        return false;
      }
    }

    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof PixelFormat)) {
      return false;
    }
    PixelFormat f = (PixelFormat) o;
    return Arrays.equals(f.colorToDataField, colorToDataField) && Arrays
        .equals(f.channelType, channelType) && Arrays.equals(f.bitSize, bitSize)
        && f.alphaDataField == alphaDataField;
  }

  /**
   * Get the bit size of the data field storing alpha data. If this format does not have alpha
   * data then this returns 0.
   *
   * @return The number of bits used to store alpha values
   */
  public int getAlphaChannelBitSize() {
    if (hasAlphaChannel()) {
      return bitSize[alphaDataField];
    } else {
      return 0;
    }
  }

  /**
   * @return The data field index that stores alpha channel data
   */
  public int getAlphaChannelDataField() {
    return alphaDataField;
  }

  /**
   * Get the type semantics of the data field that stores alpha data. If this format does not have
   * alpha data then this returns null.
   *
   * @return The type of the field storing alpha values
   */
  public Type getAlphaChannelType() {
    if (hasAlphaChannel()) {
      return channelType[alphaDataField];
    } else {
      return null;
    }
  }

  /**
   * @return The total number of bits required by this format, including skipped fields
   */
  public int getBitSize() {
    int total = 0;
    for (int i = 0; i < bitSize.length; i++) {
      total += bitSize[i];
    }
    return total;
  }

  /**
   * Get the total number of bits used by the valid channels of this format (i.e. all non-skipped
   * fields). This represents the informational content of the format although it may not equal the
   * bit size of the primitive types required to hold pixels for the format.
   *
   * @return The total number of bits for just the color and alpha channel
   */
  public int getBitSizeOfChannels() {
    int total = 0;
    for (int i = 0; i < bitSize.length; i++) {
      if (!isDataFieldSkipped(i)) {
        total += bitSize[i];
      }
    }
    return total;
  }

  /**
   * Get the number of logical channels that this pixel format contains. This is the number of
   * fields of the format that have a logical channel index or are the alpha channel, ignoring all
   * fields that are marked to skip.
   *
   * This is equal to the color channel count plus an additional one if the format has an alpha
   * channel.
   *
   * @return The logical channel count of this format
   */
  public int getChannelCount() {
    return colorToDataField.length + (alphaDataField >= 0 ? 1 : 0);
  }

  /**
   * Get the bit size of the data field associated with the given color channel.
   *
   * @param colorChannel
   *     The logical color channel
   * @return The number of bits for the color channel
   *
   * @throws IndexOutOfBoundsException
   *     if `colorChannel` is less than 0 or greater than or equal to `getColorChannelCount()`
   */
  public int getColorChannelBitSize(int colorChannel) {
    return bitSize[colorToDataField[colorChannel]];
  }

  /**
   * @return The number of color channels for this format
   */
  public int getColorChannelCount() {
    return colorToDataField.length;
  }

  /**
   * Get the data field index the given color channel is mapped to.
   *
   * @param colorChannel
   *     The logical color channel
   * @return The data field index that stores values for `colorChannel`
   *
   * @throws IndexOutOfBoundsException
   *     if `colorChannel` is less than 0 or greater than or equal to `getColorChannelCount()`
   */
  public int getColorChannelDataField(int colorChannel) {
    return colorToDataField[colorChannel];
  }

  /**
   * Get the type semantics for the data field associated with the given color channel.
   *
   * @param colorChannel
   *     The logical color channel
   * @return The type associated with the channel
   *
   * @throws IndexOutOfBoundsException
   *     if `colorChannel` is less than 0 or greater than or equal to `getColorChannelCount()`
   */
  public Type getColorChannelType(int colorChannel) {
    return channelType[colorToDataField[colorChannel]];
  }

  /**
   * Get the number of bits assigned to the given data field. This field may represent a color
   * channel, an alpha channel, or may be ignored by the format (e.g. it's padding within a packed
   * format).
   *
   * @param dataIndex
   *     The data field index
   * @return The number of bits assigned to field
   *
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` is less than 0 or greater than or equal to `getDataFieldCount()`
   */
  public int getDataFieldBitSize(int dataIndex) {
    return bitSize[dataIndex];
  }

  /**
   * Get the logical channel assigned to the data field, `dataIndex`. If the field contains a color
   * channel then a value between 0 and `getColorChannelCount() - 1` will be returned. If the field
   * holds alpha values then {@link #ALPHA_CHANNEL} is returned. If the field is skipped then {@link
   * #SKIP_CHANNEL} is returned.
   *
   * @param dataIndex
   *     The data field index
   * @return The logical channel assigned to the field
   *
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` is less than 0 or greater than or equal to `getDataFieldCount()`
   */
  public int getDataFieldChannel(int dataIndex) {
    if (dataIndex == alphaDataField) {
      return ALPHA_CHANNEL;
    } else {
      for (int i = 0; i < colorToDataField.length; i++) {
        if (colorToDataField[i] == dataIndex) {
          return i;
        }
      }

      return SKIP_CHANNEL;
    }
  }

  /**
   * @return The number of data fields of the format
   */
  public int getDataFieldCount() {
    return bitSize.length;
  }

  /**
   * Get the type semantics of the data stored in the given data field. If this is a skipped field
   * then null is returned because the values in that field are undefined.
   *
   * @param dataIndex
   *     The data field index
   * @return The type of the data field
   *
   * @throws IndexOutOfBoundsException
   *     if `dataIndex` is less than 0 or greater than or equal to `getDataFieldCount()`
   */
  public Type getDataFieldType(int dataIndex) {
    return channelType[dataIndex];
  }

  /**
   * @return True if this format has a data field representing the alpha channel
   */
  public boolean hasAlphaChannel() {
    return alphaDataField >= 0;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + Arrays.hashCode(bitSize);
    result = 31 * result + Arrays.hashCode(channelType);
    result = 31 * result + Arrays.hashCode(colorToDataField);
    result = 31 * result + alphaDataField;
    return result;
  }

  /**
   * Get whether or not the specified data field is skipped for this format, or if it represents a
   * color or alpha channel. A skipped field has no type interpretation with respect to the format,
   * although it still has a bit size.
   *
   * @param dataIndex
   *     The index of the data field
   * @return True if the field is skipped and has no defined type information
   */
  public boolean isDataFieldSkipped(int dataIndex) {
    return channelType[dataIndex] == null;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("PixelFormat(");
    for (int i = 0; i < getDataFieldCount(); i++) {
      if (i > 0) {
        sb.append(", ");
      }

      String channelName = null; // This will always be initialized but compiler doesn't realize it
      boolean ignoreType = false;
      if (alphaDataField == i) {
        // The alpha channel
        channelName = "Alpha";
      } else {
        // Search for color channel
        boolean color = false;
        for (int j = 0; j < colorToDataField.length; j++) {
          if (i == colorToDataField[j]) {
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
