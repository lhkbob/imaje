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
package com.lhkbob.imaje.io.tga;

import com.lhkbob.imaje.Image;
import com.lhkbob.imaje.Raster;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.SRGB;
import com.lhkbob.imaje.color.transform.ColorTransform;
import com.lhkbob.imaje.color.transform.Transforms;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.io.IO;
import com.lhkbob.imaje.io.ImageFileWriter;
import com.lhkbob.imaje.io.UnsupportedImageFormatException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 *
 */
public class TGAWriter implements ImageFileWriter {
  @Override
  public void write(Image<?> image, SeekableByteChannel out) throws IOException {
    if (!(image instanceof Raster)) {
      throw new UnsupportedImageFormatException("Can only write 2D Rasters");
    }

    ByteBuffer work = IO.createWorkBufferForWriting();

    Raster<?> raster = (Raster<?>) image;
    TGAHeader h = createHeader(raster);
    h.write(out, work);

    // There is no color map to write, so now append all pixel data in
    // top-down left-to-right order, while converting to SRGB
    writePixels(raster, out, work);
  }

  private <T extends Color> void writePixels(
      Raster<T> image, SeekableByteChannel out, ByteBuffer work) throws IOException {
    ColorTransform<T, SRGB> toSRGB = Transforms.newTransform(image.getColorType(), SRGB.class);

    // A top-down left-to-right pixel loop
    T color = Color.newInstance(image.getColorType());
    for (int y = image.getHeight() - 1; y >= 0; y--) {
      for (int x = 0; x < image.getWidth(); x++) {
        double alpha = image.get(x, y, color);
        SRGB srgb = toSRGB.apply(color);

        // Always write 3 bytes for BGR
        work.put((byte) Data.UNORM8.toBits(srgb.b()));
        work.put((byte) Data.UNORM8.toBits(srgb.g()));
        work.put((byte) Data.UNORM8.toBits(srgb.r()));
        if (image.hasAlphaChannel()) {
          // Write 4th byte for alpha
          work.put((byte) Data.UNORM8.toBits(alpha));
        }

        // Push work buffer to the channel if needed
        if ((image.hasAlphaChannel() && work.remaining() < 4) || work.remaining() < 3) {
          IO.write(work, out);
        }
      }
    }

    if (work.position() > 0) {
      // Finish dumping last data filled in buffer
      IO.write(work, out);
    }
  }

  private TGAHeader createHeader(Raster<?> image) {
    TGAHeader h = new TGAHeader();
    h.setID("");

    // Make it a true-color image with no compression or color mapping
    h.setHasColorMap(false);
    h.setImageType(ImageType.TRUECOLOR);

    // Since there's no color map, these can be set to 0
    h.setColorMapFirstEntry(0);
    h.setColorMapLength(0);
    h.setColorMapEntrySize(0);

    h.setXOrigin(0);
    h.setYOrigin(0);
    h.setWidth(image.getWidth());
    h.setHeight(image.getHeight());

    if (image.hasAlphaChannel()) {
      // Go with 32-bit pixels, and 8 attribute bits
      h.setPixelDepth(32);
      h.setAttributeBitsPerPixel(8);
    } else {
      // 24 bits per pixel, 0 attribute bits
      h.setPixelDepth(24);
      h.setAttributeBitsPerPixel(0);
    }

    // Configure the data layout to be top-to-bottom and left-to-right for most
    // compatibility with other readers
    h.setTopToBottom(true);
    h.setRightToLeft(false);
    h.setInterleavedType(InterleaveType.NONE);

    return h;
  }
}
