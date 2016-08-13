package com.lhkbob.imaje.util;

import com.lhkbob.imaje.Image;
import com.lhkbob.imaje.ImageBuilder;
import com.lhkbob.imaje.Mipmap;
import com.lhkbob.imaje.MipmapArray;
import com.lhkbob.imaje.MipmapVolume;
import com.lhkbob.imaje.Pixel;
import com.lhkbob.imaje.Raster;
import com.lhkbob.imaje.RasterArray;
import com.lhkbob.imaje.Volume;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.DataBuffer;
import com.lhkbob.imaje.data.NumericData;
import com.lhkbob.imaje.layout.DataLayout;
import com.lhkbob.imaje.layout.GeneralLayout;
import com.lhkbob.imaje.layout.PackedPixelArray;
import com.lhkbob.imaje.layout.PixelArray;
import com.lhkbob.imaje.layout.PixelFormat;
import com.lhkbob.imaje.layout.UnpackedPixelArray;

import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

/**
 */
public abstract class DefaultImageBuilder<T extends Color, I extends Image<T>, B extends DefaultImageBuilder<T, I, B>> {
  public static class OfMipmap<T extends Color> extends DefaultImageBuilder<T, Mipmap<T>, OfMipmap<T>> implements ImageBuilder.OfMipmap<T> {
    public OfMipmap(Class<T> color) {
      super(color);
    }

    @Override
    public Mipmap<T> build() {
      return newMipmap();
    }
  }

  public static class OfMipmapArray<T extends Color> extends DefaultImageBuilder<T, MipmapArray<T>, OfMipmapArray<T>> implements ImageBuilder.OfMipmapArray<T> {
    public OfMipmapArray(Class<T> color) {
      super(color);
    }

    @Override
    public MipmapArray<T> build() {
      return newMipmapArray();
    }

    @Override
    public DefaultImageBuilder.OfMipmapArray<T> layers(int count) {
      return super.layers(count);
    }
  }

  public static class OfMipmapVolume<T extends Color> extends DefaultImageBuilder<T, MipmapVolume<T>, OfMipmapVolume<T>> implements ImageBuilder.OfMipmapVolume<T> {
    public OfMipmapVolume(Class<T> color) {
      super(color);
    }

    @Override
    public MipmapVolume<T> build() {
      return newMipmapVolume();
    }

    @Override
    public DefaultImageBuilder.OfMipmapVolume<T> depth(int depth) {
      return super.depth(depth);
    }

    @Override
    public DefaultImageBuilder.OfMipmapVolume<T> layers(int count) {
      return super.layers(count);
    }
  }

  public static class OfRaster<T extends Color> extends DefaultImageBuilder<T, Raster<T>, OfRaster<T>> implements ImageBuilder.OfRaster<T> {
    public OfRaster(Class<T> color) {
      super(color);
    }

    @Override
    public Raster<T> build() {
      return newRaster();
    }
  }

  public static class OfRasterArray<T extends Color> extends DefaultImageBuilder<T, RasterArray<T>, OfRasterArray<T>> implements ImageBuilder.OfRasterArray<T> {
    public OfRasterArray(Class<T> color) {
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

  public static class OfVolume<T extends Color> extends DefaultImageBuilder<T, Volume<T>, OfVolume<T>> implements ImageBuilder.OfVolume<T> {
    public OfVolume(Class<T> color) {
      super(color);
    }

    @Override
    public Volume<T> build() {
      return newVolume();
    }

    @Override
    public DefaultImageBuilder.OfVolume<T> depth(int depth) {
      return super.depth(depth);
    }
  }

  private final Class<T> colorType;
  private final DataBufferBuilder dataBuilder;
  private final PixelFormatBuilder formatBuilder;
  private final PixelLayoutBuilder layoutBuilder;
  private int depth;
  private T fillColor;
  private int layers;
  private boolean packed;
  private boolean storeMipmapsHighToLow;
  private boolean storeMipmapsTogether;
  private boolean storeZFrontToBack;

  public DefaultImageBuilder(Class<T> colorType) {
    Arguments.notNull("colorType", colorType);

    this.colorType = colorType;
    formatBuilder = new PixelFormatBuilder();
    layoutBuilder = new PixelLayoutBuilder();
    dataBuilder = new DataBufferBuilder();
    packed = false;
    fillColor = null;

    defaultFormat();
    defaultDataLayout();
  }

  public B abgr() {
    formatBuilder.channels(PixelFormat.ALPHA_CHANNEL, 2, 1, 0);
    return builder();
  }

  public B addDataOption(ImageBuilder.DataOption option) {
    switch (option) {
    case PIXEL_LEFT_TO_RIGHT:
      layoutBuilder.leftToRight();
      break;
    case PIXEL_RIGHT_TO_LEFT:
      layoutBuilder.rightToLeft();
      break;
    case PIXEL_BOTTOM_TO_TOP:
      layoutBuilder.bottomToTop();
      break;
    case PIXEL_TOP_TO_BOTTOM:
      layoutBuilder.topToBottom();
      break;
    case PIXEL_ORIGIN_CLASSIC:
      layoutBuilder.leftToRight().topToBottom();
      break;
    case PIXEL_ORIGIN_OPENGL:
      layoutBuilder.leftToRight().bottomToTop();
      break;
    case MIPMAP_INDEX_LOW_TO_HIGH:
    case MIPMAP_RES_HIGH_TO_LOW:
      storeMipmapsHighToLow = false;
      break;
    case MIPMAP_INDEX_HIGH_TO_LOW:
    case MIPMAP_RES_LOW_TO_HIGH:
      storeMipmapsHighToLow = true;
      break;
    case GROUP_MIPMAP_BY_LAYER:
      storeMipmapsTogether = true;
      break;
    case GROUP_LAYER_BY_MIPMAP:
      storeMipmapsTogether = false;
      break;
    case CHANNELS_BY_PIXEL:
      layoutBuilder.interleave(GeneralLayout.InterleavingUnit.PIXEL);
      break;
    case CHANNELS_BY_SCANLINE:
      layoutBuilder.interleave(GeneralLayout.InterleavingUnit.SCANLINE);
      break;
    case CHANNELS_BY_TILE:
      layoutBuilder.interleave(GeneralLayout.InterleavingUnit.TILE);
      break;
    case CHANNELS_BY_IMAGE:
      layoutBuilder.interleave(GeneralLayout.InterleavingUnit.IMAGE);
      break;
    }
    return builder();
  }

  public B argb() {
    formatBuilder.channels(PixelFormat.ALPHA_CHANNEL, 0, 1, 2);
    return builder();
  }

  public B backedBy(byte[] data) {
    dataBuilder.wrapArray(data);
    return builder();
  }

  public B backedBy(short[] data) {
    dataBuilder.wrapArray(data);
    return builder();
  }

  public B backedBy(int[] data) {
    dataBuilder.wrapArray(data);
    return builder();
  }

  public B backedBy(long[] data) {
    dataBuilder.wrapArray(data);
    return builder();
  }

  public B backedBy(float[] data) {
    dataBuilder.wrapArray(data);
    return builder();
  }

  public B backedBy(double[] data) {
    dataBuilder.wrapArray(data);
    return builder();
  }

  public B backedBy(Buffer data) {
    dataBuilder.wrapBuffer(data);
    return builder();
  }

  public B backedBy(DataBuffer data) {
    dataBuilder.wrapDataSource(data);
    return builder();
  }

  public B backedByNewData(Data.Factory factory) {
    dataBuilder.allocateNewData(factory);
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

  public B compatibleWith(Image<?> image) {
    // First reset the layout to the default, so that image types that only require a subset
    // of the layout state do not have to repeatedly set those states to the default
    defaultDataLayout().layers(1).depth(1);

    if (image instanceof Raster) {
      // Set format and array layout based on underlying data
      compatibleWith(((Raster<?>) image).getPixelArray());
    } else if (image instanceof RasterArray) {
      // Extract first pixel array to look up format and layout properties
      compatibleWith(((RasterArray<?>) image).getPixelArray(0));
      layers(image.getLayerCount());
    } else if (image instanceof Mipmap) {
      // Extract first mipmap pixel array for format and layout
      Mipmap<?> m = (Mipmap<?>) image;
      compatibleWith(m.getPixelArray(0));

      storeMipmapsTogether = true;
      storeMipmapsHighToLow = detectInverseDataOffset(m.getPixelArrays());
    } else if (image instanceof MipmapArray) {
      // Configure format and image layout from first mipmap of first layer
      MipmapArray<?> m = (MipmapArray<?>) image;
      compatibleWith(m.getPixelArray(0, 0));
      layers(m.getLayerCount());

      storeMipmapsTogether = detectMipmapGrouping(m.getPixelArrays());
      storeMipmapsHighToLow = detectInverseDataOffset(m.getPixelArraysForLayer(0));
    } else if (image instanceof Volume) {
      // Set format and slice layout based on first slice
      Volume<?> v = (Volume<?>) image;
      compatibleWith(v.getPixelArray(0));
      depth(v.getDepth());

      storeZFrontToBack = detectInverseDataOffset(v.getPixelArrays());
    } else if (image instanceof MipmapVolume) {
      // Set format and slice layout based on first slice of first mipmap
      MipmapVolume<?> v = (MipmapVolume<?>) image;
      compatibleWith(v.getPixelArray(0, 0));
      depth(v.getDepth());

      // Judge z ordering based on first mipmap level
      storeZFrontToBack = detectInverseDataOffset(v.getPixelArraysForMipmap(0));
      // Judge mipmap ordering based on offsets of first slice in each mipmap
      storeMipmapsHighToLow = detectInverseDataOffset(v.getPixelArraysForDepthSlice(0));
    } else {
      throw new UnsupportedOperationException(
          "Unknown Image implementation, cannot be used to update builder: " + image);
    }

    return builder();
  }

  public B defaultDataLayout() {
    layoutBuilder.interleave(GeneralLayout.InterleavingUnit.PIXEL).leftToRight().bottomToTop();
    storeZFrontToBack = false;
    storeMipmapsTogether = true;
    storeMipmapsHighToLow = false;
    return builder();
  }

  public B defaultFormat() {
    int channels = Color.getChannelCount(colorType);
    int[] seqMap = new int[channels];
    for (int i = 0; i < channels; i++) {
      seqMap[i] = i;
    }
    formatBuilder.reset().channels(seqMap);
    packed = false;
    return builder();
  }

  public B filledWith(T color) {
    fillColor = color;
    return builder();
  }

  public B format(PixelFormat format, boolean packed) {
    formatBuilder.compatibleWith(format);
    this.packed = packed;
    return builder();
  }

  public B height(int height) {
    layoutBuilder.height(height);
    return builder();
  }

  public B packedA1R5G5B5() {
    formatBuilder.bits(1, 5, 5, 5).types(PixelFormat.Type.UNORM);
    return packed().argb();
  }

  public B packedA2B10G10R10() {
    formatBuilder.bits(2, 10, 10, 10).types(PixelFormat.Type.UNORM);
    return packed().abgr();
  }

  public B packedA2R10G10B10() {
    formatBuilder.bits(2, 10, 10, 10).types(PixelFormat.Type.UNORM);
    return packed().argb();
  }

  public B packedA8B8G8R8() {
    formatBuilder.bits(8).types(PixelFormat.Type.UNORM);
    return packed().abgr();
  }

  public B packedB4G4R4A4() {
    formatBuilder.bits(4).types(PixelFormat.Type.UNORM);
    return packed().bgra();
  }

  public B packedB5G5R5A1() {
    formatBuilder.bits(5, 5, 5, 1).types(PixelFormat.Type.UNORM);
    return packed().bgra();
  }

  public B packedB5G6R5() {
    formatBuilder.bits(5, 6, 5).types(PixelFormat.Type.UNORM);
    return packed().bgr();
  }

  public B packedD24() {
    formatBuilder.channels(PixelFormat.SKIP_CHANNEL, 0).bits(8, 24).types(PixelFormat.Type.UNORM);
    return packed();
  }

  public B packedD24S8() {
    formatBuilder.channels(0, 1).bits(24, 8).types(PixelFormat.Type.UNORM, PixelFormat.Type.SINT);
    return packed();
  }

  public B packedR4G4() {
    formatBuilder.channels(0, 1).bits(4).types(PixelFormat.Type.UNORM);
    return packed();
  }

  public B packedR4G4B4A4() {
    formatBuilder.bits(4).types(PixelFormat.Type.UNORM);
    return packed().rgba();
  }

  public B packedR5G5B5A1() {
    formatBuilder.bits(5, 5, 5, 1).types(PixelFormat.Type.UNORM);
    return packed().rgba();
  }

  public B packedR5G6B5() {
    formatBuilder.bits(5, 6, 5).types(PixelFormat.Type.UNORM);
    return packed().rgb();
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

  public B untiled() {
    // Explicitly set an invalid dimension rather than setting to null, since that could cause
    // the tiling to be inherited from the compatible image
    layoutBuilder.tileWidth(-1).tileHeight(-1);
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

  public B width(int width) {
    layoutBuilder.width(width);
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

  @SuppressWarnings("unchecked")
  protected B builder() {
    return (B) this;
  }

  protected B depth(int depth) {
    this.depth = depth;
    return builder();
  }

  protected B layers(int count) {
    layers = count;
    return builder();
  }

  protected Mipmap<T> newMipmap() {
    PixelFormat format = buildAndValidateFormat();
    DataLayout layout = buildLayout(format);
    // Send 1s for layers and depth to effectively disable array or volume layering
    NumericData<?> data = buildDataSource(format, layout, true, 1, 1);

    // 2D mipmap only to worry about for count
    int mipmapCount = ImageUtils.getMipmapCount(layout.getWidth(), layout.getHeight());
    List<PixelArray> mipmaps = new ArrayList<>(mipmapCount);
    for (int i = 0; i < mipmapCount; i++) {
      // Specify current mipmap (i), but send -1s for layer and z to keep it as simple 2D mipmap
      mipmaps.add(buildPixelArray(format, layout, data, -1, i, -1));
    }

    Mipmap<T> image = new Mipmap<>(colorType, mipmaps);
    setFillColor(image);
    return image;
  }

  protected MipmapArray<T> newMipmapArray() {
    validateArrayState();
    PixelFormat format = buildAndValidateFormat();
    DataLayout layout = buildLayout(format);
    // Send 1 for depth to disable volume layering
    NumericData<?> data = buildDataSource(format, layout, true, layers, 1);

    // 2D mipmap only to worry about for count
    int mipmapCount = ImageUtils.getMipmapCount(layout.getWidth(), layout.getHeight());
    List<List<PixelArray>> arrayOfMipmaps = new ArrayList<>(layers);
    for (int i = 0; i < layers; i++) {
      List<PixelArray> mipmaps = new ArrayList<>(mipmapCount);
      for (int j = 0; j < mipmapCount; j++) {
        // Provide current layer and mipmap level, but -1 for z to disable volume behavior
        mipmaps.add(buildPixelArray(format, layout, data, i, j, -1));
      }
      arrayOfMipmaps.add(mipmaps);
    }

    MipmapArray<T> image = new MipmapArray<>(colorType, arrayOfMipmaps);
    setFillColor(image);
    return image;
  }

  protected MipmapVolume<T> newMipmapVolume() {
    validateVolumeState();
    PixelFormat format = buildAndValidateFormat();
    DataLayout layout = buildLayout(format);
    // Send 1 for layer to disable array layering
    NumericData<?> data = buildDataSource(format, layout, true, 1, depth);

    // Use all 3 dimensions for mipmap count
    int mipmapCount = ImageUtils.getMipmapCount(layout.getWidth(), layout.getHeight(), depth);
    List<List<PixelArray>> mipmappedZs = new ArrayList<>(mipmapCount);
    for (int i = 0; i < mipmapCount; i++) {
      int z = ImageUtils.getMipmapDimension(depth, i);
      List<PixelArray> volume = new ArrayList<>(z);
      for (int j = 0; j < z; j++) {
        // Provide current z and mipmap level, but -1 for layer to disable array beheavior
        volume.add(buildPixelArray(format, layout, data, -1, i, j));
      }
      mipmappedZs.add(volume);
    }

    MipmapVolume<T> image = new MipmapVolume<>(colorType, mipmappedZs);
    setFillColor(image);
    return image;
  }

  protected Raster<T> newRaster() {
    PixelFormat format = buildAndValidateFormat();
    DataLayout layout = buildLayout(format);
    // Send 1s for layers and depth to effectively disable array or volume layering
    NumericData<?> data = buildDataSource(format, layout, false, 1, 1);
    // Send -1s for layer, mipmap, and z coordinate to disable everything but the 2D raster
    PixelArray array = buildPixelArray(format, layout, data, -1, -1, -1);

    Raster<T> image = new Raster<>(colorType, array);
    setFillColor(image);
    return image;
  }

  protected RasterArray<T> newRasterArray() {
    validateArrayState();
    PixelFormat format = buildAndValidateFormat();
    DataLayout layout = buildLayout(format);
    // Send a 1 for depth to disable volume layering
    NumericData<?> data = buildDataSource(format, layout, false, layers, 1);

    List<PixelArray> arrays = new ArrayList<>(layers);
    for (int i = 0; i < layers; i++) {
      // Pass in current layer index, but -1s for mipmap and z to disable those behaviors
      arrays.add(buildPixelArray(format, layout, data, i, -1, -1));
    }

    RasterArray<T> image = new RasterArray<>(colorType, arrays);
    setFillColor(image);
    return image;
  }

  protected Volume<T> newVolume() {
    validateVolumeState();
    PixelFormat format = buildAndValidateFormat();
    DataLayout layout = buildLayout(format);
    // Send a 1 for layer to disable array layering
    NumericData<?> data = buildDataSource(format, layout, false, 1, depth);

    List<PixelArray> arrays = new ArrayList<>(depth);
    for (int i = 0; i < depth; i++) {
      // Pass in current z index, but -1s for mipmap and layer to disable those behaviors
      arrays.add(buildPixelArray(format, layout, data, -1, -1, i));
    }

    Volume<T> image = new Volume<>(colorType, arrays);
    setFillColor(image);
    return image;
  }

  protected B packed() {
    packed = true;
    return builder();
  }

  private PixelFormat buildAndValidateFormat() {
    PixelFormat format = formatBuilder.build();
    // Validate that the format is compatible with the color before any more allocation is done
    if (format.getColorChannelCount() != Color.getChannelCount(colorType)) {
      throw new IllegalStateException(
          "Selected pixel format (" + format + ") incompatible with desired color type: "
              + colorType);
    }
    return format;
  }

  private NumericData<?> buildDataSource(
      PixelFormat format, DataLayout layout, boolean mipmapped, int layers, int depth) {
    // layers and depth are passed in as parameters so that they can be overridden depending on
    // if the image actually being created supports layers or depth
    int[] dims;
    if (depth > 1) {
      dims = new int[] { layout.getWidth(), layout.getHeight(), depth };
    } else {
      dims = new int[] { layout.getWidth(), layout.getHeight() };
    }
    long imageSize =
        ImageUtils.getUncompressedImageSize(dims, mipmapped) * layout.getChannelCount() * layers;

    if (packed) {
      return dataBuilder.bitSize(format.getTotalBitSize()).type(PixelFormat.Type.SINT)
          .length(imageSize).build();
    } else {
      return dataBuilder.bitSize(format.getColorChannelBitSize(0))
          .type(format.getColorChannelType(0)).length(imageSize).build();
    }
  }

  private DataLayout buildLayout(PixelFormat format) {
    if (packed) {
      return layoutBuilder.clone().channels(1).build();
    } else {
      return layoutBuilder.clone().channels(format.getDataChannelCount()).build();
    }
  }

  private PixelArray buildPixelArray(
      PixelFormat format, DataLayout layout, NumericData<?> data, int layer, int mipmapLevel,
      int z) {
    int[] baseDims;
    if (z >= 0) {
      // This is a volume supporting image, so include the 3rd dimension stored in 'depth'
      baseDims = new int[] { layout.getWidth(), layout.getHeight(), depth };
    } else {
      baseDims = new int[] { layout.getWidth(), layout.getHeight() };
    }

    long baseOffset = getDataOffset(baseDims, layer, mipmapLevel, z) * layout.getChannelCount();
    if (mipmapLevel > 0) {
      // Update the layout to use the mipmap dimensions
      layout = new PixelLayoutBuilder().compatibleWith(layout)
          .width(ImageUtils.getMipmapDimension(layout.getWidth(), mipmapLevel))
          .height(ImageUtils.getMipmapDimension(layout.getHeight(), mipmapLevel)).build();
    }

    // FIXME support selecting the shared exponent formats
    if (packed) {
      return new PackedPixelArray(format, layout, data.asBitData(), baseOffset);
    } else {
      return new UnpackedPixelArray(format, layout, data, baseOffset);
    }
  }

  private void compatibleWith(PixelArray data) {
    formatBuilder.compatibleWith(data.getFormat());
    layoutBuilder.compatibleWith(data.getLayout());
    // FIXME must handle SharedExponentArray and other PixelArray implementations
    packed = data instanceof PackedPixelArray;
  }

  private boolean detectInverseDataOffset(List<PixelArray> mipmaps) {
    // Assuming that the given list is ordered logically from low index to high index,
    // the data ordering is high to low if everything uses the same buffer and the array offsets
    // are in descending order.
    DataBuffer data = mipmaps.get(0).getData();
    long offset = mipmaps.get(0).getDataOffset();

    for (int i = 1; i < mipmaps.size(); i++) {
      if (data != mipmaps.get(i).getData()) {
        // Different data sources, so default to preferred low to high ordering
        return false;
      } else if (offset < mipmaps.get(i).getDataOffset()) {
        // Not descending order so a higher mipmap comes after a lower one in the buffer
        return false;
      } else {
        offset = mipmaps.get(i).getDataOffset();
      }
    }

    // True denotes high to low ordering
    return true;
  }

  private boolean detectMipmapGrouping(List<List<PixelArray>> arrayOfMipmaps) {
    // Mipmaps are grouped if the offset for a layer's particular level is less than the offset of
    // every subsequent layer of any mipmap level, or if the pixel arrays have different data
    // sources in which case mipmap grouping is the preferred default.
    DataBuffer data = arrayOfMipmaps.get(0).get(0).getData();

    // Layers are walked from reverse checking the one ahead of it, if that is valid then it
    // is also true that all higher layers are stored after the current layer as well.
    for (int i = arrayOfMipmaps.size() - 2; i >= 0; i--) {
      List<PixelArray> currentMips = arrayOfMipmaps.get(i);
      List<PixelArray> prevMips = arrayOfMipmaps.get(i + 1);
      for (PixelArray currentMip : currentMips) {
        // Check data source
        if (currentMip.getData() != data) {
          return true; // True denotes group mipmaps
        }
        // Offset of prevMips[k] must be greater than currentMips[j]
        for (PixelArray prevMip : prevMips) {
          if (currentMip.getDataOffset() >= prevMip.getDataOffset()) {
            // A higher layer's mipmap comes before one of this layer's mipmaps so the mipmaps
            // cannot be grouped within a layer
            return false;
          }
        }
      }
    }

    // All layers mipmaps come before the next layers so the mipmaps are likely grouped
    return true;
  }

  private long getDataOffset(int[] baseDims, int layer, int mipmapLevel, int z) {
    long offset = 0;

    // First accumulate least significant z offset, which is z * elements in each depth slice,
    // if slices are arranged low to high, and (depth - z - 1) if arranged high to low.
    if (baseDims.length > 2) {
      if (storeZFrontToBack) {
        offset += (baseDims[2] - z - 1) * baseDims[0] * baseDims[1];
      } else {
        offset += z * baseDims[0] * baseDims[1];
      }
    }

    // Second accumulate elements based on the number of complete layers to skip, and calculate
    // the layer scaling for subsequent mipmap blocks
    int layersPerMipmap = 1;
    if (layer >= 0 && layers > 0) {
      if (storeMipmapsTogether || mipmapLevel < 0) {
        // Every array layer is a full set of mipmaps, and offset must skip past 'layer' count of them
        offset += ImageUtils.getUncompressedImageSize(baseDims, mipmapLevel >= 0) * layer;
        layersPerMipmap = 1;
      } else {
        // Layers are grouped within mipmap levels, so each mipmap level has 'layers' images, so
        // update the offset by current progress through the array, sized based on mipmap level
        offset += ImageUtils
            .getUncompressedImageSize(ImageUtils.getMipmapDimensions(baseDims, mipmapLevel), false)
            * layer;
        layersPerMipmap = layers;
      }
    }

    // Third accumulate mipmap offset based on layer scaling and direction through mipmap sequent
    if (mipmapLevel >= 0) {
      int mipmapCount = ImageUtils.getMipmapCount(baseDims);
      if (storeMipmapsHighToLow) {
        for (int i = mipmapCount - 1; i > mipmapLevel; i--) {
          int[] mipDims = ImageUtils.getMipmapDimensions(baseDims, i);
          offset += ImageUtils.getUncompressedImageSize(mipDims, false) * layersPerMipmap;
        }
      } else {
        for (int i = 0; i < mipmapLevel; i++) {
          int[] mipDims = ImageUtils.getMipmapDimensions(baseDims, i);
          offset += ImageUtils.getUncompressedImageSize(mipDims, false) * layersPerMipmap;
        }
      }
    }

    return offset;
  }

  private void setFillColor(Image<T> image) {
    if (fillColor != null) {
      for (Pixel<T> p : image) {
        p.setColor(fillColor, 1.0);
      }
    }
  }

  private void validateArrayState() {
    if (layers <= 0) {
      throw new IllegalStateException(
          "Array-based images must have a layer count set to at least 1, not: " + layers);
    }
  }

  private void validateVolumeState() {
    if (depth <= 0) {
      throw new IllegalStateException(
          "Volume-based images must have a depth set to at least 1, not: " + depth);
    }
  }
}
