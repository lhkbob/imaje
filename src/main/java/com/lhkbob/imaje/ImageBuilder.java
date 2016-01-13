package com.lhkbob.imaje;

import com.lhkbob.imaje.color.CMYK;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.Depth;
import com.lhkbob.imaje.color.DepthStencil;
import com.lhkbob.imaje.color.HLS;
import com.lhkbob.imaje.color.HSV;
import com.lhkbob.imaje.color.LMS;
import com.lhkbob.imaje.color.Lab;
import com.lhkbob.imaje.color.Luminance;
import com.lhkbob.imaje.color.Luv;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.YCbCr;
import com.lhkbob.imaje.color.YUV;
import com.lhkbob.imaje.color.Yyx;
import com.lhkbob.imaje.data.DataSource;
import com.lhkbob.imaje.data.DataSources;
import com.lhkbob.imaje.data.DoubleSource;
import com.lhkbob.imaje.data.channel.DoubleChannel;
import com.lhkbob.imaje.layout.CMYKAdapter;
import com.lhkbob.imaje.layout.DepthAdapter;
import com.lhkbob.imaje.layout.HLSAdapter;
import com.lhkbob.imaje.layout.HSVAdapter;
import com.lhkbob.imaje.layout.LMSAdapter;
import com.lhkbob.imaje.layout.LabAdapter;
import com.lhkbob.imaje.layout.LuminanceAdapter;
import com.lhkbob.imaje.layout.LuvAdapter;
import com.lhkbob.imaje.layout.PixelAdapter;
import com.lhkbob.imaje.layout.PixelLayout;
import com.lhkbob.imaje.layout.RGBAdapter;
import com.lhkbob.imaje.layout.RowMajorLayout;
import com.lhkbob.imaje.layout.TiledLayout;
import com.lhkbob.imaje.layout.XYZAdapter;
import com.lhkbob.imaje.layout.YCbCrAdapter;
import com.lhkbob.imaje.layout.YUVAdapter;
import com.lhkbob.imaje.layout.YyxAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ImageBuilder<T extends Color> {
  private final Class<T> colorType;
  private int bitDepth;
  // FIXME this needs something special to distinguish between the 24-8 packed depth stencil
  // and just wanting two equal bit-depth'ed depth/stencil channels
  private boolean fixedPoint;
  // FIXME also need options to provide already existing underlying data
  private int height;
  private boolean includeAlpha;
  private boolean parallelChannels;
  private boolean storeMipmapsHighToLow;
  private boolean storeMipmapsTogether;
  private int tileHeight;
  private int tileWidth;
  private boolean useNIOBuffers;
  private int width;

  public ImageBuilder(Class<T> colorType) {
    this.colorType = colorType;
    width = 1;
    height = 1;
    parallelChannels = false;
    includeAlpha = false;
    storeMipmapsHighToLow = false;
    storeMipmapsTogether = true;
    useNIOBuffers = false;
    fixedPoint = true;
    bitDepth = 8;
    tileWidth = -1;
    tileHeight = -1;
  }

  public ImageBuilder<T> baseMipmapStoredFirst() {
    storeMipmapsHighToLow = false;
    return this;
  }

  public ImageBuilder<T> baseMipmapStoredLast() {
    storeMipmapsHighToLow = true;
    return this;
  }

  public ImageBuilder<T> groupLayersByMipmap() {
    storeMipmapsTogether = false;
    return this;
  }

  public ImageBuilder<T> groupMipmapsByLayer() {
    storeMipmapsTogether = true;
    return this;
  }

  public RasterImage<T> newImage() {
    DoubleSource data = createBackingData(false, 1);
    return createRaster(data, -1, 0, 1);
  }

  public ImageArray<T> newImageArray(int length) {
    DoubleSource data = createBackingData(false, length);
    List<RasterImage<T>> layers = new ArrayList<>(length);
    for (int i = 0; i < length; i++) {
      layers.add(createRaster(data, -1, i, length));
    }
    return new DefaultImageArray<>(layers);
  }

  public MipmapImage<T> newMipmapImage() {
    DoubleSource data = createBackingData(true, 1);
    int mipmapCount = Image.getMipmapCount(width, height);
    List<RasterImage<T>> levels = new ArrayList<>(mipmapCount);
    for (int i = 0; i < mipmapCount; i++) {
      levels.add(createRaster(data, i, 0, 1));
    }
    return new DefaultMipmapImage<>(levels);
  }

  public MipmapImageArray<T> newMipmapImageArray(int length) {
    DoubleSource data = createBackingData(true, length);
    int mipmapCount = Image.getMipmapCount(width, height);
    List<MipmapImage<T>> layers = new ArrayList<>(length);
    for (int i = 0; i < length; i++) {
      List<RasterImage<T>> levels = new ArrayList<>(mipmapCount);
      for (int j = 0; j < mipmapCount; j++) {
        levels.add(createRaster(data, j, i, length));
      }
      layers.add(new DefaultMipmapImage<>(levels));
    }
    return new DefaultMipmapImageArray<>(layers);
  }

  public ImageBuilder<T> notTiled() {
    tileWidth = -1;
    tileHeight = -1;
    return this;
  }

  public ImageBuilder<T> opaque() {
    includeAlpha = false;
    return this;
  }

  public ImageBuilder<T> sized(int width, int height) {
    this.width = width;
    this.height = height;
    return this;
  }

  public ImageBuilder<T> tiled(int tileWidth, int tileHeight) {
    this.tileWidth = tileWidth;
    this.tileHeight = tileHeight;
    return this;
  }

  public ImageBuilder<T> transparent() {
    includeAlpha = true;
    return this;
  }

  public ImageBuilder<T> usingArrays() {
    useNIOBuffers = false;
    return this;
  }

  public ImageBuilder<T> usingNativeBuffers() {
    useNIOBuffers = true;
    return this;
  }

  public ImageBuilder<T> withFixedPoint(int bitDepth) {
    fixedPoint = true;
    this.bitDepth = bitDepth;
    return this;
  }

  public ImageBuilder<T> withFloatingPoint(int bitDepth) {
    fixedPoint = false;
    this.bitDepth = bitDepth;
    return this;
  }

  public ImageBuilder<T> withPackedChannels() {
    parallelChannels = false;
    return this;
  }

  public ImageBuilder<T> withParallelChannels() {
    parallelChannels = true;
    return this;
  }

  private DoubleSource createBackingData(boolean mipmapped, int layers) {
    long primitiveCount =
        getPixelCount(width, height, layers, mipmapped) * getChannelCount(colorType);
    DataSource<?> base;
    if (fixedPoint) {
      if (bitDepth == 8) {
        if (useNIOBuffers) {
          base = DataSources.newUnsignedByteSource().ofBuffer(primitiveCount);
        } else {
          base = DataSources.newUnsignedByteSource().ofArray(primitiveCount);
        }
      } else if (bitDepth == 16) {
        if (useNIOBuffers) {
          base = DataSources.newUnsignedShortSource().ofBuffer(primitiveCount);
        } else {
          base = DataSources.newUnsignedShortSource().ofArray(primitiveCount);
        }
      } else if (bitDepth == 32) {
        if (useNIOBuffers) {
          base = DataSources.newUnsignedIntSource().ofBuffer(primitiveCount);
        } else {
          base = DataSources.newUnsignedIntSource().ofArray(primitiveCount);
        }
      } else if (bitDepth == 64) {
        if (useNIOBuffers) {
          base = DataSources.newLongSource().ofBuffer(primitiveCount);
        } else {
          base = DataSources.newLongSource().ofArray(primitiveCount);
        }
      } else {
        throw new UnsupportedOperationException(
            "Unsupported bit depth for fixed-point data: " + bitDepth);
      }
    } else {
      if (bitDepth == 16) {
        if (useNIOBuffers) {
          base = DataSources.newHalfSource().ofBuffer(primitiveCount);
        } else {
          base = DataSources.newHalfSource().ofArray(primitiveCount);
        }
      } else if (bitDepth == 32) {
        if (useNIOBuffers) {
          base = DataSources.newFloatSource().ofBuffer(primitiveCount);
        } else {
          base = DataSources.newFloatSource().ofArray(primitiveCount);
        }
      } else if (bitDepth == 64) {
        if (useNIOBuffers) {
          base = DataSources.newDoubleSource().ofBuffer(primitiveCount);
        } else {
          base = DataSources.newDoubleSource().ofArray(primitiveCount);
        }
      } else {
        throw new UnsupportedOperationException(
            "Unsupported bit depth for floating-point data: " + bitDepth);
      }
    }

    return DataSources.asDoubleSource(base);
  }

  private List<DoubleChannel> createChannelsForRaster(
      DoubleSource data, int mipmapLevel, int arrayLayer, int maxLayers) {
    int channelCount = getChannelCount(colorType);
    if (includeAlpha) {
      channelCount++;
    }

    long channelLength;
    long baseOffset;
    if (mipmapLevel < 0) {
      // Image will not be mipmapped so there's no need to bother with the mipmap vs. array ordering

      // Each channel will have pixel count elements for the full-sized image
      channelLength = getPixelCountForRaster(width, height);
      // The base offset must account for all previous full-sized images
      baseOffset = arrayLayer * channelLength * channelCount;
    } else {
      // Image is mipmapped so the channels are as long as the number of pixels in current mipmap
      channelLength = getPixelCountForMipmap(width, height, mipmapLevel);

      long layersPerMipmap;
      if (storeMipmapsTogether) {
        // Each array layer has a full set of mipmaps
        baseOffset = getPixelCountForMipmapSet(width, height) * arrayLayer * channelCount;
        layersPerMipmap = 1; // e.g. layers stored outside mipmap
      } else {
        // Start with the offset of array layer within current mipmap level
        baseOffset = getPixelCountForMipmap(width, height, mipmapLevel) * arrayLayer * channelCount;
        layersPerMipmap = maxLayers; // e.g. each mipmap level has full array
      }

      // Now update base offset based on how mipmaps are ordered (high-to-low or low-to-high)
      if (storeMipmapsHighToLow) {
        for (int i = Image.getMipmapCount(width, height) - 1; i > mipmapLevel; i--) {
          baseOffset += getPixelCountForMipmap(width, height, i) * channelCount * layersPerMipmap;
        }
      } else {
        for (int i = 0; i < mipmapLevel; i++) {
          baseOffset += getPixelCountForMipmap(width, height, i) * channelCount * layersPerMipmap;
        }
      }
    }

    List<DoubleChannel> channels = new ArrayList<>(channelCount);
    if (parallelChannels) {
      // Arrange channels in a sequence with a stride of 1 (e.g. channel values are not interleaved)
      // and the offset for each channel is shifted by the number of elements used by previous channels.
      for (int i = 0; i < channelCount; i++) {
        channels.add(new DoubleChannel(data, baseOffset + i * channelLength, 1, channelLength));
      }
    } else {
      // Arrange channels packed together, so each has a stride of channelCount with an offset
      // shifted from 0 to channelCount - 1 (which achieves the interleaving)
      for (int i = 0; i < channelCount; i++) {
        channels.add(new DoubleChannel(data, baseOffset + i, channelCount, channelLength));
      }
    }

    return channels;
  }

  @SuppressWarnings("unchecked")
  private RasterImage<T> createRaster(
      DoubleSource data, int mipmapLevel, int arrayLayer, int maxLayers) {
    // Create layout for image
    int rasterWidth, rasterHeight;
    if (mipmapLevel < 0) {
      // No mipmaps
      rasterWidth = width;
      rasterHeight = height;
    } else {
      rasterWidth = Image.getMipmapDimension(width, mipmapLevel);
      rasterHeight = Image.getMipmapDimension(height, mipmapLevel);
    }

    PixelLayout layout;
    if (tileWidth < 0 || tileHeight < 0) {
      // Use default row major layout
      layout = new RowMajorLayout(rasterWidth, rasterHeight);
    } else {
      // Tiled image requested
      layout = new TiledLayout(rasterWidth, rasterHeight, tileWidth, tileHeight);
    }

    // Create channels, separate alpha channel, and map o appropriate color adapter
    List<DoubleChannel> channels = createChannelsForRaster(
        data, mipmapLevel, arrayLayer, maxLayers);
    DoubleChannel alpha = includeAlpha ? channels.remove(channels.size() - 1) : null;

    PixelAdapter adapter;
    if (CMYK.class.isAssignableFrom(colorType)) {
      adapter = new CMYKAdapter(channels.get(0), channels.get(1), channels.get(2), channels.get(3));
    } else if (Depth.class.isAssignableFrom(colorType)) {
      adapter = new DepthAdapter(colorType, channels.get(0));
    } else if (DepthStencil.class.isAssignableFrom(colorType)) {
      // FIXME this needs an int source, so it needs special handling from much higher up
      throw new UnsupportedOperationException("NOT IMPLEMENTED YET");
    } else if (HLS.class.isAssignableFrom(colorType)) {
      adapter = new HLSAdapter(channels.get(0), channels.get(1), channels.get(2));
    } else if (HSV.class.isAssignableFrom(colorType)) {
      adapter = new HSVAdapter(channels.get(0), channels.get(1), channels.get(2));
    } else if (Lab.class.isAssignableFrom(colorType)) {
      adapter = new LabAdapter(colorType, channels.get(0), channels.get(1), channels.get(2));
    } else if (LMS.class.isAssignableFrom(colorType)) {
      adapter = new LMSAdapter(channels.get(0), channels.get(1), channels.get(2));
    } else if (Luminance.class.isAssignableFrom(colorType)) {
      adapter = new LuminanceAdapter(channels.get(0));
    } else if (Luv.class.isAssignableFrom(colorType)) {
      adapter = new LuvAdapter(channels.get(0), channels.get(1), channels.get(2));
    } else if (RGB.class.isAssignableFrom(colorType)) {
      adapter = new RGBAdapter(colorType, channels.get(0), channels.get(1), channels.get(2));
    } else if (XYZ.class.isAssignableFrom(colorType)) {
      adapter = new XYZAdapter(channels.get(0), channels.get(1), channels.get(2));
    } else if (YCbCr.class.isAssignableFrom(colorType)) {
      adapter = new YCbCrAdapter(colorType, channels.get(0), channels.get(1), channels.get(2));
    } else if (YUV.class.isAssignableFrom(colorType)) {
      adapter = new YUVAdapter(colorType, channels.get(0), channels.get(1), channels.get(2));
    } else if (Yyx.class.isAssignableFrom(colorType)) {
      adapter = new YyxAdapter(channels.get(0), channels.get(1), channels.get(2));
    } else {
      throw new UnsupportedOperationException(
          "Unknown color type with no default adapter: " + colorType);
    }

    return new DefaultRasterImage<>(layout, adapter, alpha);
  }

  private static int getChannelCount(Class<? extends Color> colorType) {
    if (CMYK.class.isAssignableFrom(colorType)) {
      return 4;
    } else if (DepthStencil.class.isAssignableFrom(colorType)) {
      return 2;
    } else if (Depth.class.isAssignableFrom(colorType) || Luminance.class
        .isAssignableFrom(colorType)) {
      return 1;
    } else {
      return 3;
    }
  }

  private static long getPixelCount(int width, int height, int layers, boolean mipmapped) {
    long size = mipmapped ? getPixelCountForMipmapSet(width, height)
        : getPixelCountForRaster(width, height);
    return size * layers;
  }

  private static long getPixelCountForMipmap(int width, int height, int level) {
    return getPixelCountForRaster(
        Image.getMipmapDimension(width, level), Image.getMipmapDimension(height, level));
  }

  private static long getPixelCountForMipmapSet(int width, int height) {
    long size = 0;
    int mipmapCount = Image.getMipmapCount(width, height);
    for (int i = 0; i < mipmapCount; i++) {
      size += getPixelCountForMipmap(width, height, i);
    }
    return size;
  }

  private static long getPixelCountForRaster(int width, int height) {
    return width * height;
  }
}
