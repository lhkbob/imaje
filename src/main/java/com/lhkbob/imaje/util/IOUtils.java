package com.lhkbob.imaje.util;

import com.lhkbob.imaje.data.Data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 *
 */
public final class IOUtils {
  public static boolean read(ReadableByteChannel in, ByteBuffer buffer) throws IOException {
    return read(in, buffer, 1);
  }

  public static boolean read(ReadableByteChannel in, ByteBuffer buffer, int minRemaining) throws IOException {
    if (buffer.remaining() >= minRemaining) {
      // Nothing needs to be done
      return true;
    }

    // Will need to read new content, so make sure the minRemaining is valid
    if (buffer.capacity() < minRemaining) {
      throw new IllegalArgumentException("Buffer is too small to contain requested bytes, requires " + minRemaining + " bytes but has " + buffer.capacity());
    }

    if (buffer.position() + minRemaining > buffer.capacity()) {
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
    while(buffer.remaining() < minRemaining) {
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

  public static ByteBuffer readFully(SeekableByteChannel channel) throws IOException {
    int size = Math.toIntExact(channel.size() - channel.position());
    ByteBuffer buffer = Data.getBufferFactory().newByteBuffer(size);
    readFully(channel, buffer);
    return buffer;
  }

  public static void readFully(ReadableByteChannel channel, ByteBuffer buffer) throws IOException {
    while(buffer.hasRemaining()) {
      int res = channel.read(buffer);
      if (res < 0) {
        throw new IOException("Unexpected end of stream");
      }
    }
  }

  public static void write(ByteBuffer buffer, WritableByteChannel channel) throws IOException {
    buffer.flip();
    while(buffer.hasRemaining()) {
      channel.write(buffer);
    }
    // All valid data is emptied from the buffer, so clear it so that it can be maximally filled
    // by the next preparatory stage.
    buffer.clear();
  }
}
