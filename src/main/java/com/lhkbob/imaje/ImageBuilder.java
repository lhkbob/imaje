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
package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.DataBuffer;
import com.lhkbob.imaje.data.NumericData;
import com.lhkbob.imaje.layout.DataLayout;
import com.lhkbob.imaje.layout.DataLayoutBuilder;
import com.lhkbob.imaje.layout.GeneralLayout;
import com.lhkbob.imaje.layout.PackedPixelArray;
import com.lhkbob.imaje.layout.PixelArray;
import com.lhkbob.imaje.layout.PixelFormat;
import com.lhkbob.imaje.layout.PixelFormatBuilder;
import com.lhkbob.imaje.layout.UnpackedPixelArray;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.DataBufferBuilder;

import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public abstract class ImageBuilder<T extends Color, I extends Image<T>, B extends ImageBuilder<T, I, B>> {
  public static class OfMipmap<T extends Color> extends ImageBuilder<T, Mipmap<T>, OfMipmap<T>> {
    public OfMipmap(Class<T> color) {
      super(color);
    }

    @Override
    public Mipmap<T> build() {
      return newMipmap();
    }
  }

  public static class OfMipmapArray<T extends Color> extends ImageBuilder<T, MipmapArray<T>, OfMipmapArray<T>> {
    public OfMipmapArray(Class<T> color) {
      super(color);
    }

    @Override
    public MipmapArray<T> build() {
      return newMipmapArray();
    }

    @Override
    public OfMipmapArray<T> layers(int count) {
      return super.layers(count);
    }
  }

  public static class OfMipmapVolume<T extends Color> extends ImageBuilder<T, MipmapVolume<T>, OfMipmapVolume<T>> {
    public OfMipmapVolume(Class<T> color) {
      super(color);
    }

    @Override
    public MipmapVolume<T> build() {
      return newMipmapVolume();
    }

    @Override
    public OfMipmapVolume<T> depth(int depth) {
      return super.depth(depth);
    }

    @Override
    public OfMipmapVolume<T> layers(int count) {
      return super.layers(count);
    }
  }

  public static class OfRaster<T extends Color> extends ImageBuilder<T, Raster<T>, OfRaster<T>> {
    public OfRaster(Class<T> color) {
      super(color);
    }

    @Override
    public Raster<T> build() {
      return newRaster();
    }
  }

  public static class OfRasterArray<T extends Color> extends ImageBuilder<T, RasterArray<T>, OfRasterArray<T>> {
    public OfRasterArray(Class<T> color) {
      super(color);
    }

    @Override
    public RasterArray<T> build() {
      return newRasterArray();
    }

    @Override
    public OfRasterArray<T> layers(int count) {
      return super.layers(count);
    }
  }

  public static class OfVolume<T extends Color> extends ImageBuilder<T, Volume<T>, OfVolume<T>> {
    public OfVolume(Class<T> color) {
      super(color);
    }

    @Override
    public Volume<T> build() {
      return newVolume();
    }

    @Override
    public OfVolume<T> depth(int depth) {
      return super.depth(depth);
    }
  }

  private final Class<T> colorType;
  private final PixelFormatBuilder formatBuilder;
  private final DataLayoutBuilder layoutBuilder;
  private int depth;
  private T fillColor;
  private int layers;
  private boolean packed;

  // {layer/depth: {mipmap: data source}}
  private final Map<Integer, Map<Integer, Object>> data;

  protected ImageBuilder(Class<T> colorType) {
    Arguments.notNull("colorType", colorType);

    this.colorType = colorType;
    formatBuilder = new PixelFormatBuilder();
    layoutBuilder = new DataLayoutBuilder();
    packed = false;
    fillColor = null;

    data = new HashMap<>();

    defaultFormat();
    defaultDataLayout();
    defaultData();
  }

  public abstract I build();

  public B abgr() {
    formatBuilder.channels(PixelFormat.ALPHA_CHANNEL, 2, 1, 0);
    return builder();
  }

  public B pixelsLeftToRight() {
    layoutBuilder.leftToRight();
    return builder();
  }

  public B pixelsRightToLeft() {
    layoutBuilder.rightToLeft();
    return builder();
  }

  public B pixelsTopToBottom() {
    layoutBuilder.topToBottom();
    return builder();
  }

  public B pixelsBottomToTop() {
    layoutBuilder.bottomToTop();
    return builder();
  }

  public B channelsByPixel() {
    layoutBuilder.interleave(GeneralLayout.InterleavingUnit.PIXEL);
    return builder();
  }

  public B channelsByScanline() {
    layoutBuilder.interleave(GeneralLayout.InterleavingUnit.SCANLINE);
    return builder();
  }

  public B channelsByTile() {
    layoutBuilder.interleave(GeneralLayout.InterleavingUnit.TILE);
    return builder();
  }

  public B channelsByImage() {
    layoutBuilder.interleave(GeneralLayout.InterleavingUnit.IMAGE);
    return builder();
  }

  public B argb() {
    formatBuilder.channels(PixelFormat.ALPHA_CHANNEL, 0, 1, 2);
    return builder();
  }

  public B defaultData() {
    data.clear();
    return newData(Data.getDefaultDataFactory());
  }

  public B newData(Data.Factory factory) {
    return newDataFor(0, 0, factory);
  }

  public B newDataForMipmap(int mipmap, Data.Factory factory) {
    return newDataFor(0, mipmap, factory);
  }

  public B newDataForLayer(int layer, Data.Factory factory) {
    return newDataFor(layer, 0, factory);
  }

  public B newDataFor(int layer, int mipmap, Data.Factory factory) {
    Arguments.notNull("factory", factory);
    return setDataFor(layer, mipmap, factory);
  }

  public B newArray() {
    return newData(Data.arrayDataFactory());
  }

  public B newArrayForMipmap(int mipmap) {
    return newDataForMipmap(mipmap, Data.arrayDataFactory());
  }

  public B newArrayForLayer(int layer) {
    return newDataForLayer(layer, Data.arrayDataFactory());
  }

  public B newArrayFor(int layer, int mipmap) {
    return newDataFor(layer, mipmap, Data.arrayDataFactory());
  }

  public B newBuffer() {
    return newData(Data.bufferDataFactory());
  }

  public B newBufferForMipmap(int mipmap) {
    return newDataForMipmap(mipmap, Data.bufferDataFactory());
  }

  public B newBufferForLayer(int layer) {
    return newDataForLayer(layer, Data.bufferDataFactory());
  }

  public B newBufferFor(int layer, int mipmap) {
    return newDataFor(layer, mipmap, Data.bufferDataFactory());
  }

  public B existingData(byte[] data) {
    return existingDataFor(0, 0, data);
  }

  public B existingDataForMipmap(int mipmap, byte[] data) {
    return existingDataFor(0, mipmap, data);
  }

  public B existingDataForLayer(int layer, byte[] data) {
    return existingDataFor(layer, 0, data);
  }

  public B existingDataFor(int layer, int mipmap, byte[] data) {
    Arguments.notNull("data", data);
    return setDataFor(layer, mipmap, data);
  }

  public B existingData(short[] data) {
    return existingDataFor(0, 0, data);
  }

  public B existingDataForMipmap(int mipmap, short[] data) {
    return existingDataFor(0, mipmap, data);
  }

  public B existingDataForLayer(int layer, short[] data) {
    return existingDataFor(layer, 0, data);
  }

  public B existingDataFor(int layer, int mipmap, short[] data) {
    Arguments.notNull("data", data);
    return setDataFor(layer, mipmap, data);
  }

  public B existingData(int[] data) {
    return existingDataFor(0, 0, data);
  }

  public B existingDataForMipmap(int mipmap, int[] data) {
    return existingDataFor(0, mipmap, data);
  }

  public B existingDataForLayer(int layer, int[] data) {
    return existingDataFor(layer, 0, data);
  }

  public B existingDataFor(int layer, int mipmap, int[] data) {
    Arguments.notNull("data", data);
    return setDataFor(layer, mipmap, data);
  }

  public B existingData(long[] data) {
    return existingDataFor(0, 0, data);
  }

  public B existingDataForMipmap(int mipmap, long[] data) {
    return existingDataFor(0, mipmap, data);
  }

  public B existingDataForLayer(int layer, long[] data) {
    return existingDataFor(layer, 0, data);
  }

  public B existingDataFor(int layer, int mipmap, long[] data) {
    Arguments.notNull("data", data);
    return setDataFor(layer, mipmap, data);
  }

  public B existingData(float[] data) {
    return existingDataFor(0, 0, data);
  }

  public B existingDataForMipmap(int mipmap, float[] data) {
    return existingDataFor(0, mipmap, data);
  }

  public B existingDataForLayer(int layer, float[] data) {
    return existingDataFor(layer, 0, data);
  }

  public B existingDataFor(int layer, int mipmap, float[] data) {
    Arguments.notNull("data", data);
    return setDataFor(layer, mipmap, data);
  }

  public B existingData(Buffer data) {
    return existingDataFor(0, 0, data);
  }

  public B existingDataForMipmap(int mipmap, Buffer data) {
    return existingDataFor(0, mipmap, data);
  }

  public B existingDataForLayer(int layer, Buffer data) {
    return existingDataFor(layer, 0, data);
  }

  public B existingDataFor(int layer, int mipmap, Buffer data) {
    Arguments.notNull("data", data);
    return setDataFor(layer, mipmap, data);
  }

  public B existingData(DataBuffer data) {
    return existingDataFor(0, 0, data);
  }

  public B existingDataForMipmap(int mipmap, DataBuffer data) {
    return existingDataFor(0, mipmap, data);
  }

  public B existingDataForLayer(int layer, DataBuffer data) {
    return existingDataFor(layer, 0, data);
  }

  public B existingDataFor(int layer, int mipmap, DataBuffer data) {
    Arguments.notNull("data", data);
    return setDataFor(layer, mipmap, data);
  }

  public B clearDataFor(int layer, int mipmap) {
    return setDataFor(layer, mipmap, null);
  }

  private B setDataFor(int layer, int mipmap, Object source) {
    Map<Integer, Object> forLayer = data.get(layer);

    if (source == null) {
      // Remove the data source for the mipmap
      if (forLayer != null) {
        forLayer.remove(mipmap);
        if (forLayer.isEmpty()) {
          data.remove(layer);
        }
      }
    } else {
      // Add the data source
      if (forLayer == null) {
        forLayer = new HashMap<>();
        data.put(layer, forLayer);
      }
      forLayer.put(mipmap, source);
    }

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
    } else if (image instanceof MipmapArray) {
      // Configure format and image layout from first mipmap of first layer
      MipmapArray<?> m = (MipmapArray<?>) image;
      compatibleWith(m.getPixelArray(0, 0));
      layers(m.getLayerCount());
    } else if (image instanceof Volume) {
      // Set format and slice layout based on first slice
      Volume<?> v = (Volume<?>) image;
      compatibleWith(v.getPixelArray(0));
      depth(v.getDepth());
    } else if (image instanceof MipmapVolume) {
      // Set format and slice layout based on first slice of first mipmap
      MipmapVolume<?> v = (MipmapVolume<?>) image;
      compatibleWith(v.getPixelArray(0, 0));
      depth(v.getDepth());
    } else {
      throw new UnsupportedOperationException(
          "Unknown Image implementation, cannot be used to update builder: " + image);
    }

    return builder();
  }

  public B defaultDataLayout() {
    layoutBuilder.interleave(GeneralLayout.InterleavingUnit.PIXEL).leftToRight().bottomToTop().tileHeight(-1).tileWidth(-1).width(1).height(1);
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

  public B format(PixelFormat format) {
    return format(format, false);
  }

  public B packedFormat(PixelFormat format) {
    return format(format, true);
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

    // 2D mipmap only to worry about for count
    int mipmapCount = Images.getMaxMipmaps(layout.getWidth(), layout.getHeight());
    List<PixelArray> mipmaps = new ArrayList<>(mipmapCount);
    for (int i = 0; i < mipmapCount; i++) {
      mipmaps.add(buildPixelArray(format, layout, 0, i));
    }

    Mipmap<T> image = new Mipmap<>(colorType, mipmaps);
    setFillColor(image);
    return image;
  }

  protected MipmapArray<T> newMipmapArray() {
    validateArrayState();
    PixelFormat format = buildAndValidateFormat();
    DataLayout layout = buildLayout(format);

    // 2D mipmap only to worry about for count
    int mipmapCount = Images.getMaxMipmaps(layout.getWidth(), layout.getHeight());
    List<List<PixelArray>> arrayOfMipmaps = new ArrayList<>(layers);
    for (int i = 0; i < layers; i++) {
      List<PixelArray> mipmaps = new ArrayList<>(mipmapCount);
      for (int j = 0; j < mipmapCount; j++) {
        mipmaps.add(buildPixelArray(format, layout, i, j));
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

    // Use all 3 dimensions for mipmap count
    int mipmapCount = Images.getMaxMipmaps(layout.getWidth(), layout.getHeight(), depth);
    List<List<PixelArray>> mipmappedZs = new ArrayList<>(mipmapCount);
    for (int i = 0; i < mipmapCount; i++) {
      int z = Images.getMipmapDimension(depth, i);
      List<PixelArray> volume = new ArrayList<>(z);
      for (int j = 0; j < z; j++) {
        volume.add(buildPixelArray(format, layout, j, i));
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
    PixelArray array = buildPixelArray(format, layout, 0, 0);

    Raster<T> image = new Raster<>(colorType, array);
    setFillColor(image);
    return image;
  }

  protected RasterArray<T> newRasterArray() {
    validateArrayState();
    PixelFormat format = buildAndValidateFormat();
    DataLayout layout = buildLayout(format);

    List<PixelArray> arrays = new ArrayList<>(layers);
    for (int i = 0; i < layers; i++) {
      arrays.add(buildPixelArray(format, layout, i, 0));
    }

    RasterArray<T> image = new RasterArray<>(colorType, arrays);
    setFillColor(image);
    return image;
  }

  protected Volume<T> newVolume() {
    validateVolumeState();
    PixelFormat format = buildAndValidateFormat();
    DataLayout layout = buildLayout(format);

    List<PixelArray> arrays = new ArrayList<>(depth);
    for (int i = 0; i < depth; i++) {
      arrays.add(buildPixelArray(format, layout, i, 0));
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
    // FIXME this logic may not be sufficient if the ImageBuilder is smart enough to support
    // transformed pixel arrays that can map fewer format color channels to more logical color channels
    // (e.g. RG images transformed to Normal).
    if (format.getColorChannelCount() != Color.getChannelCount(colorType)) {
      throw new IllegalStateException(
          "Selected pixel format (" + format + ") incompatible with desired color type: "
              + colorType);
    }
    return format;
  }

  private Object getDataProvider(int layer, int mipmap) {
    Map<Integer, Object> forLayer = data.get(layer);
    // Look within the provided layer's configured data sources
    if (forLayer != null) {
      Object source = forLayer.get(mipmap);
      if (source != null) {
        // Exact data provider specified for layer and mipmap
        return source;
      }
    }

    return null;
  }

  private Object getDataProviderOrDefault(int layer, int mipmap) {
    Object source = getDataProvider(layer, mipmap);
    if (source != null) {
      return source;
    }

    // Exact not found, so check the base level of layer for a data factory
    source = getDataProvider(layer, 0);
    if (source instanceof Data.Factory) {
      return source;
    }

    // Base level in layer wasn't found, so check first layer
    source = getDataProvider(0, 0);
    if (source instanceof Data.Factory) {
      return source;
    }

    // No valid data factory configured in base level, so use the default data factory
    return Data.getDefaultDataFactory();
  }

  private NumericData<?> buildDataSource(
      PixelFormat format, DataLayout layout, int layer, int mipmap) {
    long imageSize =
        Images.getUncompressedImageSize(layout.getWidth(), layout.getHeight()) * layout.getChannelCount();

    Object data = getDataProviderOrDefault(layer, mipmap);
    DataBufferBuilder dataBuilder = new DataBufferBuilder();

    if (data instanceof byte[]) {
      dataBuilder.wrapArray((byte[]) data);
    } else if (data instanceof short[]) {
      dataBuilder.wrapArray((short[]) data);
    } else if (data instanceof int[]) {
      dataBuilder.wrapArray((int[]) data);
    } else if (data instanceof long[]) {
      dataBuilder.wrapArray((long[]) data);
    } else if (data instanceof float[]) {
      dataBuilder.wrapArray((float[]) data);
    } else if (data instanceof double[]) {
      dataBuilder.wrapArray((double[]) data);
    } else if (data instanceof Buffer) {
      dataBuilder.wrapBuffer((Buffer) data);
    } else if (data instanceof DataBuffer) {
      dataBuilder.wrapDataBuffer((DataBuffer) data);
    } else if (data instanceof Data.Factory) {
      dataBuilder.allocateNewData((Data.Factory) data);
    } else {
      // Should not happen given the type safety enforced by current existingData functions
      throw new IllegalStateException("Unsupported data type: " + data);
    }

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
      PixelFormat format, DataLayout layout, int layer, int mipmapLevel) {
    if (mipmapLevel > 0) {
      // Update the layout to use the mipmap dimensions
      layout = new DataLayoutBuilder().compatibleWith(layout)
          .width(Images.getMipmapDimension(layout.getWidth(), mipmapLevel))
          .height(Images.getMipmapDimension(layout.getHeight(), mipmapLevel)).build();
    }

    NumericData<?> data = buildDataSource(format, layout, layer, mipmapLevel);

    // FIXME support selecting the shared exponent formats
    if (packed) {
      return new PackedPixelArray(format, layout, data.asBitData());
    } else {
      return new UnpackedPixelArray(format, layout, data);
    }
  }

  private void compatibleWith(PixelArray data) {
    formatBuilder.compatibleWith(data.getFormat());
    layoutBuilder.compatibleWith(data.getLayout());

    // override layout's dimensions to match the array's and not the layout's, which is
    // necessary if the "compatible" image is actually a subimage view.
    layoutBuilder.width(data.getWidth()).height(data.getHeight());

    // FIXME must handle SharedExponentArray and other PixelArray implementations
    packed = data instanceof PackedPixelArray;
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
