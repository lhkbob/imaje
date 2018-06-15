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
package com.lhkbob.imaje.io.awt;

import com.lhkbob.imaje.ImageBuilder;
import com.lhkbob.imaje.Images;
import com.lhkbob.imaje.Pixel;
import com.lhkbob.imaje.Raster;
import com.lhkbob.imaje.color.CMYK;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.Lab;
import com.lhkbob.imaje.color.Luminance;
import com.lhkbob.imaje.color.CIELUV;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.SRGB;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.transform.Transform;
import com.lhkbob.imaje.color.transform.Transforms;
import com.lhkbob.imaje.data.BitData;
import com.lhkbob.imaje.data.ByteData;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.DataBuffer;
import com.lhkbob.imaje.data.IntData;
import com.lhkbob.imaje.data.NumericData;
import com.lhkbob.imaje.data.ShortData;
import com.lhkbob.imaje.data.array.ByteArrayData;
import com.lhkbob.imaje.data.array.DoubleArrayData;
import com.lhkbob.imaje.data.array.FloatArrayData;
import com.lhkbob.imaje.data.array.IntArrayData;
import com.lhkbob.imaje.data.array.ShortArrayData;
import com.lhkbob.imaje.data.types.CustomBinaryData;
import com.lhkbob.imaje.layout.DataLayout;
import com.lhkbob.imaje.layout.TileInterleaveLayout;
import com.lhkbob.imaje.layout.InvertedLayout;
import com.lhkbob.imaje.layout.PackedPixelArray;
import com.lhkbob.imaje.layout.PixelArray;
import com.lhkbob.imaje.layout.PixelFormat;
import com.lhkbob.imaje.layout.PixelFormatBuilder;
import com.lhkbob.imaje.layout.ScanlineLayout;
import com.lhkbob.imaje.layout.UnpackedPixelArray;
import com.lhkbob.imaje.util.Functions;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.DirectColorModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

/**
 *
 */
public final class BufferedImageConverter {
  private static final PixelFormat FORMAT_INT_BGR = new PixelFormatBuilder()
      .channels(PixelFormat.SKIP_CHANNEL, 2, 1, 0).bits(8).types(PixelFormat.Type.UNORM).build();
  private static final PixelFormat FORMAT_INT_ARGB = new PixelFormatBuilder()
      .channels(PixelFormat.ALPHA_CHANNEL, 0, 1, 2).bits(8).types(PixelFormat.Type.UNORM).build();
  private static final PixelFormat FORMAT_INT_RGB = new PixelFormatBuilder()
      .channels(PixelFormat.SKIP_CHANNEL, 0, 1, 2).bits(8).types(PixelFormat.Type.UNORM).build();
  private static final PixelFormat FORMAT_USHORT_555_RGB = new PixelFormatBuilder()
      .channels(PixelFormat.SKIP_CHANNEL, 0, 1, 2).bits(1, 5, 5, 5).types(PixelFormat.Type.UNORM)
      .build();

  private BufferedImageConverter() {}

  public static BufferedImage convert(Raster<?> image) {
    return convertViaSRGB(image);
  }

  private static <T extends Color> BufferedImage convertViaSRGB(Raster<T> image) {
    int type;
    if (Objects.equals(image.getColorType(), Luminance.class)) {
      if (image.getPixelArray().getFormat().getColorChannelBitSize(0) > 8) {
        type = BufferedImage.TYPE_USHORT_GRAY;
      } else {
        type = BufferedImage.TYPE_BYTE_GRAY;
      }
    } else if (image.hasAlphaChannel()) {
      type = BufferedImage.TYPE_INT_ARGB;
    } else {
      type = BufferedImage.TYPE_INT_RGB;
    }

    BufferedImage copy = new BufferedImage(image.getWidth(), image.getHeight(), type);
    Transform<T, SRGB> toSRGB = Transforms.newTransform(image.getColorType(), SRGB.class);
    for (Pixel<T> p : image) {
      SRGB output = toSRGB.apply(p.getColor());
      byte a = (byte) Data.UNORM8.toBits(p.getAlpha());
      byte r = (byte) Data.UNORM8.toBits(Functions.clamp(output.r(), 0.0, 1.0));
      byte g = (byte) Data.UNORM8.toBits(Functions.clamp(output.g(), 0.0, 1.0));
      byte b = (byte) Data.UNORM8.toBits(Functions.clamp(output.b(), 0.0, 1.0));
      int rgb = ((0xff & a) << 24) | ((0xff & r) << 16) | ((0xff & g) << 8) | (0xff & b);

      // Flip the Y coordinate of the pixel when storing
      copy.setRGB(p.getX(), image.getHeight() - p.getY() - 1, rgb);
    }

    return copy;
  }

  private static void configureBuilderType(ImageBuilder<?, ?, ?> builder, int transferType) {
    switch (transferType) {
    case java.awt.image.DataBuffer.TYPE_BYTE:
      builder.unorm8();
      break;
    case java.awt.image.DataBuffer.TYPE_INT:
      builder.unorm32();
      break;
    case java.awt.image.DataBuffer.TYPE_SHORT:
      builder.snorm16();
      break;
    case java.awt.image.DataBuffer.TYPE_USHORT:
      builder.unorm16();
      break;
    case java.awt.image.DataBuffer.TYPE_FLOAT:
      builder.sfloat32();
      break;
    case java.awt.image.DataBuffer.TYPE_DOUBLE:
      builder.sfloat64();
      break;
    }
  }

  private static double packedToSRGB(int packedSRGB, boolean isAlphaPremultiplied, SRGB result) {
    double r = ((packedSRGB & 0xff0000) >> 16) / 255.0;
    double g = ((packedSRGB & 0xff00) >> 8) / 255.0;
    double b = (packedSRGB & 0xff) / 255.0;
    double a = ((packedSRGB & 0xff000000) >> 24) / 255.0;

    if (isAlphaPremultiplied) {
      result.set(r / a, g / a, b / a);
    } else {
      result.set(r, g, b);
    }

    return a;
  }

  private static Raster<Luminance> convertToLuminance(BufferedImage image, Data.Factory factory) {
    ImageBuilder.OfRaster<Luminance> builder = Images.newRaster(Luminance.class)
        .width(image.getWidth()).height(image.getHeight()).newData(factory);
    // Builder is already configured for r(), which is the only option we have really
    configureBuilderType(builder, image.getSampleModel().getDataType());
    if (image.getColorModel().hasAlpha()) {
      builder.withAlpha();
    }

    SRGB temp = new SRGB();
    Transform<SRGB, Luminance> toLuminance = Transforms
        .newTransform(SRGB.class, Luminance.class);
    Raster<Luminance> copy = builder.build();

    boolean alphaPremult = image.isAlphaPremultiplied() && image.getColorModel().hasAlpha();
    for (Pixel<Luminance> p : copy) {
      // Look up flipped Y coordinate
      int v = image.getRGB(p.getX(), image.getHeight() - p.getY() - 1);
      double a = packedToSRGB(v, alphaPremult, temp);
      toLuminance.apply(temp, p.getColor());
      p.persist(a);
    }

    return copy;
  }

  private static Raster<SRGB> convertToSRGB(BufferedImage image, Data.Factory factory) {
    ImageBuilder.OfRaster<SRGB> builder = Images.newRaster(SRGB.class)
        .width(image.getWidth()).height(image.getHeight()).newData(factory);

    // Try and match channel arrangement and bit depths based on known types
    switch (image.getType()) {
    case BufferedImage.TYPE_3BYTE_BGR:
      builder.bgr().unorm8();
      break;
    case BufferedImage.TYPE_INT_BGR:
      builder.packedFormat(FORMAT_INT_BGR);
      break;
    case BufferedImage.TYPE_4BYTE_ABGR_PRE:
    case BufferedImage.TYPE_4BYTE_ABGR:
      builder.abgr().unorm8();
      break;
    case BufferedImage.TYPE_INT_ARGB:
    case BufferedImage.TYPE_INT_ARGB_PRE:
      builder.packedFormat(FORMAT_INT_ARGB);
      break;
    case BufferedImage.TYPE_INT_RGB:
      builder.packedFormat(FORMAT_INT_RGB);
      break;
    case BufferedImage.TYPE_USHORT_555_RGB:
      builder.packedFormat(FORMAT_USHORT_555_RGB);
      break;
    case BufferedImage.TYPE_USHORT_565_RGB:
      builder.packedR5G6B5();
      break;
    default:
      configureBuilderType(builder, image.getSampleModel().getDataType());
      break;
    }

    if (image.getColorModel().hasAlpha()) {
      builder.withAlpha();
    }

    boolean alphaPremult = image.isAlphaPremultiplied() && image.getColorModel().hasAlpha();
    Raster<SRGB> copy = builder.build();
    for (Pixel<SRGB> p : copy) {
      // Look up flipped Y coordinate
      int v = image.getRGB(p.getX(), image.getHeight() - p.getY() - 1);
      double a = packedToSRGB(v, alphaPremult, p.getColor());
      p.persist(a);
    }

    return copy;
  }

  public static Raster<?> convert(BufferedImage image) {
    return convert(image, Data.getDefaultDataFactory());
  }

  public static Raster<?> convert(BufferedImage image, Data.Factory factory) {
    // To function better with java.image and javax.imageio's limited application of color space
    // correction, the converted images are treated very simply. Gray scale images are calculated
    // by first converting to SRGB and then to luminance as well.
    boolean isGrayscale = image.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_GRAY;
    if (isGrayscale) {
      return convertToLuminance(image, factory);
    } else {
      return convertToSRGB(image, factory);
    }
  }

  public static Raster<?> wrap(BufferedImage image) {
    // Filter out images that are subimages wrapping larger data
    if (image.getRaster().getMinX() != 0 || image.getRaster().getMinY() != 0) {
      return null;
    }
    if (image.getSampleModel().getWidth() != image.getWidth()
        || image.getSampleModel().getHeight() != image.getHeight()) {
      return null;
    }
    if (image.getRaster().getParent() != null) {
      return null;
    }

    // Filter out images that use an offset or multiple banks, or are longer than necessary
    if (image.getRaster().getDataBuffer().getOffset() != 0) {
      return null;
    }
    if (image.getRaster().getDataBuffer().getNumBanks() != 1) {
      return null;
    }
    if (image.getRaster().getDataBuffer().getSize() != image.getWidth() * image.getHeight() * image
        .getSampleModel().getNumDataElements()) {
      return null;
    }

    // Filter out images that use premultiplied alpha, because there is currently no
    // premultiplied alpha adapter available
    if (image.getColorModel().hasAlpha() && image.getColorModel().isAlphaPremultiplied()) {
      return null;
    }

    Class<? extends Color> colorType = getClassFromColorSpace(
        image.getColorModel().getColorSpace());
    if (colorType == null) {
      // Unknown color space to map to one of imaje's defined types
      return null;
    }

    DataBuffer wrappedData = getImajeBuffer(image.getRaster().getDataBuffer(),
        image.getSampleModel() instanceof SinglePixelPackedSampleModel);
    if (wrappedData == null) {
      // Unknown DataBuffer implementation
      return null;
    }

    PixelArray wrapper;
    if (image.getSampleModel() instanceof SinglePixelPackedSampleModel) {
      SinglePixelPackedSampleModel s = (SinglePixelPackedSampleModel) image.getSampleModel();
      // Must make sure this is a simple sample model
      if (s.getScanlineStride() != image.getWidth() && s.getNumDataElements() != 1) {
        return null;
      }

      // Reconstruct pixel from the bit offsets of the model
      PixelFormat format = getFormatFromSinglePixelSampleModel(image, s);

      // This will use a conventional raster layout with a single data element
      DataLayout layout = new ScanlineLayout(image.getWidth(), image.getHeight(), 1);

      // It will use a packed pixel array
      wrapper = new PackedPixelArray(
          format, new InvertedLayout(layout, false, true), (BitData) wrappedData);
    } else if (image.getSampleModel() instanceof ComponentSampleModel) {
      ComponentSampleModel s = (ComponentSampleModel) image.getSampleModel();
      TileInterleaveLayout.InterleavingUnit interleave = getInterleaving(s);
      PixelFormat format = getFormatFromComponentSampleModel(image, s);
      if (interleave == null || format == null) {
        // Incompatible component model
        return null;
      }

      DataLayout layout;
      if (interleave == TileInterleaveLayout.InterleavingUnit.PIXEL) {
        layout = new ScanlineLayout(
            image.getWidth(), image.getHeight(), format.getDataChannelCount());
      } else {
        // There is no supported way of having tiled interleaving, so make the tile size equal to the full image
        layout = new TileInterleaveLayout(image.getWidth(), image.getHeight(), image.getWidth(),
            image.getHeight(), format.getDataChannelCount(), interleave);
      }

      // Unpacked pixel array
      wrapper = new UnpackedPixelArray(
          format, new InvertedLayout(layout, false, true), (NumericData) wrappedData);
    } else {
      // Unknown sample model, so data arrangement is unknown
      return null;
    }

    return new Raster<>(colorType, wrapper);
  }

  public static BufferedImage wrap(Raster<?> image) {
    // The color space has to be available to BufferedImage
    ColorSpace colorSpace = getColorSpaceFromClass(image.getColorType());
    if (colorSpace == null) {
      return null;
    }

    PixelArray data = image.getPixelArray();
    boolean isPacked = data instanceof PackedPixelArray;
    if (!(data instanceof PackedPixelArray) && !(data instanceof UnpackedPixelArray)) {
      // Unexpected type of pixel array, most likely does something other than what would be
      // supported by a buffered image
      return null;
    }

    // Only wrap images that fill the data source completely
    if (data.getData().getLength() != data.getLayout().getRequiredDataElements()) {
      return null;
    }

    java.awt.image.DataBuffer wrappedData = getAWTBuffer(data.getData(), data.getFormat());
    if (wrappedData == null) {
      // Unsupported DataSource type, e.g. NIO instead of array
      return null;
    }

    DataLayout layout = data.getLayout();
    if (!(layout instanceof InvertedLayout)) {
      // Must flip Y axis so that coordinate systems align
      return null;
    }

    layout = ((InvertedLayout) layout).getOriginalLayout();

    WritableRaster raster;
    ColorModel colorModel;
    if (isPixelInterleaved(layout)) {
      // This type of layout is definitely supported by BufferedImages
      if (isPacked) {
        // The packed direct color model for BufferedImage assumes RGB color type
        if (colorSpace.getType() != ColorSpace.TYPE_RGB) {
          return null;
        }

        int[] bandMasks = getBandMasksFromPackedPixelFormat(data.getFormat());
        int alphaMask = (bandMasks.length > 3 ? bandMasks[3] : 0);
        raster = java.awt.image.Raster
            .createPackedRaster(wrappedData, image.getWidth(), image.getHeight(), image.getWidth(),
                bandMasks, null);
        colorModel = new DirectColorModel(colorSpace, data.getFormat().getElementBitSize(),
            bandMasks[0], bandMasks[1], bandMasks[2], alphaMask, false, wrappedData.getDataType());
      } else {
        int[] bandOffsets = getBandOffsetsFromUnpackedPixelFormat(data.getFormat(), 1);
        raster = java.awt.image.Raster
            .createInterleavedRaster(wrappedData, image.getWidth(), image.getHeight(),
                image.getWidth() * bandOffsets.length, bandOffsets.length, bandOffsets, null);
        colorModel = new ComponentColorModel(colorSpace, data.getFormat().hasAlphaChannel(), false,
            ColorModel.TRANSLUCENT, wrappedData.getDataType());
      }
    } else if (layout instanceof TileInterleaveLayout) {
      TileInterleaveLayout l = (TileInterleaveLayout) layout;
      // Tiled images aren't supported, regardless of interleaving type
      if (l.getTileHeight() != l.getHeight() || l.getTileWidth() != l.getWidth()) {
        return null;
      }

      int[] bandOffsets;
      int stride;
      switch (l.getInterleavingUnit()) {
      case SCANLINE:
        bandOffsets = getBandOffsetsFromUnpackedPixelFormat(data.getFormat(), image.getWidth());
        stride = image.getWidth() * data.getFormat().getElementCount();
        break;
      case TILE:
        // If tile interleaving is used, but we've come this far then the tile is the same size as
        // the image and they are equivalent in structure
      case IMAGE:
        bandOffsets = getBandOffsetsFromUnpackedPixelFormat(
            data.getFormat(), image.getWidth() * image.getHeight());
        stride = image.getWidth();
        break;
      default:
        // Code should never get here, since it will either go through the first part of the
        // pixel-interleaved if condition, or the image's tiling is invalid and we returned null earlier
        throw new RuntimeException("Unexpected pixel interleaving");
      }

      raster = java.awt.image.Raster
          .createBandedRaster(wrappedData, image.getWidth(), image.getHeight(), stride,
              new int[bandOffsets.length], bandOffsets, null);
      colorModel = new ComponentColorModel(colorSpace, data.getFormat().hasAlphaChannel(), false,
          ColorModel.TRANSLUCENT, wrappedData.getDataType());
    } else {
      // Unknown layout implementation
      return null;
    }

    return new BufferedImage(colorModel, raster, false, new Hashtable<>());
  }

  public static BufferedImage wrapOrConvert(Raster<?> image) {
    BufferedImage wrapped = wrap(image);
    if (wrapped != null) {
      return wrapped;
    }
    return convert(image);
  }

  public static Raster<?> wrapOrConvert(BufferedImage image) {
    return wrapOrConvert(image, Data.getDefaultDataFactory());
  }

  public static Raster<?> wrapOrConvert(BufferedImage image, Data.Factory factory) {
    if (factory == Data.arrayDataFactory()) {
      Raster<?> wrapped = wrap(image);
      if (wrapped != null) {
        return wrapped;
      }
    }

    return convert(image, factory);
  }

  private static int[] getBandMasksFromPackedPixelFormat(PixelFormat format) {
    int numComp = format.getElementCount();
    int[] bandMasks = new int[numComp];
    for (int i = 0; i < numComp; i++) {
      int dataIndex;
      if (i == numComp - 1 && format.hasAlphaChannel()) {
        // This is the alpha channel and must be addressed differently
        bandMasks[i] = format.getAlphaChannelBitSize();
        dataIndex = format.getAlphaChannelDataIndex();
      } else {
        bandMasks[i] = format.getColorChannelBitSize(i);
        dataIndex = format.getColorChannelDataIndex(i);
      }

      int shift = 0;
      for (int j = format.getDataChannelCount() - 1; j > dataIndex; j--) {
        shift += format.getDataChannelBitSize(j);
      }

      bandMasks[i] = bandMasks[i] << shift;
    }

    return bandMasks;
  }

  private static int[] getBandOffsetsFromUnpackedPixelFormat(PixelFormat format, int width) {
    int numComp = format.getElementCount();
    int[] bandOffsets = new int[numComp];
    for (int i = 0; i < numComp; i++) {
      if (i == numComp - 1 && format.hasAlphaChannel()) {
        // This is the alpha channel and must be addressed differently
        bandOffsets[i] = format.getAlphaChannelDataIndex() * width;
      } else {
        bandOffsets[i] = format.getColorChannelDataIndex(i) * width;
      }
    }
    return bandOffsets;
  }

  private static Class<? extends Color> getClassFromColorSpace(ColorSpace cs) {
    if (Objects.equals(cs, ColorSpace.getInstance(ColorSpace.CS_GRAY))) {
      return Luminance.class;
    } else if (Objects.equals(cs, ColorSpace.getInstance(ColorSpace.CS_CIEXYZ))) {
      return XYZ.class;
    } else if (Objects.equals(cs, ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB))) {
      return RGB.Linear.class;
    } else if (Objects.equals(cs, ColorSpace.getInstance(ColorSpace.CS_sRGB))) {
      return SRGB.class;
    } else if (cs.getType() == ColorSpace.TYPE_Lab) {
      return Lab.CIE.class;
    } else if (cs.getType() == ColorSpace.TYPE_Luv) {
      return CIELUV.class;
    } else if (cs.getType() == ColorSpace.TYPE_CMYK) {
      return CMYK.class;
    } else {
      // Unknown color space to map to one of imaje's defined types
      return null;
    }
  }

  private static ColorSpace getColorSpaceFromClass(Class<? extends Color> cType) {
    if (Objects.equals(Luminance.class, cType)) {
      return ColorSpace.getInstance(ColorSpace.CS_GRAY);
    } else if (Objects.equals(SRGB.class, cType)) {
      return ColorSpace.getInstance(ColorSpace.CS_sRGB);
    } else {
      // While AWT defines other color spaces besides the above two, the I/O image pipeline in AWT
      // seems to just assume srgb without doing any conversion.
      return null;
    }
  }

  private static boolean isChannelType(PixelFormat format, PixelFormat.Type type) {
    for (int i = 0; i < format.getDataChannelCount(); i++) {
      if (!format.isDataChannelSkipped(i) && format.getDataChannelType(i) != type) {
        return false;
      }
    }

    return true;
  }

  private static java.awt.image.DataBuffer getAWTBuffer(DataBuffer source, PixelFormat format) {
    Object realData = Data.getViewedData(source);
    if (realData instanceof byte[] && isChannelType(format, PixelFormat.Type.UNORM)) {
      byte[] d = (byte[]) realData;
      return new DataBufferByte(d, d.length);
    } else if (realData instanceof short[] && isChannelType(format, PixelFormat.Type.UNORM)) {
      short[] d = (short[]) realData;
      return new DataBufferUShort(d, d.length);
    } else if (realData instanceof int[] && isChannelType(format, PixelFormat.Type.UNORM)) {
      int[] d = (int[]) realData;
      return new DataBufferInt(d, d.length);
    } else if (realData instanceof float[] && isChannelType(format, PixelFormat.Type.SFLOAT)) {
      float[] d = (float[]) realData;
      return new DataBufferFloat(d, d.length);
    } else if (realData instanceof double[] && isChannelType(format, PixelFormat.Type.SFLOAT)) {
      double[] d = (double[]) realData;
      return new DataBufferDouble(d, d.length);
    } else {
      return null;
    }
  }

  private static DataBuffer getImajeBuffer(java.awt.image.DataBuffer data, boolean isPacked) {
    if (data instanceof DataBufferByte) {
      ByteData wrapped = new ByteArrayData(((DataBufferByte) data).getData());
      if (isPacked) {
        return wrapped;
      } else {
        return new CustomBinaryData<>(Data.UNORM8, wrapped);
      }
    } else if (data instanceof DataBufferUShort) {
      ShortData wrapped = new ShortArrayData(((DataBufferUShort) data).getData());
      if (isPacked) {
        return wrapped;
      } else {
        return new CustomBinaryData<>(Data.UNORM16, wrapped);
      }
    } else if (data instanceof DataBufferShort) {
      ShortData wrapped = new ShortArrayData(((DataBufferShort) data).getData());
      if (isPacked) {
        return wrapped;
      } else {
        return new CustomBinaryData<>(Data.SNORM16, wrapped);
      }
    } else if (data instanceof DataBufferInt) {
      IntData wrapped = new IntArrayData(((DataBufferInt) data).getData());
      if (isPacked) {
        return wrapped;
      } else {
        return new CustomBinaryData<>(Data.UNORM32, wrapped);
      }
    } else if (data instanceof DataBufferFloat) {
      return new FloatArrayData(((DataBufferFloat) data).getData());
    } else if (data instanceof DataBufferDouble) {
      return new DoubleArrayData(((DataBufferDouble) data).getData());
    } else {
      // Unknown DataBuffer implementation
      return null;
    }
  }

  private static PixelFormat getFormatFromComponentSampleModel(
      BufferedImage image, ComponentSampleModel s) {
    // This will assume that all data is stored within a single bank, and that the interleaving is
    // one of PIXEL, SCANLINE, or IMAGE. This means that there are three ways that the band offsets
    // could specify the data to color channel mapping.

    TileInterleaveLayout.InterleavingUnit interleavingUnit = getInterleaving(s);
    if (interleavingUnit == null) {
      return null;
    }
    int bandWidth;
    switch (interleavingUnit) {
    case IMAGE:
    case TILE:
      bandWidth = image.getWidth() * image.getHeight();
      break;
    case SCANLINE:
      bandWidth = image.getWidth();
      break;
    default:
      bandWidth = 1;
      break;
    }

    // Since this is used with a component color model, there will not be skipped bits or anything
    // like that to reach a target bit size.
    int[] dataMap = new int[s.getNumBands()];
    int[] bandOffsets = s.getBandOffsets();
    for (int i = 0; i < bandOffsets.length; i++) {
      int dataIndex = bandOffsets[i] / bandWidth;
      int channel = (i == bandOffsets.length - 1 && image.getColorModel().hasAlpha()
          ? PixelFormat.ALPHA_CHANNEL : i);
      dataMap[dataIndex] = channel;
    }

    int[] bitSizes = new int[dataMap.length];
    PixelFormat.Type[] types = new PixelFormat.Type[dataMap.length];
    Arrays.fill(bitSizes, java.awt.image.DataBuffer.getDataTypeSize(s.getTransferType()));
    Arrays.fill(types, PixelFormat.Type.UNORM);

    return new PixelFormat(dataMap, types, bitSizes);
  }

  private static PixelFormat getFormatFromSinglePixelSampleModel(
      BufferedImage image, SinglePixelPackedSampleModel s) {
    int primitiveBits = java.awt.image.DataBuffer
        .getDataTypeSize(image.getRaster().getTransferType());

    // Get bit masks and bit offsets from sample model once, since it returns a cloned array each time
    int[] sampleOffsets = s.getBitOffsets();
    int[] sampleMasks = s.getBitMasks();

    // Reconstructed array maps for PixelFormat constructor
    List<Integer> bitSizes = new ArrayList<>();
    List<Integer> dataMap = new ArrayList<>();

    // Channels are reconstructed from bit offset of 0, so the lists will be in reverse
    int bitSkip = 0;
    while (bitSkip < primitiveBits) {
      int size = 0;
      int channel = 0;

      // i represents a data channel in the new pixel format, so search for the band in the
      // sample model that has the targeted bitSkip. If none is found, that means a skip has
      // to be inserted into the format to fill up the primitive.
      boolean bandFound = false;
      for (int j = 0; j < sampleOffsets.length; j++) {
        if (sampleOffsets[j] == bitSkip) {
          // Found the band that provides the targeted bit offset
          size = Integer.bitCount(sampleMasks[j]);
          if (j < sampleOffsets.length - 1 || !image.getColorModel().hasAlpha()) {
            // A color channel
            channel = j;
          } else {
            // BufferedImage sample models use last logical band to store alpha
            channel = PixelFormat.ALPHA_CHANNEL;
          }

          bandFound = true;
          break;
        }
      }

      if (!bandFound) {
        // Data channel i is a skip channel, but since there is no mask available, its size is
        // determined by the difference between current bit skip and next.
        int nextColorChannel = -1;
        for (int j = 0; j < sampleOffsets.length; j++) {
          if (sampleOffsets[j] > bitSkip) {
            // The color channel has yet to be processed, so check if it's the closest
            if (nextColorChannel < 0 || sampleOffsets[j] < sampleOffsets[nextColorChannel]) {
              nextColorChannel = j;
            }
          }
        }

        channel = PixelFormat.SKIP_CHANNEL;
        if (nextColorChannel >= 0) {
          // Found the next band, which determines the number of skipped bits
          size = sampleOffsets[nextColorChannel] - bitSkip;
        } else {
          // Skip to the end of the remaining bits
          size = primitiveBits - bitSkip;
        }
      }

      // Record channel
      bitSizes.add(size);
      dataMap.add(channel);
      // Advance to the next targeted offset based on size of current channel
      bitSkip += size;
    }

    int[] dataArrayMap = new int[bitSizes.size()];
    int[] bitSizeArrayMap = new int[bitSizes.size()];
    PixelFormat.Type[] typeArrayMap = new PixelFormat.Type[bitSizes.size()];
    // Walk list in reverse to restore proper order of elements
    int j = 0;
    for (int i = bitSizes.size() - 1; i >= 0; i--) {
      dataArrayMap[j] = dataMap.get(i);
      bitSizeArrayMap[j] = bitSizes.get(i);
      typeArrayMap[j] = PixelFormat.Type.UNORM;
      j++;
    }

    return new PixelFormat(dataArrayMap, typeArrayMap, bitSizeArrayMap);
  }

  private static TileInterleaveLayout.InterleavingUnit getInterleaving(ComponentSampleModel s) {
    int pixelStride = s.getPixelStride(); // increment to get same band of next pixel in scanline
    int scanlineStride = s
        .getScanlineStride(); // increment to get same band of pixel in next scanline
    int width = s.getWidth();
    int height = s.getHeight();
    int bands = s.getNumBands();

    int[] offsets = s.getBandOffsets(); // element offsets for each band

    // Simple pixel interleaving has the stride of each scanline equal to # of bands x width,
    // and the stride for each pixel equal to # of bands, and offsets of bands are multiples of 1
    if (pixelStride == bands && scanlineStride == width * bands && isBandOffsetContiguous(
        offsets, 1)) {
      return TileInterleaveLayout.InterleavingUnit.PIXEL;
    }
    // Scanline interleaving has stride for each pixel equal to 1, and the stride of each scanline
    // is equal to # bands x width, and band offsets are multiples of image width
    if (pixelStride == 1 && scanlineStride == width * bands && isBandOffsetContiguous(
        offsets, width)) {
      return TileInterleaveLayout.InterleavingUnit.SCANLINE;
    }
    // Image interleaving has stride for each pixel equal to 1, and the stride for each scanline
    // equal to width, and band offsets are multiples of image width x height
    if (pixelStride == 1 && scanlineStride == width && isBandOffsetContiguous(
        offsets, width * height)) {
      return TileInterleaveLayout.InterleavingUnit.IMAGE;
    }

    // Otherwise it's something else that is unsupported
    return null;
  }

  private static boolean isBandOffsetContiguous(int[] offsets, int bandWidth) {
    int[] dataIndex = new int[offsets.length];
    Arrays.fill(dataIndex, -1); // Fill with definitely invalid data

    for (int i = 0; i < offsets.length; i++) {
      int index = offsets[i] / bandWidth;
      if (offsets[i] % bandWidth == 0 && index < dataIndex.length && index >= 0) {
        dataIndex[index] = i;
      }
    }

    Arrays.sort(dataIndex);
    for (int i = 0; i < offsets.length; i++) {
      if (dataIndex[i] != i) {
        // Index is not present, duplicated, or not contiguous
        return false;
      }
    }
    return true;
  }

  private static boolean isPixelInterleaved(DataLayout layout) {
    if (layout instanceof TileInterleaveLayout) {
      TileInterleaveLayout l = (TileInterleaveLayout) layout;
      return l.getInterleavingUnit() == TileInterleaveLayout.InterleavingUnit.PIXEL
          && l.getWidth() == l.getTileWidth() && l.getHeight() == l.getTileHeight();
    } else {
      return layout instanceof ScanlineLayout;
    }
  }
}
