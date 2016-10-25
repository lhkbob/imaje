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
package com.lhkbob.imaje.io.awt;

import com.lhkbob.imaje.Raster;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.io.ImageFileReader;
import com.lhkbob.imaje.io.InvalidImageException;
import com.lhkbob.imaje.io.UnsupportedImageFormatException;
import com.lhkbob.imaje.util.Arguments;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 */
public class ImageIOReader implements ImageFileReader {
  private final String formatSuffix;
  private final Data.Factory factory;

  public ImageIOReader(String formatSuffix) {
    this(formatSuffix, null);
  }

  public ImageIOReader(String formatSuffix, @Arguments.Nullable Data.Factory factory) {
    if (factory == null) {
      factory = Data.getDefaultDataFactory();
    }
    this.formatSuffix = formatSuffix;
    this.factory = factory;
  }

  private ImageReader getReader() throws UnsupportedImageFormatException {
    Iterator<ImageReader> readers = ImageIO.getImageReadersBySuffix(formatSuffix);
    if (!readers.hasNext()) {
      throw new UnsupportedImageFormatException(
          "Unavailable or unknown image format file suffix: " + formatSuffix);
    }
    return readers.next();
  }

  @Override
  public Raster<?> read(SeekableByteChannel in) throws IOException {
    // ImageIO uses plain Java IO as its interface, so unwrap NIO type
    InputStream ioWrapper = Channels.newInputStream(in);
    try (ImageInputStream stream = ImageIO.createImageInputStream(ioWrapper)) {
      ImageReader reader = getReader();
      reader.setInput(stream);
      BufferedImage img = reader.read(0);
      if (img == null) {
        throw new InvalidImageException("Cannot read image using ImageIO libraries");
      }
      return BufferedImageConverter.wrapOrConvert(img, factory);
    }
  }
}
