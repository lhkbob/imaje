package com.lhkbob.imaje.gpu;

import com.lhkbob.imaje.layout.PixelFormat;
import com.lhkbob.imaje.layout.PixelFormat.Type;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 */
public enum GPUFormat {
  UNDEFINED(byte.class, null),
  R4G4_UNORM_PACK8(
      byte.class, false, repeat(4, 2), repeat(Type.UNORM, 2),
      new Channel[] { Channel.R, Channel.G }),
  R4G4B4A4_UNORM_PACK16(short.class, false, repeat(4, 4), repeat(Type.UNORM, 4), rgba()),
  B4G4R4A4_UNORM_PACK16(short.class, false, repeat(4, 4), repeat(Type.UNORM, 4), bgra()),
  R5G6B5_UNORM_PACK16(short.class, false, new int[] { 5, 6, 5 }, repeat(Type.UNORM, 3), rgb()),
  B5G6R5_UNORM_PACK16(short.class, false, new int[] { 5, 6, 5 }, repeat(Type.UNORM, 3), bgr()),
  R5G5B5A1_UNORM_PACK16(
      short.class, false, new int[] { 5, 5, 5, 1 }, repeat(Type.UNORM, 4), rgba()),
  B5G5R5A1_UNORM_PACK16(
      short.class, false, new int[] { 5, 5, 5, 1 }, repeat(Type.UNORM, 4), rgba()),
  A1R5G5B5_UNORM_PACK16(
      short.class, false, new int[] { 1, 5, 5, 5 }, repeat(Type.UNORM, 4), argb()),
  R8_UNORM(byte.class, Type.UNORM, Channel.R),
  R8_SNORM(byte.class, Type.SNORM, Channel.R),
  R8_USCALED(byte.class, Type.USCALED, Channel.R),
  R8_SSCALED(byte.class, Type.SSCALED, Channel.R),
  R8_UINT(byte.class, Type.UINT, Channel.R),
  R8_SINT(byte.class, Type.SINT, Channel.R),
  R8_SRGB(
      byte.class, true, new int[] { 8 }, new Type[] { Type.UNORM }, new Channel[] { Channel.R }),
  R8G8_UNORM(byte.class, Type.UNORM, Channel.R, Channel.G),
  R8G8_SNORM(byte.class, Type.SNORM, Channel.R, Channel.G),
  R8G8_USCALED(byte.class, Type.USCALED, Channel.R, Channel.G),
  R8G8_SSCALED(byte.class, Type.SSCALED, Channel.R, Channel.G),
  R8G8_UINT(byte.class, Type.UINT, Channel.R, Channel.G),
  R8G8_SINT(byte.class, Type.SINT, Channel.R, Channel.G),
  R8G8_SRGB(
      byte.class, true, new int[] { 8 }, repeat(Type.UNORM, 2),
      new Channel[] { Channel.R, Channel.G }),
  R8G8B8_UNORM(byte.class, Type.UNORM, rgb()),
  R8G8B8_SNORM(byte.class, Type.SNORM, rgb()),
  R8G8B8_USCALED(byte.class, Type.USCALED, rgb()),
  R8G8B8_SSCALED(byte.class, Type.SSCALED, rgb()),
  R8G8B8_UINT(byte.class, Type.UINT, rgb()),
  R8G8B8_SINT(byte.class, Type.SINT, rgb()),
  R8G8B8_SRGB(byte.class, true, new int[] { 8 }, repeat(Type.UNORM, 3), rgb()),
  B8G8R8_UNORM(byte.class, Type.UNORM, bgr()),
  B8G8R8_SNORM(byte.class, Type.SNORM, bgr()),
  B8G8R8_USCALED(byte.class, Type.USCALED, bgr()),
  B8G8R8_SSCALED(byte.class, Type.SSCALED, bgr()),
  B8G8R8_UINT(byte.class, Type.UINT, bgr()),
  B8G8R8_SINT(byte.class, Type.SINT, bgr()),
  B8G8R8_SRGB(byte.class, true, new int[] { 8 }, repeat(Type.UNORM, 3), bgr()),
  R8G8B8A8_UNORM(byte.class, Type.UNORM, rgba()),
  R8G8B8A8_SNORM(byte.class, Type.SNORM, rgba()),
  R8G8B8A8_USCALED(byte.class, Type.USCALED, rgba()),
  R8G8B8A8_SSCALED(byte.class, Type.SSCALED, rgba()),
  R8G8B8A8_UINT(byte.class, Type.UINT, rgba()),
  R8G8B8A8_SINT(byte.class, Type.SINT, rgba()),
  R8G8B8A8_SRGB(byte.class, true, new int[] { 8 }, repeat(Type.UNORM, 4), rgba()),
  B8G8R8A8_UNORM(byte.class, Type.UNORM, bgra()),
  B8G8R8A8_SNORM(byte.class, Type.SNORM, bgra()),
  B8G8R8A8_USCALED(byte.class, Type.USCALED, bgra()),
  B8G8R8A8_SSCALED(byte.class, Type.SSCALED, bgra()),
  B8G8R8A8_UINT(byte.class, Type.UINT, bgra()),
  B8G8R8A8_SINT(byte.class, Type.SINT, bgra()),
  B8G8R8A8_SRGB(byte.class, true, new int[] { 8 }, repeat(Type.UNORM, 4), bgra()),
  A8B8G8R8_UNORM_PACK32(int.class, false, repeat(8, 4), repeat(Type.UNORM, 4), abgr()),
  A8B8G8R8_SNORM_PACK32(int.class, false, repeat(8, 4), repeat(Type.SNORM, 4), abgr()),
  A8B8G8R8_USCALED_PACK32(int.class, false, repeat(8, 4), repeat(Type.USCALED, 4), abgr()),
  A8B8G8R8_SSCALED_PACK32(int.class, false, repeat(8, 4), repeat(Type.SSCALED, 4), abgr()),
  A8B8G8R8_UINT_PACK32(int.class, false, repeat(8, 4), repeat(Type.UINT, 4), abgr()),
  A8B8G8R8_SINT_PACK32(int.class, false, repeat(8, 4), repeat(Type.SINT, 4), abgr()),
  A8B8G8R8_SRGB_PACK32(int.class, true, repeat(8, 4), repeat(Type.UNORM, 4), abgr()),
  A2R10G10B10_UNORM_PACK32(
      int.class, false, new int[] { 2, 10, 10, 10 }, repeat(Type.UNORM, 4), argb()),
  A2R10G10B10_SNORM_PACK32(int.class, false, new int[] { 2, 10, 10, 10 }, repeat(Type.SNORM, 4),
      argb()),
  A2R10G10B10_USCALED_PACK32(int.class, false, new int[] { 2, 10, 10, 10 }, repeat(Type.USCALED, 4),
      argb()),
  A2R10G10B10_SSCALED_PACK32(int.class, false, new int[] { 2, 10, 10, 10 }, repeat(Type.SSCALED, 4),
      argb()),
  A2R10G10B10_UINT_PACK32(
      int.class, false, new int[] { 2, 10, 10, 10 }, repeat(Type.UINT, 4), argb()),
  A2R10G10B10_SINT_PACK32(
      int.class, false, new int[] { 2, 10, 10, 10 }, repeat(Type.SINT, 4), argb()),
  A2B10G10R10_UNORM_PACK32(int.class, false, new int[] { 2, 10, 10, 10 }, repeat(Type.UNORM, 4),
      abgr()),
  A2B10G10R10_SNORM_PACK32(int.class, false, new int[] { 2, 10, 10, 10 }, repeat(Type.SNORM, 4),
      abgr()),
  A2B10G10R10_USCALED_PACK32(int.class, false, new int[] { 2, 10, 10, 10 }, repeat(Type.USCALED, 4),
      abgr()),
  A2B10G10R10_SSCALED_PACK32(int.class, false, new int[] { 2, 10, 10, 10 }, repeat(Type.SSCALED, 4),
      abgr()),
  A2B10G10R10_UINT_PACK32(int.class, false, new int[] { 2, 10, 10, 10 }, repeat(Type.UINT, 4),
      abgr()),
  A2B10G10R10_SINT_PACK32(int.class, false, new int[] { 2, 10, 10, 10 }, repeat(Type.SINT, 4),
      abgr()),
  R16_UNORM(short.class, Type.UNORM, Channel.R),
  R16_SNORM(short.class, Type.SNORM, Channel.R),
  R16_USCALED(short.class, Type.USCALED, Channel.R),
  R16_SSCALED(short.class, Type.SSCALED, Channel.R),
  R16_UINT(short.class, Type.UINT, Channel.R),
  R16_SINT(short.class, Type.SINT, Channel.R),
  R16_SFLOAT(short.class, Type.SFLOAT, Channel.R),
  R16G16_UNORM(short.class, Type.UINT, Channel.R, Channel.G),
  R16G16_SNORM(short.class, Type.SNORM, Channel.R, Channel.G),
  R16G16_USCALED(short.class, Type.USCALED, Channel.R, Channel.G),
  R16G16_SSCALED(short.class, Type.SSCALED, Channel.R, Channel.G),
  R16G16_UINT(short.class, Type.UINT, Channel.R, Channel.G),
  R16G16_SINT(short.class, Type.SINT, Channel.R, Channel.G),
  R16G16_SFLOAT(short.class, Type.SFLOAT, Channel.R, Channel.G),
  R16G16B16_UNORM(short.class, Type.UNORM, rgb()),
  R16G16B16_SNORM(short.class, Type.SNORM, rgb()),
  R16G16B16_USCALED(short.class, Type.USCALED, rgb()),
  R16G16B16_SSCALED(short.class, Type.SSCALED, rgb()),
  R16G16B16_UINT(short.class, Type.UINT, rgb()),
  R16G16B16_SINT(short.class, Type.SINT, rgb()),
  R16G16B16_SFLOAT(short.class, Type.SFLOAT, rgb()),
  R16G16B16A16_UNORM(short.class, Type.UNORM, rgba()),
  R16G16B16A16_SNORM(short.class, Type.SNORM, rgba()),
  R16G16B16A16_USCALED(short.class, Type.USCALED, rgba()),
  R16G16B16A16_SSCALED(short.class, Type.SSCALED, rgba()),
  R16G16B16A16_UINT(short.class, Type.UINT, rgba()),
  R16G16B16A16_SINT(short.class, Type.SINT, rgba()),
  R16G16B16A16_SFLOAT(short.class, Type.SFLOAT, rgba()),
  R32_UINT(int.class, Type.UINT, Channel.R),
  R32_SINT(int.class, Type.SINT, Channel.R),
  R32_SFLOAT(float.class, Type.SFLOAT, Channel.R),
  R32G32_UINT(int.class, Type.UINT, Channel.R, Channel.G),
  R32G32_SINT(int.class, Type.SINT, Channel.R, Channel.G),
  R32G32_SFLOAT(float.class, Type.SFLOAT, Channel.R, Channel.G),
  R32G32B32_UINT(int.class, Type.UINT, rgb()),
  R32G32B32_SINT(int.class, Type.SINT, rgb()),
  R32G32B32_SFLOAT(float.class, Type.SFLOAT, rgb()),
  R32G32B32A32_UINT(int.class, Type.UINT, rgba()),
  R32G32B32A32_SINT(int.class, Type.SINT, rgba()),
  R32G32B32A32_SFLOAT(float.class, Type.SFLOAT, rgba()),
  R64_UINT(long.class, Type.UINT, Channel.R),
  R64_SINT(long.class, Type.SINT, Channel.R),
  R64_SFLOAT(double.class, Type.SFLOAT, Channel.R),
  R64G64_UINT(long.class, Type.UINT, Channel.R, Channel.G),
  R64G64_SINT(long.class, Type.SINT, Channel.R, Channel.G),
  R64G64_SFLOAT(double.class, Type.SFLOAT, Channel.R, Channel.G),
  R64G64B64_UINT(long.class, Type.UINT, rgb()),
  R64G64B64_SINT(long.class, Type.SINT, rgb()),
  R64G64B64_SFLOAT(double.class, Type.SFLOAT, rgb()),
  R64G64B64A64_UINT(long.class, Type.UINT, rgba()),
  R64G64B64A64_SINT(long.class, Type.SINT, rgba()),
  R64G64B64A64_SFLOAT(double.class, Type.SFLOAT, rgba()),
  B10G11R11_UFLOAT_PACK32(
      int.class, false, new int[] { 10, 11, 11 }, repeat(Type.UFLOAT, 3), bgr()),
  E5B9G9R9_UFLOAT_PACK32(
      int.class, false, new int[] { 5, 9, 9, 9 }, repeat(Type.UFLOAT, 4),
      new Channel[] { Channel.E, Channel.B, Channel.G, Channel.R }),
  D16_UNORM(short.class, Type.UNORM, Channel.D),
  X8_D24_UNORM_PACK32(
      int.class, false, new int[] { 8, 24 }, new Type[] { null, Type.UNORM },
      new Channel[] { Channel.X, Channel.D }),
  D32_SFLOAT(float.class, Type.SFLOAT, Channel.D),
  S8_UINT(byte.class, Type.UINT, Channel.S),
  D16_UNORM_S8_UINT(
      int.class, false, new int[] { 16, 8, 8 }, new Type[] { Type.UNORM, Type.UINT, null },
      new Channel[] { Channel.D, Channel.S, Channel.X }),
  D24_UNORM_S8_UINT(
      int.class, false, new int[] { 24, 8 }, new Type[] { Type.UNORM, Type.UINT },
      new Channel[] { Channel.D, Channel.S }),
  D32_SFLOAT_S8_UINT(
      long.class, false, new int[] { 32, 8, 24 }, new Type[] { Type.SFLOAT, Type.UINT, null },
      new Channel[] { Channel.D, Channel.S, Channel.X }),
  BC1_RGB_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 3), rgb()),
  BC1_RGB_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 3), rgb()),
  BC1_RGBA_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 4), rgba()),
  BC1_RGBA_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 4), rgba()),
  BC2_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 4), rgba()),
  BC2_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 4), rgba()),
  BC3_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 4), rgba()),
  BC3_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 4), rgba()),
  BC4_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 4), rgba()),
  BC4_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 4), rgba()),
  BC5_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 4), rgba()),
  BC5_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 4), rgba()),
  // FIXME am I sure about what these channels are? they might be for normals and do I care then?
  BC6H_UFLOAT_BLOCK(byte.class, false, null, repeat(Type.UFLOAT, 4), rgba()),
  BC6H_SFLOAT_BLOCK(byte.class, false, null, repeat(Type.SFLOAT, 4), rgba()),
  BC7_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 4), rgba()),
  BC7_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 4), rgba()),
  ETC2_R8G8B8_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 3), rgb()),
  ETC2_R8G8B8_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 3), rgb()),
  ETC2_R8G8B8A1_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 4), rgba()),
  ETC2_R8G8B8A1_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 4), rgba()),
  ETC2_R8G8B8A8_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 4), rgba()),
  ETC2_R8G8B8A8_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 4), rgba()),
  // FIXME are these compressed, what's with 11 bits of precision?
  EAC_R11_UNORM_BLOCK(
      byte.class, false, null, new Type[] { Type.UNORM }, new Channel[] { Channel.R }),
  EAC_R11_SNORM_BLOCK(
      byte.class, false, null, new Type[] { Type.SNORM }, new Channel[] { Channel.R }),
  EAC_R11G11_UNORM_BLOCK(
      byte.class, false, null, repeat(Type.UNORM, 2), new Channel[] { Channel.R, Channel.G }),
  EAC_R11G11_SNORM_BLOCK(byte.class, false, null, repeat(Type.SNORM, 2),
      new Channel[] { Channel.R, Channel.G }),
  ASTC_4X4_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_4X4_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_5X4_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_5X4_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_5X5_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_5X5_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_6X5_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_6X5_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_6X6_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_6X6_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_8X5_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_8X5_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_8X6_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_8X6_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_8X8_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_8X8_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_10X5_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_10X5_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_10X6_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_10X6_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_10X8_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_10X8_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_10X10_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_10X10_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_12X10_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_12X10_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_12X12_UNORM_BLOCK(byte.class, false, null, repeat(Type.UNORM, 4), rgba()),
  ASTC_12X12_SRGB_BLOCK(byte.class, true, null, repeat(Type.UNORM, 4), rgba());


  public enum Channel {
    R, G, B, A, D, S, E, X
  }
  private final PixelFormat format;
  private final boolean isPacked;
  private final boolean isSRGB;
  private final Class<?> javaType;
  private final Channel[] logicalChannels; // Additional semantic information about color channels not included in base PixelFormat

  GPUFormat(Class<?> javaType, Type type, Channel... logicalChannels) {
    this(javaType, false, type, logicalChannels);
  }

  GPUFormat(Class<?> javaType, boolean isSRGB, Type type, Channel... logicalChannels) {
    this(javaType, isSRGB, new int[] { getBitSize(javaType) }, repeat(type, logicalChannels.length),
        logicalChannels);
  }

  GPUFormat(
      Class<?> javaType, boolean isSRGB, int[] dataBitAllocation, Type[] types,
      Channel[] channels) {
    // Validate the data bit allocation assuming it's not a compressed format
    if (dataBitAllocation != null) {
      if (dataBitAllocation.length == 1) {
        // Not packed, so the single value must equal the bit size of the primitive type
        if (dataBitAllocation[0] != getBitSize(javaType)) {
          throw new RuntimeException(
              "Bad GPUFormat definition, unpacked type bit total different from primitive type");
        }

        isPacked = false;
        dataBitAllocation = repeat(dataBitAllocation[0], channels.length);
      } else {
        // Packed, so the length must equal that of logicalChannels and its total must equal the
        // bit size of the primitive type
        if (dataBitAllocation.length != channels.length) {
          throw new RuntimeException(
              "Bad GPUFormat definition, packed bit length different from logical channel count");
        }
        int total = 0;
        for (int i : dataBitAllocation) {
          total += i;
        }
        if (total != getBitSize(javaType)) {
          throw new RuntimeException(
              "Bad GPUFormat definition, packed bit total different from primitive type");
        }

        isPacked = true;
      }

      // Convert the logical channels into the data to color map PixelFormat expects.
      int[] dataToChannelMap = new int[channels.length];
      for (int i = 0; i < channels.length; i++) {
        int colorChannel;
        switch (channels[i]) {
        case R:
        case D:
          colorChannel = 0;
          break;
        case G:
          colorChannel = 1;
          break;
        case B:
          colorChannel = 2;
          break;
        case A:
          colorChannel = PixelFormat.ALPHA_CHANNEL;
          break;
        case S:
          if (channels.length == 1) {
            // Stencil only
            colorChannel = 0;
          } else {
            colorChannel = 1;
          }
          break;
        default: // Includes E and X
          colorChannel = PixelFormat.SKIP_CHANNEL;
          break;
        }
        dataToChannelMap[i] = colorChannel;
      }

      format = new PixelFormat(dataToChannelMap, types, dataBitAllocation);
    } else {
      // This represents a compressed GPU format, which for the time being has fairly vague
      // details and is not well handled by actual image layouts, etc.
      isPacked = false;
      format = null;
    }

    this.logicalChannels = Arrays.copyOf(channels, channels.length);
    this.javaType = javaType;
    this.isSRGB = isSRGB;
  }

  public static Predicate<GPUFormat> bitSize(int bitSize) {
    return format -> isBitSize(format, bitSize);
  }

  public static Predicate<GPUFormat> channelLayout(GPUFormat.Channel... channels) {
    return format -> isChannelLayout(format, channels);
  }

  public static Predicate<GPUFormat> dataType(PixelFormat.Type... types) {
    return format -> isDataType(format, types);
  }

  public static Predicate<GPUFormat> format(PixelFormat pixelFormat) {
    return format -> isPixelFormat(format, pixelFormat);
  }

  public static boolean isBitSize(GPUFormat format, int bitSize) {
    return format.getBitSize() == bitSize;
  }

  public static boolean isChannelLayout(GPUFormat format, GPUFormat.Channel... channels) {
    if (format.getChannelCount() != channels.length) {
      return false;
    }
    for (int i = 0; i < channels.length; i++) {
      if (format.getChannelSemantic(i) != channels[i]) {
        return false;
      }
    }
    return true;
  }

  public static boolean isDataType(GPUFormat format, PixelFormat.Type... types) {
    if (format.isCompressed()) {
      return false;
    }
    if (format.getPixelFormat().getDataChannelCount() != types.length) {
      return false;
    }
    for (int i = 0; i < types.length; i++) {
      if (format.getPixelFormat().getDataChannelType(i) != types[i]) {
        return false;
      }
    }
    return true;
  }

  public static boolean isPackedLayout(GPUFormat format, int... bits) {
    if (!format.isPacked()) {
      return false;
    }
    if (format.getChannelCount() != bits.length) {
      return false;
    }
    for (int i = 0; i < bits.length; i++) {
      if (format.getPixelFormat().getDataChannelBitSize(i) != bits[i]) {
        return false;
      }
    }
    return true;
  }

  public static boolean isPixelFormat(GPUFormat format, PixelFormat pixelFormat) {
    if (format.isCompressed())
      return false;
    return format.getPixelFormat().equals(pixelFormat);
  }

  public static boolean isUnpackedLayout(GPUFormat format) {
    return !format.isPacked() && !format.isCompressed();
  }

  public static Predicate<GPUFormat> packedLayout(int... bits) {
    return format -> isPackedLayout(format, bits);
  }

  public static Stream<GPUFormat> streamAll() {
    return Arrays.stream(GPUFormat.values());
  }

  public int getBitSize() {
    return getBitSize(javaType);
  }

  public int getChannelCount() {
    return logicalChannels.length;
  }

  public Channel getChannelSemantic(int channel) {
    return logicalChannels[channel];
  }

  public Channel[] getChannels() {
    return Arrays.copyOf(logicalChannels, logicalChannels.length);
  }

  public Class<?> getJavaType() {
    return javaType;
  }

  public PixelFormat getPixelFormat() {
    return format;
  }

  public int getPrimitiveCount() {
    if (isCompressed()) {
      return -1;
    } else if (isPacked()) {
      return 1;
    } else {
      return logicalChannels.length;
    }
  }

  public boolean isCompressed() {
    return format == null;
  }

  public boolean isPacked() {
    return isPacked;
  }

  public boolean isSRGB() {
    return isSRGB;
  }

  static Channel[] abgr() {
    return new Channel[] { Channel.A, Channel.B, Channel.G, Channel.R };
  }

  static Channel[] argb() {
    return new Channel[] { Channel.A, Channel.R, Channel.G, Channel.B };
  }

  static Channel[] bgr() {
    return new Channel[] { Channel.B, Channel.G, Channel.R };
  }

  static Channel[] bgra() {
    return new Channel[] { Channel.B, Channel.G, Channel.R, Channel.A };
  }

  static Type[] repeat(Type type, int length) {
    Type[] t = new Type[length];
    for (int i = 0; i < length; i++) {
      t[i] = type;
    }
    return t;
  }

  static int[] repeat(int value, int length) {
    int[] v = new int[length];
    for (int i = 0; i < length; i++) {
      v[i] = value;
    }
    return v;
  }

  static Channel[] rgb() {
    return new Channel[] { Channel.R, Channel.G, Channel.B };
  }

  static Channel[] rgba() {
    return new Channel[] { Channel.R, Channel.G, Channel.B, Channel.A };
  }

  private static int getBitSize(Class<?> javaType) {
    if (javaType.equals(byte.class)) {
      return 8;
    } else if (javaType.equals(short.class)) {
      return 16;
    } else if (javaType.equals(int.class) || javaType.equals(float.class)) {
      return 32;
    } else if (javaType.equals(long.class) || javaType.equals(double.class)) {
      return 64;
    } else {
      throw new RuntimeException(
          "Bad GPUFormat definition, unexpected java primitive type: " + javaType);
    }
  }
}
