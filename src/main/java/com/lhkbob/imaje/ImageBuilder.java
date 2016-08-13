package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.DataBuffer;
import com.lhkbob.imaje.layout.PixelFormat;

import java.nio.Buffer;
import java.util.EnumSet;

/**
 */
public interface ImageBuilder<T extends Color, I extends Image<T>, B extends ImageBuilder<T, I, B>> {
  enum DataOption {
    PIXEL_LEFT_TO_RIGHT, PIXEL_RIGHT_TO_LEFT, PIXEL_BOTTOM_TO_TOP, PIXEL_TOP_TO_BOTTOM,
    PIXEL_FRONT_TO_BACK, PIXEL_BACK_TO_FRONT,
    PIXEL_ORIGIN_CLASSIC, PIXEL_ORIGIN_OPENGL,

    MIPMAP_INDEX_LOW_TO_HIGH, MIPMAP_INDEX_HIGH_TO_LOW, MIPMAP_RES_LOW_TO_HIGH,
    MIPMAP_RES_HIGH_TO_LOW,

    GROUP_MIPMAP_BY_LAYER, GROUP_LAYER_BY_MIPMAP,

    CHANNELS_BY_PIXEL, CHANNELS_BY_SCANLINE, CHANNELS_BY_TILE, CHANNELS_BY_IMAGE
  }

  interface OfMipmap<T extends Color> extends ImageBuilder<T, Mipmap<T>, OfMipmap<T>> {
  }

  interface OfMipmapArray<T extends Color> extends ImageBuilder<T, MipmapArray<T>, OfMipmapArray<T>> {
    OfMipmapArray<T> layers(int count);
  }

  interface OfMipmapVolume<T extends Color> extends ImageBuilder<T, MipmapVolume<T>, OfMipmapVolume<T>> {
    OfMipmapVolume<T> depth(int depth);
  }

  interface OfRaster<T extends Color> extends ImageBuilder<T, Raster<T>, OfRaster<T>> {
  }

  interface OfRasterArray<T extends Color> extends ImageBuilder<T, RasterArray<T>, OfRasterArray<T>> {
    OfRasterArray<T> layers(int count);
  }

  interface OfVolume<T extends Color> extends ImageBuilder<T, Volume<T>, OfVolume<T>> {
    OfVolume<T> depth(int depth);
  }

  B abgr();

  B addDataOption(DataOption option);

  B argb();

  B backedBy(byte[] data);

  B backedBy(short[] data);

  B backedBy(int[] data);

  B backedBy(long[] data);

  B backedBy(float[] data);

  B backedBy(double[] data);

  B backedBy(Buffer data);

  B backedBy(DataBuffer data);

  default B backedByNewArray() {
    return backedByNewData(Data.arrayDataFactory());
  }

  default B backedByNewBuffer() {
    return backedByNewData(Data.bufferDataFactory());
  }

  B backedByNewData(Data.Factory factory);

  B bgr();

  B bgra();

  I build();

  B compatibleWith(Image<?> image);

  default B dataLayout(DataOption firstOption, DataOption... otherOptions) {
    return dataLayout(EnumSet.of(firstOption, otherOptions));
  }

  default B dataLayout(EnumSet<DataOption> options) {
    B theBuilder = defaultDataLayout();
    for (DataOption o: options) {
      theBuilder = addDataOption(o);
    }
    return theBuilder;
  }

  B defaultDataLayout();

  B defaultFormat();

  B filledWith(T color);

  default B format(PixelFormat format) {
    return format(format, false);
  }

  B format(PixelFormat format, boolean packed);

  B height(int height);

  B packedA1R5G5B5();

  B packedA2B10G10R10();

  B packedA2R10G10B10();

  B packedA8B8G8R8();

  B packedB4G4R4A4();

  B packedB5G5R5A1();

  B packedB5G6R5();

  B packedD24();

  B packedD24S8();

  default B packedFormat(PixelFormat format) {
    return format(format, true);
  }

  B packedR4G4();

  B packedR4G4B4A4();

  B packedR5G5B5A1();

  B packedR5G6B5();

  B r();

  B rg();

  B rgb();

  B rgba();

  B sfloat16();

  B sfloat32();

  B sfloat64();

  B sint16();

  B sint32();

  B sint64();

  B sint8();

  B snorm16();

  B snorm32();

  B snorm64();

  B snorm8();

  B sscaled16();

  B sscaled32();

  B sscaled64();

  B sscaled8();

  B tiled(int tileWidth, int tileHeight);

  B uint16();

  B uint32();

  B uint64();

  B uint8();

  B unorm16();

  B unorm32();

  B unorm64();

  B unorm8();

  B untiled();

  B uscaled16();

  B uscaled32();

  B uscaled64();

  B uscaled8();

  B width(int width);

  B withAlpha();

  B withAlpha(int bits);

  B withAlpha(PixelFormat.Type type);

  B withAlpha(int bits, PixelFormat.Type type);
}
