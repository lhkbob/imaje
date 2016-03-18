package com.lhkbob.imaje.layout;

import java.util.Arrays;

/**
 *
 */
public enum GPUFormat {
  UNDEFINED(null, 0, null),
  R4G4_UNORM_PACK8(byte.class, 1, Type.UNORM, Channel.R, Channel.G),
  R4G4B4A4_UNORM_PACK16(short.class, 1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  B4G4R4A4_UNORM_PACK16(short.class,  1, Type.UNORM, Channel.B, Channel.G, Channel.R, Channel.A),
  R5G6B5_UNORM_PACK16(short.class, 1, Type.UNORM, Channel.R, Channel.G, Channel.B),
  B5G6R5_UNORM_PACK16(short.class, 1, Type.UNORM, Channel.B, Channel.G, Channel.R),
  R5G5B5A1_UNORM_PACK16(short.class, 1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  B5G5R5A1_UNORM_PACK16(short.class, 1, Type.UNORM, Channel.B, Channel.G, Channel.R, Channel.A),
  A1R5G5B5_UNORM_PACK16(short.class, 1, Type.UNORM, Channel.A, Channel.R, Channel.G, Channel.B),
  R8_UNORM(byte.class, 1, Type.UNORM, Channel.R),
  R8_SNORM(byte.class, 1, Type.SNORM, Channel.R),
  R8_USCALED(byte.class, 1, Type.USCALED, Channel.R),
  R8_SSCALED(byte.class, 1, Type.SSCALED, Channel.R),
  R8_UINT(byte.class, 1, Type.UINT, Channel.R),
  R8_SINT(byte.class, 1, Type.SINT, Channel.R),
  R8_SRGB(byte.class, 1, Type.SRGB, Channel.R),
  R8G8_UNORM(byte.class, 2, Type.UNORM, Channel.R, Channel.G),
  R8G8_SNORM(byte.class, 2, Type.SNORM, Channel.R, Channel.G),
  R8G8_USCALED(byte.class, 2, Type.USCALED, Channel.R, Channel.G),
  R8G8_SSCALED(byte.class, 2, Type.SSCALED, Channel.R, Channel.G),
  R8G8_UINT(byte.class, 2, Type.UINT, Channel.R, Channel.G),
  R8G8_SINT(byte.class, 2, Type.SINT, Channel.R, Channel.G),
  R8G8_SRGB(byte.class, 2, Type.SRGB, Channel.R, Channel.G),
  R8G8B8_UNORM(byte.class, 3, Type.UNORM, Channel.R, Channel.G, Channel.B),
  R8G8B8_SNORM(byte.class, 3, Type.SNORM, Channel.R, Channel.G, Channel.B),
  R8G8B8_USCALED(byte.class, 3, Type.USCALED, Channel.R, Channel.G, Channel.B),
  R8G8B8_SSCALED(byte.class, 3, Type.SSCALED, Channel.R, Channel.G, Channel.B),
  R8G8B8_UINT(byte.class, 3, Type.UINT, Channel.R, Channel.G, Channel.B),
  R8G8B8_SINT(byte.class, 3, Type.SINT, Channel.R, Channel.G, Channel.B),
  R8G8B8_SRGB(byte.class, 3, Type.SRGB, Channel.R, Channel.G, Channel.B),
  B8G8R8_UNORM(byte.class, 3, Type.UNORM, Channel.B, Channel.G, Channel.R),
  B8G8R8_SNORM(byte.class, 3, Type.SNORM, Channel.B, Channel.G, Channel.R),
  B8G8R8_USCALED(byte.class, 3, Type.USCALED, Channel.B, Channel.G, Channel.R),
  B8G8R8_SSCALED(byte.class, 3, Type.SSCALED, Channel.B, Channel.G, Channel.R),
  B8G8R8_UINT(byte.class, 3, Type.UINT, Channel.B, Channel.G, Channel.R),
  B8G8R8_SINT(byte.class, 3, Type.SINT, Channel.B, Channel.G, Channel.R),
  B8G8R8_SRGB(byte.class, 3, Type.SRGB, Channel.B, Channel.G, Channel.R),
  R8G8B8A8_UNORM(byte.class, 4, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  R8G8B8A8_SNORM(byte.class, 4, Type.SNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  R8G8B8A8_USCALED(byte.class, 4, Type.USCALED, Channel.R, Channel.G, Channel.B, Channel.A),
  R8G8B8A8_SSCALED(byte.class, 4, Type.SSCALED, Channel.R, Channel.G, Channel.B, Channel.A),
  R8G8B8A8_UINT(byte.class, 4, Type.UINT, Channel.R, Channel.G, Channel.B, Channel.A),
  R8G8B8A8_SINT(byte.class, 4, Type.SINT, Channel.R, Channel.G, Channel.B, Channel.A),
  R8G8B8A8_SRGB(byte.class, 4, Type.SRGB, Channel.R, Channel.G, Channel.B, Channel.A),
  B8G8R8A8_UNORM(byte.class, 4, Type.UNORM, Channel.B, Channel.G, Channel.R, Channel.A),
  B8G8R8A8_SNORM(byte.class, 4, Type.SNORM, Channel.B, Channel.G, Channel.R, Channel.A),
  B8G8R8A8_USCALED(byte.class, 4, Type.USCALED, Channel.B, Channel.G, Channel.R, Channel.A),
  B8G8R8A8_SSCALED(byte.class, 4, Type.SSCALED, Channel.B, Channel.G, Channel.R, Channel.A),
  B8G8R8A8_UINT(byte.class, 4, Type.UINT, Channel.B, Channel.G, Channel.R, Channel.A),
  B8G8R8A8_SINT(byte.class, 4, Type.SINT, Channel.B, Channel.G, Channel.R, Channel.A),
  B8G8R8A8_SRGB(byte.class, 4, Type.SRGB, Channel.B, Channel.G, Channel.R, Channel.A),
  A8B8G8R8_UNORM_PACK32(int.class, 1, Type.UNORM, Channel.A, Channel.B, Channel.G, Channel.R),
  A8B8G8R8_SNORM_PACK32(int.class, 1, Type.SNORM, Channel.A, Channel.B, Channel.G, Channel.R),
  A8B8G8R8_USCALED_PACK32(int.class, 1, Type.USCALED, Channel.A, Channel.B, Channel.G, Channel.R),
  A8B8G8R8_SSCALED_PACK32(int.class, 1, Type.SSCALED, Channel.A, Channel.B, Channel.G, Channel.R),
  A8B8G8R8_UINT_PACK32(int.class, 1, Type.UINT, Channel.A, Channel.B, Channel.G, Channel.R),
  A8B8G8R8_SINT_PACK32(int.class, 1, Type.SINT, Channel.A, Channel.B, Channel.G, Channel.R),
  A8B8G8R8_SRGB_PACK32(int.class, 1, Type.SRGB, Channel.A, Channel.B, Channel.G, Channel.R),
  A2R10G10B10_UNORM_PACK32(int.class, 1, Type.UNORM, Channel.A, Channel.R, Channel.G, Channel.B),
  A2R10G10B10_SNORM_PACK32(int.class, 1, Type.SNORM, Channel.A, Channel.R, Channel.G, Channel.B),
  A2R10G10B10_USCALED_PACK32(int.class, 1, Type.USCALED, Channel.A, Channel.R, Channel.G, Channel.B),
  A2R10G10B10_SSCALED_PACK32(int.class, 1, Type.SSCALED, Channel.A, Channel.R, Channel.G, Channel.B),
  A2R10G10B10_UINT_PACK32(int.class, 1, Type.UINT, Channel.A, Channel.R, Channel.G, Channel.B),
  A2R10G10B10_SINT_PACK32(int.class, 1, Type.SINT, Channel.A, Channel.R, Channel.G, Channel.B),
  A2B10G10R10_UNORM_PACK32(int.class, 1, Type.UNORM, Channel.A, Channel.B, Channel.G, Channel.R),
  A2B10G10R10_SNORM_PACK32(int.class, 1, Type.SNORM, Channel.A, Channel.B, Channel.G, Channel.R),
  A2B10G10R10_USCALED_PACK32(int.class, 1, Type.USCALED, Channel.A, Channel.B, Channel.G, Channel.R),
  A2B10G10R10_SSCALED_PACK32(int.class, 1, Type.SSCALED, Channel.A, Channel.B, Channel.G, Channel.R),
  A2B10G10R10_UINT_PACK32(int.class, 1, Type.UINT, Channel.A, Channel.B, Channel.G, Channel.R),
  A2B10G10R10_SINT_PACK32(int.class, 1, Type.SINT, Channel.A, Channel.B, Channel.G, Channel.R),
  R16_UNORM(short.class, 1, Type.UNORM, Channel.R),
  R16_SNORM(short.class, 1, Type.SNORM, Channel.R),
  R16_USCALED(short.class, 1, Type.USCALED, Channel.R),
  R16_SSCALED(short.class, 1, Type.SSCALED, Channel.R),
  R16_UINT(short.class, 1, Type.UINT, Channel.R),
  R16_SINT(short.class, 1, Type.SINT, Channel.R),
  R16_SFLOAT(short.class, 1, Type.SFLOAT, Channel.R),
  R16G16_UNORM(short.class, 2, Type.UINT, Channel.R, Channel.G),
  R16G16_SNORM(short.class, 2, Type.SNORM, Channel.R, Channel.G),
  R16G16_USCALED(short.class, 2, Type.USCALED, Channel.R, Channel.G),
  R16G16_SSCALED(short.class, 2, Type.SSCALED, Channel.R, Channel.G),
  R16G16_UINT(short.class, 2, Type.UINT, Channel.R, Channel.G),
  R16G16_SINT(short.class, 2, Type.SINT, Channel.R, Channel.G),
  R16G16_SFLOAT(short.class, 2, Type.SFLOAT, Channel.R, Channel.G),
  R16G16B16_UNORM(short.class, 3, Type.UNORM, Channel.R, Channel.G, Channel.B),
  R16G16B16_SNORM(short.class, 3, Type.SNORM, Channel.R, Channel.G, Channel.B),
  R16G16B16_USCALED(short.class, 3, Type.USCALED, Channel.R, Channel.G, Channel.B),
  R16G16B16_SSCALED(short.class, 3, Type.SSCALED, Channel.R, Channel.G, Channel.B),
  R16G16B16_UINT(short.class, 3, Type.UINT, Channel.R, Channel.G, Channel.B),
  R16G16B16_SINT(short.class, 3, Type.SINT, Channel.R, Channel.G, Channel.B),
  R16G16B16_SFLOAT(short.class, 3, Type.SFLOAT, Channel.R, Channel.G, Channel.B),
  R16G16B16A16_UNORM(short.class, 4, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  R16G16B16A16_SNORM(short.class, 4, Type.SNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  R16G16B16A16_USCALED(short.class, 4, Type.USCALED, Channel.R, Channel.G, Channel.B, Channel.A),
  R16G16B16A16_SSCALED(short.class, 4, Type.SSCALED, Channel.R, Channel.G, Channel.B, Channel.A),
  R16G16B16A16_UINT(short.class, 4, Type.UINT, Channel.R, Channel.G, Channel.B, Channel.A),
  R16G16B16A16_SINT(short.class, 4, Type.SINT, Channel.R, Channel.G, Channel.B, Channel.A),
  R16G16B16A16_SFLOAT(short.class, 4, Type.SFLOAT, Channel.R, Channel.G, Channel.B, Channel.A),
  R32_UINT(int.class, 1, Type.UINT, Channel.R),
  R32_SINT(int.class, 1, Type.SINT, Channel.R),
  R32_SFLOAT(float.class, 1, Type.SFLOAT, Channel.R),
  R32G32_UINT(int.class, 2, Type.UINT, Channel.R, Channel.G),
  R32G32_SINT(int.class, 2, Type.SINT, Channel.R, Channel.G),
  R32G32_SFLOAT(float.class, 2, Type.SFLOAT, Channel.R, Channel.G),
  R32G32B32_UINT(int.class, 3, Type.UINT, Channel.R, Channel.G, Channel.B),
  R32G32B32_SINT(int.class, 3, Type.SINT, Channel.R, Channel.G, Channel.B),
  R32G32B32_SFLOAT(float.class, 3, Type.SFLOAT, Channel.R, Channel.G, Channel.B),
  R32G32B32A32_UINT(int.class, 4, Type.UINT, Channel.R, Channel.G, Channel.B, Channel.A),
  R32G32B32A32_SINT(int.class, 4, Type.SINT, Channel.R, Channel.G, Channel.B, Channel.A),
  R32G32B32A32_SFLOAT(float.class, 4, Type.SFLOAT, Channel.R, Channel.G, Channel.B, Channel.A),
  R64_UINT(long.class, 1, Type.UINT, Channel.R),
  R64_SINT(long.class, 1, Type.SINT, Channel.R),
  R64_SFLOAT(double.class, 1, Type.SFLOAT, Channel.R),
  R64G64_UINT(long.class, 2, Type.UINT, Channel.R, Channel.G),
  R64G64_SINT(long.class, 2, Type.SINT, Channel.R, Channel.G),
  R64G64_SFLOAT(double.class, 2, Type.SFLOAT, Channel.R, Channel.G),
  R64G64B64_UINT(long.class, 3, Type.UINT, Channel.R, Channel.G, Channel.B),
  R64G64B64_SINT(long.class, 3, Type.SINT, Channel.R, Channel.G, Channel.B),
  R64G64B64_SFLOAT(double.class, 3, Type.SFLOAT, Channel.R, Channel.G, Channel.B),
  R64G64B64A64_UINT(long.class, 4, Type.UINT, Channel.R, Channel.G, Channel.B, Channel.A),
  R64G64B64A64_SINT(long.class, 4, Type.SINT, Channel.R, Channel.G, Channel.B, Channel.A),
  R64G64B64A64_SFLOAT(double.class, 4, Type.SFLOAT, Channel.R, Channel.G, Channel.B, Channel.A),
  B10G11R11_UFLOAT_PACK32(int.class, 1, Type.UFLOAT, Channel.B, Channel.G, Channel.R),
  E5B9G9R9_UFLOAT_PACK32(int.class, 1, Type.UFLOAT, Channel.B, Channel.G, Channel.R), // FIXME how to distinguish exponent, which is not a logical channel but is a field
  D16_UNORM(short.class, 1, Type.UNORM, Channel.D),
  X8_D24_UNORM_PACK32(int.class, 1, Type.UNORM, Channel.X, Channel.D), // FIXME is X really important here?
  D32_SFLOAT(float.class, 1, Type.SFLOAT, Channel.D),
  S8_UINT(byte.class, 1, Type.UINT, Channel.S),
  D16_UNORM_S8_UINT(int.class, 1, Type.UNORM, Channel.D, Channel.S), // FIXME how do I represent these with mixed channel types?
  D24_UNORM_S8_UINT(int.class, 1, Type.UNORM, Channel.D, Channel.S),
  D32_SFLOAT_S8_UINT(float.class, 2, Type.SFLOAT), // FIXME is this actually a 64-bit field with 24 bits of padding?
  BC1_RGB_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B),
  BC1_RGB_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B),
  BC1_RGBA_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  BC1_RGBA_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B, Channel.A),
  BC2_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  BC2_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B, Channel.A),
  BC3_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  BC3_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B, Channel.A),
  BC4_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  BC4_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B, Channel.A),
  BC5_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  BC5_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B, Channel.A),
  BC6H_UFLOAT_BLOCK(byte.class, -1, Type.UFLOAT, Channel.R, Channel.G, Channel.B, Channel.A), // FIXME am I sure about what these channels are? they might be for normals
  BC6H_SFLOAT_BLOCK(byte.class, -1, Type.SFLOAT, Channel.R, Channel.G, Channel.B, Channel.A), // FIXME
  BC7_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A), // FIXME
  BC7_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B, Channel.A), // FIXME
  ETC2_R8G8B8_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B),
  ETC2_R8G8B8_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B),
  ETC2_R8G8B8A1_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  ETC2_R8G8B8A1_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B, Channel.A),
  ETC2_R8G8B8A8_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  ETC2_R8G8B8A8_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B, Channel.A),
  EAC_R11_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R), // FIXME are these compressed, what's with 11 bits of precision?
  EAC_R11_SNORM_BLOCK(byte.class, -1, Type.SNORM, Channel.R),
  EAC_R11G11_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G),
  EAC_R11G11_SNORM_BLOCK(byte.class, -1, Type.SNORM, Channel.R, Channel.G),
  ASTC_4X4_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_4X4_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_5X4_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_5X4_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_5X5_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_5X5_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_6X5_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_6X5_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_6X6_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_6X6_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_8X5_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_8X5_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_8X6_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_8X6_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_8X8_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_8X8_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_10X5_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_10X5_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_10X6_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_10X6_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_10X8_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_10X8_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_10X10_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_10X10_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_12X10_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_12X10_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_12X12_UNORM_BLOCK(byte.class, -1, Type.UNORM, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_12X12_SRGB_BLOCK(byte.class, -1, Type.SRGB, Channel.R, Channel.G, Channel.B, Channel.A);

  public enum Type {
    UINT, SINT, USCALED, SSCALED, UNORM, SNORM, SFLOAT, UFLOAT, SRGB
  }

  public enum Channel {
    R, G, B, A, D, S, X
  }

  private Class<?> javaType;
  private final int dataElements;
  private Channel[] logicalChannels;
  private final Type type;

  GPUFormat(Class<?> javaType, int dataElements, Type type, Channel... logicalChannels) {
    this.logicalChannels = Arrays.copyOf(logicalChannels, logicalChannels.length);
    this.javaType = javaType;
    this.dataElements = dataElements;
    this.type = type;
  }

  public Class<?> getJavaType() {
    return javaType;
  }

  public Type getDataType() {
    return type;
  }

  public int getBitSize() {
    if (javaType.equals(byte.class)) {
      return 8;
    } else if (javaType.equals(short.class)) {
      return 16;
    } else if (javaType.equals(int.class) || javaType.equals(float.class)) {
      return 32;
    } else if (javaType.equals(long.class) || javaType.equals(double.class)) {
      return 64;
    } else {
      return 0;
    }
  }

  public int getDataElementCount() {
    return dataElements;
  }

  public int getLogicalChannelCount() {
    return logicalChannels.length;
  }

  public Channel[] getLogicalChannels() {
    return Arrays.copyOf(logicalChannels, logicalChannels.length);
  }

  public Channel getLogicalChannel(int channel) {
    return logicalChannels[channel];
  }

  public boolean isPacked() {
    return dataElements == 1 && logicalChannels.length > 1;
  }

  public boolean isCompressed() {
    return dataElements < 0;
  }
}
