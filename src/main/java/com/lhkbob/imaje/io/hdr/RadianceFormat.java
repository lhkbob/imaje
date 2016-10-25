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
package com.lhkbob.imaje.io.hdr;

import com.lhkbob.imaje.Image;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.types.UnsignedSharedExponent;
import com.lhkbob.imaje.io.ImageFileFormat;
import com.lhkbob.imaje.util.Arguments;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

/**
 * TODO: implement an actual image stream for this file type
 * http://radsite.lbl.gov/radiance/refer/filefmts.pdf
 * https://github.com/NREL/Radiance/blob/combined/src/common/color.c
 */
public class RadianceFormat implements ImageFileFormat {
  private final RadianceReader reader;
  private final RadianceWriter writer;

  public RadianceFormat() {
    this(null);
  }

  public RadianceFormat(@Arguments.Nullable Data.Factory factory) {
    reader = new RadianceReader(factory);
    writer = new RadianceWriter();
  }

  @Override
  public Image<?> read(SeekableByteChannel in) throws IOException {
    return reader.read(in);
  }

  @Override
  public void write(Image<?> image, SeekableByteChannel out) throws IOException {
    writer.write(image, out);
  }

  // The masks represent the 4 bytes for RGBE, assuming the bytes were processed as big endian.
  static final UnsignedSharedExponent CONVERSION = new UnsignedSharedExponent(
      0xffL, new long[] { 0xff000000L, 0xff0000L, 0xff00L }, 128);
}
