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

import com.lhkbob.imaje.Image;
import com.lhkbob.imaje.Raster;
import com.lhkbob.imaje.io.ImageFileWriter;
import com.lhkbob.imaje.io.InvalidImageException;
import com.lhkbob.imaje.io.UnsupportedImageFormatException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

/**
 */
public class ImageIOWriter implements ImageFileWriter {
  private final String formatSuffix;

  public ImageIOWriter(String formatSuffix) {
    this.formatSuffix = formatSuffix;

  }

  private javax.imageio.ImageWriter getWriter() throws UnsupportedImageFormatException {
    Iterator<javax.imageio.ImageWriter> writers = ImageIO.getImageWritersBySuffix(formatSuffix);
    if (!writers.hasNext()) {
      throw new UnsupportedImageFormatException("Unavailable or unknown image format file suffix: " + formatSuffix);
    }
    return writers.next();
  }

  @Override
  public void write(Image<?> image, SeekableByteChannel out) throws IOException {
    if (!(image instanceof Raster)) {
      throw new InvalidImageException("Only 2D Raster images are supported for ImageIO writing");
    }

    OutputStream ioWrapper = Channels.newOutputStream(out);
    try (ImageOutputStream stream = ImageIO.createImageOutputStream(ioWrapper)) {
      javax.imageio.ImageWriter writer = getWriter();
      writer.setOutput(stream);
      writer.write(BufferedImageConverter.wrapOrConvert((Raster<?>) image));
    }
  }
}
