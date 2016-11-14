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
package com.lhkbob.imaje.data;

import com.lhkbob.imaje.util.Arguments;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Bytes
 * =====
 *
 * Utility class container for functions for byte manipulation, specifically to convert byte words
 * into multi-byte primitive types given a particular byte ordering (little or big endian). Each
 * type conversion and direction has three variants:
 *
 * 1. Providing byte words expanded as method arguments, returning a single primitive. The inverse
 * direction (value to bytes) returns a new byte array sized based on the primitive type.
 * 2. Providing bytes in an array, along with an offset to the first word. This variant works both
 * as a source for bytes and as the destination when converting a primitive to bytes.
 * 3. Providing an NIO ByteBuffer, in which case bytes are read or written based on the position of
 * the buffer and the position is modified.
 *
 * @author Michael Ludwig
 */
public final class Bytes {
  private Bytes() {}

  /**
   * Combine 8 bytes in `data` starting at `offset` into a double, assuming the byte words are
   * ordered big endian.
   *
   * @param data
   *     The byte source
   * @param offset
   *     The offset into `data` for the first word
   * @return The double value the 8 bytes represent when combined and interpreted with the proper
   * byte ordering
   *
   * @throws IndexOutOfBoundsException
   *     if there are not 8 remaining bytes in `data` at `offset`
   */
  public static double bytesToDoubleBE(byte[] data, int offset) {
    return bytesToDoubleBE(data[offset], data[offset + 1], data[offset + 2], data[offset + 3],
        data[offset + 4], data[offset + 5], data[offset + 6], data[offset + 7]);
  }

  /**
   * Combine 8 bytes in `buffer`, starting at its current position into a double. The buffer's
   * position will be advanced by 8 on success. This converts the bytes as if they were big endian.
   * This uses big endian regardless of the configured byte order of the buffer. If `buffer`'s order
   * is big endian, this is equivalent to {@link ByteBuffer#getDouble()}, otherwise 8 bytes are
   * fetched in order and converted explicitly.
   *
   * @param buffer
   *     The byte source
   * @return The double value the 8 bytes represent when combined and interpreted with the proper
   * byte ordering
   *
   * @throws BufferUnderflowException
   *     if `buffer` does not have 8 remaining bytes
   */
  public static double bytesToDoubleBE(ByteBuffer buffer) {
    if (buffer.order() == ByteOrder.BIG_ENDIAN) {
      return buffer.getDouble();
    } else {
      if (buffer.remaining() < 8) {
        throw new BufferUnderflowException();
      }

      byte w1 = buffer.get();
      byte w2 = buffer.get();
      byte w3 = buffer.get();
      byte w4 = buffer.get();
      byte w5 = buffer.get();
      byte w6 = buffer.get();
      byte w7 = buffer.get();
      byte w8 = buffer.get();
      return bytesToDoubleBE(w1, w2, w3, w4, w5, w6, w7, w8);
    }
  }

  /**
   * Combine the 8 bytes into a double assuming the words are in big endian order.
   *
   * @param w1
   *     The first byte word
   * @param w2
   *     The second byte word
   * @param w3
   *     The third byte word
   * @param w4
   *     The fourth byte word
   * @param w5
   *     The fifth byte word
   * @param w6
   *     The sixth byte word
   * @param w7
   *     The seventh byte word
   * @param w8
   *     The eighth byte word
   * @return The double value represented by the 8 byte words in big endian order
   */
  public static double bytesToDoubleBE(
      byte w1, byte w2, byte w3, byte w4, byte w5, byte w6, byte w7, byte w8) {
    return Double.longBitsToDouble(bytesToLongBE(w1, w2, w3, w4, w5, w6, w7, w8));
  }

  /**
   * Combine 8 bytes in `data` starting at `offset` into a double, assuming the byte words are
   * ordered little endian.
   *
   * @param data
   *     The byte source
   * @param offset
   *     The offset into `data` for the first word
   * @return The double value the 8 bytes represent when combined and interpreted with the proper
   * byte ordering
   *
   * @throws IndexOutOfBoundsException
   *     if there are not 8 remaining bytes in `data` at `offset`
   */
  public static double bytesToDoubleLE(byte[] data, int offset) {
    return bytesToDoubleLE(data[offset], data[offset + 1], data[offset + 2], data[offset + 3],
        data[offset + 4], data[offset + 5], data[offset + 6], data[offset + 7]);
  }

  /**
   * Combine 8 bytes in `buffer`, starting at its current position into a double. The buffer's
   * position will be advanced by 8 on success. This converts the bytes as if they were little
   * endian. This uses little endian regardless of the configured byte order of the buffer. If
   * `buffer`'s order is little endian, this is equivalent to {@link ByteBuffer#getDouble()},
   * otherwise 8 bytes are fetched in order and converted explicitly.
   *
   * @param buffer
   *     The byte source
   * @return The double value the 8 bytes represent when combined and interpreted with the proper
   * byte ordering
   *
   * @throws BufferUnderflowException
   *     if `buffer` does not have 8 remaining bytes
   */
  public static double bytesToDoubleLE(ByteBuffer buffer) {
    if (buffer.order() == ByteOrder.LITTLE_ENDIAN) {
      return buffer.getDouble();
    } else {
      if (buffer.remaining() < 8) {
        throw new BufferUnderflowException();
      }

      byte w1 = buffer.get();
      byte w2 = buffer.get();
      byte w3 = buffer.get();
      byte w4 = buffer.get();
      byte w5 = buffer.get();
      byte w6 = buffer.get();
      byte w7 = buffer.get();
      byte w8 = buffer.get();
      return bytesToDoubleLE(w1, w2, w3, w4, w5, w6, w7, w8);
    }
  }

  /**
   * Combine the 8 bytes into a double assuming the words are in little endian order.
   *
   * @param w1
   *     The first byte word
   * @param w2
   *     The second byte word
   * @param w3
   *     The third byte word
   * @param w4
   *     The fourth byte word
   * @param w5
   *     The fifth byte word
   * @param w6
   *     The sixth byte word
   * @param w7
   *     The seventh byte word
   * @param w8
   *     The eighth byte word
   * @return The double value represented by the 8 byte words in little endian order
   */
  public static double bytesToDoubleLE(
      byte w1, byte w2, byte w3, byte w4, byte w5, byte w6, byte w7, byte w8) {
    return Double.longBitsToDouble(bytesToLongLE(w1, w2, w3, w4, w5, w6, w7, w8));
  }

  /**
   * Combine 4 bytes in `data` starting at `offset` into a float, assuming the byte words are
   * ordered big endian.
   *
   * @param data
   *     The byte source
   * @param offset
   *     The offset into `data` for the first word
   * @return The float value the 4 bytes represent when combined and interpreted with the proper
   * byte ordering
   *
   * @throws IndexOutOfBoundsException
   *     if there are not 4 remaining bytes in `data` at `offset`
   */
  public static float bytesToFloatBE(byte[] data, int offset) {
    return bytesToFloatBE(data[offset], data[offset + 1], data[offset + 2], data[offset + 3]);
  }

  /**
   * Combine 4 bytes in `buffer`, starting at its current position into a float. The buffer's
   * position will be advanced by 4 on success. This converts the bytes as if they were big endian.
   * This uses big endian regardless of the configured byte order of the buffer. If `buffer`'s order
   * is big endian, this is equivalent to {@link ByteBuffer#getFloat()}, otherwise 4 bytes are
   * fetched in order and converted explicitly.
   *
   * @param buffer
   *     The byte source
   * @return The float value the 4 bytes represent when combined and interpreted with the proper
   * byte ordering
   *
   * @throws BufferUnderflowException
   *     if `buffer` does not have 4 remaining bytes
   */
  public static float bytesToFloatBE(ByteBuffer buffer) {
    if (buffer.order() == ByteOrder.BIG_ENDIAN) {
      return buffer.getFloat();
    } else {
      if (buffer.remaining() < 4) {
        throw new BufferUnderflowException();
      }

      byte w1 = buffer.get();
      byte w2 = buffer.get();
      byte w3 = buffer.get();
      byte w4 = buffer.get();
      return bytesToFloatBE(w1, w2, w3, w4);
    }
  }

  /**
   * Combine the 4 bytes into a float assuming the words are in big endian order.
   *
   * @param w1
   *     The first byte word
   * @param w2
   *     The second byte word
   * @param w3
   *     The third byte word
   * @param w4
   *     The fourth byte word
   * @return The float value represented by the 4 byte words in big endian order
   */
  public static float bytesToFloatBE(byte w1, byte w2, byte w3, byte w4) {
    return Float.intBitsToFloat(bytesToIntBE(w1, w2, w3, w4));
  }

  /**
   * Combine 4 bytes in `data` starting at `offset` into a float, assuming the byte words are
   * ordered little endian.
   *
   * @param data
   *     The byte source
   * @param offset
   *     The offset into `data` for the first word
   * @return The float value the 4 bytes represent when combined and interpreted with the proper
   * byte ordering
   *
   * @throws IndexOutOfBoundsException
   *     if there are not 4 remaining bytes in `data` at `offset`
   */
  public static float bytesToFloatLE(byte[] data, int offset) {
    return bytesToFloatLE(data[offset], data[offset + 1], data[offset + 2], data[offset + 3]);
  }

  /**
   * Combine 4 bytes in `buffer`, starting at its current position into a float. The buffer's
   * position will be advanced by 4 on success. This converts the bytes as if they were little
   * endian. This uses little endian regardless of the configured byte order of the buffer. If
   * `buffer`'s order is little endian, this is equivalent to {@link ByteBuffer#getFloat()},
   * otherwise 4 bytes are fetched in order and converted explicitly.
   *
   * @param buffer
   *     The byte source
   * @return The float value the 4 bytes represent when combined and interpreted with the proper
   * byte ordering
   *
   * @throws BufferUnderflowException
   *     if `buffer` does not have 4 remaining bytes
   */
  public static float bytesToFloatLE(ByteBuffer buffer) {
    if (buffer.order() == ByteOrder.LITTLE_ENDIAN) {
      return buffer.getFloat();
    } else {
      if (buffer.remaining() < 4) {
        throw new BufferUnderflowException();
      }

      byte w1 = buffer.get();
      byte w2 = buffer.get();
      byte w3 = buffer.get();
      byte w4 = buffer.get();
      return bytesToFloatLE(w1, w2, w3, w4);
    }
  }

  /**
   * Combine the 4 bytes into a float assuming the words are in little endian order.
   *
   * @param w1
   *     The first byte word
   * @param w2
   *     The second byte word
   * @param w3
   *     The third byte word
   * @param w4
   *     The fourth byte word
   * @return The float value represented by the 4 byte words in little endian order
   */
  public static float bytesToFloatLE(byte w1, byte w2, byte w3, byte w4) {
    return Float.intBitsToFloat(bytesToIntLE(w1, w2, w3, w4));
  }

  /**
   * Combine 4 bytes in `data` starting at `offset` into an int, assuming the byte words are
   * ordered big endian.
   *
   * @param data
   *     The byte source
   * @param offset
   *     The offset into `data` for the first word
   * @return The int value the 4 bytes represent when combined and interpreted with the proper
   * byte ordering
   *
   * @throws IndexOutOfBoundsException
   *     if there are not 4 remaining bytes in `data` at `offset`
   */
  public static int bytesToIntBE(byte[] data, int offset) {
    return bytesToIntBE(data[offset], data[offset + 1], data[offset + 2], data[offset + 3]);
  }

  /**
   * Combine 4 bytes in `buffer`, starting at its current position into an int. The buffer's
   * position will be advanced by 4 on success. This converts the bytes as if they were big endian.
   * This uses big endian regardless of the configured byte order of the buffer. If `buffer`'s order
   * is big endian, this is equivalent to {@link ByteBuffer#getInt()}, otherwise 4 bytes are
   * fetched in order and converted explicitly.
   *
   * @param buffer
   *     The byte source
   * @return The int value the 4 bytes represent when combined and interpreted with the proper
   * byte ordering
   *
   * @throws BufferUnderflowException
   *     if `buffer` does not have 4 remaining bytes
   */
  public static int bytesToIntBE(ByteBuffer buffer) {
    if (buffer.order() == ByteOrder.BIG_ENDIAN) {
      return buffer.getInt();
    } else {
      if (buffer.remaining() < 4) {
        throw new BufferUnderflowException();
      }

      byte w1 = buffer.get();
      byte w2 = buffer.get();
      byte w3 = buffer.get();
      byte w4 = buffer.get();
      return bytesToIntBE(w1, w2, w3, w4);
    }
  }

  /**
   * Combine the 4 bytes into an int assuming the words are in big endian order.
   *
   * @param w1
   *     The first byte word
   * @param w2
   *     The second byte word
   * @param w3
   *     The third byte word
   * @param w4
   *     The fourth byte word
   * @return The int value represented by the 4 byte words in big endian order
   */
  public static int bytesToIntBE(byte w1, byte w2, byte w3, byte w4) {
    return (((0xff & w1) << 24) | ((0xff & w2) << 16) | ((0xff & w3) << 8) | (0xff & w4));
  }

  /**
   * Combine 4 bytes in `data` starting at `offset` into an int, assuming the byte words are
   * ordered little endian.
   *
   * @param data
   *     The byte source
   * @param offset
   *     The offset into `data` for the first word
   * @return The int value the 4 bytes represent when combined and interpreted with the proper
   * byte ordering
   *
   * @throws IndexOutOfBoundsException
   *     if there are not 4 remaining bytes in `data` at `offset`
   */
  public static int bytesToIntLE(byte[] data, int offset) {
    return bytesToIntLE(data[offset], data[offset + 1], data[offset + 2], data[offset + 3]);
  }

  /**
   * Combine 4 bytes in `buffer`, starting at its current position into an int. The buffer's
   * position will be advanced by 4 on success. This converts the bytes as if they were little
   * endian. This uses little endian regardless of the configured byte order of the buffer. If
   * `buffer`'s order is little endian, this is equivalent to {@link ByteBuffer#getInt()}, otherwise
   * 4 bytes are fetched in order and converted explicitly.
   *
   * @param buffer
   *     The byte source
   * @return The int value the 4 bytes represent when combined and interpreted with the proper
   * byte ordering
   *
   * @throws BufferUnderflowException
   *     if `buffer` does not have 4 remaining bytes
   */
  public static int bytesToIntLE(ByteBuffer buffer) {
    if (buffer.order() == ByteOrder.LITTLE_ENDIAN) {
      return buffer.getInt();
    } else {
      if (buffer.remaining() < 4) {
        throw new BufferUnderflowException();
      }

      byte w1 = buffer.get();
      byte w2 = buffer.get();
      byte w3 = buffer.get();
      byte w4 = buffer.get();
      return bytesToIntLE(w1, w2, w3, w4);
    }
  }

  /**
   * Combine the 4 bytes into an int assuming the words are in little endian order.
   *
   * @param w1
   *     The first byte word
   * @param w2
   *     The second byte word
   * @param w3
   *     The third byte word
   * @param w4
   *     The fourth byte word
   * @return The int value represented by the 4 byte words in little endian order
   */
  public static int bytesToIntLE(byte w1, byte w2, byte w3, byte w4) {
    return ((0xff & w1) | ((0xff & w2) << 8) | ((0xff & w3) << 16) | ((0xff & w4) << 24));
  }

  /**
   * Combine 8 bytes in `data` starting at `offset` into a long, assuming the byte words are
   * ordered big endian.
   *
   * @param data
   *     The byte source
   * @param offset
   *     The offset into `data` for the first word
   * @return The long value the 8 bytes represent when combined and interpreted with the proper
   * byte ordering
   *
   * @throws IndexOutOfBoundsException
   *     if there are not 8 remaining bytes in `data` at `offset`
   */
  public static long bytesToLongBE(byte[] data, int offset) {
    return bytesToLongBE(data[offset], data[offset + 1], data[offset + 2], data[offset + 3],
        data[offset + 4], data[offset + 5], data[offset + 6], data[offset + 7]);
  }

  /**
   * Combine 8 bytes in `buffer`, starting at its current position into a long. The buffer's
   * position will be advanced by 8 on success. This converts the bytes as if they were big endian.
   * This uses big endian regardless of the configured byte order of the buffer. If `buffer`'s order
   * is big endian, this is equivalent to {@link ByteBuffer#getLong()}, otherwise 8 bytes are
   * fetched in order and converted explicitly.
   *
   * @param buffer
   *     The byte source
   * @return The long value the 8 bytes represent when combined and interpreted with the proper
   * byte ordering
   *
   * @throws BufferUnderflowException
   *     if `buffer` does not have 8 remaining bytes
   */
  public static long bytesToLongBE(ByteBuffer buffer) {
    if (buffer.order() == ByteOrder.BIG_ENDIAN) {
      return buffer.getLong();
    } else {
      if (buffer.remaining() < 8) {
        throw new BufferUnderflowException();
      }

      byte w1 = buffer.get();
      byte w2 = buffer.get();
      byte w3 = buffer.get();
      byte w4 = buffer.get();
      byte w5 = buffer.get();
      byte w6 = buffer.get();
      byte w7 = buffer.get();
      byte w8 = buffer.get();
      return bytesToLongBE(w1, w2, w3, w4, w5, w6, w7, w8);
    }
  }

  /**
   * Combine the 8 bytes into a long assuming the words are in big endian order.
   *
   * @param w1
   *     The first byte word
   * @param w2
   *     The second byte word
   * @param w3
   *     The third byte word
   * @param w4
   *     The fourth byte word
   * @param w5
   *     The fifth byte word
   * @param w6
   *     The sixth byte word
   * @param w7
   *     The seventh byte word
   * @param w8
   *     The eighth byte word
   * @return The long value represented by the 8 byte words in big endian order
   */
  public static long bytesToLongBE(
      byte w1, byte w2, byte w3, byte w4, byte w5, byte w6, byte w7, byte w8) {
    return (((0xffL & w1) << 56) | ((0xffL & w2) << 48) | ((0xffL & w3) << 40) | ((0xffL & w4)
        << 32) | ((0xffL & w5) << 24) | ((0xffL & w6) << 16) | ((0xffL & w7) << 8) | (0xffL & w8));
  }

  /**
   * Combine 8 bytes in `data` starting at `offset` into a long, assuming the byte words are
   * ordered little endian.
   *
   * @param data
   *     The byte source
   * @param offset
   *     The offset into `data` for the first word
   * @return The long value the 8 bytes represent when combined and interpreted with the proper
   * byte ordering
   *
   * @throws IndexOutOfBoundsException
   *     if there are not 8 remaining bytes in `data` at `offset`
   */
  public static long bytesToLongLE(byte[] data, int offset) {
    return bytesToLongLE(data[offset], data[offset + 1], data[offset + 2], data[offset + 3],
        data[offset + 4], data[offset + 5], data[offset + 6], data[offset + 7]);
  }

  /**
   * Combine 8 bytes in `buffer`, starting at its current position into a long. The buffer's
   * position will be advanced by 8 on success. This converts the bytes as if they were little
   * endian. This uses little endian regardless of the configured byte order of the buffer. If
   * `buffer`'s order is little endian, this is equivalent to {@link ByteBuffer#getLong()},
   * otherwise 8 bytes are fetched in order and converted explicitly.
   *
   * @param buffer
   *     The byte source
   * @return The long value the 8 bytes represent when combined and interpreted with the proper
   * byte ordering
   *
   * @throws BufferUnderflowException
   *     if `buffer` does not have 8 remaining bytes
   */
  public static long bytesToLongLE(ByteBuffer buffer) {
    if (buffer.order() == ByteOrder.LITTLE_ENDIAN) {
      return buffer.getLong();
    } else {
      if (buffer.remaining() < 8) {
        throw new BufferUnderflowException();
      }

      byte w1 = buffer.get();
      byte w2 = buffer.get();
      byte w3 = buffer.get();
      byte w4 = buffer.get();
      byte w5 = buffer.get();
      byte w6 = buffer.get();
      byte w7 = buffer.get();
      byte w8 = buffer.get();
      return bytesToLongLE(w1, w2, w3, w4, w5, w6, w7, w8);
    }
  }

  /**
   * Combine the 8 bytes into a long assuming the words are in little endian order.
   *
   * @param w1
   *     The first byte word
   * @param w2
   *     The second byte word
   * @param w3
   *     The third byte word
   * @param w4
   *     The fourth byte word
   * @param w5
   *     The fifth byte word
   * @param w6
   *     The sixth byte word
   * @param w7
   *     The seventh byte word
   * @param w8
   *     The eighth byte word
   * @return The long value represented by the 8 byte words in little endian order
   */
  public static long bytesToLongLE(
      byte w1, byte w2, byte w3, byte w4, byte w5, byte w6, byte w7, byte w8) {
    return ((0xffL & w1) | ((0xffL & w2) << 8) | ((0xffL & w3) << 16) | ((0xffL & w4) << 24) | (
        (0xffL & w5) << 32) | ((0xffL & w6) << 40) | ((0xffL & w7) << 48) | ((0xffL & w8) << 56));
  }

  /**
   * Combine 2 bytes in `data` starting at `offset` into a short, assuming the byte words are
   * ordered big endian.
   *
   * @param data
   *     The byte source
   * @param offset
   *     The offset into `data` for the first word
   * @return The short value the 2 bytes represent when combined and interpreted with the proper
   * byte ordering
   *
   * @throws IndexOutOfBoundsException
   *     if there are not 2 remaining bytes in `data` at `offset`
   */
  public static short bytesToShortBE(byte[] data, int offset) {
    return bytesToShortBE(data[offset], data[offset + 1]);
  }

  /**
   * Combine 2 bytes in `buffer`, starting at its current position into a short. The buffer's
   * position will be advanced by 2 on success. This converts the bytes as if they were big endian.
   * This uses big endian regardless of the configured byte order of the buffer. If `buffer`'s order
   * is big endian, this is equivalent to {@link ByteBuffer#getShort()}, otherwise 2 bytes are
   * fetched in order and converted explicitly.
   *
   * @param buffer
   *     The byte source
   * @return The short value the 2 bytes represent when combined and interpreted with the proper
   * byte ordering
   *
   * @throws BufferUnderflowException
   *     if `buffer` does not have 2 remaining bytes
   */
  public static short bytesToShortBE(ByteBuffer buffer) {
    if (buffer.order() == ByteOrder.BIG_ENDIAN) {
      return buffer.getShort();
    } else {
      if (buffer.remaining() < 2) {
        throw new BufferUnderflowException();
      }

      byte w1 = buffer.get();
      byte w2 = buffer.get();
      return bytesToShortBE(w1, w2);
    }
  }

  /**
   * Combine the 2 bytes into a short assuming the words are in big endian order.
   *
   * @param w1
   *     The first byte word
   * @param w2
   *     The second byte word
   * @return The short value represented by the 2 byte words in big endian order
   */
  public static short bytesToShortBE(byte w1, byte w2) {
    return (short) (((0xff & w1) << 8) | (0xff & w2));
  }

  /**
   * Combine 2 bytes in `data` starting at `offset` into a short, assuming the byte words are
   * ordered little endian.
   *
   * @param data
   *     The byte source
   * @param offset
   *     The offset into `data` for the first word
   * @return The short value the 2 bytes represent when combined and interpreted with the proper
   * byte ordering
   *
   * @throws IndexOutOfBoundsException
   *     if there are not 2 remaining bytes in `data` at `offset`
   */
  public static short bytesToShortLE(byte[] data, int offset) {
    return bytesToShortLE(data[offset], data[offset + 1]);
  }

  /**
   * Combine 2 bytes in `buffer`, starting at its current position into a short. The buffer's
   * position will be advanced by 2 on success. This converts the bytes as if they were little
   * endian. This uses little endian regardless of the configured byte order of the buffer. If
   * `buffer`'s order is little endian, this is equivalent to {@link ByteBuffer#getShort()},
   * otherwise 2 bytes are fetched in order and converted explicitly.
   *
   * @param buffer
   *     The byte source
   * @return The short value the 2 bytes represent when combined and interpreted with the proper
   * byte ordering
   *
   * @throws BufferUnderflowException
   *     if `buffer` does not have 2 remaining bytes
   */
  public static short bytesToShortLE(ByteBuffer buffer) {
    if (buffer.order() == ByteOrder.LITTLE_ENDIAN) {
      return buffer.getShort();
    } else {
      if (buffer.remaining() < 2) {
        throw new BufferUnderflowException();
      }

      byte w1 = buffer.get();
      byte w2 = buffer.get();
      return bytesToShortLE(w1, w2);
    }
  }

  /**
   * Combine the 2 bytes into a short assuming the words are in little endian order.
   *
   * @param w1
   *     The first byte word
   * @param w2
   *     The second byte word
   * @return The short value represented by the 2 byte words in little endian order
   */
  public static short bytesToShortLE(byte w1, byte w2) {
    return (short) ((0xff & w1) | ((0xff & w2) << 8));
  }

  /**
   * Expand `value` into 8 bytes in big endian order.
   *
   * @param value
   *     The value to expand into bytes
   * @return A new 8-element byte array holding the converted value
   */
  public static byte[] doubleToBytesBE(double value) {
    return longToBytesBE(Double.doubleToLongBits(value));
  }

  /**
   * Expand `value` into 8 byte words, assuming big endian order for the words, and store them into
   * `out` with the first word at index `offset`.
   *
   * @param value
   *     The value to expand into bytes
   * @param out
   *     The byte destination that has 8 bytes written to it
   * @param offset
   *     The index of the first word that is written
   * @throws IndexOutOfBoundsException
   *     if `out` does not have 8 remaining bytes at `offset`
   */
  public static void doubleToBytesBE(double value, byte[] out, int offset) {
    longToBytesBE(Double.doubleToLongBits(value), out, offset);
  }

  /**
   * Expand `value` into 8 byte words, assuming big endian order for the words. They are stored into
   * `buffer` starting at its current position. On success, its position is advanced by 8. If `out`
   * has a big endian order, this is equivalent to calling {@link ByteBuffer#putDouble(double)},
   * otherwise the the value is expanded and stored explicitly in big endian order.
   *
   * @param value
   *     The value to expand into bytes
   * @param out
   *     The byte destination
   * @throws BufferOverflowException
   *     if `out` does not have 8 remaining bytes
   */
  public static void doubleToBytesBE(double value, ByteBuffer out) {
    if (out.order() == ByteOrder.BIG_ENDIAN) {
      out.putDouble(value);
    } else {
      longToBytesBE(Double.doubleToLongBits(value), out);
    }
  }

  /**
   * Expand `value` into 8 bytes in little endian order.
   *
   * @param value
   *     The value to expand into bytes
   * @return A new 8-element byte array holding the converted value
   */
  public static byte[] doubleToBytesLE(double value) {
    return longToBytesLE(Double.doubleToLongBits(value));
  }

  /**
   * Expand `value` into 8 byte words, assuming little endian order for the words, and store them
   * into `out` with the first word at index `offset`.
   *
   * @param value
   *     The value to expand into bytes
   * @param out
   *     The byte destination that has 8 bytes written to it
   * @param offset
   *     The index of the first word that is written
   * @throws IndexOutOfBoundsException
   *     if `out` does not have 8 remaining bytes at `offset`
   */
  public static void doubleToBytesLE(double value, byte[] out, int offset) {
    longToBytesLE(Double.doubleToLongBits(value), out, offset);
  }

  /**
   * Expand `value` into 8 byte words, assuming little endian order for the words. They are stored
   * into `buffer` starting at its current position. On success, its position is advanced by 8. If
   * `out` has a little endian order, this is equivalent to calling {@link
   * ByteBuffer#putDouble(double)}, otherwise the the value is expanded and stored explicitly in
   * little endian order.
   *
   * @param value
   *     The value to expand into bytes
   * @param out
   *     The byte destination
   * @throws BufferOverflowException
   *     if `out` does not have 8 remaining bytes
   */
  public static void doubleToBytesLE(double value, ByteBuffer out) {
    if (out.order() == ByteOrder.LITTLE_ENDIAN) {
      out.putDouble(value);
    } else {
      longToBytesLE(Double.doubleToLongBits(value), out);
    }
  }

  /**
   * Expand `value` into 4 bytes in big endian order.
   *
   * @param value
   *     The value to expand into bytes
   * @return A new 4-element byte array holding the converted value
   */
  public static byte[] floatToBytesBE(float value) {
    return intToBytesBE(Float.floatToIntBits(value));
  }

  /**
   * Expand `value` into 4 byte words, assuming big endian order for the words, and store them into
   * `out` with the first word at index `offset`.
   *
   * @param value
   *     The value to expand into bytes
   * @param out
   *     The byte destination that has 4 bytes written to it
   * @param offset
   *     The index of the first word that is written
   * @throws IndexOutOfBoundsException
   *     if `out` does not have 8 remaining bytes at `offset`
   */
  public static void floatToBytesBE(float value, byte[] out, int offset) {
    intToBytesBE(Float.floatToIntBits(value), out, offset);
  }

  /**
   * Expand `value` into 4 byte words, assuming big endian order for the words. They are stored into
   * `buffer` starting at its current position. On success, its position is advanced by 4. If `out`
   * has a big endian order, this is equivalent to calling {@link ByteBuffer#putFloat(float)},
   * otherwise the the value is expanded and stored explicitly in big endian order.
   *
   * @param value
   *     The value to expand into bytes
   * @param out
   *     The byte destination
   * @throws BufferOverflowException
   *     if `out` does not have 4 remaining bytes
   */
  public static void floatToBytesBE(float value, ByteBuffer out) {
    if (out.order() == ByteOrder.BIG_ENDIAN) {
      out.putFloat(value);
    } else {
      intToBytesBE(Float.floatToIntBits(value), out);
    }
  }

  /**
   * Expand `value` into 4 bytes in little endian order.
   *
   * @param value
   *     The value to expand into bytes
   * @return A new 4-element byte array holding the converted value
   */
  public static byte[] floatToBytesLE(float value) {
    return intToBytesLE(Float.floatToIntBits(value));
  }

  /**
   * Expand `value` into 4 byte words, assuming little endian order for the words, and store them
   * into `out` with the first word at index `offset`.
   *
   * @param value
   *     The value to expand into bytes
   * @param out
   *     The byte destination that has 8 bytes written to it
   * @param offset
   *     The index of the first word that is written
   * @throws IndexOutOfBoundsException
   *     if `out` does not have 8 remaining bytes at `offset`
   */
  public static void floatToBytesLE(float value, byte[] out, int offset) {
    intToBytesLE(Float.floatToIntBits(value), out, offset);
  }

  /**
   * Expand `value` into 4 byte words, assuming little endian order for the words. They are stored
   * into `buffer` starting at its current position. On success, its position is advanced by 4. If
   * `out` has a little endian order, this is equivalent to calling {@link
   * ByteBuffer#putFloat(float)}, otherwise the the value is expanded and stored explicitly in
   * little endian order.
   *
   * @param value
   *     The value to expand into bytes
   * @param out
   *     The byte destination
   * @throws BufferOverflowException
   *     if `out` does not have 4 remaining bytes
   */
  public static void floatToBytesLE(float value, ByteBuffer out) {
    if (out.order() == ByteOrder.LITTLE_ENDIAN) {
      out.putFloat(value);
    } else {
      intToBytesLE(Float.floatToIntBits(value), out);
    }
  }

  /**
   * Expand `value` into 4 byte words, assuming big endian order for the words, and store them into
   * `out` with the first word at index `offset`.
   *
   * @param value
   *     The value to expand into bytes
   * @param out
   *     The byte destination that has 4 bytes written to it
   * @param offset
   *     The index of the first word that is written
   * @throws IndexOutOfBoundsException
   *     if `out` does not have 8 remaining bytes at `offset`
   */
  public static void intToBytesBE(int value, byte[] out, int offset) {
    Arguments.checkArrayRange("out.length", out.length, offset, 4);
    out[offset] = (byte) (value >> 24);
    out[offset + 1] = (byte) (value >> 16);
    out[offset + 2] = (byte) (value >> 8);
    out[offset + 3] = (byte) value;
  }

  /**
   * Expand `value` into 4 bytes in big endian order.
   *
   * @param value
   *     The value to expand into bytes
   * @return A new 4-element byte array holding the converted value
   */
  public static byte[] intToBytesBE(int value) {
    byte[] out = new byte[4];
    intToBytesBE(value, out, 0);
    return out;
  }

  /**
   * Expand `value` into 4 byte words, assuming big endian order for the words. They are stored into
   * `buffer` starting at its current position. On success, its position is advanced by 4. If `out`
   * has a big endian order, this is equivalent to calling {@link ByteBuffer#putInt(int)},
   * otherwise the the value is expanded and stored explicitly in big endian order.
   *
   * @param value
   *     The value to expand into bytes
   * @param out
   *     The byte destination
   * @throws BufferOverflowException
   *     if `out` does not have 4 remaining bytes
   */
  public static void intToBytesBE(int value, ByteBuffer out) {
    if (out.order() == ByteOrder.BIG_ENDIAN) {
      out.putInt(value);
    } else {
      if (out.remaining() < 4) {
        throw new BufferOverflowException();
      }
      out.put((byte) (value >> 24));
      out.put((byte) (value >> 16));
      out.put((byte) (value >> 8));
      out.put((byte) value);
    }
  }

  /**
   * Expand `value` into 4 byte words, assuming little endian order for the words, and store them
   * into `out` with the first word at index `offset`.
   *
   * @param value
   *     The value to expand into bytes
   * @param out
   *     The byte destination that has 4 bytes written to it
   * @param offset
   *     The index of the first word that is written
   * @throws IndexOutOfBoundsException
   *     if `out` does not have 8 remaining bytes at `offset`
   */
  public static void intToBytesLE(int value, byte[] out, int offset) {
    Arguments.checkArrayRange("out.length", out.length, offset, 4);
    out[offset] = (byte) value;
    out[offset + 1] = (byte) (value >> 8);
    out[offset + 2] = (byte) (value >> 16);
    out[offset + 3] = (byte) (value >> 24);
  }

  /**
   * Expand `value` into 4 bytes in little endian order.
   *
   * @param value
   *     The value to expand into bytes
   * @return A new 4-element byte array holding the converted value
   */
  public static byte[] intToBytesLE(int value) {
    byte[] out = new byte[4];
    intToBytesLE(value, out, 0);
    return out;
  }

  /**
   * Expand `value` into 4 byte words, assuming little endian order for the words. They are stored
   * into `buffer` starting at its current position. On success, its position is advanced by 4. If
   * `out` has a little endian order, this is equivalent to calling {@link ByteBuffer#putInt(int)},
   * otherwise the the value is expanded and stored explicitly in little endian order.
   *
   * @param value
   *     The value to expand into bytes
   * @param out
   *     The byte destination
   * @throws BufferOverflowException
   *     if `out` does not have 4 remaining bytes
   */
  public static void intToBytesLE(int value, ByteBuffer out) {
    if (out.order() == ByteOrder.LITTLE_ENDIAN) {
      out.putInt(value);
    } else {
      if (out.remaining() < 4) {
        throw new BufferOverflowException();
      }
      out.put((byte) value);
      out.put((byte) (value >> 8));
      out.put((byte) (value >> 16));
      out.put((byte) (value >> 24));
    }
  }

  /**
   * Expand `value` into 8 byte words, assuming big endian order for the words, and store them into
   * `out` with the first word at index `offset`.
   *
   * @param value
   *     The value to expand into bytes
   * @param out
   *     The byte destination that has 8 bytes written to it
   * @param offset
   *     The index of the first word that is written
   * @throws IndexOutOfBoundsException
   *     if `out` does not have 8 remaining bytes at `offset`
   */
  public static void longToBytesBE(long value, byte[] out, int offset) {
    Arguments.checkArrayRange("out.length", out.length, offset, 8);
    out[offset] = (byte) (value >> 56);
    out[offset + 1] = (byte) (value >> 48);
    out[offset + 2] = (byte) (value >> 40);
    out[offset + 3] = (byte) (value >> 32);
    out[offset + 4] = (byte) (value >> 24);
    out[offset + 5] = (byte) (value >> 16);
    out[offset + 6] = (byte) (value >> 8);
    out[offset + 7] = (byte) value;
  }

  /**
   * Expand `value` into 8 bytes in big endian order.
   *
   * @param value
   *     The value to expand into bytes
   * @return A new 8-element byte array holding the converted value
   */
  public static byte[] longToBytesBE(long value) {
    byte[] out = new byte[8];
    longToBytesBE(value, out, 0);
    return out;
  }

  /**
   * Expand `value` into 8 byte words, assuming big endian order for the words. They are stored into
   * `buffer` starting at its current position. On success, its position is advanced by 8. If `out`
   * has a big endian order, this is equivalent to calling {@link ByteBuffer#putLong(long)},
   * otherwise the the value is expanded and stored explicitly in big endian order.
   *
   * @param value
   *     The value to expand into bytes
   * @param out
   *     The byte destination
   * @throws BufferOverflowException
   *     if `out` does not have 8 remaining bytes
   */
  public static void longToBytesBE(long value, ByteBuffer out) {
    if (out.order() == ByteOrder.BIG_ENDIAN) {
      out.putLong(value);
    } else {
      if (out.remaining() < 8) {
        throw new BufferOverflowException();
      }

      out.put((byte) (value >> 56));
      out.put((byte) (value >> 48));
      out.put((byte) (value >> 32));
      out.put((byte) (value >> 24));
      out.put((byte) (value >> 16));
      out.put((byte) (value >> 8));
      out.put((byte) value);
    }
  }

  /**
   * Expand `value` into 8 byte words, assuming little endian order for the words, and store them
   * into `out` with the first word at index `offset`.
   *
   * @param value
   *     The value to expand into bytes
   * @param out
   *     The byte destination that has 8 bytes written to it
   * @param offset
   *     The index of the first word that is written
   * @throws IndexOutOfBoundsException
   *     if `out` does not have 8 remaining bytes at `offset`
   */
  public static void longToBytesLE(long value, byte[] out, int offset) {
    Arguments.checkArrayRange("out.length", out.length, offset, 8);
    out[offset] = (byte) value;
    out[offset + 1] = (byte) (value >> 8);
    out[offset + 2] = (byte) (value >> 16);
    out[offset + 3] = (byte) (value >> 24);
    out[offset + 4] = (byte) (value >> 32);
    out[offset + 5] = (byte) (value >> 40);
    out[offset + 6] = (byte) (value >> 48);
    out[offset + 7] = (byte) (value >> 56);
  }

  /**
   * Expand `value` into 8 bytes in little endian order.
   *
   * @param value
   *     The value to expand into bytes
   * @return A new 8-element byte array holding the converted value
   */
  public static byte[] longToBytesLE(long value) {
    byte[] out = new byte[8];
    longToBytesLE(value, out, 0);
    return out;
  }

  /**
   * Expand `value` into 8 byte words, assuming little endian order for the words. They are stored
   * into `buffer` starting at its current position. On success, its position is advanced by 8. If
   * `out` has a little endian order, this is equivalent to calling {@link
   * ByteBuffer#putLong(long)}, otherwise the the value is expanded and stored explicitly in little
   * endian order.
   *
   * @param value
   *     The value to expand into bytes
   * @param out
   *     The byte destination
   * @throws BufferOverflowException
   *     if `out` does not have 8 remaining bytes
   */
  public static void longToBytesLE(long value, ByteBuffer out) {
    if (out.order() == ByteOrder.LITTLE_ENDIAN) {
      out.putLong(value);
    } else {
      if (out.remaining() < 8) {
        throw new BufferOverflowException();
      }
      out.put((byte) value);
      out.put((byte) (value >> 8));
      out.put((byte) (value >> 16));
      out.put((byte) (value >> 24));
      out.put((byte) (value >> 32));
      out.put((byte) (value >> 40));
      out.put((byte) (value >> 48));
      out.put((byte) (value >> 56));
    }
  }

  /**
   * Expand `value` into 2 byte words, assuming big endian order for the words, and store them into
   * `out` with the first word at index `offset`.
   *
   * @param value
   *     The value to expand into bytes
   * @param out
   *     The byte destination that has 2 bytes written to it
   * @param offset
   *     The index of the first word that is written
   * @throws IndexOutOfBoundsException
   *     if `out` does not have 8 remaining bytes at `offset`
   */
  public static void shortToBytesBE(short value, byte[] out, int offset) {
    Arguments.checkArrayRange("out.length", out.length, offset, 2);
    out[offset] = (byte) (value >> 8);
    out[offset + 1] = (byte) value;
  }

  /**
   * Expand `value` into 2 bytes in big endian order.
   *
   * @param value
   *     The value to expand into bytes
   * @return A new 2-element byte array holding the converted value
   */
  public static byte[] shortToBytesBE(short value) {
    byte[] out = new byte[2];
    shortToBytesBE(value, out, 0);
    return out;
  }

  /**
   * Expand `value` into 2 byte words, assuming big endian order for the words. They are stored into
   * `buffer` starting at its current position. On success, its position is advanced by 2. If `out`
   * has a big endian order, this is equivalent to calling {@link ByteBuffer#putShort(short)},
   * otherwise the the value is expanded and stored explicitly in big endian order.
   *
   * @param value
   *     The value to expand into bytes
   * @param out
   *     The byte destination
   * @throws BufferOverflowException
   *     if `out` does not have 2 remaining bytes
   */
  public static void shortToBytesBE(short value, ByteBuffer out) {
    if (out.order() == ByteOrder.BIG_ENDIAN) {
      out.putShort(value);
    } else {
      if (out.remaining() < 2) {
        throw new BufferOverflowException();
      }
      out.put((byte) (value >> 8));
      out.put((byte) value);
    }
  }

  /**
   * Expand `value` into 2 byte words, assuming little endian order for the words, and store them
   * into `out` with the first word at index `offset`.
   *
   * @param value
   *     The value to expand into bytes
   * @param out
   *     The byte destination that has 2 bytes written to it
   * @param offset
   *     The index of the first word that is written
   * @throws IndexOutOfBoundsException
   *     if `out` does not have 8 remaining bytes at `offset`
   */
  public static void shortToBytesLE(short value, byte[] out, int offset) {
    Arguments.checkArrayRange("out.length", out.length, offset, 2);
    out[offset] = (byte) value;
    out[offset + 1] = (byte) (value >> 8);
  }

  /**
   * Expand `value` into 2 bytes in little endian order.
   *
   * @param value
   *     The value to expand into bytes
   * @return A new 2-element byte array holding the converted value
   */
  public static byte[] shortToBytesLE(short value) {
    byte[] out = new byte[2];
    shortToBytesLE(value, out, 0);
    return out;
  }

  /**
   * Expand `value` into 2 byte words, assuming little endian order for the words. They are stored
   * into `buffer` starting at its current position. On success, its position is advanced by 2. If
   * `out` has a little endian order, this is equivalent to calling {@link
   * ByteBuffer#putShort(short)}, otherwise the the value is expanded and stored explicitly in
   * little endian order.
   *
   * @param value
   *     The value to expand into bytes
   * @param out
   *     The byte destination
   * @throws BufferOverflowException
   *     if `out` does not have 2 remaining bytes
   */
  public static void shortToBytesLE(short value, ByteBuffer out) {
    if (out.order() == ByteOrder.LITTLE_ENDIAN) {
      out.putShort(value);
    } else {
      if (out.remaining() < 2) {
        throw new BufferOverflowException();
      }
      out.put((byte) value);
      out.put((byte) (value >> 8));
    }
  }
}
