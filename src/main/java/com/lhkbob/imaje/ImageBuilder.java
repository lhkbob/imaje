package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.DataBuffer;
import com.lhkbob.imaje.layout.PixelFormat;

import java.nio.Buffer;

/**
 */
public interface ImageBuilder<I extends Image<?>, B extends ImageBuilder<I, B>> {
  interface OfMipmapArray<T extends Color> extends ImageBuilder<MipmapArray<T>, OfMipmapArray<T>> {
    OfMipmapArray<T> groupLayersByMipmap();

    OfMipmapArray<T> groupMipmapsByLayer();

    OfMipmapArray<T> layers(int count);

    OfMipmapArray<T> orderMipmapsHighToLow();

    OfMipmapArray<T> orderMipmapsLowToHigh();
  }

  interface OfMipmap<T extends Color> extends ImageBuilder<Mipmap<T>, OfMipmap<T>> {
    OfMipmap<T> orderMipmapsHighToLow();

    OfMipmap<T> orderMipmapsLowToHigh();
  }

  interface OfRasterArray<T extends Color> extends ImageBuilder<RasterArray<T>, OfRasterArray<T>> {
    OfRasterArray<T> layers(int count);
  }

  interface OfRaster<T extends Color> extends ImageBuilder<Raster<T>, OfRaster<T>> {
    // No additional interface needed
  }

  B abgr();

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

  B channelsByImage();

  B channelsByPixel();

  B channelsByScanline();

  B channelsByTile();

  B compatibleWith(Image<?> image);

  B dataArrangedBottomUp();

  B dataArrangedTopDown();

  B defaultFormat();

  B format(PixelFormat format);

  B packedA1R5G5B5();

  B packedA2B10G10R10();

  B packedA2R10G10B10();

  B packedA8B8G8R8();

  B packedB4G4R4A4();

  B packedB5G5R5A1();

  B packedB5G6R5();

  B packedD24();

  B packedD24S8();

  B packedFormat(PixelFormat format);

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

  B sized(int width, int height);

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

  B withAlpha();

  B withAlpha(int bits);

  B withAlpha(PixelFormat.Type type);

  B withAlpha(int bits, PixelFormat.Type type);
}
