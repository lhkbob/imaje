package com.lhkbob.imaje.layout;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 */
public enum GPUFormat {
  UNDEFINED(null, null, null),
  R4G4_UNORM_PACK8(byte.class, Type.UNORM, new int[] { 4, 4 }, Channel.R, Channel.G),
  R4G4B4A4_UNORM_PACK16(
      short.class, Type.UNORM, new int[] { 4, 4, 4, 4 }, Channel.R, Channel.G, Channel.B,
      Channel.A),
  B4G4R4A4_UNORM_PACK16(
      short.class, Type.UNORM, new int[] { 4, 4, 4, 4 }, Channel.B, Channel.G, Channel.R,
      Channel.A),
  R5G6B5_UNORM_PACK16(
      short.class, Type.UNORM, new int[] { 5, 6, 5 }, Channel.R, Channel.G, Channel.B),
  B5G6R5_UNORM_PACK16(
      short.class, Type.UNORM, new int[] { 5, 6, 5 }, Channel.B, Channel.G, Channel.R),
  R5G5B5A1_UNORM_PACK16(
      short.class, Type.UNORM, new int[] { 5, 5, 5, 1 }, Channel.R, Channel.G, Channel.B,
      Channel.A),
  B5G5R5A1_UNORM_PACK16(
      short.class, Type.UNORM, new int[] { 5, 5, 5, 1 }, Channel.B, Channel.G, Channel.R,
      Channel.A),
  A1R5G5B5_UNORM_PACK16(
      short.class, Type.UNORM, new int[] { 1, 5, 5, 5 }, Channel.A, Channel.R, Channel.G,
      Channel.B),
  R8_UNORM(byte.class, Type.UNORM, new int[] { 8 }, Channel.R),
  R8_SNORM(byte.class, Type.SNORM, new int[] { 8 }, Channel.R),
  R8_USCALED(byte.class, Type.USCALED, new int[] { 8 }, Channel.R),
  R8_SSCALED(byte.class, Type.SSCALED, new int[] { 8 }, Channel.R),
  R8_UINT(byte.class, Type.UINT, new int[] { 8 }, Channel.R),
  R8_SINT(byte.class, Type.SINT, new int[] { 8 }, Channel.R),
  R8_SRGB(byte.class, Type.SRGB, new int[] { 8 }, Channel.R),
  R8G8_UNORM(byte.class, Type.UNORM, new int[] { 8 }, Channel.R, Channel.G),
  R8G8_SNORM(byte.class, Type.SNORM, new int[] { 8 }, Channel.R, Channel.G),
  R8G8_USCALED(byte.class, Type.USCALED, new int[] { 8 }, Channel.R, Channel.G),
  R8G8_SSCALED(byte.class, Type.SSCALED, new int[] { 8 }, Channel.R, Channel.G),
  R8G8_UINT(byte.class, Type.UINT, new int[] { 8 }, Channel.R, Channel.G),
  R8G8_SINT(byte.class, Type.SINT, new int[] { 8 }, Channel.R, Channel.G),
  R8G8_SRGB(byte.class, Type.SRGB, new int[] { 8 }, Channel.R, Channel.G),
  R8G8B8_UNORM(byte.class, Type.UNORM, new int[] { 8 }, Channel.R, Channel.G, Channel.B),
  R8G8B8_SNORM(byte.class, Type.SNORM, new int[] { 8 }, Channel.R, Channel.G, Channel.B),
  R8G8B8_USCALED(byte.class, Type.USCALED, new int[] { 8 }, Channel.R, Channel.G, Channel.B),
  R8G8B8_SSCALED(byte.class, Type.SSCALED, new int[] { 8 }, Channel.R, Channel.G, Channel.B),
  R8G8B8_UINT(byte.class, Type.UINT, new int[] { 8 }, Channel.R, Channel.G, Channel.B),
  R8G8B8_SINT(byte.class, Type.SINT, new int[] { 8 }, Channel.R, Channel.G, Channel.B),
  R8G8B8_SRGB(byte.class, Type.SRGB, new int[] { 8 }, Channel.R, Channel.G, Channel.B),
  B8G8R8_UNORM(byte.class, Type.UNORM, new int[] { 8 }, Channel.B, Channel.G, Channel.R),
  B8G8R8_SNORM(byte.class, Type.SNORM, new int[] { 8 }, Channel.B, Channel.G, Channel.R),
  B8G8R8_USCALED(byte.class, Type.USCALED, new int[] { 8 }, Channel.B, Channel.G, Channel.R),
  B8G8R8_SSCALED(byte.class, Type.SSCALED, new int[] { 8 }, Channel.B, Channel.G, Channel.R),
  B8G8R8_UINT(byte.class, Type.UINT, new int[] { 8 }, Channel.B, Channel.G, Channel.R),
  B8G8R8_SINT(byte.class, Type.SINT, new int[] { 8 }, Channel.B, Channel.G, Channel.R),
  B8G8R8_SRGB(byte.class, Type.SRGB, new int[] { 8 }, Channel.B, Channel.G, Channel.R),
  R8G8B8A8_UNORM(
      byte.class, Type.UNORM, new int[] { 8 }, Channel.R, Channel.G, Channel.B, Channel.A),
  R8G8B8A8_SNORM(
      byte.class, Type.SNORM, new int[] { 8 }, Channel.R, Channel.G, Channel.B, Channel.A),
  R8G8B8A8_USCALED(
      byte.class, Type.USCALED, new int[] { 8 }, Channel.R, Channel.G, Channel.B, Channel.A),
  R8G8B8A8_SSCALED(
      byte.class, Type.SSCALED, new int[] { 8 }, Channel.R, Channel.G, Channel.B, Channel.A),
  R8G8B8A8_UINT(byte.class, Type.UINT, new int[] { 8 }, Channel.R, Channel.G, Channel.B, Channel.A),
  R8G8B8A8_SINT(byte.class, Type.SINT, new int[] { 8 }, Channel.R, Channel.G, Channel.B, Channel.A),
  R8G8B8A8_SRGB(byte.class, Type.SRGB, new int[] { 8 }, Channel.R, Channel.G, Channel.B, Channel.A),
  B8G8R8A8_UNORM(
      byte.class, Type.UNORM, new int[] { 8 }, Channel.B, Channel.G, Channel.R, Channel.A),
  B8G8R8A8_SNORM(
      byte.class, Type.SNORM, new int[] { 8 }, Channel.B, Channel.G, Channel.R, Channel.A),
  B8G8R8A8_USCALED(
      byte.class, Type.USCALED, new int[] { 8 }, Channel.B, Channel.G, Channel.R, Channel.A),
  B8G8R8A8_SSCALED(
      byte.class, Type.SSCALED, new int[] { 8 }, Channel.B, Channel.G, Channel.R, Channel.A),
  B8G8R8A8_UINT(byte.class, Type.UINT, new int[] { 8 }, Channel.B, Channel.G, Channel.R, Channel.A),
  B8G8R8A8_SINT(byte.class, Type.SINT, new int[] { 8 }, Channel.B, Channel.G, Channel.R, Channel.A),
  B8G8R8A8_SRGB(byte.class, Type.SRGB, new int[] { 8 }, Channel.B, Channel.G, Channel.R, Channel.A),
  A8B8G8R8_UNORM_PACK32(
      int.class, Type.UNORM, new int[] { 8, 8, 8, 8 }, Channel.A, Channel.B, Channel.G, Channel.R),
  A8B8G8R8_SNORM_PACK32(
      int.class, Type.SNORM, new int[] { 8, 8, 8, 8 }, Channel.A, Channel.B, Channel.G, Channel.R),
  A8B8G8R8_USCALED_PACK32(
      int.class, Type.USCALED, new int[] { 8, 8, 8, 8 }, Channel.A, Channel.B, Channel.G,
      Channel.R),
  A8B8G8R8_SSCALED_PACK32(
      int.class, Type.SSCALED, new int[] { 8, 8, 8, 8 }, Channel.A, Channel.B, Channel.G,
      Channel.R),
  A8B8G8R8_UINT_PACK32(
      int.class, Type.UINT, new int[] { 8, 8, 8, 8 }, Channel.A, Channel.B, Channel.G, Channel.R),
  A8B8G8R8_SINT_PACK32(
      int.class, Type.SINT, new int[] { 8, 8, 8, 8 }, Channel.A, Channel.B, Channel.G, Channel.R),
  A8B8G8R8_SRGB_PACK32(
      int.class, Type.SRGB, new int[] { 8, 8, 8, 8 }, Channel.A, Channel.B, Channel.G, Channel.R),
  A2R10G10B10_UNORM_PACK32(
      int.class, Type.UNORM, new int[] { 2, 10, 10, 10 }, Channel.A, Channel.R, Channel.G,
      Channel.B),
  A2R10G10B10_SNORM_PACK32(
      int.class, Type.SNORM, new int[] { 2, 10, 10, 10 }, Channel.A, Channel.R, Channel.G,
      Channel.B),
  A2R10G10B10_USCALED_PACK32(
      int.class, Type.USCALED, new int[] { 2, 10, 10, 10 }, Channel.A, Channel.R, Channel.G,
      Channel.B),
  A2R10G10B10_SSCALED_PACK32(
      int.class, Type.SSCALED, new int[] { 2, 10, 10, 10 }, Channel.A, Channel.R, Channel.G,
      Channel.B),
  A2R10G10B10_UINT_PACK32(
      int.class, Type.UINT, new int[] { 2, 10, 10, 10 }, Channel.A, Channel.R, Channel.G,
      Channel.B),
  A2R10G10B10_SINT_PACK32(
      int.class, Type.SINT, new int[] { 2, 10, 10, 10 }, Channel.A, Channel.R, Channel.G,
      Channel.B),
  A2B10G10R10_UNORM_PACK32(
      int.class, Type.UNORM, new int[] { 2, 10, 10, 10 }, Channel.A, Channel.B, Channel.G,
      Channel.R),
  A2B10G10R10_SNORM_PACK32(
      int.class, Type.SNORM, new int[] { 2, 10, 10, 10 }, Channel.A, Channel.B, Channel.G,
      Channel.R),
  A2B10G10R10_USCALED_PACK32(
      int.class, Type.USCALED, new int[] { 2, 10, 10, 10 }, Channel.A, Channel.B, Channel.G,
      Channel.R),
  A2B10G10R10_SSCALED_PACK32(
      int.class, Type.SSCALED, new int[] { 2, 10, 10, 10 }, Channel.A, Channel.B, Channel.G,
      Channel.R),
  A2B10G10R10_UINT_PACK32(
      int.class, Type.UINT, new int[] { 2, 10, 10, 10 }, Channel.A, Channel.B, Channel.G,
      Channel.R),
  A2B10G10R10_SINT_PACK32(
      int.class, Type.SINT, new int[] { 2, 10, 10, 10 }, Channel.A, Channel.B, Channel.G,
      Channel.R),
  R16_UNORM(short.class, Type.UNORM, new int[] { 16 }, Channel.R),
  R16_SNORM(short.class, Type.SNORM, new int[] { 16 }, Channel.R),
  R16_USCALED(short.class, Type.USCALED, new int[] { 16 }, Channel.R),
  R16_SSCALED(short.class, Type.SSCALED, new int[] { 16 }, Channel.R),
  R16_UINT(short.class, Type.UINT, new int[] { 16 }, Channel.R),
  R16_SINT(short.class, Type.SINT, new int[] { 16 }, Channel.R),
  R16_SFLOAT(short.class, Type.SFLOAT, new int[] { 16 }, Channel.R),
  R16G16_UNORM(short.class, Type.UINT, new int[] { 16 }, Channel.R, Channel.G),
  R16G16_SNORM(short.class, Type.SNORM, new int[] { 16 }, Channel.R, Channel.G),
  R16G16_USCALED(short.class, Type.USCALED, new int[] { 16 }, Channel.R, Channel.G),
  R16G16_SSCALED(short.class, Type.SSCALED, new int[] { 16 }, Channel.R, Channel.G),
  R16G16_UINT(short.class, Type.UINT, new int[] { 16 }, Channel.R, Channel.G),
  R16G16_SINT(short.class, Type.SINT, new int[] { 16 }, Channel.R, Channel.G),
  R16G16_SFLOAT(short.class, Type.SFLOAT, new int[] { 16 }, Channel.R, Channel.G),
  R16G16B16_UNORM(short.class, Type.UNORM, new int[] { 16 }, Channel.R, Channel.G, Channel.B),
  R16G16B16_SNORM(short.class, Type.SNORM, new int[] { 16 }, Channel.R, Channel.G, Channel.B),
  R16G16B16_USCALED(short.class, Type.USCALED, new int[] { 16 }, Channel.R, Channel.G, Channel.B),
  R16G16B16_SSCALED(short.class, Type.SSCALED, new int[] { 16 }, Channel.R, Channel.G, Channel.B),
  R16G16B16_UINT(short.class, Type.UINT, new int[] { 16 }, Channel.R, Channel.G, Channel.B),
  R16G16B16_SINT(short.class, Type.SINT, new int[] { 16 }, Channel.R, Channel.G, Channel.B),
  R16G16B16_SFLOAT(short.class, Type.SFLOAT, new int[] { 16 }, Channel.R, Channel.G, Channel.B),
  R16G16B16A16_UNORM(
      short.class, Type.UNORM, new int[] { 16 }, Channel.R, Channel.G, Channel.B, Channel.A),
  R16G16B16A16_SNORM(
      short.class, Type.SNORM, new int[] { 16 }, Channel.R, Channel.G, Channel.B, Channel.A),
  R16G16B16A16_USCALED(
      short.class, Type.USCALED, new int[] { 16 }, Channel.R, Channel.G, Channel.B, Channel.A),
  R16G16B16A16_SSCALED(
      short.class, Type.SSCALED, new int[] { 16 }, Channel.R, Channel.G, Channel.B, Channel.A),
  R16G16B16A16_UINT(
      short.class, Type.UINT, new int[] { 16 }, Channel.R, Channel.G, Channel.B, Channel.A),
  R16G16B16A16_SINT(
      short.class, Type.SINT, new int[] { 16 }, Channel.R, Channel.G, Channel.B, Channel.A),
  R16G16B16A16_SFLOAT(
      short.class, Type.SFLOAT, new int[] { 16 }, Channel.R, Channel.G, Channel.B, Channel.A),
  R32_UINT(int.class, Type.UINT, new int[] { 32 }, Channel.R),
  R32_SINT(int.class, Type.SINT, new int[] { 32 }, Channel.R),
  R32_SFLOAT(float.class, Type.SFLOAT, new int[] { 32 }, Channel.R),
  R32G32_UINT(int.class, Type.UINT, new int[] { 32 }, Channel.R, Channel.G),
  R32G32_SINT(int.class, Type.SINT, new int[] { 32 }, Channel.R, Channel.G),
  R32G32_SFLOAT(float.class, Type.SFLOAT, new int[] { 32 }, Channel.R, Channel.G),
  R32G32B32_UINT(int.class, Type.UINT, new int[] { 32 }, Channel.R, Channel.G, Channel.B),
  R32G32B32_SINT(int.class, Type.SINT, new int[] { 32 }, Channel.R, Channel.G, Channel.B),
  R32G32B32_SFLOAT(float.class, Type.SFLOAT, new int[] { 32 }, Channel.R, Channel.G, Channel.B),
  R32G32B32A32_UINT(
      int.class, Type.UINT, new int[] { 32 }, Channel.R, Channel.G, Channel.B, Channel.A),
  R32G32B32A32_SINT(
      int.class, Type.SINT, new int[] { 32 }, Channel.R, Channel.G, Channel.B, Channel.A),
  R32G32B32A32_SFLOAT(
      float.class, Type.SFLOAT, new int[] { 32 }, Channel.R, Channel.G, Channel.B, Channel.A),
  R64_UINT(long.class, Type.UINT, new int[] { 64 }, Channel.R),
  R64_SINT(long.class, Type.SINT, new int[] { 64 }, Channel.R),
  R64_SFLOAT(double.class, Type.SFLOAT, new int[] { 64 }, Channel.R),
  R64G64_UINT(long.class, Type.UINT, new int[] { 64 }, Channel.R, Channel.G),
  R64G64_SINT(long.class, Type.SINT, new int[] { 64 }, Channel.R, Channel.G),
  R64G64_SFLOAT(double.class, Type.SFLOAT, new int[] { 64 }, Channel.R, Channel.G),
  R64G64B64_UINT(long.class, Type.UINT, new int[] { 64 }, Channel.R, Channel.G, Channel.B),
  R64G64B64_SINT(long.class, Type.SINT, new int[] { 64 }, Channel.R, Channel.G, Channel.B),
  R64G64B64_SFLOAT(double.class, Type.SFLOAT, new int[] { 64 }, Channel.R, Channel.G, Channel.B),
  R64G64B64A64_UINT(
      long.class, Type.UINT, new int[] { 64 }, Channel.R, Channel.G, Channel.B, Channel.A),
  R64G64B64A64_SINT(
      long.class, Type.SINT, new int[] { 64 }, Channel.R, Channel.G, Channel.B, Channel.A),
  R64G64B64A64_SFLOAT(
      double.class, Type.SFLOAT, new int[] { 64 }, Channel.R, Channel.G, Channel.B, Channel.A),
  B10G11R11_UFLOAT_PACK32(
      int.class, Type.UFLOAT, new int[] { 10, 11, 11 }, Channel.B, Channel.G, Channel.R),
  E5B9G9R9_UFLOAT_PACK32(
      int.class, Type.UFLOAT, new int[] { 5, 9, 9, 9 }, Channel.E, Channel.B, Channel.G, Channel.R),
  D16_UNORM(short.class, Type.UNORM, new int[] { 16 }, Channel.D),
  X8_D24_UNORM_PACK32(int.class, Type.UNORM, new int[] { 8, 24 }, Channel.X, Channel.D),
  D32_SFLOAT(float.class, Type.SFLOAT, new int[] { 32 }, Channel.D),
  S8_UINT(byte.class, Type.UINT, new int[] { 8 }, Channel.S),
  D16_UNORM_S8_UINT(int.class, Type.UNORM, new int[] { 16, 8, 8 }, Channel.D, Channel.S, Channel.X),
  D24_UNORM_S8_UINT(int.class, Type.UNORM, new int[] { 24, 8 }, Channel.D, Channel.S),
  D32_SFLOAT_S8_UINT(
      float.class, Type.SFLOAT, new int[] { 32, 8, 24 }, Channel.D, Channel.S, Channel.X),
  BC1_RGB_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B),
  BC1_RGB_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B),
  BC1_RGBA_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B, Channel.A),
  BC1_RGBA_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B, Channel.A),
  BC2_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B, Channel.A),
  BC2_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B, Channel.A),
  BC3_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B, Channel.A),
  BC3_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B, Channel.A),
  BC4_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B, Channel.A),
  BC4_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B, Channel.A),
  BC5_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B, Channel.A),
  BC5_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B, Channel.A),
  BC6H_UFLOAT_BLOCK(byte.class, Type.UFLOAT, null, Channel.R, Channel.G, Channel.B, Channel.A),
  // FIXME am I sure about what these channels are? they might be for normals and do I care then?
  BC6H_SFLOAT_BLOCK(byte.class, Type.SFLOAT, null, Channel.R, Channel.G, Channel.B, Channel.A),
  BC7_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B, Channel.A),
  BC7_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ETC2_R8G8B8_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B),
  ETC2_R8G8B8_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B),
  ETC2_R8G8B8A1_UNORM_BLOCK(
      byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ETC2_R8G8B8A1_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ETC2_R8G8B8A8_UNORM_BLOCK(
      byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ETC2_R8G8B8A8_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B, Channel.A),
  EAC_R11_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R),
  // FIXME are these compressed, what's with 11 bits of precision?
  EAC_R11_SNORM_BLOCK(byte.class, Type.SNORM, null, Channel.R),
  EAC_R11G11_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R, Channel.G),
  EAC_R11G11_SNORM_BLOCK(byte.class, Type.SNORM, null, Channel.R, Channel.G),
  ASTC_4X4_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_4X4_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_5X4_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_5X4_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_5X5_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_5X5_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_6X5_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_6X5_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_6X6_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_6X6_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_8X5_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_8X5_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_8X6_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_8X6_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_8X8_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_8X8_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_10X5_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_10X5_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_10X6_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_10X6_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_10X8_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_10X8_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_10X10_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_10X10_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_12X10_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_12X10_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_12X12_UNORM_BLOCK(byte.class, Type.UNORM, null, Channel.R, Channel.G, Channel.B, Channel.A),
  ASTC_12X12_SRGB_BLOCK(byte.class, Type.SRGB, null, Channel.R, Channel.G, Channel.B, Channel.A);

  public enum Type {
    UINT, SINT, USCALED, SSCALED, UNORM, SNORM, SFLOAT, UFLOAT, SRGB
  }

  public enum Channel {
    R, G, B, A, D, S, E, X
  }

  private final Class<?> javaType;
  private final int[] dataBitAllocation;
  private final Channel[] logicalChannels;
  private final Type type;

  GPUFormat(Class<?> javaType, Type type, int[] dataBitAllocation, Channel... logicalChannels) {
    // Validate the data bit allocation assuming it's not a compressed format
    if (dataBitAllocation != null) {
      if (dataBitAllocation.length == 1) {
        // Not packed, so the single value must equal the bit size of the primitive type
        if (dataBitAllocation[0] != getBitSize(javaType)) {
          throw new RuntimeException(
              "Bad GPUFormat definition, unpacked type bit total different from primitive type");
        }
      } else {
        // Packed, so the length must equal that of logicalChannels and its total must equal the
        // bit size of the primitive type
        if (dataBitAllocation.length != logicalChannels.length) {
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
      }
    }

    this.logicalChannels = Arrays.copyOf(logicalChannels, logicalChannels.length);
    this.javaType = javaType;
    if (dataBitAllocation != null) {
      this.dataBitAllocation = Arrays.copyOf(dataBitAllocation, dataBitAllocation.length);
    } else {
      this.dataBitAllocation = null;
    }
    this.type = type;
  }

  public Class<?> getJavaType() {
    return javaType;
  }

  public Type getDataType() {
    return type;
  }

  public int getBitSize() {
    return getBitSize(javaType);
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
      throw new RuntimeException("Bad GPUFormat definition, unexpected java primitive type: " + javaType);
    }
  }

  public int getDataElementCount() {
    if (isCompressed()) {
      return -1;
    } else if (isPacked()) {
      return 1;
    } else {
      return logicalChannels.length;
    }
  }

  // FIXME remove logical from names
  public int getLogicalChannelCount() {
    return logicalChannels.length;
  }

  public Channel[] getLogicalChannels() {
    return Arrays.copyOf(logicalChannels, logicalChannels.length);
  }

  public int[] getChannelBitAllocations() {
    if (dataBitAllocation == null) {
      return null;
    } else {
      return Arrays.copyOf(dataBitAllocation, dataBitAllocation.length);
    }
  }

  public int getChannelBits(int channel) {
    if (dataBitAllocation == null) {
      return 0;
    } else if (dataBitAllocation.length == 1) {
      return dataBitAllocation[0]; // Also equal to getBitSize()
    } else {
      return dataBitAllocation[channel];
    }
  }

  // FIXME rename to getChannelSemantic
  public Channel getLogicalChannel(int channel) {
    return logicalChannels[channel];
  }

  public boolean isPacked() {
    return dataBitAllocation != null && dataBitAllocation.length > 1;
  }

  public boolean isCompressed() {
    return dataBitAllocation == null;
  }

  public static Stream<GPUFormat> streamAll() {
    return Arrays.stream(GPUFormat.values());
  }

  public static Predicate<GPUFormat> channelLayout(GPUFormat.Channel... channels) {
    return format -> isChannelLayout(format, channels);
  }

  public static boolean isChannelLayout(GPUFormat format, GPUFormat.Channel... channels) {
    if (format.getLogicalChannelCount() != channels.length)
      return false;
    for (int i = 0; i < channels.length; i++) {
      if (format.getLogicalChannel(i) != channels[i])
        return false;
    }
    return true;
  }

  public static Predicate<GPUFormat> packedLayout(int... bits) {
    return format -> isPackedLayout(format, bits);
  }

  public static boolean isPackedLayout(GPUFormat format, int... bits) {
    if (!format.isPacked())
      return false;
    if (format.getLogicalChannelCount() != bits.length)
      return false;
    for (int i = 0; i < bits.length; i++) {
      if (format.getChannelBits(i) != bits[i])
        return false;
    }
    return true;
  }

  public static boolean isUnpackedLayout(GPUFormat format) {
    return !format.isPacked() && !format.isCompressed();
  }

  public static boolean isDataType(GPUFormat format, GPUFormat.Type type) {
    return format.getDataType() == type;
  }

  public static Predicate<GPUFormat> dataType(GPUFormat.Type type) {
    return format -> isDataType(format, type);
  }

  public static boolean isBitSize(GPUFormat format, int bitSize) {
    return format.getBitSize() == bitSize;
  }

  public static Predicate<GPUFormat> bitSize(int bitSize) {
    return format -> isBitSize(format, bitSize);
  }
}
