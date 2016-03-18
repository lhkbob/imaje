package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.data.DataSource;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 */
public final class GPUFormats {
  private GPUFormats() {}

  public static Stream<GPUFormat> streamAll() {
    return Arrays.stream(GPUFormat.values());
  }

  public static boolean isRGB(GPUFormat format) {
    return format.getLogicalChannelCount() == 3
        && format.getLogicalChannel(0) == GPUFormat.Channel.R
        && format.getLogicalChannel(1) == GPUFormat.Channel.G
        && format.getLogicalChannel(2) == GPUFormat.Channel.B;
  }

  public static boolean isRGBA(GPUFormat format) {
    return format.getLogicalChannelCount() == 4
        && format.getLogicalChannel(0) == GPUFormat.Channel.R
        && format.getLogicalChannel(1) == GPUFormat.Channel.G
        && format.getLogicalChannel(2) == GPUFormat.Channel.B
        && format.getLogicalChannel(3) == GPUFormat.Channel.A;
  }

  public static boolean isARGB(GPUFormat format) {
    return format.getLogicalChannelCount() == 4
        && format.getLogicalChannel(0) == GPUFormat.Channel.A
        && format.getLogicalChannel(1) == GPUFormat.Channel.R
        && format.getLogicalChannel(2) == GPUFormat.Channel.G
        && format.getLogicalChannel(3) == GPUFormat.Channel.B;
  }

  public static boolean isBGR(GPUFormat format) {
    return format.getLogicalChannelCount() == 3
        && format.getLogicalChannel(0) == GPUFormat.Channel.B
        && format.getLogicalChannel(1) == GPUFormat.Channel.G
        && format.getLogicalChannel(2) == GPUFormat.Channel.R;
  }

  public static boolean isBGRA(GPUFormat format) {
    return format.getLogicalChannelCount() == 4
        && format.getLogicalChannel(0) == GPUFormat.Channel.B
        && format.getLogicalChannel(1) == GPUFormat.Channel.G
        && format.getLogicalChannel(2) == GPUFormat.Channel.R
        && format.getLogicalChannel(3) == GPUFormat.Channel.A;
  }

  public static boolean isABGR(GPUFormat format) {
    return format.getLogicalChannelCount() == 4
        && format.getLogicalChannel(0) == GPUFormat.Channel.A
        && format.getLogicalChannel(1) == GPUFormat.Channel.B
        && format.getLogicalChannel(2) == GPUFormat.Channel.G
        && format.getLogicalChannel(3) == GPUFormat.Channel.R;
  }

  public static boolean isR(GPUFormat format) {
    return format.getLogicalChannelCount() == 1
        && format.getLogicalChannel(0) == GPUFormat.Channel.R;
  }

  public static boolean isRG(GPUFormat format) {
    return format.getLogicalChannelCount() == 2
        && format.getLogicalChannel(0) == GPUFormat.Channel.R
        && format.getLogicalChannel(1) == GPUFormat.Channel.G;
  }

  public static boolean isDepth(GPUFormat format) {
    return format.getLogicalChannelCount() == 1
        && format.getLogicalChannel(0) == GPUFormat.Channel.D;
  }

  public static boolean isDepthStencil(GPUFormat format) {
    return format.getLogicalChannelCount() == 2
        && format.getLogicalChannel(0) == GPUFormat.Channel.D
        && format.getLogicalChannel(1) == GPUFormat.Channel.S;
  }

  public static boolean isUInt(GPUFormat format) {
    return format.getDataType() == GPUFormat.Type.UINT;
  }

  public static boolean isUNorm(GPUFormat format) {
    return format.getDataType() == GPUFormat.Type.UNORM;
  }

  public static boolean isUScaled(GPUFormat format) {
    return format.getDataType() == GPUFormat.Type.USCALED;
  }

  public static boolean isUFloat(GPUFormat format) {
    return format.getDataType() == GPUFormat.Type.UFLOAT;
  }

  public static boolean isSInt(GPUFormat format) {
    return format.getDataType() == GPUFormat.Type.SINT;
  }

  public static boolean isSNorm(GPUFormat format) {
    return format.getDataType() == GPUFormat.Type.SNORM;
  }

  public static boolean isSScaled(GPUFormat format) {
    return format.getDataType() == GPUFormat.Type.SSCALED;
  }

  public static boolean isSFloat(GPUFormat format) {
    return format.getDataType() == GPUFormat.Type.SFLOAT;
  }

  public static boolean isSRGB(GPUFormat format) {
    return format.getDataType() == GPUFormat.Type.SRGB;
  }

  public static boolean isCompressed(GPUFormat format) {
    return format.isCompressed();
  }

  public static boolean isPacked(GPUFormat format) {
    return format.isPacked();
  }

  public static boolean is8Bit(GPUFormat format) {
    return format.getJavaType().equals(byte.class);
  }

  public static boolean is16Bit(GPUFormat format) {
    return format.getJavaType().equals(short.class);
  }

  public static boolean is32Bit(GPUFormat format) {
    return format.getJavaType().equals(int.class) || format.getJavaType().equals(float.class);
  }

  public static boolean is64Bit(GPUFormat format) {
    return format.getJavaType().equals(long.class) || format.getJavaType().equals(double.class);
  }


  public static final Predicate<GPUFormat> RGB = GPUFormats::isRGB;
  public static final Predicate<GPUFormat> RGBA = GPUFormats::isRGBA;
  public static final Predicate<GPUFormat> ARGB = GPUFormats::isARGB;
  public static final Predicate<GPUFormat> BGR = GPUFormats::isBGR;
  public static final Predicate<GPUFormat> BGRA = GPUFormats::isBGRA;
  public static final Predicate<GPUFormat> ABGR = GPUFormats::isABGR;
  public static final Predicate<GPUFormat> R = GPUFormats::isR;
  public static final Predicate<GPUFormat> RG = GPUFormats::isRG;
  public static final Predicate<GPUFormat> DEPTH = GPUFormats::isDepth;
  public static final Predicate<GPUFormat> DEPTH_STENCIL = GPUFormats::isDepthStencil;
  public static final Predicate<GPUFormat> UINT = GPUFormats::isUInt;
  public static final Predicate<GPUFormat> UNORM = GPUFormats::isUNorm;
  public static final Predicate<GPUFormat> USCALED = GPUFormats::isUScaled;
  public static final Predicate<GPUFormat> UFLOAT = GPUFormats::isUFloat;
  public static final Predicate<GPUFormat> SINT = GPUFormats::isSInt;
  public static final Predicate<GPUFormat> SNORM = GPUFormats::isSNorm;
  public static final Predicate<GPUFormat> SSCALED = GPUFormats::isSScaled;
  public static final Predicate<GPUFormat> SFLOAT = GPUFormats::isSFloat;
  public static final Predicate<GPUFormat> SRGB = GPUFormats::isSRGB;
  public static final Predicate<GPUFormat> COMPRESSED = GPUFormats::isCompressed;
  public static final Predicate<GPUFormat> PACKED = GPUFormats::isPacked;
  public static final Predicate<GPUFormat> BITS8 = GPUFormats::is8Bit;
  public static final Predicate<GPUFormat> BITS16 = GPUFormats::is16Bit;
  public static final Predicate<GPUFormat> BITS32 = GPUFormats::is32Bit;
  public static final Predicate<GPUFormat> BITS64 = GPUFormats::is64Bit;

  public static Stream<GPUFormat> streamCompatible(DataSource<?> data) {
    // Ignore compressed and packed formats
    Stream<GPUFormat> valid = streamAll().filter(COMPRESSED.and(PACKED).negate());
    switch (data.getDataType()) {
    case UINT8:
    case UINT16:
    case UINT32:
    case UINT64:
      valid = valid.filter(UINT);
      break;
    case SINT8:
    case SINT16:
    case SINT32:
    case SINT64:
      valid = valid.filter(SINT);
      break;
    case UFIXED8:
    case UFIXED16:
    case UFIXED32:
    case UFIXED64:
      valid = valid.filter(UNORM.or(SRGB));
      break;
    case SFIXED8:
    case SFIXED16:
    case SFIXED32:
    case SFIXED64:
      valid = valid.filter(SNORM);
      break;
    case FLOAT16:
    case FLOAT32:
    case FLOAT64:
      valid = valid.filter(SFLOAT);
      break;
    }

    switch (data.getDataType()) {
    case UINT8:
    case SINT8:
    case UFIXED8:
    case SFIXED8:
      valid = valid.filter(BITS8);
      break;
    case UINT16:
    case SINT16:
    case UFIXED16:
    case SFIXED16:
    case FLOAT16:
      valid = valid.filter(BITS16);
      break;
    case UINT32:
    case SINT32:
    case UFIXED32:
    case SFIXED32:
    case FLOAT32:
      valid = valid.filter(BITS32);
      break;
    case UINT64:
    case SINT64:
    case UFIXED64:
    case SFIXED64:
    case FLOAT64:
      valid = valid.filter(BITS64);
      break;
    }

    return valid;
  }
}
