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
package com.lhkbob.imaje.io.exr;

import com.lhkbob.imaje.io.InvalidImageException;
import com.lhkbob.imaje.layout.DataLayout;
import com.lhkbob.imaje.layout.TileInterleaveLayout;
import com.lhkbob.imaje.layout.InvertedLayout;
import com.lhkbob.imaje.util.Arguments;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public class OpenEXRHeader {
  private final List<Channel> channels;
  private final Compression compression;
  private final Box2Int dataWindow;
  private final Box2Int displayWindow;
  private final LineOrder lineOrder;
  private final float pixelAspectRatio;
  private final V2F screenWindowCenter;
  private final float screenWindowWidth;

  private final TileDescription tiles; // only used for tiled images

  private final String view; // only used in multipart files for stereo

  // only used in multipart and deep files
  private final String name;
  private final PartFormat type; // must match high-level image format
  private final int version; // should be 1
  private final int chunkCount;

  private final int maxSamplesPerPixel; // only used for deep images
  private final Chromaticity chromaticity; // only used for images with RGB data or CIE XYZ data

  private final Map<String, Attribute> allUnknownAttrs;

  private final int bytesPerPixel; // Sum of all channel's bytes based on channel type

  private final DataLayout[] levelLayouts;
  private final int levelStride;

  // FIXME calculate the data layouts for all levels in the image

  public OpenEXRHeader(
      List<Channel> channels, Compression compression, Box2Int dataWindow, Box2Int displayWindow,
      LineOrder lineOrder, float pixelAspectRatio, V2F screenWindowCenter, float screenWindowWidth,
      @Arguments.Nullable TileDescription tiles, @Arguments.Nullable String view,
      @Arguments.Nullable String name, @Arguments.Nullable Integer version,
      @Arguments.Nullable Integer chunkCount, @Arguments.Nullable Integer maxSamplesPerPixel,
      @Arguments.Nullable Chromaticity chromaticity, PartFormat type,
      Map<String, Attribute> allUnknownAttrs) throws InvalidImageException {
    this.allUnknownAttrs = Collections.unmodifiableMap(new HashMap<>(allUnknownAttrs));
    this.channels = Collections.unmodifiableList(new ArrayList<>(channels));
    this.compression = compression;
    this.dataWindow = dataWindow;
    this.displayWindow = displayWindow;
    this.lineOrder = lineOrder;
    this.pixelAspectRatio = pixelAspectRatio;
    this.screenWindowCenter = screenWindowCenter;
    this.screenWindowWidth = screenWindowWidth;
    this.tiles = tiles;
    this.view = view;
    this.name = name;
    this.version = version == null ? 1 : version; // Default version is 1
    this.chunkCount = chunkCount == null ? 0 : chunkCount;
    this.maxSamplesPerPixel = maxSamplesPerPixel == null ? 0 : maxSamplesPerPixel;
    this.chromaticity = chromaticity;
    this.type = type;

    int byteChannelCount = 0;
    for (Channel c : channels) {
      byteChannelCount += c.getFormat().getByteCount();
    }
    bytesPerPixel = byteChannelCount;

    if (tiles != null && tiles.getLevelMode() != LevelMode.ONE_LEVEL) {
      // MIPMAP or RIPMAP
      if (tiles.getLevelMode() == LevelMode.MIPMAP_LEVELS) {
        levelStride = 0; // Ignore the ly argument
        levelLayouts = new DataLayout[tiles.getLevelCountX(dataWindow)];

        for (int lx = 0; lx < levelLayouts.length; lx++) {
          Box2Int chunk = tiles.getLevelDataWindow(dataWindow, lx, lx);
          levelLayouts[lx] = new InvertedLayout(
              new TileInterleaveLayout(chunk.width(), chunk.height(), tiles.getWidth(), tiles.getHeight(),
                  channels.size(), TileInterleaveLayout.InterleavingUnit.SCANLINE), false, true);
        }
      } else {
        int numX = tiles.getLevelCountX(dataWindow);
        int numY = tiles.getLevelCountY(dataWindow);

        levelStride = numX;
        levelLayouts = new DataLayout[numX * numY];

        for (int ly = 0; ly < numY; ly++) {
          for (int lx = 0; lx < numX; lx++) {
            Box2Int chunk = tiles.getLevelDataWindow(dataWindow, lx, ly);
            levelLayouts[lx + ly * numX] = new InvertedLayout(
                new TileInterleaveLayout(chunk.width(), chunk.height(), tiles.getWidth(),
                    tiles.getHeight(), channels.size(), TileInterleaveLayout.InterleavingUnit.SCANLINE),
                false, true);
          }
        }
      }
    } else {
      // Only a single level of data is provided
      levelStride = 0; // Ignore lx and ly arguments
      levelLayouts = new DataLayout[1];

      int tileWidth, tileHeight;
      if (tiles != null) {
        // The layout is tiled
        tileWidth = tiles.getWidth();
        tileHeight = tiles.getHeight();
      } else {
        // The layout is a simple scanline
        tileWidth = dataWindow.width();
        tileHeight = dataWindow.height();
      }

      levelLayouts[0] = new InvertedLayout(
          new TileInterleaveLayout(dataWindow.width(), dataWindow.height(), tileWidth, tileHeight,
              channels.size(), TileInterleaveLayout.InterleavingUnit.SCANLINE), false, true);
    }
  }

  public DataLayout getLayoutForMipmap(int level) {
    if (levelLayouts.length == 1) {
      if (level != 0) {
        throw new IndexOutOfBoundsException(
            "Bad mipmap level for header without mipmaps: " + level);
      }
      return levelLayouts[0];
    } else {
      if (tiles.getLevelMode() == LevelMode.MIPMAP_LEVELS) {
        // The dual argument version will fail as expected if level is incorrect
        return getLayoutForLevel(level, level);
      } else {
        int maxNumX = tiles.getLevelCountX(dataWindow);
        int maxNumY = tiles.getLevelCountY(dataWindow);
        Arguments.checkIndex("level", Math.max(maxNumX, maxNumY), level);

        // Clamp the lx and ly arguments to the maximum valid for the tile size, this effectively
        // has level follow the diagonal through the ripmap until the shortest boundary is reached
        // at which point that dimension is clamped while the larger continues to be divided.
        return getLayoutForLevel(Math.min(maxNumX - 1, level), Math.min(maxNumY - 1, level));
      }
    }
  }

  public DataLayout getLayoutForLevel(int lx, int ly) {
    return levelLayouts[lx + levelStride * ly];
  }

  public Chromaticity getChromaticity() {
    return chromaticity;
  }

  public int getBytesPerPixel() {
    return bytesPerPixel;
  }

  public Map<String, Attribute> getUnprocessedAttributes() {
    return allUnknownAttrs;
  }

  public int getVersion() {
    return version;
  }

  public PartFormat getFormat() {
    return type;
  }

  public List<Channel> getChannels() {
    return channels;
  }

  public Compression getCompression() {
    return compression;
  }

  public Box2Int getDataWindow() {
    return dataWindow;
  }

  public Box2Int getDisplayWindow() {
    return displayWindow;
  }

  public LineOrder getLineOrder() {
    return lineOrder;
  }

  public int getMaxSamplesPerPixel() {
    return maxSamplesPerPixel;
  }

  public int getChunkCount() {
    return chunkCount;
  }

  public String getName() {
    return name;
  }

  public String getView() {
    return view;
  }

  public float getPixelAspectRatio() {
    return pixelAspectRatio;
  }

  public float getScreenWindowWidth() {
    return screenWindowWidth;
  }

  public V2F getScreenWindowCenter() {
    return screenWindowCenter;
  }

  public TileDescription getTileDescription() {
    return tiles;
  }

  private static <T> T requiredAttr(
      String name, String type, Class<T> cls, Map<String, Attribute> attrs) throws
      InvalidImageException {
    if (!attrs.containsKey(name) || !Objects.equals(attrs.get(name).getType(), type)) {
      throw new InvalidImageException(
          "File missing required attribute (" + name + ") with type " + type);
    }
    return cls.cast(attrs.remove(name).getValue());
  }

  private static <T> T optionalAttr(
      String name, String type, Class<T> cls, Map<String, Attribute> attrs) {
    if (!attrs.containsKey(name) || !Objects.equals(attrs.get(name).getType(), type)) {
      return null;
    }
    return cls.cast(attrs.remove(name).getValue());
  }

  public static OpenEXRHeader read(
      ImageFormat format, SeekableByteChannel in, ByteBuffer work) throws IOException {
    List<Attribute> listAttrs = TypeReader.readAll(in, work, Attribute::read);
    // Convert to a key-based map
    Map<String, Attribute> allAttrs = new HashMap<>();
    for (Attribute a : listAttrs) {
      allAttrs.put(a.getName(), a);
    }

    // required attributes
    @SuppressWarnings("unchecked") List<Channel> channels = requiredAttr(
        "channels", "chlist", List.class, allAttrs);
    Compression compression = requiredAttr(
        "compression", "compression", Compression.class, allAttrs);
    Box2Int dataWindow = requiredAttr("dataWindow", "box2i", Box2Int.class, allAttrs);
    Box2Int displayWindow = requiredAttr("displayWindow", "box2i", Box2Int.class, allAttrs);
    LineOrder lineOrder = requiredAttr("lineOrder", "lineorder", LineOrder.class, allAttrs);
    float pixelAspectRatio = requiredAttr("pixelAspectRatio", "float", Float.class, allAttrs);
    V2F screenWindowCenter = requiredAttr("screenWindowCenter", "v2f", V2F.class, allAttrs);
    float screenWindowWidth = requiredAttr("screenWindowWidth", "float", Float.class, allAttrs);

    // optional attributes
    TileDescription tiles = optionalAttr("tiles", "tiledesc", TileDescription.class, allAttrs);
    String view = optionalAttr("view", "string", String.class, allAttrs);
    String name = optionalAttr("name", "string", String.class, allAttrs);
    Integer version = optionalAttr("version", "int", Integer.class, allAttrs);
    Integer chunkCount = optionalAttr("chunkCount", "int", Integer.class, allAttrs);
    Integer maxSamplesPerPixel = optionalAttr("maxSamplesPerPixel", "int", Integer.class, allAttrs);
    Chromaticity chromaticity = optionalAttr(
        "chromaticities", "chromaticities", Chromaticity.class, allAttrs);

    String typeName = optionalAttr("type", "string", String.class, allAttrs);
    PartFormat type;
    if (typeName == null) {
      // Determine it based on the image format
      if (format == ImageFormat.SCANLINE) {
        type = PartFormat.SCANLINE;
      } else if (format == ImageFormat.TILE) {
        type = PartFormat.TILE;
      } else {
        throw new InvalidImageException(
            "Header specific type is required for deep or multipart data");
      }
    } else {
      switch (typeName) {
      case "scanlineimage":
        type = PartFormat.SCANLINE;
        break;
      case "tiledimage":
        type = PartFormat.TILE;
        break;
      case "deepscanline":
        type = PartFormat.DEEP_SCANLINE;
        break;
      case "deeptile":
        type = PartFormat.DEEP_TILE;
        break;
      default:
        throw new IllegalStateException("Unsupported type value: " + typeName);
      }
    }

    OpenEXRHeader h = new OpenEXRHeader(channels, compression, dataWindow, displayWindow, lineOrder,
        pixelAspectRatio, screenWindowCenter, screenWindowWidth, tiles, view, name, version,
        chunkCount, maxSamplesPerPixel, chromaticity, type, allAttrs);
    h.validate(format);
    return h;
  }

  private void validate(ImageFormat format) throws InvalidImageException {
    if (format == ImageFormat.TILE && tiles == null) {
      throw new InvalidImageException("Single-part tile image requires a tile description");
    }

    if (format == ImageFormat.DEEP || format == ImageFormat.MULTIPART
        || format == ImageFormat.MULTIPART_DEEP) {
      // verify required 2.0 attributes are present
      if (name == null) {
        throw new InvalidImageException("Header name is required for multipart and deep images");
      }
      if (type == PartFormat.TILE || type == PartFormat.DEEP_TILE) {
        // make sure tile description is specified in this case as well
        if (tiles == null) {
          throw new InvalidImageException(
              "Multi-part image requires a tile description for " + name);
        }
      }
    }

    if (format == ImageFormat.DEEP || format == ImageFormat.MULTIPART_DEEP) {
      // deep-data only validation
      if (maxSamplesPerPixel <= 0) {
        throw new InvalidImageException(
            "Max samples per pixel is not specified for deep data image in " + name);
      }
      if (version != 1) {
        throw new InvalidImageException(
            "Unsupported deep-pixel version number: " + version + " in part " + name);
      }
      if (type != PartFormat.DEEP_TILE && type != PartFormat.DEEP_SCANLINE) {
        throw new InvalidImageException(
            "Unsupported part format for deep-data image: " + type + " in part " + name);
      }
    } else {
      // Make sure type is not deep
      if (type == PartFormat.DEEP_SCANLINE || type == PartFormat.DEEP_TILE) {
        throw new InvalidImageException(
            "Unsupported format for regular image: " + type + " in part " + name);
      }
    }
  }
}
