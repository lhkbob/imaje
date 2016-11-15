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

import java.util.Iterator;
import java.util.Spliterator;

/**
 * DataLayout
 * ==========
 *
 * DataLayout describes a mapping from a two-dimensional array of vector values (color channels and
 * optional alpha) to a pure one dimensional sequence. This mapping is critical for describing how
 * the actual pixel data can be put into a {@link com.lhkbob.imaje.data.DataBuffer}.
 *
 * Within the context of DataLayout, a pixel's state is represented by some number of bands. The
 * exact number of bands depends on the number of color channels and alpha channel that is desired
 * and the {@link RootPixelArray} implementation that interacts with the layout. A band value for a
 * single pixel refers to a single primitive element in the laid-out buffer. The PixelArray
 * implementations have requirements on the band count for compatible layouts.
 *
 * For example, the {@link PackedPixelArray} requires a the band count to be `1`. All channel values
 * that the array holds are packed into a single primitive per pixel so the layout does not need to
 * manage multiple bands. The {@link UnpackedPixelArray} however has one band per color channel, and
 * an additional band if it is to have an alpha channel.
 *
 * In the case of layouts with multiple bands per pixel, the order of bands is independent of the
 * logical color channel order that is used in the {@link PixelArray} methods. The band order here
 * refers to which band comes first in the in the data buffer once mapped to a single dimension. A
 * {@link PixelFormat} can be used to map between the logical color channel ordering and the
 * particular bands in a multi-band layout.
 *
 * There are two primary DataLayout implementations for uncompressed images. {@link ScanlineLayout}
 * arranges data in a row-major way, with all band values for a pixel packed together. This is
 * equivalent to the layout used by a default {@link java.awt.image.BufferedImage} and what is
 * assumed for OpenGL or Vulkan image data. It is a very optimized and simple implementation with
 * little flexibility. {@link TileInterleaveLayout} is a more complex layout that can rearrange the
 * pixels into consecutive tiles and group bands for all pixels together in contiguous ranges. It is
 * slower but very flexible.
 *
 * @author Michael Ludwig
 */
public interface DataLayout extends Iterable<ImageCoordinate> {
  /**
   * BlockVisitor
   * ============
   *
   * Functional interface that is invoked when the layout iterates over a requested window of
   * the data. The layout breaks apart the 2D window into contiguous row segments and invokes
   * {@link #visit(int, int, int, int, long[])}. The starting `x` and `y` coordinate of the
   * row segment is provided with the number of pixels in row segment so that progress through
   * the requested window can be tracked by the visitor.
   *
   * @author Michael Ludwig
   */
  @FunctionalInterface
  interface BlockVisitor {
    /**
     * Visit the contiguous pixel elements of this layout. Continuity is with respect to the
     * one-dimensional arrangement of data within a DataBuffer. Actual elements, per band, are
     * continuous up to a factor of `stride`. Regardless of the internals of the layout, all band
     * values can be iterated over as follows:
     *
     * ```!java
     *    for (int b = 0; b < bandOffsets.length; b++) {
     *      for (int i = 0; i < length; i++) {
     *        long offset = bandOffsets[b] + i * stride;
     *        // offset now is the data offset for the b band value of pixel (x + i, y)
     *      }
     *    }
     * ```
     *
     * If `stride == 1`, then each band is truly continuous and copying actions can be optimized
     * per-band by doing a bulk copy based on `length` and the particular band offset. If `stride ==
     * bandOffsets.length` and each band offset is 1 past the first, then all pixel data for the row
     * segment is contiuous and can be copied based on the first band offset and `length * stride`.
     *
     * The `x` and `y` coordinates for the start of the visited row segment are provided so that
     * visitor can track overall progress through the requested iteration window.
     *
     * @param x
     *     The x coordinate of the first pixel in the row segment
     * @param y
     *     The y coordinate of the second pixel iln the row segment
     * @param stride
     *     The number of primitives to advance in each band for the next pixel in the row
     * @param length
     *     The number of pixels in the row segment
     * @param bandOffsets
     *     The calculated offsets for each band at `(x, y)`
     */
    void visit(int x, int y, int stride, int length, long[] bandOffsets);
  }

  /**
   * @return The height of the 2D array mapped to a single dimension by this layout
   */
  int getHeight();

  /**
   * Compute offsets for every band into a DataBuffer arranged according to this layout. The
   * computed offsets are calculated such that the one dimensional indices uniquely represent the
   * provided `(x, y)` location for each band.
   *
   * The length of `bandOffsets` must be equal to {@link #getBandCount()}. The computed offsets will
   * be written into `bandOffsets`.
   *
   * This is equivalent to calling {@link #getBandOffset(int, int, int)} for each band and storing
   * them into a `long[]` but is potentially much more efficient (especially when accumulating time
   * over all pixels in a large image).
   *
   * @param x
   *     The x coordinate in the 2D coordinate frame
   * @param y
   *     The y coordinate in the 2D coordinate frame
   * @param bandOffsets
   *     The array to hold the computed offsets
   * @throws IndexOutOfBoundsException
   *     if `x` is less than 0 or greater than or equal to the width, or if `y` is less than 0 or
   *     greater than or equal to the height
   * @throws IllegalArgumentException
   *     if `bandOffsets.length` does not equal the band count
   */
  void getBandOffsets(int x, int y, long[] bandOffsets);

  /**
   * Compute `band`'s offset into a DataBuffer arranged according to this layout. The
   * computed offset is calculated such that the one dimensional index uniquely represent the
   * provided `(x, y)` location for the requested band.
   *
   * `band` must be between `0` and `getBandCount() - 1`.
   *
   * @param x
   *     The x coordinate in the 2D coordinate frame
   * @param y
   *     The y coordinate in the 2D coordinate frame
   * @param band
   *     The band to compute
   * @throws IndexOutOfBoundsException
   *     if `x` is less than 0 or greater than or equal to the width, or if `y` is less than 0 or
   *     greater than or equal to the height, or if `band` is less than 0 or greater than or
   *     equal to the band count
   */
  long getBandOffset(int x, int y, int band);

  /**
   * @return The number of bands that this layout arranges into a single dimension
   */
  int getBandCount();

  /**
   * Get the required number of primitive elements for a DataBuffer that would contain the data for
   * an image arranged by this layout. All computed offsets provided by {@link #getBandOffset(int,
   * int, int)} and {@link #getBandOffsets(int, int, long[])} will be between 0 and the returned
   * value (exclusive).
   *
   * By default, this assumes the layout does not compress data across pixels, in which case it
   * requires `width X height x #bands` primitives.
   *
   * @return The required minimum length of a DataBuffer holding data associated with this layout
   */
  default long getRequiredDataElements() {
    return getWidth() * getHeight() * getBandCount();
  }

  /**
   * Get whether or not this layout is compatible with how GPUs expect data to be arranged into an
   * array. Specifically, this should return true if pixels are ordered row-major with band values
   * interleaved per-pixel. This is equivalent to the layout described by {@link ScanlineLayout},
   * although it is possible for {@link TileInterleaveLayout} to be configured in a compatible
   * manner.
   *
   * This does not need to take into account whether or not any DataBuffer used to store the
   * pixel data is accessible to the GPU.
   *
   * @return True if the layout is directly compatible with the GPU's expected pixel layout
   */
  boolean isGPUCompatible();

  /**
   * @return The width of the 2D array mapped to a single dimension by this layout
   */
  int getWidth();

  /**
   * Iterate over the given window described by `x`, `y`, `width`, and `height` using the bulk
   * paradigm supported by {@link BlockVisitor}. This is equivalent to {@link
   * #iterateWindow(ImageWindow, BlockVisitor)} with an ImageWindow formed by `x`, `y`, `width`, and
   * `height`.
   *
   * @param x
   *     The x coordinate of the window
   * @param y
   *     The y coordinate of the window
   * @param width
   *     The width of the window
   * @param height
   *     The height of the window
   * @param receiver
   *     The receiver that is visited for each continuous row segment within the window
   */
  void iterateWindow(int x, int y, int width, int height, BlockVisitor receiver);

  /**
   * Iterate over the given `window` within the 2D array of this layout. If `window` extends
   * past the valid boundary of this data then it is effectively clamped to the dimensions of
   * this layout so that only valid pixel locations will be reported to `receiver`.
   *
   * This iteration pattern breaks the requested window into row segments of continuous data,
   * with respect to how the 2D array is mapped to one dimension. By breaking it into continuous
   * sections, based on a implementation dependent stride, tile sizing, etc. certain operations can
   * be handled in bulk without needing to proceed pixel by pixel.
   *
   * @param window
   *     The window to iterate over
   * @param receiver
   *     The receiver that is visited for each continuous row segment within the window
   * @see BlockVisitor#visit(int, int, int, int, long[])
   */
  default void iterateWindow(ImageWindow window, BlockVisitor receiver) {
    iterateWindow(window.getX(), window.getY(), window.getWidth(), window.getHeight(), receiver);
  }

  /**
   * Iterate over a row segment within the 2D array described by this layout. The row
   * starts at `(x, y)` and extends across `width` pixels. This is equivalent to calling
   * {@link #iterator(int, int, int, int)} with a height of ` pixel.
   *
   * @param x
   *     The x coordinate of the row segment
   * @param y
   *     The y coordinate of the row segment
   * @param width
   *     The width of the row segment
   * @param receiver
   *     The receiver that is visited for each continuous block within the row segment
   */
  default void iterateRow(int x, int y, int width, BlockVisitor receiver) {
    iterateWindow(x, y, width, 1, receiver);
  }

  /**
   * Get a new Iterator that iterates over a subregion of the 2D array, as described by the
   * method arguments. This is equivalent to {@link #iterator(ImageWindow)} with an ImageWindow
   * formed by `x`, `y`, `width`, and `height`.
   *
   * @param x
   *     The x coordinate of the window
   * @param y
   *     The y coordinate of the window
   * @param width
   *     The width of the window
   * @param height
   *     The height of the window
   * @return A new Iterator over the subregion
   *
   * @see #iterator(ImageWindow)
   */
  Iterator<ImageCoordinate> iterator(int x, int y, int width, int height);

  /**
   * Get a new Iterator that iterates over all coordinates that are within `window`. If `window`
   * exceeds the edges of this 2D array described by layout, only valid pixel locations will be
   * returned by the iterator.
   *
   * The order of coordinates returned by the iterator is not defined. Implementations can choose to
   * simply iterate from left to right and bottom to top. However, it is recommended to iterate over
   * coordinates in the most cache-friendly manner given the details of the implementation.
   *
   * The returned iterator does not support removing elements. It also uses "fast" iteration where a
   * single ImageCoordinate instance is reused for every call to `next()` on the iterator.
   * Modifications to the ImageCoordinate's state should not confuse the iterator and will be
   * overridden on future calls to `next()`.
   *
   * @param window
   *     The window to iterate over
   * @return A new fast Iterator for valid coordinates within `window`
   */
  default Iterator<ImageCoordinate> iterator(ImageWindow window) {
    return iterator(window.getX(), window.getY(), window.getWidth(), window.getHeight());
  }

  /**
   * Get a new Spliterator that iterates over a subregion of the 2D array, as described by the
   * method arguments. This is equivalent to {@link #spliterator(ImageWindow)} with an ImageWindow
   * formed by `x`, `y`, `width`, and `height`.
   *
   * @param x
   *     The x coordinate of the window
   * @param y
   *     The y coordinate of the window
   * @param width
   *     The width of the window
   * @param height
   *     The height of the window
   * @return A new Spliterator over the subregion
   *
   * @see #spliterator(ImageWindow)
   */
  Spliterator<ImageCoordinate> spliterator(int x, int y, int width, int height);

  /**
   * Get a new Spliterator that iterates over all coordinates that are within `window`. If `window`
   * exceeds the edges of this 2D array described by layout, only valid pixel locations will be
   * returned by the spliterator.
   *
   * The order of coordinates returned by the spliterator is not defined. Implementations can choose
   * to simply iterate from left to right and bottom to top. However, it is recommended to iterate
   * over coordinates in the most cache-friendly manner given the details of the implementation.
   *
   * The returned spliterator uses "fast" iteration where a single ImageCoordinate instance is
   * reused every time a Consumer is invoked for elements of the spliterator. Modifications to the
   * ImageCoordinate's state should not confuse the spliterator. Splitting the spliterator, if
   * supported, should correctly use a new ImageCoordinate for the new sub-Spliterator.
   *
   * @param window
   *     The window to iterate over
   * @return A new fast Iterator for valid coordinates within `window`
   */
  default Spliterator<ImageCoordinate> spliterator(ImageWindow window) {
    return spliterator(window.getX(), window.getY(), window.getWidth(), window.getHeight());
  }

  /**
   * @return Get a new Iterator that iterates over the entire 2D array of coordinates
   *
   * @see #iterator(ImageWindow)
   */
  @Override
  default Iterator<ImageCoordinate> iterator() {
    return iterator(0, 0, getWidth(), getHeight());
  }

  /**
   * @return Get a new Spliterator that iterates over the entire 2D array of coordinates
   *
   * @see #spliterator(ImageWindow)
   */
  @Override
  default Spliterator<ImageCoordinate> spliterator() {
    return spliterator(0, 0, getWidth(), getHeight());
  }

  /**
   * @return Create a new DataLayoutBuilder used to configure and instantiate a new DataLayout
   */
  static DataLayoutBuilder newBuilder() {
    return new DataLayoutBuilder();
  }
}
