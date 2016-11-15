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

/**
 * PixelArray
 * ==========
 *
 * PixelArray is the core interface for describing and accessing a two dimensional array of color
 * values. The interface represents colors as just a `double[]`, making it easy to integrate with
 * type-safe color definitions without forcing color space definitions into the data layout
 * specification. PixelArrays are intentionally limited to just two dimensions; more complex image
 * types (such as mipmaps or arrays) can be formed by a collection of PixelArrays.
 *
 * The PixelArray interface provides functionality to get and set color and alpha (transparency)
 * values for specific pixels. PixelArrays can also be composed together to dynamically transform
 * or re-interpet the underlying data. The {@link RootPixelArray} sub-interface that actively
 * defines the pixel data, while other implementations can wrap an existing PixelArray (becoming
 * the parent) and then delegate and modify the parent's data. Examples of behavior achieved
 * with composition:
 *
 * + {@link ReorientedArray}: rearranging the data (treat as column-major, or change directionality
 * of axis).
 * + {@link SubImagePixelArray}: Select a dynamic sub-image.
 * + {@link VirtualWindowArray}: Virtual image that extends beyond the defined data.
 * + {@link PremultipliedAlphaArray}: Interprets the parent's data as having pre-multiplied alpha,
 * seamlessly transforming channels into non-multiplied values.
 * + {@link TransformedPixelArray}: Dynamically applies a color transformation to the parent's
 * pixel data.
 * + {@link ReadOnlyArray}: Disables modifying the pixel data.
 *
 * Several RootPixelArray implementations for concretely representing pixel data are also provided.
 * While RootPixelArray documents details of what's required, the options are briefly highlighted
 * here:
 *
 * + {@link UnpackedPixelArray}: Each channel value is a primitive element in the same DataBuffer,
 * the layout determining how channels and pixels are arranged in the 1D buffer. The format
 * defines how the logical color channels map to primitives (such as reording RGB as BGR).
 * + {@link PackedPixelArray}: Every channel value for a single pixel is packed into a single
 * primitive of a DataBuffer. The layout determines how each pixel primitive is arranged in the
 * 1D buffer, and the format determines how channels are extracted from the primitive.
 * + {@link MultiBufferArray}: Similar to the unpacked pixel array except that each channel is
 * stored in its own DataBuffer. Each data buffer shares the same layout for how 2D pixels are
 * mapped to a single dimension.
 * + {@link SharedExponentArray}: Every channel for a pixel is packed into a primitive along side a
 * shared exponent to compactly represent HDR data. See {@link
 * com.lhkbob.imaje.data.types.UnsignedSharedExponent}.
 *
 * Use {@link PixelArrayBuilder} to conveniently and fluently create new PixelArrays that
 * automatically pick and combine the implementations described above.
 *
 * # Coordinate system
 *
 * PixelArray has certain default assumptions about the XY coordinate system it uses to access
 * pixels. Technically, the orientation of the image is entirely up to application code and the
 * semantics that it uses. However, for consistency and ease of documentation the default assumed
 * coordinate frame and orientation for a PixelArray is:
 *
 * 1. The origin of the image is located in the bottom left corner.
 * 2. The X axis goes from left to right.
 * 3. The Y axis goes from bottom to top.
 * 4. This is consistent with the Cartesian coordinate system used in mathematics, but has a flipped
 * Y axis direction and origin location compared to how images and user interfaces are often
 * described.
 * 5. The X axis is defined from 0 to `width - 1`.
 * 6. The Y axis is defined from 0 to `height - 1`.
 * 7. Pixels are described by discrete, integer coordinates in the XY plane. Sampling and
 * interpolating between integer pixel locations is handled in the {@link com.lhkbob.imaje.sampler
 * sampler} package.
 *
 * If image data does not fit these orientation and layout assumptions, the {@link ReorientedArray}
 * can be used to automatically transform into this default coordinate frame.
 *
 * @author Michael Ludwig
 */
public interface PixelArray {
  /**
   * Fetch the color channel values and alpha value for the pixel at `(x, y)`. The color values will
   * be stored into `channelValues` and the alpha value is returned. The length of `channelValues`
   * must equal {@link #getColorChannelCount()}. The order of color channel values written into
   * `channelValues` is the logical order that is defined by the various Color types in the {@link
   * com.lhkbob.imaje.color color} package. For example, if an array represents an RGB color then
   * the channel values will contain R, G, and B in that order, regardless of if the underlying data
   * orders them as RGB or BGR.
   *
   * If {@link #hasAlphaChannel()} returns false then this will always return `1.0`.
   *
   * This getter variant should be preferred for one-off pixel lookups that may not have an
   * allocated `long[]` to store the band offsets. Implementations should endeavor to avoid
   * just allocating a `long[]` and delegating to {@link #get(int, int, double[], long[])}.
   *
   * @param x
   *     The x coordinate of the pixel to access
   * @param y
   *     The y coordinate of the pixel to access
   * @param channelValues
   *     The array to store the pixel's color channel values
   * @return The alpha value of the pixel
   *
   * @throws IndexOutOfBoundsException
   *     if `x` is less than 0 or greater than or equal to the width of the image, or if `y` is less
   *     than 0 or greater than equal to the height of the image
   * @throws IllegalArgumentException
   *     if `channelValues.length` does not equal the color channel count of the array
   */
  double get(int x, int y, double[] channelValues);

  /**
   * Fetch the color channel values and alpha value for the pixel at `(x, y)`. The color values will
   * be stored into `channelValues` and the alpha value is returned. The length of `channelValues`
   * must equal {@link #getColorChannelCount()}. The order of color channel values written into
   * `channelValues` is the logical order that is defined by the various Color types in the {@link
   * com.lhkbob.imaje.color color} package. For example, if an array represents an RGB color then
   * the channel values will contain R, G, and B in that order, regardless of if the underlying data
   * orders them as RGB or BGR.
   *
   * If {@link #hasAlphaChannel()} returns false then this will always return `1.0`.
   *
   * This getter variant should be preferred when multiple pixel values will be accessed in a short
   * period of time and the `bandOffsets` array can be reused between them. Implementations for this
   * method can then rely on {@link DataLayout#getBandOffsets(int, int, long[])} to compute
   * all offsets in one go, which is often more efficient. The length of `bandOffsets` must
   * equal {@link #getBandCount()}.
   *
   * @param x
   *     The x coordinate of the pixel to access
   * @param y
   *     The y coordinate of the pixel to access
   * @param channelValues
   *     The array to store the pixel's color channel values
   * @param bandOffsets
   *     The array to store the band offsets for each band of the pixel
   * @return The alpha value of the pixel
   *
   * @throws IndexOutOfBoundsException
   *     if `x` is less than 0 or greater than or equal to the width of the image, or if `y` is less
   *     than 0 or greater than equal to the height of the image
   * @throws IllegalArgumentException
   *     if `channelValues.length` does not equal the color channel count of the array, or if
   *     `bandOffsets.length` does not equal the band count of the array
   */
  double get(int x, int y, double[] channelValues, long[] bandOffsets);

  /**
   * Get the alpha value stored for the pixel at `(x, y)`. If {@link #hasAlphaChannel()} returns
   * false then this will always return `1.0`.
   *
   * @param x
   *     The x coordinate of the pixel to access
   * @param y
   *     The y coordinate of the pixel to access
   * @return The alpha value of the pixel
   *
   * @throws IndexOutOfBoundsException
   *     if `x` is less than 0 or greater than or equal to the width of the image, or if `y` is less
   *     than 0 or greater than equal to the height of the image
   */
  double getAlpha(int x, int y);

  /**
   * Update the color channel values and alpha value for the pixel at `(x, y)`. The array's data
   * will be modified to store the given color channel values in `channelValues`. The length of
   * `channelValues` must equal {@link #getColorChannelCount()}. The order of color channel values
   * read from `channelValues` is the logical order that is defined by the various Color types in
   * the {@link com.lhkbob.imaje.color color} package. For example, if an array represents an RGB
   * color then the channel values will contain R, G, and B in that order, regardless of if the
   * underlying data re-orders them as RGB or BGR when updating the pixel data.
   *
   * If {@link #hasAlphaChannel()} returns false then the alpha value will be ignored.
   *
   * This setter variant should be preferred for one-off pixel modifications that may not have an
   * allocated `long[]` to store the band offsets. Implementations should endeavor to avoid
   * just allocating a `long[]` and delegating to {@link #set(int, int, double[], double, long[])}.
   *
   * @param x
   *     The x coordinate of the pixel to access
   * @param y
   *     The y coordinate of the pixel to access
   * @param channelValues
   *     The array of new color channel values
   * @param a
   *     The new alpha value of the pixel
   * @throws IndexOutOfBoundsException
   *     if `x` is less than 0 or greater than or equal to the width of the image, or if `y` is less
   *     than 0 or greater than equal to the height of the image
   * @throws IllegalArgumentException
   *     if `channelValues.length` does not equal the color channel count of the array
   */
  void set(int x, int y, double[] channelValues, double a);

  /**
   * Update the color channel values and alpha value for the pixel at `(x, y)`. The array's data
   * will be modified to store the given color channel values in `channelValues`. The length of
   * `channelValues` must equal {@link #getColorChannelCount()}. The order of color channel values
   * read from `channelValues` is the logical order that is defined by the various Color types in
   * the {@link com.lhkbob.imaje.color color} package. For example, if an array represents an RGB
   * color then the channel values will contain R, G, and B in that order, regardless of if the
   * underlying data re-orders them as RGB or BGR when updating the pixel data.
   *
   * If {@link #hasAlphaChannel()} returns false then the alpha value will be ignored.
   *
   * This setter variant should be preferred when multiple pixel values will be modified in a short
   * period of time and the `bandOffsets` array can be reused between them. Implementations for this
   * method can then rely on {@link DataLayout#getBandOffsets(int, int, long[])} to compute
   * all offsets in one go, which is often more efficient. The length of `bandOffsets` must
   * equal {@link #getBandCount()}.
   *
   * @param x
   *     The x coordinate of the pixel to access
   * @param y
   *     The y coordinate of the pixel to access
   * @param channelValues
   *     The array of new color channel values
   * @param a
   *     The new alpha value of the pixel
   * @param bandOffsets
   *     The array to store the band offsets for each band of the pixel
   * @throws IndexOutOfBoundsException
   *     if `x` is less than 0 or greater than or equal to the width of the image, or if `y` is less
   *     than 0 or greater than equal to the height of the image
   * @throws IllegalArgumentException
   *     if `channelValues.length` does not equal the color channel count of the array, or if
   *     `bandOffsets.length` does not equal the band count of the array
   */
  void set(int x, int y, double[] channelValues, double a, long[] bandOffsets);

  /**
   * Set the alpha value stored for the pixel at `(x, y)`. If {@link #hasAlphaChannel()} returns
   * false then this will do nothing.
   *
   * @param x
   *     The x coordinate of the pixel to access
   * @param y
   *     The y coordinate of the pixel to access
   * @param alpha
   *     The alpha value of the pixel
   * @throws IndexOutOfBoundsException
   *     if `x` is less than 0 or greater than or equal to the width of the image, or if `y` is less
   *     than 0 or greater than equal to the height of the image
   */
  void setAlpha(int x, int y, double alpha);

  /**
   * Get whether or not this PixelArray is read-only. If an array is read-only, then all methods
   * that modify the state of a pixel will be silently ignored. It may technically be possible to
   * bypass an array's read-only state by walking up its {@link #getParent() parent} chain until a
   * non-read-only image is found. Doing so produces undefined behavior with respect to this pixel
   * array and is not recommended.
   *
   * @return True if this PixelArray is read-only
   */
  boolean isReadOnly();

  /**
   * Get the parent PixelArray of this array. The parent array is the source of data that this array
   * delegates to or dynamically modifies. A null parent implies that the pixel array is a root
   * array and defines its own pixel data. Only PixelArray implementations that also implement
   * {@link RootPixelArray} can return null. All others must return the non-null array that they are
   * composed with.
   *
   * @return This array's parent array that it's composed with
   */
  PixelArray getParent();

  /**
   * Get the width of this PixelArray, which is the rightmost extent of the 2D array that can be
   * accessed.
   *
   * @return The width of the image, always at least 1.
   */
  int getWidth();

  /**
   * Get the height of this PixelArray, which is the uppermost extent of the 2D array that
   * can be accessed.
   *
   * @return The height of the image, always at least 1.
   */
  int getHeight();

  /**
   * Get the number of color channels that are defined per-pixel for this PixelArray. This does
   * not include the presence of an alpha channel in the returned value. It is not necessarily
   * equal to the {@link #getBandCount() band count} or its parent's color channel count.
   *
   * The `double[] channelValues` arguments in the pixel-accessor methods must have a length
   * equal to the returned color channel count.
   *
   * @return The color channel count of this array
   */
  int getColorChannelCount();

  /**
   * Get whether or not the array has an alpha channel value for each pixel in the image. If this
   * returns true then the `double` returned by the pixel-getters are per-pixel alpha values and the
   * pixel-setters will store the provided alpha value. If it returns false, the pixel-getters will
   * always return an alpha of `1.0` and the setters will ignore the alpha value.
   *
   * @return True if the image has an alpha channel
   */
  boolean hasAlphaChannel();

  /**
   * Get the number of bands used to store the color and alpha data per pixel. The band count does
   * not necessarily equal the color channel count. It may also include a band for the alpha
   * channel, or it may be significantly different if the array has some form of within-primitive
   * data packing.
   *
   * The `long[] bandOffsets` arguments in the pixel-accessor methods must have a length equal to
   * the returned band count.
   *
   * @return The band count of this array
   */
  int getBandCount();

  /**
   * Transform the `(x, y)` pixel coordinate in this image's coordinate frame to its parent's
   * coordinate frame. If the PixelArray has no parent, or if it does not modify its coordinate
   * frame with respect to its parent, then `(x, y)` should be returned as is. Otherwise the
   * returned coordinate should represent the location of the pixel in the parent that `(x, y)`
   * refers to in this image.
   *
   * Unlike the pixel-accessors, which fail if the coordinates reference an out-of-bounds pixel,
   * `toParentCoordinate` can operate on out-of-bounds pixels. This is to allow pixel locations that
   * may be defined in the parent but not the child to computed.
   *
   * This is the inverse of {@link #fromParentCoordinate(ImageCoordinate)}.
   *
   * @param x
   *     The x coordinate in this image's coordinate frame
   * @param y
   *     The y coordinate in this image's coordinate frame
   * @return The equivalent pixel coordinate with respect to the image's parent's coordinate frame
   */
  default ImageCoordinate toParentCoordinate(int x, int y) {
    ImageCoordinate c = new ImageCoordinate(x, y);
    toParentCoordinate(c);
    return c;
  }

  /**
   * Transform the `(x, y)` values in `coord` from this image's coordinate frame to its parent's
   * coordinate frame. If the PixelArray has no parent, or if it does not modify its coordinate
   * frame with respect to its parent, then `coord` should not be modified. Otherwise the `coord`
   * should be updated *in-place* to represent the location of the pixel in the parent that `coord`
   * refers to in this image.
   *
   * Unlike the pixel-accessors, which fail if the coordinates reference an out-of-bounds pixel,
   * `toParentCoordinate` can operate on out-of-bounds pixels. This is to allow pixel locations that
   * may be defined in the parent but not the child to computed.
   *
   * This is the inverse of {@link #fromParentCoordinate(ImageCoordinate)}.
   *
   * @param coord
   *     The pixel coordinate in this image's coordinate frame, which will be updated to be
   *     in the parent's coordinate frame
   */
  void toParentCoordinate(ImageCoordinate coord);

  /**
   * Transform the `(x, y)` pixel coordinate in its parent's coordinate frame to this image's
   * coordinate frame. If the PixelArray has no parent, or if it does not modify its coordinate
   * frame with respect to its parent, then `(x, y)` should be returned as is. Otherwise the
   * returned coordinate should represent the location of the pixel in this image that `(x, y)`
   * refers to in the parent.
   *
   * Unlike the pixel-accessors, which fail if the coordinates reference an out-of-bounds pixel,
   * `fromParentCoordinate` can operate on out-of-bounds pixels. This is to allow pixel locations
   * that may be defined in the parent but not the child to computed. The returned coordinate may
   * then be out of bounds for this array's actual data.
   *
   * This is the inverse of {@link #toParentCoordinate(ImageCoordinate)}.
   *
   * @param x
   *     The x coordinate in this image's parent's coordinate frame
   * @param y
   *     The y coordinate in this image's parent's coordinate frame
   * @return The equivalent pixel coordinate with respect to the image's coordinate frame
   */
  default ImageCoordinate fromParentCoordinate(int x, int y) {
    ImageCoordinate c = new ImageCoordinate(x, y);
    fromParentCoordinate(c);
    return c;
  }

  /**
   * Transform the `(x, y)` values in `coord` from its parent's coordinate frame to this image's
   * coordinate frame. If the PixelArray has no parent, or if it does not modify its coordinate
   * frame with respect to its parent, then `coord` should not be modified. Otherwise the `coord`
   * should be updated *in-place* to represent the location of the pixel in this image that `coord`
   * refers to in the parent.
   *
   * Unlike the pixel-accessors, which fail if the coordinates reference an out-of-bounds pixel,
   * `fromParentCoordinate` can operate on out-of-bounds pixels. This is to allow pixel locations
   * that may be defined in the parent but not the child to computed. The update coordinate may then
   * be out of bounds for this array's actual data.
   *
   * This is the inverse of {@link #toParentCoordinate(ImageCoordinate)}.
   *
   * @param coord
   *     The pixel coordinate in this image's parent's coordinate frame, which will be updated to be
   *     in this image's coordinate frame
   */
  void fromParentCoordinate(ImageCoordinate coord);

  /**
   * Transform the `width X height` window at `(x, y)` window in this image's coordinate frame to
   * its parent's coordinate frame. If the PixelArray has no parent, or if it does not modify its
   * coordinate frame with respect to its parent, then the window should be returned as is.
   * Otherwise the returned windowe should represent the equivalent pixel block in the parent
   * coordinate frame.
   *
   * Unlike the pixel-accessors, which fail if the coordinates reference an out-of-bounds pixel,
   * `toParentCWindow` can operate on out-of-bounds pixel blocks. This is to allow pixel locations
   * that may be defined in the parent but not the child to computed.
   *
   * This is the inverse of {@link #fromParentWindow(ImageWindow)}.
   *
   * @param x
   *     The x coordinate in this image's coordinate frame
   * @param y
   *     The y coordinate in this image's coordinate frame
   * @param width
   *     The width of the window in this coordinate frame
   * @param height
   *     The height of the window in this coordinate frame
   * @return The equivalent pixel block with respect to the image's parent's coordinate frame
   */
  default ImageWindow toParentWindow(int x, int y, int width, int height) {
    ImageWindow w = new ImageWindow(x, y, width, height);
    toParentWindow(w);
    return w;
  }

  /**
   * Transform the `window` from this image's coordinate frame to its parent's coordinate frame. If
   * the PixelArray has no parent, or if it does not modify its coordinate frame with respect to its
   * parent, then `window` should not be modified. Otherwise the `window` should be updated
   * *in-place* to represent the equivalent pixel block in the parent coordinate frame.
   *
   * Unlike the pixel-accessors, which fail if the coordinates reference an out-of-bounds pixel,
   * `toParentCWindow` can operate on out-of-bounds pixel blocks. This is to allow pixel locations
   * that may be defined in the parent but not the child to computed.
   *
   * This is the inverse of {@link #fromParentWindow(ImageWindow)}.
   *
   * @param window
   *     The pixel block in this image's coordinate frame, which will be updated to be
   *     in the parent's coordinate frame
   */
  void toParentWindow(ImageWindow window);

  /**
   * Transform the `width X height` window at `(x, y)` in its parent's coordinate frame to this
   * image's coordinate frame. If the PixelArray has no parent, or if it does not modify its
   * coordinate frame with respect to its parent, then the window should be returned as is.
   * Otherwise the returned window should represent the pixel block in this image that the
   * window refers to in the parent.
   *
   * Unlike the pixel-accessors, which fail if the coordinates reference an out-of-bounds pixel,
   * `fromParentCoordinate` can operate on out-of-bounds blocks. This is to allow pixel locations
   * that may be defined in the parent but not the child to computed. The updated block may then be
   * out of bounds, or extend partially beyond this array's actual data.
   *
   * This is the inverse of {@link #toParentWindow(ImageWindow)}.
   *
   * @param x
   *     The x coordinate in this image's parent's coordinate frame
   * @param y
   *     The y coordinate in this image's parent's coordinate frame
   * @param width
   *     The width of the window in the parent's coordinate frame
   * @param height
   *     The height of the window in the parent's coordinate frame
   * @return The equivalent pixel block with respect to the image's coordinate frame
   */
  default ImageWindow fromParentWindow(int x, int y, int width, int height) {
    ImageWindow w = new ImageWindow(x, y, width, height);
    fromParentWindow(w);
    return w;
  }

  /**
   * Transform the `window` from its parent's coordinate frame to this image's coordinate frame. If
   * the PixelArray has no parent, or if it does not modify its coordinate frame with respect to its
   * parent, then `window` should not be modified. Otherwise the `window` should be updated
   * *in-place* to represent the pixel block in this image that `window` refers to in the parent.
   *
   * Unlike the pixel-accessors, which fail if the coordinates reference an out-of-bounds pixel,
   * `fromParentCoordinate` can operate on out-of-bounds blocks. This is to allow pixel locations
   * that may be defined in the parent but not the child to computed. The updated block may then be
   * out of bounds, or extend partially beyond this array's actual data.
   *
   * This is the inverse of {@link #toParentWindow(ImageWindow)}.
   *
   * @param window
   *     The pixel block in this image's parent's coordinate frame, which will be updated to be
   *     in this image's coordinate frame
   */
  void fromParentWindow(ImageWindow window);
}
