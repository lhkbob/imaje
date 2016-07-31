package com.lhkbob.imaje.util;

import com.lhkbob.imaje.Image;
import com.lhkbob.imaje.ImageBuilder;
import com.lhkbob.imaje.Mipmap;
import com.lhkbob.imaje.MipmapArray;
import com.lhkbob.imaje.Pixel;
import com.lhkbob.imaje.Raster;
import com.lhkbob.imaje.RasterArray;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.DataBuffer;
import com.lhkbob.imaje.data.NumericData;
import com.lhkbob.imaje.layout.GeneralPixelLayout;
import com.lhkbob.imaje.layout.PackedPixelArray;
import com.lhkbob.imaje.layout.PixelArray;
import com.lhkbob.imaje.layout.PixelFormat;
import com.lhkbob.imaje.layout.PixelLayout;
import com.lhkbob.imaje.layout.UnpackedPixelArray;

import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

/**
 */
public abstract class DefaultImageBuilder<T extends Color, I extends Image<T>, B extends DefaultImageBuilder<T, I, B>>  {
  private final DataBufferBuilder dataBuilder;
  private final T defaultColor; // Also the default color for newly allocated data
  private final PixelFormatBuilder formatBuilder;
  private final PixelLayoutBuilder layoutBuilder;
  private boolean packed;
  private boolean storeMipmapsHighToLow;
  private boolean storeMipmapsTogether;
  private boolean newDataAllocated;
  private int layers;

  public DefaultImageBuilder(T color) {
    Arguments.notNull("color", color);

    defaultColor = color;
    formatBuilder = new PixelFormatBuilder();
    layoutBuilder = new PixelLayoutBuilder();
    dataBuilder = new DataBufferBuilder();
    packed = false;
    storeMipmapsHighToLow = false;
    storeMipmapsTogether = false;
    newDataAllocated = true;

    defaultFormat();
  }

  public static class OfRaster<T extends Color> extends DefaultImageBuilder<T, Raster<T>, OfRaster<T>> implements ImageBuilder.OfRaster<T> {
    public OfRaster(T color) {
      super(color);
    }

    @Override
    public Raster<T> build() {
      return newRaster();
    }
  }

  public static class OfMipmap<T extends Color> extends DefaultImageBuilder<T, Mipmap<T>, OfMipmap<T>> implements ImageBuilder.OfMipmap<T> {
    public OfMipmap(T color) {
      super(color);
    }

    @Override
    public Mipmap<T> build() {
      return newMipmap();
    }

    @Override
    public DefaultImageBuilder.OfMipmap<T> orderMipmapsHighToLow() {
      return super.orderMipmapsHighToLow();
    }

    @Override
    public DefaultImageBuilder.OfMipmap<T> orderMipmapsLowToHigh() {
      return super.orderMipmapsLowToHigh();
    }
  }

  public static class OfRasterArray<T extends Color> extends DefaultImageBuilder<T, RasterArray<T>, OfRasterArray<T>> implements ImageBuilder.OfRasterArray<T> {
    public OfRasterArray(T color) {
      super(color);
    }

    @Override
    public RasterArray<T> build() {
      return newRasterArray();
    }

    @Override
    public DefaultImageBuilder.OfRasterArray<T> layers(int count) {
      return super.layers(count);
    }
  }

  public static class OfMipmapArray<T extends Color> extends DefaultImageBuilder<T, MipmapArray<T>, OfMipmapArray<T>> implements ImageBuilder.OfMipmapArray<T> {
    public OfMipmapArray(T color) {
      super(color);
    }

    @Override
    public MipmapArray<T> build() {
      return newMipmapArray();
    }

    @Override
    public DefaultImageBuilder.OfMipmapArray<T> groupLayersByMipmap() {
      return super.groupLayersByMipmap();
    }

    @Override
    public DefaultImageBuilder.OfMipmapArray<T> groupMipmapsByLayer() {
      return super.groupMipmapsByLayer();
    }

    @Override
    public DefaultImageBuilder.OfMipmapArray<T> layers(int count) {
      return super.layers(count);
    }

    @Override
    public DefaultImageBuilder.OfMipmapArray<T> orderMipmapsHighToLow() {
      return super.orderMipmapsHighToLow();
    }

    @Override
    public DefaultImageBuilder.OfMipmapArray<T> orderMipmapsLowToHigh() {
      return super.orderMipmapsLowToHigh();
    }
  }
  
  @SuppressWarnings("unchecked")
  protected B builder() {
    return (B) this;
  }

  protected B layers(int count) {
    layers = count;
    return builder();
  }

  public B compatibleWith(Image<?> image) {
    if (image instanceof Raster) {
      // First extract the PixelArray
      PixelArray data = ((Raster<?>) image).getPixelArray();

      formatBuilder.compatibleWith(data.getFormat());
      layoutBuilder.compatibleWith(data.getLayout());
      // FIXME must handle SharedExponentArray
      packed = data instanceof PackedPixelArray;
      // But don't modify new data allocation or mipmap organization
      return builder();
    } else if (image instanceof RasterArray) {
      // There is nothing configurable about a RasterArray, so just grab the first raster since all
      // rasters in the array are formatted the same.
      return compatibleWith(((RasterArray<?>) image).getLayer(0));
    } else if (image instanceof Mipmap) {
      // Try and determine the mipmap ordering and set that property, then configure the rest
      // of the builder based on the highest level mipmap.
      // FIXME
      return compatibleWith(((Mipmap<?>) image).getMipmap(0));
    } else if (image instanceof MipmapArray) {
      // Try to determine mipmap grouping, then configure the rest of the
      // builder based on the first layer (which also handles mipmap ordering)
      // FIXME
      return compatibleWith(((MipmapArray<?>) image).getLayer(0));
    } else {
      throw new UnsupportedOperationException(
          "Unknown Image implementation, cannot be used to update builder: " + image);
    }
  }

  public B format(PixelFormat format) {
    formatBuilder.compatibleWith(format);
    packed = false;
    return builder();
  }

  public B packedFormat(PixelFormat format) {
    formatBuilder.compatibleWith(format);
    packed = true;
    return builder();
  }

  public B abgr() {
    formatBuilder.channels(PixelFormat.ALPHA_CHANNEL, 2, 1, 0);
    return builder();
  }

  public B withAlpha() {
    formatBuilder.addChannel(PixelFormat.ALPHA_CHANNEL);
    return builder();
  }

  public B withAlpha(int bits) {
    formatBuilder.addChannel(PixelFormat.ALPHA_CHANNEL, bits);
    return builder();
  }

  public B withAlpha(PixelFormat.Type type) {
    formatBuilder.addChannel(PixelFormat.ALPHA_CHANNEL, type);
    return builder();
  }

  public B withAlpha(int bits, PixelFormat.Type type) {
    formatBuilder.addChannel(PixelFormat.ALPHA_CHANNEL, bits, type);
    return builder();
  }

  public B argb() {
    formatBuilder.channels(PixelFormat.ALPHA_CHANNEL, 0, 1, 2);
    return builder();
  }

  public B bgr() {
    formatBuilder.channels(2, 1, 0);
    return builder();
  }

  public B bgra() {
    formatBuilder.channels(2, 1, 0, PixelFormat.ALPHA_CHANNEL);
    return builder();
  }

  protected Mipmap<T> newMipmap() {
    PixelFormat format = formatBuilder.build();
    PixelLayout layout = buildLayout(format);
    NumericData<?> data = buildDataSource(format, layout, true, 1);

    int mipmapCount = ImageUtils.getMipmapCount(layout.getWidth(), layout.getHeight());
    List<Raster<T>> levels = new ArrayList<>(mipmapCount);
    for (int i = 0; i < mipmapCount; i++) {
      levels.add(buildRaster(format, layout, data, i, 0, 1));
    }

    Mipmap<T> image = new Mipmap<>(levels);
    setDefaultColor(image);
    return image;
  }

  protected MipmapArray<T> newMipmapArray() {
    PixelFormat format = formatBuilder.build();
    PixelLayout layout = buildLayout(format);
    NumericData<?> data = buildDataSource(format, layout, true, layers);

    int mipmapCount = ImageUtils.getMipmapCount(layout.getWidth(), layout.getHeight());
    List<Mipmap<T>> layerImages = new ArrayList<>(layers);
    for (int i = 0; i < layers; i++) {
      List<Raster<T>> levels = new ArrayList<>(mipmapCount);
      for (int j = 0; j < mipmapCount; j++) {
        levels.add(buildRaster(format, layout, data, j, i, layers));
      }
      layerImages.add(new Mipmap<>(levels));
    }

    MipmapArray<T> image = new MipmapArray<>(layerImages);
    setDefaultColor(image);
    return image;
  }

  private PixelLayout buildLayout(PixelFormat format) {
    if (packed) {
      return layoutBuilder.clone().channels(1).build();
    } else {
      return layoutBuilder.clone().channels(format.getDataChannelCount()).build();
    }
  }

  protected Raster<T> newRaster() {
    PixelFormat format = formatBuilder.build();
    PixelLayout layout = buildLayout(format);
    NumericData<?> data = buildDataSource(format, layout, false, 1);

    Raster<T> image = buildRaster(format, layout, data, -1, 0, 1);
    setDefaultColor(image);
    return image;
  }

  protected RasterArray<T> newRasterArray() {
    PixelFormat format = formatBuilder.build();
    PixelLayout layout = buildLayout(format);
    NumericData<?> data = buildDataSource(format, layout, false, layers);

    List<Raster<T>> layerImages = new ArrayList<>(layers);
    for (int i = 0; i < layers; i++) {
      layerImages.add(buildRaster(format, layout, data, -1, i, layers));
    }

    RasterArray<T> image = new RasterArray<>(layerImages);
    setDefaultColor(image);
    return image;
  }

  public B backedBy(byte[] data) {
    dataBuilder.wrapArray(data);
    newDataAllocated = false;
    return builder();
  }

  public B backedBy(short[] data) {
    dataBuilder.wrapArray(data);
    newDataAllocated = false;
    return builder();
  }

  public B backedBy(int[] data) {
    dataBuilder.wrapArray(data);
    newDataAllocated = false;
    return builder();
  }

  public B backedBy(long[] data) {
    dataBuilder.wrapArray(data);
    newDataAllocated = false;
    return builder();
  }

  public B backedBy(float[] data) {
    dataBuilder.wrapArray(data);
    newDataAllocated = false;
    return builder();
  }

  public B backedBy(double[] data) {
    dataBuilder.wrapArray(data);
    newDataAllocated = false;
    return builder();
  }

  public B backedBy(Buffer data) {
    dataBuilder.wrapBuffer(data);
    newDataAllocated = false;
    return builder();
  }

  public B backedBy(DataBuffer data) {
    dataBuilder.wrapDataSource(data);
    newDataAllocated = false;
    return builder();
  }

  private void setDefaultColor(Image<T> image) {
    if (newDataAllocated) {
      // Only update pixel contents if existing data was not provided.
      for (Pixel<T> p : image) {
        p.setColor(defaultColor, 1.0);
      }
    }
  }

  protected B groupLayersByMipmap() {
    storeMipmapsTogether = false;
    return builder();
  }

  protected B groupMipmapsByLayer() {
    storeMipmapsTogether = true;
    return builder();
  }

  public B channelsByImage() {
    layoutBuilder.interleave(GeneralPixelLayout.InterleavingUnit.IMAGE);
    return builder();
  }

  public B channelsByPixel() {
    layoutBuilder.interleave(GeneralPixelLayout.InterleavingUnit.PIXEL);
    return builder();
  }

  public B channelsByScanline() {
    layoutBuilder.interleave(GeneralPixelLayout.InterleavingUnit.SCANLINE);
    return builder();
  }

  public B channelsByTile() {
    layoutBuilder.interleave(GeneralPixelLayout.InterleavingUnit.TILE);
    return builder();
  }

  public B backedByNewData(Data.Factory factory) {
    dataBuilder.allocateNewData(factory);
    return builder();
  }

  public B untiled() {
    // Explicitly set an invalid dimension rather than setting to null, since that could cause
    // the tiling to be inherited from the compatible image
    layoutBuilder.tileWidth(-1).tileHeight(-1);
    return builder();
  }

  public B dataArrangedBottomUp() {
    layoutBuilder.standardYAxis();
    return builder();
  }

  protected B orderMipmapsHighToLow() {
    storeMipmapsHighToLow = true;
    return builder();
  }

  protected B orderMipmapsLowToHigh() {
    storeMipmapsHighToLow = false;
    return builder();
  }

  protected B packed() {
    packed = true;
    return builder();
  }

  public B packedA2B10G10R10() {
    formatBuilder.bits(2, 10, 10, 10).types(PixelFormat.Type.UNORM);
    return packed().abgr();
  }

  public B packedA8B8G8R8() {
    formatBuilder.bits(8).types(PixelFormat.Type.UNORM);
    return packed().abgr();
  }

  public B packedA1R5G5B5() {
    formatBuilder.bits(1, 5, 5, 5).types(PixelFormat.Type.UNORM);
    return packed().argb();
  }

  public B packedA2R10G10B10() {
    formatBuilder.bits(2, 10, 10, 10).types(PixelFormat.Type.UNORM);
    return packed().argb();
  }

  public B packedB5G6R5() {
    formatBuilder.bits(5, 6, 5).types(PixelFormat.Type.UNORM);
    return packed().bgr();
  }

  public B packedB4G4R4A4() {
    formatBuilder.bits(4).types(PixelFormat.Type.UNORM);
    return packed().bgra();
  }

  public B packedB5G5R5A1() {
    formatBuilder.bits(5, 5, 5, 1).types(PixelFormat.Type.UNORM);
    return packed().bgra();
  }

  public B packedR5G6B5() {
    formatBuilder.bits(5, 6, 5).types(PixelFormat.Type.UNORM);
    return packed().rgb();
  }

  public B packedR4G4B4A4() {
    formatBuilder.bits(4).types(PixelFormat.Type.UNORM);
    return packed().rgba();
  }

  public B packedR5G5B5A1() {
    formatBuilder.bits(5, 5, 5, 1).types(PixelFormat.Type.UNORM);
    return packed().rgba();
  }

  public B packedR4G4() {
    formatBuilder.channels(0, 1).bits(4).types(PixelFormat.Type.UNORM);
    return packed();
  }

  public B packedD24() {
    formatBuilder.channels(PixelFormat.SKIP_CHANNEL, 0).bits(8, 24).types(PixelFormat.Type.UNORM);
    return packed();
  }

  public B packedD24S8() {
    formatBuilder.channels(0, 1).bits(24, 8).types(PixelFormat.Type.UNORM, PixelFormat.Type.SINT);
    return packed();
  }

  public B defaultFormat() {
    int channels = defaultColor.getChannelCount();
    int[] seqMap = new int[channels];
    for (int i = 0; i < channels; i++) {
      seqMap[i] = i;
    }
    formatBuilder.reset().channels(seqMap);
    packed = false;
    return builder();
  }

  public B r() {
    formatBuilder.channels(0);
    return builder();
  }

  public B rg() {
    formatBuilder.channels(0, 1);
    return builder();
  }

  public B rgb() {
    formatBuilder.channels(0, 1, 2);
    return builder();
  }

  public B rgba() {
    formatBuilder.channels(0, 1, 2, PixelFormat.ALPHA_CHANNEL);
    return builder();
  }

  public B sfloat16() {
    formatBuilder.bits(16).types(PixelFormat.Type.SFLOAT);
    return builder();
  }

  public B sfloat32() {
    formatBuilder.bits(32).types(PixelFormat.Type.SFLOAT);
    return builder();
  }

  public B sfloat64() {
    formatBuilder.bits(64).types(PixelFormat.Type.SFLOAT);
    return builder();
  }

  public B sint16() {
    formatBuilder.bits(16).types(PixelFormat.Type.SINT);
    return builder();
  }

  public B sint32() {
    formatBuilder.bits(32).types(PixelFormat.Type.SINT);
    return builder();
  }

  public B sint64() {
    formatBuilder.bits(64).types(PixelFormat.Type.SINT);
    return builder();
  }

  public B sint8() {
    formatBuilder.bits(8).types(PixelFormat.Type.SINT);
    return builder();
  }

  public B sized(int width, int height) {
    layoutBuilder.width(width).height(height);
    return builder();
  }

  public B snorm16() {
    formatBuilder.bits(16).types(PixelFormat.Type.SNORM);
    return builder();
  }

  public B snorm32() {
    formatBuilder.bits(32).types(PixelFormat.Type.SNORM);
    return builder();
  }

  public B snorm64() {
    formatBuilder.bits(64).types(PixelFormat.Type.SNORM);
    return builder();
  }

  public B snorm8() {
    formatBuilder.bits(8).types(PixelFormat.Type.SNORM);
    return builder();
  }

  public B sscaled16() {
    formatBuilder.bits(16).types(PixelFormat.Type.SSCALED);
    return builder();
  }

  public B sscaled32() {
    formatBuilder.bits(32).types(PixelFormat.Type.SSCALED);
    return builder();
  }

  public B sscaled64() {
    formatBuilder.bits(64).types(PixelFormat.Type.SSCALED);
    return builder();
  }

  public B sscaled8() {
    formatBuilder.bits(8).types(PixelFormat.Type.SSCALED);
    return builder();
  }

  public B tiled(int tileWidth, int tileHeight) {
    layoutBuilder.tileWidth(tileWidth).tileHeight(tileHeight);
    return builder();
  }

  public B dataArrangedTopDown() {
    layoutBuilder.flippedYAxis();
    return builder();
  }

  public B uint16() {
    formatBuilder.bits(16).types(PixelFormat.Type.UINT);
    return builder();
  }

  public B uint32() {
    formatBuilder.bits(32).types(PixelFormat.Type.UINT);
    return builder();
  }

  public B uint64() {
    formatBuilder.bits(64).types(PixelFormat.Type.UINT);
    return builder();
  }

  public B uint8() {
    formatBuilder.bits(8).types(PixelFormat.Type.UINT);
    return builder();
  }

  public B unorm16() {
    formatBuilder.bits(16).types(PixelFormat.Type.UNORM);
    return builder();
  }

  public B unorm32() {
    formatBuilder.bits(32).types(PixelFormat.Type.UNORM);
    return builder();
  }

  public B unorm64() {
    formatBuilder.bits(64).types(PixelFormat.Type.UNORM);
    return builder();
  }

  public B unorm8() {
    formatBuilder.bits(8).types(PixelFormat.Type.UNORM);
    return builder();
  }

  public B uscaled16() {
    formatBuilder.bits(16).types(PixelFormat.Type.USCALED);
    return builder();
  }

  public B uscaled32() {
    formatBuilder.bits(32).types(PixelFormat.Type.USCALED);
    return builder();
  }

  public B uscaled64() {
    formatBuilder.bits(64).types(PixelFormat.Type.USCALED);
    return builder();
  }

  public B uscaled8() {
    formatBuilder.bits(8).types(PixelFormat.Type.USCALED);
    return builder();
  }

  private NumericData<?> buildDataSource(
      PixelFormat format, PixelLayout layout, boolean mipmapped, int layers) {
    long imageSize = ImageUtils
        .getImageSize(layout.getWidth(), layout.getHeight(), layout.getChannelCount(), mipmapped,
            layers);

    if (packed) {
      return dataBuilder.bitSize(format.getTotalBitSize()).type(PixelFormat.Type.SINT)
          .length(imageSize).build();
    } else {
      return dataBuilder.bitSize(format.getColorChannelBitSize(0))
          .type(format.getColorChannelType(0)).length(imageSize).build();
    }
  }

  @SuppressWarnings("unchecked")
  private Raster<T> buildRaster(
      PixelFormat format, PixelLayout layout, NumericData<?> data, int mipmapLevel, int layer,
      int maxLayers) {
    long baseOffset;
    if (mipmapLevel < 0) {
      // The image has no mipmaps, so the provided layout is correct, and any base offset is based
      // solely on the array layer provided.
      baseOffset = layout.getRequiredDataElements() * layer;
    } else {
      // Reconfigure the layout for the current mipmap level
      PixelLayout mippedLayout = new PixelLayoutBuilder().compatibleWith(layout)
          .width(ImageUtils.getMipmapDimension(layout.getWidth(), mipmapLevel))
          .height(ImageUtils.getMipmapDimension(layout.getHeight(), mipmapLevel)).build();

      int layersPerMipmap;
      // Update offset based on layers skipped
      if (storeMipmapsTogether) {
        // Every array layer as a full set of mipmaps
        baseOffset = ImageUtils
            .getImageSize(layout.getWidth(), layout.getHeight(), layout.getChannelCount(), true,
                layer);
        layersPerMipmap = 1; // i.e. layers are stored outside the mipmap set
      } else {
        // Every mipmap has maxLayers images, so adjust the offset by the current layer of current mipmap level
        baseOffset = ImageUtils.getImageArraySize(mippedLayout.getWidth(), mippedLayout.getHeight(),
            layout.getChannelCount(), layer);
        layersPerMipmap = maxLayers;
      }

      // Now update offset based on mipmaps skipped within current image layer
      if (storeMipmapsHighToLow) {
        for (int i = ImageUtils.getMipmapCount(layout.getWidth(), layout.getHeight()) - 1;
             i > mipmapLevel; i--) {
          baseOffset += ImageUtils
              .getImageArraySize(ImageUtils.getMipmapDimension(layout.getWidth(), i),
                  ImageUtils.getMipmapDimension(layout.getHeight(), i), layout.getChannelCount(),
                  layersPerMipmap);
        }
      } else {
        for (int i = 0; i < mipmapLevel; i++) {
          baseOffset += ImageUtils
              .getImageArraySize(ImageUtils.getMipmapDimension(layout.getWidth(), i),
                  ImageUtils.getMipmapDimension(layout.getHeight(), i), layout.getChannelCount(),
                  layersPerMipmap);
        }
      }

      layout = mippedLayout;
    }

    PixelArray image =
        packed ? new PackedPixelArray(format, layout, data.asBitData(), baseOffset)
            : new UnpackedPixelArray(format, layout, data, baseOffset);
    return new Raster<>((Class<T>) defaultColor.getClass(), image);
  }
}
