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
import com.lhkbob.imaje.data.Bytes;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.io.ImageFileFormat;
import com.lhkbob.imaje.util.Arguments;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * See: http://www.paulbourke.net/dataformats/tga/
 * http://www.fileformat.info/format/tga/egff.htm#TGA-DMYID.2
 * http://www.dca.fee.unicamp.br/~martino/disciplinas/ea978/tgaffs.pdf
 */
public class TGAFormat implements ImageFileFormat {
  private final TGAReader reader;
  private final TGAWriter writer;

  public TGAFormat() {
    this(null);
  }

  public TGAFormat(@Arguments.Nullable Data.Factory factory) {
    reader = new TGAReader(factory);
    writer = new TGAWriter();
  }

  @Override
  public Image<?> read(SeekableByteChannel in) throws IOException {
    return reader.read(in);
  }

  @Override
  public void write(Image<?> image, SeekableByteChannel out) throws IOException {
    writer.write(image, out);
  }

  static void putUnsignedByte(ByteBuffer work, int value) {
    work.put((byte) (0xff & value));
  }

  static void putUnsignedShort(ByteBuffer work, int value) {
    Bytes.shortToBytesBE((short) (0xffff & value), work);
  }

  static int getUnsignedByte(ByteBuffer work) {
    return 0xff & work.get();
  }

  static int getUnsignedShort(ByteBuffer work) {
    return 0xffff & Bytes.bytesToShortBE(work);
  }
}
