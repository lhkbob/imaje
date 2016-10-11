package com.lhkbob.imaje.io.dds;

import com.lhkbob.imaje.color.Alpha;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.Depth;
import com.lhkbob.imaje.color.DepthStencil;
import com.lhkbob.imaje.color.Generic;
import com.lhkbob.imaje.color.Luminance;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.SRGB;
import com.lhkbob.imaje.color.YCbCr;
import com.lhkbob.imaje.color.YUV;
import com.lhkbob.imaje.gpu.GPUFormat;
import com.lhkbob.imaje.layout.PixelFormat;
import com.lhkbob.imaje.layout.PixelFormatBuilder;

/**
 *
 */ /*
 * DXGI_FORMAT enumeration: https://msdn.microsoft.com/en-us/library/bb173059(v=vs.85).aspx Enum
 * values are ordered such that ordinal() corresponds to defined value from above spec. This maps
 * the DDS format to the Vulkan-based GPUFormat where possible, or to the more flexible
 * PixelFormat class and an associated Color type.
 *
 * Note that for non-byte aligned formats, DXGI lists least-significant bit components first,
 * which is why the component order is reversed from corresponding GPUFormat names.
 */
public enum DXGIFormat {
  UNKNOWN(null), R32G32B32A32_TYPELESS(RGB.Linear.class),
  R32G32B32A32_FLOAT(RGB.Linear.class, GPUFormat.R32G32B32A32_SFLOAT),
  R32G32B32A32_UINT(RGB.Linear.class, GPUFormat.R32G32B32A32_UINT),
  R32G32B32A32_SINT(RGB.Linear.class, GPUFormat.R32G32B32A32_SINT),
  R32G32B32_TYPELESS(RGB.Linear.class),
  R32G32B32_FLOAT(RGB.Linear.class, GPUFormat.R32G32B32_SFLOAT),
  R32G32B32_UINT(RGB.Linear.class, GPUFormat.R32G32B32_UINT),
  R32G32B32_SINT(RGB.Linear.class, GPUFormat.R32G32B32_SINT),
  R16G16B16A16_TYPELESS(RGB.Linear.class),
  R16G16B16A16_FLOAT(RGB.Linear.class, GPUFormat.R16G16B16A16_SFLOAT),
  R16G16B16A16_UNORM(RGB.Linear.class, GPUFormat.R16G16B16A16_UNORM),
  R16G16B16A16_UINT(RGB.Linear.class, GPUFormat.R16G16B16A16_UINT),
  R16G16B16A16_SNORM(RGB.Linear.class, GPUFormat.R16G16B16A16_SNORM),
  R16G16B16A16_SINT(RGB.Linear.class, GPUFormat.R16G16B16A16_SINT), R32G32_TYPELESS(null),
  R32G32_FLOAT(Generic.C2.class, GPUFormat.R32G32_SFLOAT),
  R32G32_UINT(Generic.C2.class, GPUFormat.R32G32_UINT),
  R32G32_SINT(Generic.C2.class, GPUFormat.R32G32_SINT), R32G8X24_TYPELESS(null),
  D32_FLOAT_S8X24_UINT(DepthStencil.class, GPUFormat.D32_SFLOAT_S8_UINT),
  R32_FLOAT_X8X24_TYPELESS(null), X32_TYPELESS_G8X24_UINT(null),
  R10G10B10A2_TYPELESS(RGB.Linear.class),
  R10G10B10A2_UNORM(RGB.Linear.class, GPUFormat.A2B10G10R10_UNORM_PACK32),
  R10G10B10A2_UINT(RGB.Linear.class, GPUFormat.A2B10G10R10_UINT_PACK32),
  R11G11B10_FLOAT(RGB.Linear.class, GPUFormat.B10G11R11_UFLOAT_PACK32),
  R8G8B8A8_UNORM(RGB.Linear.class, GPUFormat.R8G8B8A8_UNORM),
  R8G8B8A8_UNORM_SRGB(SRGB.class, GPUFormat.R8G8B8A8_SRGB),
  R8G8B8A8_UINT(RGB.Linear.class, GPUFormat.R8G8B8A8_UINT),
  R8G8B8A8_SNORM(RGB.Linear.class, GPUFormat.R8G8B8A8_SNORM),
  R8G8B8A8_SINT(RGB.Linear.class, GPUFormat.R8G8B8A8_SINT), R16G16_TYPELESS(null),
  R16G16_FLOAT(Generic.C2.class, GPUFormat.R16G16_SFLOAT),
  R16G16_UNORM(Generic.C2.class, GPUFormat.R16G16_UNORM),
  R16G16_UINT(Generic.C2.class, GPUFormat.R16G16_UINT),
  R16G16_SNORM(Generic.C2.class, GPUFormat.R16G16_SNORM),
  R16G16_SINT(Generic.C2.class, GPUFormat.R16G16_SINT), R32_TYPELESS(Luminance.class),
  D32_FLOAT(Depth.class, GPUFormat.D32_SFLOAT), R32_FLOAT(Luminance.class, GPUFormat.R32_SFLOAT),
  R32_UINT(Luminance.class, GPUFormat.R32_UINT), R32_SINT(Luminance.class, GPUFormat.R32_SINT),
  R24G8_TYPELESS(null), D24_UNORM_S8_UINT(DepthStencil.class, GPUFormat.D24_UNORM_S8_UINT),
  R24_UNORM_X8_TYPELESS(null), X24_TYPELESS_G8_UINT(null), R8G8_TYPELESS(null),
  R8G8_UNORM(Generic.C2.class, GPUFormat.R8G8_UNORM),
  R8G8_UINT(Generic.C2.class, GPUFormat.R8G8_UINT),
  R8G8_SNORM(Generic.C2.class, GPUFormat.R8G8_SNORM),
  R8G8_SINT(Generic.C2.class, GPUFormat.R8G8_SINT), R16_TYPELESS(Luminance.class),
  R16_FLOAT(Luminance.class, GPUFormat.R16_SFLOAT), D16_UNORM(Depth.class, GPUFormat.D16_UNORM),
  R16_UNORM(Luminance.class, GPUFormat.R16_UNORM), R16_UINT(Luminance.class, GPUFormat.R16_UINT),
  R16_SNORM(Luminance.class, GPUFormat.R16_SNORM), R16_SINT(Luminance.class, GPUFormat.R16_SINT),
  R8_TYPELESS(null), R8_UNORM(Luminance.class, GPUFormat.R8_UNORM),
  R8_UINT(Luminance.class, GPUFormat.R8_UINT), R8_SNORM(Luminance.class, GPUFormat.R8_SINT),
  R8_SINT(Luminance.class, GPUFormat.R8_SINT), A8_UNORM(Alpha.class, GPUFormat.R8_UNORM),
  R1_UNORM(null), R9G9B9E5_SHAREDEXP(RGB.Linear.class, GPUFormat.E5B9G9R9_UFLOAT_PACK32),
  R8G8_B8G8_UNORM(null), G8R8_G8B8_UNORM(null), BC1_TYPELESS(null),
  BC1_UNORM(RGB.Linear.class, GPUFormat.BC1_RGB_UNORM_BLOCK),
  BC1_UNORM_SRGB(SRGB.class, GPUFormat.BC1_RGB_SRGB_BLOCK), BC2_TYPELESS(null),
  BC2_UNORM(RGB.Linear.class, GPUFormat.BC2_UNORM_BLOCK),
  BC2_UNORM_SRGB(SRGB.class, GPUFormat.BC2_SRGB_BLOCK), BC3_TYPELESS(null),
  BC3_UNORM(RGB.Linear.class, GPUFormat.BC3_UNORM_BLOCK),
  BC3_UNORM_SRGB(SRGB.class, GPUFormat.BC3_SRGB_BLOCK), BC4_TYPELESS(null),
  BC4_UNORM(Luminance.class, GPUFormat.BC4_UNORM_BLOCK), BC4_SNORM(null), BC5_TYPELESS(null),
  BC5_UNORM(Generic.C2.class, GPUFormat.BC5_UNORM_BLOCK), BC5_SNORM(null),
  B5G6R5_UNORM(RGB.Linear.class, GPUFormat.B5G6R5_UNORM_PACK16),
  B5G5R5A1_UNORM(RGB.Linear.class, GPUFormat.B5G5R5A1_UNORM_PACK16),
  B8G8R8A8_UNORM(RGB.Linear.class, GPUFormat.B8G8R8A8_UNORM), B8G8R8X8_UNORM(
      RGB.Linear.class, new PixelFormatBuilder().channels(2, 1, 0, PixelFormat.SKIP_CHANNEL).bits(8)
      .types(PixelFormat.Type.UNORM).build(), false),
  R10G10B10_XR_BIAS_A2_UNORM(RGB.Linear.class, null), B8G8R8A8_TYPELESS(RGB.Linear.class, null),
  B8G8R8A8_UNORM_SRGB(SRGB.class, GPUFormat.B8G8R8A8_SRGB),
  B8G8R8X8_TYPELESS(RGB.Linear.class, null), B8G8R8X8_UNORM_SRGB(
      SRGB.class, new PixelFormatBuilder().channels(2, 1, 0, PixelFormat.SKIP_CHANNEL).bits(8)
      .types(PixelFormat.Type.UNORM).build(), false), BC6H_TYPELESS(null),
  BC6H_UF16(null, GPUFormat.BC6H_UFLOAT_BLOCK), BC6H_SF16(null, GPUFormat.BC6H_SFLOAT_BLOCK),
  BC7_TYPELESS(null), BC7_UNORM(null, GPUFormat.BC7_UNORM_BLOCK),
  BC7_UNORM_SRGB(null, GPUFormat.BC7_SRGB_BLOCK),
  // FIXME See https://msdn.microsoft.com/en-us/library/dd206750(v=vs.85).aspx
  // Will require implementing different chroma vs intensity resolution sample rates
  AYUV(YUV.class, null), Y410(YUV.class, null), Y416(YUV.class, null), NV12(YUV.class, null),
  P010(YUV.class, null), P016(YUV.class, null), _420_OPAQUE(YUV.class, null), YUY2(YUV.class, null),
  Y216(YUV.class, null), NV11(YUV.class, null), AI44(YUV.class, null), IA44(YUV.class, null),
  P8(YUV.class, null), A8P8(YUV.class, null),
  B4G4R4A4_UNORM(RGB.Linear.class, GPUFormat.B4G4R4A4_UNORM_PACK16), P208(YUV.class, null),
  V208(YCbCr.class, null), V408(YCbCr.class, null);

  private final PixelFormat format;
  private final boolean packed;
  private final Class<? extends Color> color;

  DXGIFormat(Class<? extends Color> color) {
    this(color, null, false);
  }

  DXGIFormat(Class<? extends Color> color, GPUFormat format) {
    this(color, format.getPixelFormat(), format.isPacked());
  }

  DXGIFormat(Class<? extends Color> color, PixelFormat format, boolean packed) {
    this.color = color;
    this.format = format;
    this.packed = packed;
  }

  public Class<? extends Color> getColorType() {
    return color;
  }

  public PixelFormat getFormat() {
    return format;
  }

  public boolean isPacked() {
    return packed;
  }

  public boolean isSupported() {
    return color != null && format != null;
  }
}
