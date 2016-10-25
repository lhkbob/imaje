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
import com.lhkbob.imaje.util.IndexIterator;
import com.lhkbob.imaje.util.IndexSpliterator;

import java.util.Iterator;
import java.util.Spliterator;

/**
 * FIXME rename to ScanlineLayout
 */
public class SimpleLayout implements DataLayout {
  private final int imageWidth; // width in pixels of the image
  private final int imageHeight; // height in pixels of the image

  private final int channelCount;

  public SimpleLayout(int imageWidth, int imageHeight, int channelCount) {
    Arguments.isPositive("imageWidth", imageWidth);
    Arguments.isPositive("imageHeight", imageHeight);
    Arguments.isPositive("channelCount", channelCount);

    this.channelCount = channelCount;

    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
  }

  @Override
  public int getHeight() {
    return imageHeight;
  }

  private void checkImageBounds(int x, int y) {
    Arguments.inRangeExcludeMax("x", 0, imageWidth, x);
    Arguments.inRangeExcludeMax("y", 0, imageHeight, y);
  }

  @Override
  public void getChannelIndices(int x, int y, long[] channelIndices) {
    Arguments.equals("channelIndices.length", channelCount, channelIndices.length);
    checkImageBounds(x, y);

    long base = channelCount * (y * imageWidth + x);
    for (int i = 0; i < channelIndices.length; i++) {
      channelIndices[i] = base + i;
    }
  }

  @Override
  public long getChannelIndex(int x, int y, int channel) {
    Arguments.inRangeExcludeMax("channel", 0, channelCount, channel);
    checkImageBounds(x, y);

    return channelCount * (y * imageWidth + x) + channel;
  }

  @Override
  public int getChannelCount() {
    return channelCount;
  }

  @Override
  public boolean isGPUCompatible() {
    return true;
  }

  @Override
  public int getWidth() {
    return imageWidth;
  }

  @Override
  public boolean isDataBottomToTop() {
    return true;
  }

  @Override
  public boolean isDataLeftToRight() {
    return true;
  }

  @Override
  public Iterator<ImageCoordinate> iterator() {
    return new ImageCoordinate.FastIterator(
        new IndexIterator(imageWidth * imageHeight), this::updateCoordinate);
  }

  @Override
  public Spliterator<ImageCoordinate> spliterator() {
    return new ImageCoordinate.FastSpliterator(
        new IndexSpliterator(imageWidth * imageHeight, imageWidth), this::updateCoordinate);
  }

  private void updateCoordinate(ImageCoordinate coord, long index) {
    int y = (int) (index / imageWidth);
    int x = (int) (index - y * imageWidth);
    coord.setX(x);
    coord.setY(y);
  }
}
