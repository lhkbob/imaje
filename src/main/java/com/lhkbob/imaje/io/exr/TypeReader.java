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
package com.lhkbob.imaje.io.exr;

import com.lhkbob.imaje.io.IO;
import com.lhkbob.imaje.io.InvalidImageException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@FunctionalInterface
interface TypeReader<T> {
  T read(SeekableByteChannel in, ByteBuffer work) throws IOException;

  static <T> List<T> readAll(
      SeekableByteChannel in, ByteBuffer work, TypeReader<T> reader) throws IOException {
    List<T> values = new ArrayList<>();

    // The IO.read() is not pushed into the while loop condition so that it can be an exception
    // condition since the list is defined to be terminated by a null byte.
    if (!IO.read(in, work)) {
      throw new InvalidImageException("Unexpected EOF while reading type list");
    }
    while (work.get() != 0) {
      // reset back to the byte we just read since it is actually part of the channel definition
      work.position(work.position() - 1);
      values.add(reader.read(in, work));

      if (!IO.read(in, work)) {
        throw new InvalidImageException("Unexpected EOF while reading type list");
      }
    }
    // we're done when we reach a null byte, no need to reset the stream
    return values;
  }

  static String readNullTerminatedString(SeekableByteChannel in, ByteBuffer work) throws
      IOException {
    StringBuilder sb = new StringBuilder();
    while (IO.read(in, work)) {
      while (work.hasRemaining()) {
        int b = (0xff & work.get());
        if (b != 0) {
          sb.append((char) b);
        } else {
          return sb.toString();
        }
      }
    }

    throw new InvalidImageException("Unexpected EOF before null character string terminator");
  }
}
