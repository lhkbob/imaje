package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.data.BitDataSource;
import com.lhkbob.imaje.data.DataSource;
import com.lhkbob.imaje.data.NumericDataSource;
import com.lhkbob.imaje.layout.ColorAdapter;
import com.lhkbob.imaje.layout.GeneralPixelLayout;
import com.lhkbob.imaje.layout.PackedPixelArray;
import com.lhkbob.imaje.layout.PixelArray;
import com.lhkbob.imaje.layout.PixelFormat;
import com.lhkbob.imaje.layout.PixelLayout;
import com.lhkbob.imaje.layout.SimpleColorAdapter;
import com.lhkbob.imaje.layout.UnpackedPixelArray;
import com.lhkbob.imaje.util.DataSourceBuilder;
import com.lhkbob.imaje.util.ImageUtils;
import com.lhkbob.imaje.util.PixelFormatBuilder;
import com.lhkbob.imaje.util.PixelLayoutBuilder;

import java.nio.Buffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class ImageBuilder<T extends Color> {
  private final DataSourceBuilder dataBuilder;
  private final T defaultColor; // Also the default color for newly allocated data
  private final PixelFormatBuilder formatBuilder;
  private final PixelLayoutBuilder layoutBuilder;
  private boolean packed;
  private boolean storeMipmapsHighToLow;
  private boolean storeMipmapsTogether;
  private boolean newDataAllocated;

  public ImageBuilder(Class<T> colorType) {
    this(createDefaultColor(colorType));
  }

  public ImageBuilder(T color) {
    defaultColor = color;
    formatBuilder = new PixelFormatBuilder();
    layoutBuilder = new PixelLayoutBuilder();
    dataBuilder = new DataSourceBuilder();
    packed = false;
    storeMipmapsHighToLow = false;
    storeMipmapsTogether = false;
    newDataAllocated = true;

    defaultFormat();
  }

  public ImageBuilder<T> compatibleWith(Image<?> image) {
    if (image instanceof Raster) {
      // First extract the PixelArray
      PixelArray data = ((Raster<?>) image).getPixelArray();
      if (data == null)
        throw new UnsupportedOperationException("Raster does not use a PixelArray representation, cannot be used to update builder.");

      formatBuilder.compatibleWith(data.getFormat());
      layoutBuilder.compatibleWith(data.getLayout());
      packed = data instanceof PackedPixelArray;
      dataBuilder.useBuffersForNewData(data.getData().isGPUAccessible());
      // But don't modify newDataAllocated or mipmap organization
      return this;
    } else if (image instanceof RasterArray) {
      // There is nothing configurable about a RasterArray, so just grab the first raster since all
      // rasters in the array are formatted the same.
      return compatibleWith(((RasterArray<?>) image).getLayer(0));
    } else if (image instanceof Mipmap) {
      // Try and determine the mipmap ordering and set that property, then configure the rest
      // of the builder based on the highest level mipmap.
     // FIXME
      return compatibleWith(((Mipmap<?>) image).getLevel(0));
    } else if (image instanceof MipmapArray) {
      // Try to determine mipmap grouping, then configure the rest of the
      // builder based on the first layer (which also handles mipmap ordering)
      // FIXME
      return compatibleWith(((MipmapArray<?>) image).getLayer(0));
    } else {
      throw new UnsupportedOperationException("Unknown Image implementation, cannot be used to update builder: " + image);
    }
  }

  public ImageBuilder<T> abgr() {
    formatBuilder.channels(PixelFormat.ALPHA_CHANNEL, 2, 1, 0);
    return this;
  }

  public ImageBuilder<T> alpha() {
    formatBuilder.addChannel(PixelFormat.ALPHA_CHANNEL);
    return this;
  }

  public ImageBuilder<T> alpha(int bits) {
    formatBuilder.addChannel(PixelFormat.ALPHA_CHANNEL, bits);
    return this;
  }

  public ImageBuilder<T> alpha(PixelFormat.Type type) {
    formatBuilder.addChannel(PixelFormat.ALPHA_CHANNEL, type);
    return this;
  }

  public ImageBuilder<T> alpha(int bits, PixelFormat.Type type) {
    formatBuilder.addChannel(PixelFormat.ALPHA_CHANNEL, bits, type);
    return this;
  }

  public ImageBuilder<T> argb() {
    formatBuilder.channels(PixelFormat.ALPHA_CHANNEL, 0, 1, 2);
    return this;
  }

  public ImageBuilder<T> bgr() {
    formatBuilder.channels(2, 1, 0);
    return this;
  }

  public ImageBuilder<T> bgra() {
    formatBuilder.channels(2, 1, 0, PixelFormat.ALPHA_CHANNEL);
    return this;
  }

  public Mipmap<T> buildMipmap() {
    PixelFormat format = formatBuilder.build();
    PixelLayout layout = buildLayout(format);
    DataSource data = buildDataSource(format, layout, true, 1);

    int mipmapCount = ImageUtils.getMipmapCount(layout.getWidth(), layout.getHeight());
    List<Raster<T>> levels = new ArrayList<>(mipmapCount);
    for (int i = 0; i < mipmapCount; i++) {
      levels.add(buildRaster(format, layout, data, i, 0, 1));
    }

    Mipmap<T> image = new Mipmap<>(levels);
    setDefaultColor(image);
    return image;
  }

  public MipmapArray<T> buildMipmapArray(int length) {
    PixelFormat format = formatBuilder.build();
    PixelLayout layout = buildLayout(format);
    DataSource data = buildDataSource(format, layout, true, length);

    int mipmapCount = ImageUtils.getMipmapCount(layout.getWidth(), layout.getHeight());
    List<Mipmap<T>> layers = new ArrayList<>(length);
    for (int i = 0; i < length; i++) {
      List<Raster<T>> levels = new ArrayList<>(mipmapCount);
      for (int j = 0; j < mipmapCount; j++) {
        levels.add(buildRaster(format, layout, data, j, i, length));
      }
      layers.add(new Mipmap<>(levels));
    }

    MipmapArray<T> image = new MipmapArray<>(layers);
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

  public Raster<T> buildRaster() {
    PixelFormat format = formatBuilder.build();
    PixelLayout layout = buildLayout(format);
    DataSource data = buildDataSource(format, layout, false, 1);

    Raster<T> image = buildRaster(format, layout, data, -1, 0, 1);
    setDefaultColor(image);
    return image;
  }

  public RasterArray<T> buildRasterArray(int length) {
    PixelFormat format = formatBuilder.build();
    PixelLayout layout = buildLayout(format);
    DataSource data = buildDataSource(format, layout, false, length);

    List<Raster<T>> layers = new ArrayList<>(length);
    for (int i = 0; i < length; i++) {
      layers.add(buildRaster(format, layout, data, -1, i, length));
    }

    RasterArray<T> image = new RasterArray<>(layers);
    setDefaultColor(image);
    return image;
  }

  public ImageBuilder<T> fromArray(byte[] data) {
    dataBuilder.wrapArray(data);
    newDataAllocated = false;
    return this;
  }

  public ImageBuilder<T> fromArray(short[] data) {
    dataBuilder.wrapArray(data);
    newDataAllocated = false;
    return this;
  }

  public ImageBuilder<T> fromArray(int[] data) {
    dataBuilder.wrapArray(data);
    newDataAllocated = false;
    return this;
  }

  public ImageBuilder<T> fromArray(long[] data) {
    dataBuilder.wrapArray(data);
    newDataAllocated = false;
    return this;
  }

  public ImageBuilder<T> fromArray(float[] data) {
    dataBuilder.wrapArray(data);
    newDataAllocated = false;
    return this;
  }

  public ImageBuilder<T> fromArray(double[] data) {
    dataBuilder.wrapArray(data);
    newDataAllocated = false;
    return this;
  }

  public ImageBuilder<T> fromBuffer(Buffer data) {
    dataBuilder.wrapBuffer(data);
    newDataAllocated = false;
    return this;
  }

  public ImageBuilder<T> fromMemoryMappedFile(Path path) {
    dataBuilder.mapFile(path);
    newDataAllocated = false;
    return this;
  }

  public ImageBuilder<T> fromDataSource(DataSource data) {
    dataBuilder.wrapDataSource(data);
    newDataAllocated = false;
    return this;
  }

  private void setDefaultColor(Image<T> image) {
    if (newDataAllocated) {
      // Only update pixel contents if existing data was not provided.
      for (Pixel<T> p : image) {
        p.set(defaultColor, 1.0);
      }
    }
  }

  public ImageBuilder<T> groupLayersByMipmap() {
    storeMipmapsTogether = false;
    return this;
  }

  public ImageBuilder<T> groupMipmapsByLayer() {
    storeMipmapsTogether = true;
    return this;
  }

  public ImageBuilder<T> interleaveChannelsByImage() {
    layoutBuilder.interleave(GeneralPixelLayout.InterleavingUnit.IMAGE);
    return this;
  }

  public ImageBuilder<T> interleaveChannelsByPixel() {
    layoutBuilder.interleave(GeneralPixelLayout.InterleavingUnit.PIXEL);
    return this;
  }

  public ImageBuilder<T> interleaveChannelsByScanline() {
    layoutBuilder.interleave(GeneralPixelLayout.InterleavingUnit.SCANLINE);
    return this;
  }

  public ImageBuilder<T> interleaveChannelsByTile() {
    layoutBuilder.interleave(GeneralPixelLayout.InterleavingUnit.TILE);
    return this;
  }

  public ImageBuilder<T> newArrayData() {
    dataBuilder.allocateNewData().useBuffersForNewData(false);
    newDataAllocated = true;
    return this;
  }

  public ImageBuilder<T> newBufferData() {
    dataBuilder.allocateNewData().useBuffersForNewData(true);
    newDataAllocated = true;
    return this;
  }

  public ImageBuilder<T> notTiled() {
    // Explicitly set an invalid dimension rather than setting to null, since that could cause
    // the tiling to be inherited from the compatible image
    layoutBuilder.tileWidth(-1).tileHeight(-1);
    return this;
  }

  public ImageBuilder<T> openGLAxisConvention() {
    layoutBuilder.standardYAxis();
    return this;
  }

  public ImageBuilder<T> orderMipmapsHighToLow() {
    storeMipmapsHighToLow = true;
    return this;
  }

  public ImageBuilder<T> orderMipmapsLowToHigh() {
    storeMipmapsHighToLow = false;
    return this;
  }

  private ImageBuilder<T> packed() {
    packed = true;
    return this;
  }

  public ImageBuilder<T> packedA2B10G10R10() {
    formatBuilder.bits(2, 10, 10, 10).types(PixelFormat.Type.UNORM);
    return packed().abgr();
  }

  public ImageBuilder<T> packedA8B8G8R8() {
    formatBuilder.bits(8).types(PixelFormat.Type.UNORM);
    return packed().abgr();
  }

  public ImageBuilder<T> packedA1R5G5B5() {
    formatBuilder.bits(1, 5, 5, 5).types(PixelFormat.Type.UNORM);
    return packed().argb();
  }

  public ImageBuilder<T> packedA2R10G10B10() {
    formatBuilder.bits(2, 10, 10, 10).types(PixelFormat.Type.UNORM);
    return packed().argb();
  }

  public ImageBuilder<T> packedB5G6R5() {
    formatBuilder.bits(5, 6, 5).types(PixelFormat.Type.UNORM);
    return packed().bgr();
  }

  public ImageBuilder<T> packedB4G4R4A4() {
    formatBuilder.bits(4).types(PixelFormat.Type.UNORM);
    return packed().bgra();
  }

  public ImageBuilder<T> packedB5G5R5A1() {
    formatBuilder.bits(5, 5, 5, 1).types(PixelFormat.Type.UNORM);
    return packed().bgra();
  }

  public ImageBuilder<T> packedR5G6B5() {
    formatBuilder.bits(5, 6, 5).types(PixelFormat.Type.UNORM);
    return packed().rgb();
  }

  public ImageBuilder<T> packedR4G4B4A4() {
    formatBuilder.bits(4).types(PixelFormat.Type.UNORM);
    return packed().rgba();
  }

  public ImageBuilder<T> packedR5G5B5A1() {
    formatBuilder.bits(5, 5, 5, 1).types(PixelFormat.Type.UNORM);
    return packed().rgba();
  }

  public ImageBuilder<T> packedR4G4() {
    formatBuilder.channels(0, 1).bits(4).types(PixelFormat.Type.UNORM);
    return packed();
  }

  public ImageBuilder<T> packedD24() {
    formatBuilder.channels(PixelFormat.SKIP_CHANNEL, 0).bits(8, 24).types(PixelFormat.Type.UNORM);
    return packed();
  }

  public ImageBuilder<T> packedD24S8() {
    formatBuilder.channels(0, 1).bits(24, 8).types(PixelFormat.Type.UNORM, PixelFormat.Type.SINT);
    return packed();
  }

  public ImageBuilder<T> defaultFormat() {
    int channels = defaultColor.getChannelCount();
    int[] seqMap = new int[channels];
    for (int i = 0; i < channels; i++) {
      seqMap[i] = i;
    }
    formatBuilder.reset().channels(seqMap);
    packed = false;
    return this;
  }

  public ImageBuilder<T> r() {
    formatBuilder.channels(0);
    return this;
  }

  public ImageBuilder<T> rg() {
    formatBuilder.channels(0, 1);
    return this;
  }

  public ImageBuilder<T> rgb() {
    formatBuilder.channels(0, 1, 2);
    return this;
  }

  public ImageBuilder<T> rgba() {
    formatBuilder.channels(0, 1, 2, PixelFormat.ALPHA_CHANNEL);
    return this;
  }

  public ImageBuilder<T> sfloat16() {
    formatBuilder.bits(16).types(PixelFormat.Type.SFLOAT);
    return this;
  }

  public ImageBuilder<T> sfloat32() {
    formatBuilder.bits(32).types(PixelFormat.Type.SFLOAT);
    return this;
  }

  public ImageBuilder<T> sfloat64() {
    formatBuilder.bits(64).types(PixelFormat.Type.SFLOAT);
    return this;
  }

  public ImageBuilder<T> sint16() {
    formatBuilder.bits(16).types(PixelFormat.Type.SINT);
    return this;
  }

  public ImageBuilder<T> sint32() {
    formatBuilder.bits(32).types(PixelFormat.Type.SINT);
    return this;
  }

  public ImageBuilder<T> sint64() {
    formatBuilder.bits(64).types(PixelFormat.Type.SINT);
    return this;
  }

  public ImageBuilder<T> sint8() {
    formatBuilder.bits(8).types(PixelFormat.Type.SINT);
    return this;
  }

  public ImageBuilder<T> size(int width, int height) {
    layoutBuilder.width(width).height(height);
    return this;
  }

  public ImageBuilder<T> snorm16() {
    formatBuilder.bits(16).types(PixelFormat.Type.SNORM);
    return this;
  }

  public ImageBuilder<T> snorm32() {
    formatBuilder.bits(32).types(PixelFormat.Type.SNORM);
    return this;
  }

  public ImageBuilder<T> snorm64() {
    formatBuilder.bits(64).types(PixelFormat.Type.SNORM);
    return this;
  }

  public ImageBuilder<T> snorm8() {
    formatBuilder.bits(8).types(PixelFormat.Type.SNORM);
    return this;
  }

  public ImageBuilder<T> sscaled16() {
    formatBuilder.bits(16).types(PixelFormat.Type.SSCALED);
    return this;
  }

  public ImageBuilder<T> sscaled32() {
    formatBuilder.bits(32).types(PixelFormat.Type.SSCALED);
    return this;
  }

  public ImageBuilder<T> sscaled64() {
    formatBuilder.bits(64).types(PixelFormat.Type.SSCALED);
    return this;
  }

  public ImageBuilder<T> sscaled8() {
    formatBuilder.bits(8).types(PixelFormat.Type.SSCALED);
    return this;
  }

  public ImageBuilder<T> tiled(int tileWidth, int tileHeight) {
    layoutBuilder.tileWidth(tileWidth).tileHeight(tileHeight);
    return this;
  }

  public ImageBuilder<T> uiAxisConvention() {
    layoutBuilder.flippedYAxis();
    return this;
  }

  public ImageBuilder<T> uint16() {
    formatBuilder.bits(16).types(PixelFormat.Type.UINT);
    return this;
  }

  public ImageBuilder<T> uint32() {
    formatBuilder.bits(32).types(PixelFormat.Type.UINT);
    return this;
  }

  public ImageBuilder<T> uint64() {
    formatBuilder.bits(64).types(PixelFormat.Type.UINT);
    return this;
  }

  public ImageBuilder<T> uint8() {
    formatBuilder.bits(8).types(PixelFormat.Type.UINT);
    return this;
  }

  public ImageBuilder<T> unorm16() {
    formatBuilder.bits(16).types(PixelFormat.Type.UNORM);
    return this;
  }

  public ImageBuilder<T> unorm32() {
    formatBuilder.bits(32).types(PixelFormat.Type.UNORM);
    return this;
  }

  public ImageBuilder<T> unorm64() {
    formatBuilder.bits(64).types(PixelFormat.Type.UNORM);
    return this;
  }

  public ImageBuilder<T> unorm8() {
    formatBuilder.bits(8).types(PixelFormat.Type.UNORM);
    return this;
  }

  public ImageBuilder<T> uscaled16() {
    formatBuilder.bits(16).types(PixelFormat.Type.USCALED);
    return this;
  }

  public ImageBuilder<T> uscaled32() {
    formatBuilder.bits(32).types(PixelFormat.Type.USCALED);
    return this;
  }

  public ImageBuilder<T> uscaled64() {
    formatBuilder.bits(64).types(PixelFormat.Type.USCALED);
    return this;
  }

  public ImageBuilder<T> uscaled8() {
    formatBuilder.bits(8).types(PixelFormat.Type.USCALED);
    return this;
  }

  private DataSource buildDataSource(
      PixelFormat format, PixelLayout layout, boolean mipmapped, int layers) {
    long imageSize = ImageUtils
        .getImageSize(layout.getWidth(), layout.getHeight(), layout.getChannelCount(), mipmapped, layers);

    if (packed) {
      DataSource d = dataBuilder.bitSize(format.getTotalBitSize()).type(PixelFormat.Type.SINT)
          .length(imageSize).build();
      if (!(d instanceof BitDataSource)) {
        throw new UnsupportedOperationException(
            "Data cannot be used to represent packed pixels: " + d);
      }
      return d;
    } else {
      DataSource d = dataBuilder.bitSize(format.getColorChannelBitSize(0))
          .type(format.getColorChannelType(0)).length(imageSize).build();
      if (!(d instanceof NumericDataSource)) {
        throw new UnsupportedOperationException(
            "Data cannot be used to represent unpacked pixels: " + d);
      }
      return d;
    }
  }

  private Raster<T> buildRaster(
      PixelFormat format, PixelLayout layout, DataSource data, int mipmapLevel, int layer,
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
            .getImageSize(layout.getWidth(), layout.getHeight(), layout.getChannelCount(), true, layer);
        layersPerMipmap = 1; // i.e. layers are stored outside the mipmap set
      } else {
        // Every mipmap has maxLayers images, so adjust the offset by the current layer of current mipmap level
        baseOffset = ImageUtils
            .getImageArraySize(mippedLayout.getWidth(), mippedLayout.getHeight(), layout.getChannelCount(),
                layer);
        layersPerMipmap = maxLayers;
      }

      // Now update offset based on mipmaps skipped within current image layer
      if (storeMipmapsHighToLow) {
        for (int i = ImageUtils.getMipmapCount(layout.getWidth(), layout.getHeight()) - 1;
             i > mipmapLevel; i--) {
          baseOffset += ImageUtils.getImageArraySize(ImageUtils.getMipmapDimension(layout.getWidth(), i),
              ImageUtils.getMipmapDimension(layout.getHeight(), i), layout.getChannelCount(),
              layersPerMipmap);
        }
      } else {
        for (int i = 0; i < mipmapLevel; i++) {
          baseOffset += ImageUtils.getImageArraySize(ImageUtils.getMipmapDimension(layout.getWidth(), i),
              ImageUtils.getMipmapDimension(layout.getHeight(), i), layout.getChannelCount(),
              layersPerMipmap);
        }
      }

      layout = mippedLayout;
    }

    PixelArray image =
        packed ? new PackedPixelArray(format, layout, (BitDataSource) data, baseOffset)
            : new UnpackedPixelArray(format, layout, (NumericDataSource) data, baseOffset);
    // FIXME how do we distinguish and handle the RGBE formats that need the exponent adapter?
    ColorAdapter<T> adapter = new SimpleColorAdapter(defaultColor.getClass(), image);

    return new Raster<>(adapter);
  }

  private static <T extends Color> T createDefaultColor(Class<T> color) {
    try {
      return color.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new UnsupportedOperationException(
          "Cannot create default color instance for type " + color, e);
    }
  }
}
