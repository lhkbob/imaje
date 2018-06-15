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
package com.lhkbob.imaje.util;

import com.lhkbob.imaje.data.Data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * The ByteBuffer and Channel equivalent to ByteArrayInputStream and ByteArrayOutputStream,
 * depending on which constructor is used.
 */
public class ByteBufferChannel implements SeekableByteChannel {
  private static final double GROWTH_FACTOR = 2.0;
  private static final int INITIAL_BUFFER_SIZE = 1024;

  private final ByteBuffer buffer;
  private boolean isClosed;
  private final Object ioLock;

  // Position to resolve is set to a non-negative value when the newly requested position exceeds
  // the limit of the buffer. Future reads report end-of-stream, and future writes expand the
  // buffer to be large enough that the position is valid. Position updates within the current
  // buffer size happen through the buffer's position field and do not modify this variable.
  private int positionToResolve;

  public ByteBufferChannel() {
    this(null);
  }

  public ByteBufferChannel(ByteBuffer source) {
    isClosed = false;
    ioLock = new Object();

    positionToResolve = -1;

    if (source == null) {
      // Allocate a new buffer with a limit set to 0 but a reasonable capacity for growth
      buffer = Data.getBufferFactory().newByteBuffer(INITIAL_BUFFER_SIZE);
      buffer.limit(0);
    } else {
      // Make a slice of the buffer, so that the buffer's position is independent from the source,
      // slice() is used so that source's position and limit are respected while still appearing
      // as having a position of 0.
      buffer = source.slice();
    }
  }

  public ByteBuffer getBuffer() {
    synchronized (ioLock) {
      int oldPos = buffer.position();
      buffer.rewind();
      // The sliced buffer will include everything from 0 to the limit of the underlying buffer
      ByteBuffer toReturn = buffer.slice();
      // Restore the position of the buffer; this will be less than the limit and could be stale
      // if positionToResolve >= 0, but we don't need to touch positionToResolve and it will be
      // handled correctly later and overwrite this position set.
      buffer.position(oldPos);

      return toReturn;
    }
  }

  @Override
  public boolean isOpen() {
    return !isClosed;
  }

  @Override
  public void close() throws IOException {
    synchronized (ioLock) {
      isClosed = true;
    }
  }

  @Override
  public int read(ByteBuffer dst) throws IOException {
    synchronized (ioLock) {
      if (!buffer.hasRemaining() || positionToResolve >= 0) {
        // End of stream, do not clear position resolution since it hasn't been moved into the
        // buffer's actual state
        return -1;
      }

      int toRead = Math.min(dst.remaining(), buffer.remaining());
      int oldLimit = buffer.limit();
      buffer.limit(buffer.position() + toRead);
      dst.put(buffer);
      buffer.limit(oldLimit);

      return toRead;
    }
  }

  @Override
  public int write(ByteBuffer src) throws IOException {
    synchronized (ioLock) {
      // This may exceed the current buffer's limit and capacity, so buffer.remaining() is incorrect
      int bufferPos;
      if (positionToResolve >= 0) {
        bufferPos = positionToResolve;
        // Cleared as remaining() logic below will update the buffer's position to match positionToResolve
        // This is guaranteed to happen since in this case bufferPos > limit, so bufferRemaining < 0,
        // so src.remaining() >= 0 will always have src.remaining() > bufferRemaining.
        positionToResolve = -1;
      } else {
        bufferPos = buffer.position();
      }
      int bufferRemaining = buffer.limit() - bufferPos;

      if (src.remaining() > bufferRemaining) {
        // Must update the buffer's limit, or allocate a new buffer with a greater capacity
        if (bufferPos + src.remaining() < buffer.capacity()) {
          // There is room to update the limit, also set position in case we're resolving a lazy position
          buffer.limit(bufferPos + src.remaining()).position(bufferPos);
        } else {
          // Allocate a new buffer
          int newSize = Math.max((int) Math.ceil(buffer.capacity() * GROWTH_FACTOR), bufferPos + src.remaining());
          ByteBuffer newBuffer = Data.getBufferFactory().newByteBuffer(newSize);

          // Copy old buffer contents into new buffer, from 0 to buffer's current limit
          buffer.rewind();
          newBuffer.put(buffer);
          // Update the new buffer's position to equal where the previous buffer was set to, and
          // set its limit to what's necessary to contain the incoming src buffer
          buffer.limit(bufferPos + src.remaining()).position(bufferPos);
        }
      }

      // There is now sufficient room in buffer to contain src
      int written = src.remaining();
      buffer.put(src);
      return written;
    }
  }

  @Override
  public long position() throws IOException {
    synchronized (ioLock) {
      if (positionToResolve < 0)
      return buffer.position();
      else
        return positionToResolve;
    }
  }

  @Override
  public SeekableByteChannel position(long newPosition) throws IOException {
    synchronized (ioLock) {
      if (newPosition > buffer.limit()) {
        // Position is outside of the buffer's current state
        positionToResolve = Math.toIntExact(newPosition);
      } else {
        // Position is within current limit, so set the actual position and clear any previously
        // set position that needed lazy resolution
        buffer.position(Math.toIntExact(newPosition));
        positionToResolve = -1;
      }
    }
    return this;
  }

  @Override
  public long size() throws IOException {
    synchronized (ioLock) {
      return buffer.limit();
    }
  }

  @Override
  public SeekableByteChannel truncate(long size) throws IOException {
    synchronized (ioLock) {
      // Only truncate if new size is actually smaller than the current size
      if (size < buffer.limit()) {
        int truncatedPos;
        if (positionToResolve >= 0 || buffer.position() > size) {
          // The new position is set to the end of the buffer
          truncatedPos = (int) size;
        } else {
          // No change in position needed
          truncatedPos = buffer.position();
        }

        buffer.limit((int) size).position(truncatedPos);
        // And clear the positionToResolve, since the position was pushed into the buffer state
        positionToResolve = -1;
      }
    }

    return this;
  }
}
