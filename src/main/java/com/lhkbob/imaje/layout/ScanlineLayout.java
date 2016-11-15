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

import java.util.Iterator;
import java.util.Spliterator;

/**
 * ScanlineLayout
 * ==============
 *
 * An optimized DataLayout implementation for the most common way that pixels are arranged into a
 * single dimension. The band values associated with a pixel are arranged in consecutive elements.
 * Pixels are arranged row-major from left to right and bottom to top. This layout is compatible
 * with GPU expectations.
 *
 * @author Michael Ludwig
 */
public class ScanlineLayout implements DataLayout {
  private final int bandCount;
  private final int imageHeight; // height in pixels of the image
  private final int imageWidth; // width in pixels of the image

  /**
   * Create a new ScanlineLayout sized for a 2D image with width equal to `imageWidth` and
   * height equal to `imageHeight`. The number of bands per pixel is defined by `bandCount`.
   *
   * @param imageWidth
   *     The image width
   * @param imageHeight
   *     The image height
   * @param bandCount
   *     The number of bands per pixel
   * @throws IllegalArgumentException
   *     if any argument is not positive
   */
  public ScanlineLayout(int imageWidth, int imageHeight, int bandCount) {
    Arguments.isPositive("imageWidth", imageWidth);
    Arguments.isPositive("imageHeight", imageHeight);
    Arguments.isPositive("bandCount", bandCount);

    this.bandCount = bandCount;

    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
  }

  @Override
  public int getBandCount() {
    return bandCount;
  }

  @Override
  public long getBandOffset(int x, int y, int band) {
    Arguments.checkIndex("band", bandCount, band);
    checkImageBounds(x, y);

    return bandCount * (y * imageWidth + x) + band;
  }

  @Override
  public void getBandOffsets(int x, int y, long[] bandOffsets) {
    Arguments.equals("bandOffsets.length", bandCount, bandOffsets.length);
    checkImageBounds(x, y);

    long base = bandCount * (y * imageWidth + x);
    for (int i = 0; i < bandOffsets.length; i++) {
      bandOffsets[i] = base + i;
    }
  }

  @Override
  public int getHeight() {
    return imageHeight;
  }

  @Override
  public int getWidth() {
    return imageWidth;
  }

  @Override
  public boolean isGPUCompatible() {
    return true;
  }

  @Override
  public void iterateWindow(
      int x, int y, int width, int height, BlockVisitor receiver) {
    Arguments.notNull("receiver", receiver);
    x = getContainedX(x);
    y = getContainedY(y);
    width = getContainedWidth(x, width);
    height = getContainedHeight(y, height);

    long[] channelOffsets = new long[bandCount];
    for (int row = y; row < y + height; row++) {
      // Since this is a scanline layout and the window is the same width or narrower, then
      // we can compute the offset as the location of (x, y) for the channel and use the window
      // width as the block length.
      getBandOffsets(x, row, channelOffsets);
      receiver.visit(x, row, bandCount, width, channelOffsets);
    }
  }

  @Override
  public Iterator<ImageCoordinate> iterator(int x, int y, int width, int height) {
    x = getContainedX(x);
    y = getContainedY(y);
    width = getContainedWidth(x, width);
    height = getContainedHeight(y, height);
    return new ImageCoordinate.FastIterator(x, y, width, height);
  }

  @Override
  public Spliterator<ImageCoordinate> spliterator(int x, int y, int width, int height) {
    x = getContainedX(x);
    y = getContainedY(y);
    width = getContainedWidth(x, width);
    height = getContainedHeight(y, height);
    return new ImageCoordinate.FastSpliterator(x, y, width, height);
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
}
