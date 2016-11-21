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

/**
 * SubImagePixelArray
 * ==================
 *
 * SubImagePixelArray is a wrapper PixelArray that accesses a window within its parent based on an
 * offset along the X and Y axis. Unlike {@link VirtualWindowArray}, this wrapping array must be
 * completely contained in the parent image's dimensions (as per {@link
 * ImageWindow#isContainedInImage(int, int)}. As a wrapper, any modifications made via this array
 * directly affect the parent's state, and any modifications to the parent's state (within the
 * window) will be visible in this array.
 *
 * The window's lower-left corner in the parent PixelArray is mapped to the origin of the
 * SubImagePixelArray, and the sub-image array's dimensions are that of the window.
 *
 * @author Michael Ludwig
 */
public class SubImagePixelArray implements PixelArray {
  private final PixelArray parent;

  private final int offsetX;
  private final int offsetY;
  private final int width;
  private final int height;

  /**
   * Create a SubImagePixelArray that represents the given `window` within the parent's
   * coordinate space. This is equivalent to `new SubImagePixelArray(parent, window.getX(),
   * window.getY(), window.getWidth(), window.getHeight())`.
   *
   * @param parent
   *     The parent array
   * @param window
   *     The window to access within parent
   * @throws NullPointerException
   *     if `parent` or `window` are null
   * @throws IndexOutOfBoundsException
   *     if the window is not contained in `parent`'s dimensions
   */
  public SubImagePixelArray(PixelArray parent, ImageWindow window) {
    this(parent, window.getX(), window.getY(), window.getWidth(), window.getHeight());
  }

  /**
   * Create a SubImagePixelArray that represents a window within the parent's coordinate space.
   * The window is fully contained within the pixel data defined by `parent`. The pixel of this
   * image at `(0,0)` corresponds to `(x,y)` in `parent`. This array's reported dimensions are
   * `w X h`.
   *
   * @param parent
   *     The array this sub-image accesses
   * @param x
   *     The x offset into parent for this image's origin
   * @param y
   *     The y offset into parent for this image's origin
   * @param w
   *     The width of this array
   * @param h
   *     The height of this array
   * @throws NullPointerException
   *     if `parent` is null
   * @throws IndexOutOfBoundsException
   *     `x`, `y`, `w`, and `h` define a region not contained with in `parent`'s dimensions.
   */
  public SubImagePixelArray(PixelArray parent, int x, int y, int w, int h) {
    Arguments.notNull("parent", parent);
    Arguments.checkArrayRange("width", parent.getWidth(), x, w);
    Arguments.checkArrayRange("height", parent.getHeight(), y, h);

    this.parent = parent;
    offsetX = x;
    offsetY = y;
    width = w;
    height = h;
  }

  /**
   * @return The offset along the x axis into its parent's coordinate system.
   */
  public int getOffsetX() {
    return offsetX;
  }

  /**
   * @return The offset along the y axis into its parent's coordinate system.
   */
  public int getOffsetY() {
    return offsetY;
  }

  @Override
  public int getColorChannelCount() {
    return parent.getColorChannelCount();
  }

  @Override
  public boolean hasAlphaChannel() {
    return parent.hasAlphaChannel();
  }

  @Override
  public int getBandCount() {
    return parent.getBandCount();
  }

  @Override
  public void toParentCoordinate(ImageCoordinate coord) {
    coord.setX(coord.getX() + offsetX);
    coord.setY(coord.getY() + offsetY);
  }

  @Override
  public void fromParentCoordinate(ImageCoordinate coord) {
    coord.setX(coord.getX() - offsetX);
    coord.setY(coord.getY() - offsetY);
  }

  @Override
  public void toParentWindow(ImageWindow window) {
    window.setX(window.getX() + offsetX);
    window.setY(window.getY() + offsetY);
  }

  @Override
  public void fromParentWindow(ImageWindow window) {
    window.setX(window.getX() - offsetX);
    window.setY(window.getY() - offsetY);
  }

  @Override
  public double get(int x, int y, double[] channelValues) {
    checkCoordinate(x, y);
    return parent.get(offsetX + x, offsetY + y, channelValues);
  }

  @Override
  public double get(int x, int y, double[] channelValues, long[] bandOffsets) {
    checkCoordinate(x, y);
    return parent.get(offsetX + x, offsetY + y, channelValues, bandOffsets);
  }

  @Override
  public double getAlpha(int x, int y) {
    checkCoordinate(x, y);
    return parent.getAlpha(offsetX + x, offsetY + y);
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a) {
    checkCoordinate(x, y);
    parent.set(offsetX + x, offsetY + y, channelValues, a);
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a, long[] bandOffsets) {
    checkCoordinate(x, y);
    parent.set(offsetX + x, offsetY + y, channelValues, a, bandOffsets);
  }

  @Override
  public void setAlpha(int x, int y, double alpha) {
    checkCoordinate(x, y);
    parent.setAlpha(offsetX + x, offsetY + y, alpha);
  }

  @Override
  public boolean isReadOnly() {
    return parent.isReadOnly();
  }

  @Override
  public PixelArray getParent() {
    return parent;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  private void checkCoordinate(int x, int y) {
    // Hypothetically, out-of-bounds x and y values for the sub image can access valid pixels of
    // the parent image, which is inconsistent with PixelArray's API and is not detected by the
    // parent so this array must validate coordinates explicitly
    Arguments.checkArrayRange("x", width, 0, x);
    Arguments.checkArrayRange("y", height, 0, y);
  }
}
