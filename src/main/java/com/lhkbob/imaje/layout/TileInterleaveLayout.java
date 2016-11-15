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
package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.Functions;
import com.lhkbob.imaje.util.IteratorChain;
import com.lhkbob.imaje.util.SpliteratorChain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Function;

/**
 * TileInterleaveLayout
 * ====================
 *
 * A very flexible DataLayout implementation that breaks the logical 2D array into tiles of
 * configurable dimensions. Within each tile the pixels are arranged left to right, bottom to top.
 * The tiles are arranged from left to right and bottom to top as well, covering the entire 2D
 * space. If the image dimensions are not multiples of the tile dimensions then the last row and/or
 * column of tiles will have smaller dimensions than the rest of the tiles in the row or column.
 *
 * This layout also supports arranging band values per pixel in different ways. This is configured
 * by the assigned {@link InterleavingUnit}, which goes into more detail. Depending on the tile
 * dimension used for a layout, tiling can be disabled. This can be combined with a non-PIXEL
 * interleaving policy to have pixel coordinates arranged like a scanline layout but with a
 * different band interleaving style. Similarly, the PIXEL policy can be combined with tiling to
 * have bands arranged like the standard scanline layout but where blocks of pixels are arranged in
 * tiles.
 *
 * While it is possible to create a TileInterleaveUnit that is equivalent to a ScanlineLayout of
 * the same image dimensions, it should be avoided for performance reasons. Band offset calculations
 * are much heavier weight in this implementation.
 *
 * @author Michael Ludwig
 */
public class TileInterleaveLayout implements DataLayout {
  /**
   * InterleavingUnit
   * ================
   *
   * Specifies the band interleaving policy for a data layout. This controls how the
   * TileInterleaveLayout arranges band values for pixels. It can either have all bands for a pixel
   * packed adjacently together (PIXEL), all values of a band for a scanline of a tile packed
   * together (SCANLINE), all values of a band for a tile packed together (TILE), or all values of a
   * band for the entire image packed together (IMAGE).
   *
   * @author Michael Ludwig
   */
  public enum InterleavingUnit {
    /**
     * Every band value for a pixel is packed together in adjacent primitives. Pixels are then
     * arranged in the data buffer according to the tiling and dimensions of the layout.
     */
    PIXEL, /**
     * A band's values for every pixel in a scanline are packed together, followed by the next
     * band's values for the same scanline row. The scanline row is restricted by the tiling (i.e.
     * scanline interleaving is more fine-grained than tile interlreaving).
     */
    SCANLINE, /**
     * A band's values for every pixel within a tile are packed together, followed by the next
     * band's values for the same tile. The tile blocks (holding every band values) are arranged
     * according to the image and tile dimensions.
     */
    TILE, /**
     * A band's values for the entire image (arranged according to the tile and image dimensions)
     * are packed together as subsequent primitive elements. The next band's values are arranged the
     * same immediately following the previous band's data.
     */
    IMAGE
  }

  private final int bandCount;
  private final int hangingTileHeight; // remainder if tileHeight does not evenly divide imageHeight
  private final int hangingTileWidth; // remainder if tileWidth does not evenly divide imageWidth
  private final int imageHeight; // height in pixels of the image
  private final int imageWidth; // width in pixels of the image
  private final InterleavingUnit interleave;
  private final int tileColumnCount; // total number of full tile columns (excluding a hanging tile)
  private final int tileHeight; // full height in pixels of a tile
  private final int tileRowCount; // total number of full tile rows (excluding any hanging tile)
  private final int tileWidth; // full width in pixels of a tile

  /**
   * Create a new TileInterleaveLayout with the given image dimensions and number of bands per
   * pixel. The tile dimensions will be equal to the image dimensions, effectively disabling tiling.
   * The band interleaving will be PIXEL. Thus, this constructor creates a layout that is equivalent
   * to that of {@link ScanlineLayout}.
   *
   * @param imageWidth
   *     The image width
   * @param imageHeight
   *     The image height
   * @param bandCount
   *     The number of bands per pixel
   * @throws IllegalArgumentException
   *     if `imageWidth`, `imageHeight`, or `bandCount` are not positive
   */
  public TileInterleaveLayout(int imageWidth, int imageHeight, int bandCount) {
    this(imageWidth, imageHeight, imageWidth, imageHeight, bandCount);
  }

  /**
   * Create a new TileInterleaveLayout with the given image dimensions, tile dimensions, and
   * bands per pixel. If the tile dimensions are greater than or equal to the image dimensions
   * then tiling is effectively disabled along that corresponding dimension. Band interleaving
   * will be set to PIXEL.
   *
   * @param imageWidth
   *     The image width
   * @param imageHeight
   *     The image height
   * @param tileWidth
   *     The tile width within the image
   * @param tileHeight
   *     The tile height within the image
   * @param bandCount
   *     The number of bands per pixel
   * @throws IllegalArgumentException
   *     if any argument is not positive
   */
  public TileInterleaveLayout(
      int imageWidth, int imageHeight, int tileWidth, int tileHeight, int bandCount) {
    this(imageWidth, imageHeight, tileWidth, tileHeight, bandCount, InterleavingUnit.PIXEL);
  }

  /**
   * Create a new TileInterleaveLayout with the given image dimensions, tile dimensions, and
   * bands per pixel. If the tile dimensions are greater than or equal to the image dimensions
   * then tiling is effectively disabled along that corresponding dimension. Band interleaving
   * will be set to `interleave`.
   *
   * @param imageWidth
   *     The image width
   * @param imageHeight
   *     The image height
   * @param tileWidth
   *     The tile width within the image
   * @param tileHeight
   *     The tile height within the image
   * @param bandCount
   *     The number of bands per pixel
   * @param interleave
   *     The band interleaving policy
   * @throws IllegalArgumentException
   *     if any argument is not positive
   */
  public TileInterleaveLayout(
      int imageWidth, int imageHeight, int tileWidth, int tileHeight, int bandCount,
      InterleavingUnit interleave) {
    Arguments.isPositive("imageWidth", imageWidth);
    Arguments.isPositive("imageHeight", imageHeight);
    Arguments.isPositive("tileWidth", tileWidth);
    Arguments.isPositive("tileHeight", tileHeight);
    Arguments.isPositive("bandCount", bandCount);

    this.bandCount = bandCount;
    this.interleave = interleave;

    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
    this.tileWidth = tileWidth;
    this.tileHeight = tileHeight;

    tileColumnCount = imageWidth / tileWidth;
    tileRowCount = imageHeight / tileHeight;
    hangingTileWidth = imageWidth - tileWidth * tileColumnCount;
    hangingTileHeight = imageHeight - tileHeight * tileRowCount;
  }

  @Override
  public int getBandCount() {
    return bandCount;
  }

  @Override
  public long getBandOffset(int x, int y, int band) {
    Arguments.checkIndex("channel", bandCount, band);
    checkImageBounds(x, y);

    int tileX = x / tileWidth;
    int tileY = y / tileHeight;

    int withinTileX = x % tileWidth;
    int withinTileY = y % tileHeight;

    int actualTileWidth = tileX >= tileColumnCount ? hangingTileWidth : tileWidth;
    int actualTileHeight = tileY >= tileRowCount ? hangingTileHeight : tileHeight;

    // PIXEL
    int tileStride = bandCount;
    int withinTileStride = bandCount;
    int pixelStride = bandCount;
    int offset = 1;
    switch (interleave) {
    case SCANLINE:
      tileStride = bandCount;
      withinTileStride = bandCount;
      pixelStride = 1;
      offset = actualTileWidth;
      break;
    case TILE:
      tileStride = bandCount;
      withinTileStride = 1;
      pixelStride = 1;
      offset = actualTileWidth * actualTileHeight;
      break;
    case IMAGE:
      tileStride = 1;
      withinTileStride = 1;
      pixelStride = 1;
      offset = imageWidth * imageHeight;
      break;
    }

    return tileStride * (tileY * tileHeight * imageWidth + tileX * tileWidth * actualTileHeight)
        + withinTileStride * withinTileY * actualTileWidth + pixelStride * withinTileX
        + band * offset;
  }

  @Override
  public void getBandOffsets(int x, int y, long[] bandOffsets) {
    Arguments.equals("channelIndices.length", bandCount, bandOffsets.length);
    checkImageBounds(x, y);

    int tileX = x / tileWidth;
    int tileY = y / tileHeight;

    int withinTileX = x % tileWidth;
    int withinTileY = y % tileHeight;

    int actualTileWidth = tileX >= tileColumnCount ? hangingTileWidth : tileWidth;
    int actualTileHeight = tileY >= tileRowCount ? hangingTileHeight : tileHeight;

    // PIXEL
    int tileStride = bandCount;
    int withinTileStride = bandCount;
    int pixelStride = bandCount;
    int offset = 1;
    switch (interleave) {
    case SCANLINE:
      tileStride = bandCount;
      withinTileStride = bandCount;
      pixelStride = 1;
      offset = actualTileWidth;
      break;
    case TILE:
      tileStride = bandCount;
      withinTileStride = 1;
      pixelStride = 1;
      offset = actualTileWidth * actualTileHeight;
      break;
    case IMAGE:
      tileStride = 1;
      withinTileStride = 1;
      pixelStride = 1;
      offset = imageWidth * imageHeight;
      break;
    }

    long base =
        tileStride * (tileY * tileHeight * imageWidth + tileX * tileWidth * actualTileHeight)
            + withinTileStride * withinTileY * actualTileWidth + pixelStride * withinTileX;
    for (int i = 0; i < bandOffsets.length; i++) {
      bandOffsets[i] = base + i * offset;
    }
  }

  @Override
  public int getHeight() {
    return imageHeight;
  }

  /**
   * @return The interleaving configuration for bands with this layout
   */
  public InterleavingUnit getInterleavingUnit() {
    return interleave;
  }

  /**
   * Get the tile height of the layout. If this is greater than or equal to {@link #getHeight()}
   * then this layout effectively is not tiled along the vertical dimension, although its bands will
   * still be arranged according to the configured {@link #getInterleavingUnit() interleaving unit}.
   *
   * @return The height of tiles in this layout
   */
  public int getTileHeight() {
    return tileHeight;
  }

  /**
   * Get the tile width of the layout. If this is greater than or equal to {@link #getHeight()} then
   * this layout effectively is not tiled along the horizontal dimension, although its bands will
   * still be arranged according to the configured {@link #getInterleavingUnit() interleaving unit}.
   *
   * @return The width of tiles in this layout
   */
  public int getTileWidth() {
    return tileWidth;
  }

  @Override
  public int getWidth() {
    return imageWidth;
  }

  @Override
  public boolean isGPUCompatible() {
    return interleave == InterleavingUnit.PIXEL && tileWidth == imageWidth
        && tileHeight == imageHeight;
  }

  @Override
  public void iterateWindow(
      int x, int y, int width, int height, BlockVisitor receiver) {
    Arguments.notNull("receiver", receiver);
    x = getContainedX(x);
    y = getContainedY(y);
    width = getContainedWidth(x, width);
    height = getContainedHeight(y, height);

    int stride = interleave == InterleavingUnit.PIXEL ? bandCount : 1;
    long[] offsets = new long[bandCount];
    for (int row = y; row < y + height; row++) {
      // The offsets for the continuous block will be be based on (x, row). However, depending on
      // where within the tile x falls, then the row itself will need to be split apart into
      // smaller blocks so it doesn't cross tile boundaries.
      int col = x;
      while (col < width) {
        int farEdge = ((col / tileWidth) + 1) * tileWidth;
        // Do not exceed the window width, which may be less than a full tile width at the end.
        // But since x + width is contained within imageWidth, farEdge is also inside imageWidth.
        if (farEdge > width) {
          farEdge = width;
        }

        getBandOffsets(col, row, offsets);
        receiver.visit(col, row, stride, farEdge - col, offsets);
        col = farEdge;
      }
    }
  }

  @Override
  public Iterator<ImageCoordinate> iterator(int x, int y, int width, int height) {
    List<ImageCoordinate.FastIterator> subBlockIterators = iterateWindow(
        x, y, width, height, ImageCoordinate.FastIterator::new);
    return new IteratorChain<>(subBlockIterators);
  }

  @Override
  public Spliterator<ImageCoordinate> spliterator(int x, int y, int width, int height) {
    List<ImageCoordinate.FastSpliterator> subBlockIterators = iterateWindow(
        x, y, width, height, ImageCoordinate.FastSpliterator::new);
    return new SpliteratorChain<>(subBlockIterators);
  }

  private void checkImageBounds(int x, int y) {
    Arguments.checkIndex("x", imageWidth, x);
    Arguments.checkIndex("y", imageHeight, y);
  }

  private int getContainedHeight(int y, int height) {
    Arguments.isPositive("height", height);
    return Functions.clamp(height, 1, imageHeight - y);
  }

  private int getContainedWidth(int x, int width) {
    Arguments.isPositive("width", width);
    return Functions.clamp(width, 1, imageWidth - x);
  }

  private int getContainedX(int x) {
    return Functions.clamp(x, 0, imageWidth - 1);
  }

  private int getContainedY(int y) {
    return Functions.clamp(y, 0, imageHeight - 1);
  }

  private <T> List<T> iterateWindow(
      int x, int y, int width, int height, Function<ImageWindow, T> ctor) {
    x = getContainedX(x);
    y = getContainedY(y);
    width = getContainedWidth(x, width);
    height = getContainedHeight(y, height);

    List<T> blocks = new ArrayList<>();
    ImageWindow window = new ImageWindow();

    int cy = y;
    while (cy < y + height) {
      int top;
      if (cy % tileHeight != 0) {
        // Not on a tile edge so the top edge will up to the top edge of current tile
        top = ((cy / tileHeight) + 1) * tileHeight;
      } else {
        // Advance by tile height
        top = cy + tileHeight;
      }
      // Make sure we don't exceed the requested window height
      if (top > y + height) {
        top = y + height;
      }

      // Start at the left edge
      int cx = x;
      while (cx < x + width) {
        int right;
        if (cx % tileWidth != 0) {
          // Not on a tile edge so the right edge will be up to the right edge of the current tile
          right = ((cx / tileWidth) + 1) * tileWidth;
        } else {
          // Just advance by tile width
          right = cx + tileWidth;
        }

        // Make sure we don't exceed the requested window dimensions however
        if (right > x + width) {
          right = x + width;
        }

        window.setX(cx);
        window.setY(cy);
        window.setWidth(right - cx - 1);
        window.setHeight(top - cy - 1);

        blocks.add(ctor.apply(window));

        // Advance left edge to the right - next tile
        cx = right;
      }
      // Row complete, advance bottom edge to the top - next tile row
      cy = top;
    }

    return blocks;
  }
}
