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
package com.lhkbob.imaje.io;

import com.lhkbob.imaje.data.Data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 *
 */
public final class IO {
  public static final int DEFAULT_WORKBUFFER_LEN = 4096;

  private IO() {}

  public static ByteBuffer createWorkBufferForReading() {
    return createWorkBufferForReading(DEFAULT_WORKBUFFER_LEN);
  }

  public static ByteBuffer createWorkBufferForReading(int len) {
    ByteBuffer work = Data.getBufferFactory().newByteBuffer(len);
    // To prepare the buffer for reading, its limit must also be set to 0 since there is no valid
    // data in it yet.
    work.position(0).limit(0);
    return work;
  }

  public static ByteBuffer createWorkBufferForWriting() {
    return createWOrkBufferForWriting(DEFAULT_WORKBUFFER_LEN);
  }

  public static ByteBuffer createWOrkBufferForWriting(int len) {
    ByteBuffer work = Data.getBufferFactory().newByteBuffer(len);
    // To prepare for writing, the buffer's position should be 0 and its limit should be at capacity
    work.clear();
    return work;
  }

  public static long remaining(SeekableByteChannel in, ByteBuffer work) throws IOException {
    // Assuming that work is a buffer managed by IOUtils conventions, then the total remaining
    // number of bytes in the "file" are the remaining bytes in work and the remaining bytes in
    // the channel based on its current position and size.
    return work.remaining() + (in.size() - in.position());
  }

  public static boolean skip(ReadableByteChannel in, ByteBuffer work, int toSkip) throws
      IOException {
    if (in instanceof SeekableByteChannel) {
      return skip((SeekableByteChannel) in, work, toSkip);
    } else {
      // Skip by reading the bytes and then discarding them from the buffer
      while (toSkip > 0) {
        int consume = Math.min(toSkip, work.capacity());
        if (!read(in, work, consume)) {
          return false;
        }
        work.position(work.position() + consume);
        toSkip -= consume;
      }

      return true;
    }
  }

  public static boolean skip(SeekableByteChannel in, ByteBuffer work, int toSkip) throws
      IOException {
    // First attempt to skip bytes that were already loaded into the byte work buffer.
    int remainingInWork = work.remaining();
    if (remainingInWork >= toSkip) {
      // Skip can be entirely simulated in the work buffer
      work.position(work.position() + toSkip);
      return true;
    }

    // Otherwise, consume all bytes in the work buffer and subtract that from the total that must
    // be skipped in the actual channel
    toSkip -= remainingInWork;
    work.position(work.limit());

    boolean skippedPastEnd = (in.size() - in.position()) < toSkip;
    // Still update the position even if the end of the file has been exceeded, since this can
    // be used when reading OR writing, and channel has expected behavior in either of those cases.
    in.position(in.position() + toSkip);
    return skippedPastEnd;
  }

  public static boolean read(ReadableByteChannel in, ByteBuffer buffer) throws IOException {
    return read(in, buffer, 1);
  }

  public static boolean read(ReadableByteChannel in, ByteBuffer buffer, int minRemaining) throws
      IOException {
    if (buffer.remaining() >= minRemaining) {
      // Nothing needs to be done
      return true;
    }

    // Will need to read new content, so make sure the minRemaining is valid
    if (buffer.capacity() < minRemaining) {
      throw new IllegalArgumentException(
          "Buffer is too small to contain requested bytes, requires " + minRemaining
              + " bytes but has " + buffer.capacity());
    }

    if (buffer.limit() + minRemaining > buffer.capacity()) {
      // Cannot fit minRemaining bytes into the buffer without overflowing, so must compact it to have room.
      // compact + flip moves data between position and limit to be 0 to remaining, with limit set
      // to remaining as expected.
      buffer.compact().flip();
    }

    // While the buffer may have some bytes remaining, it does not have the required minimum so
    // a read operation must be issued. The read request will append to the current limit (i.e.
    // the start of invalid data) and read as much as possible (i.e. the buffer's capacity). After
    // the read is complete, the position is restored to the original and the limit set to the end
    // of the read content (i.e. the position value after read() returns).
    while (buffer.remaining() < minRemaining) {
      // Save the current position, and move the buffer range to be from old limit to capacity
      buffer.mark().position(buffer.limit()).limit(buffer.capacity());
      int read = in.read(buffer);
      // Set limit to the current position (which marks the end of the channel's read data), and reset the position
      buffer.limit(buffer.position()).reset();

      if (read < 0) {
        // End of file was reached, so there's no way minRemaining can be satisfied
        return false;
      }
    }

    // There are now sufficiently many remaining bytes, and the buffer position and limit are
    // already set as if the buffer was flipped after the read (plus still containing the original
    // content that had been between position and limit).
    return true;
  }

  public static boolean fill(byte[] data, SeekableByteChannel in, ByteBuffer work) throws
      IOException {
    return fill(data, data.length, in, work);
  }

  public static boolean fill(
      byte[] data, int dataLength, SeekableByteChannel in, ByteBuffer work) throws IOException {
    if (dataLength > data.length) {
      throw new IllegalArgumentException(String
          .format("Data length (%d) must be less than actual array length (%d)", dataLength,
              data.length));
    }

    if (dataLength < work.capacity()) {
      // Can use the basic read functionality already in IOUtils
      boolean res = IO.read(in, work, dataLength);
      if (!res) {
        return false;
      } else {
        work.get(data, 0, dataLength);
        return true;
      }
    } else {
      // The requested block of data is larger than the work buffer so it must be incrementally filled
      int filled = 0;
      while (filled < dataLength) {
        if (!IO.read(in, work)) {
          return false;
        }

        int consumed = Math.min(work.remaining(), dataLength - filled);
        work.get(data, filled, consumed);
        filled += consumed;
      }

      return true;
    }
  }

  public static ByteBuffer readFully(SeekableByteChannel channel) throws IOException {
    int size = Math.toIntExact(channel.size() - channel.position());
    ByteBuffer buffer = Data.getBufferFactory().newByteBuffer(size);
    readFully(channel, buffer);
    return buffer;
  }

  public static void readFully(ReadableByteChannel channel, ByteBuffer buffer) throws IOException {
    while (buffer.hasRemaining()) {
      int res = channel.read(buffer);
      if (res < 0) {
        throw new IOException("Unexpected end of stream");
      }
    }
  }

  public static void write(ByteBuffer buffer, WritableByteChannel channel) throws IOException {
    buffer.flip();
    while (buffer.hasRemaining()) {
      channel.write(buffer);
    }
    // All valid data is emptied from the buffer, so clear it so that it can be maximally filled
    // by the next preparatory stage.
    buffer.clear();
  }
}
