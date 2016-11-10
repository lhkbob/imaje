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

/**
 * DataBuffer
 * ==========
 *
 * DataBuffer is the top-level interface to represent a one-dimensional array of primitive data. The
 * data can be stored in many different ways, ranging from arrays to NIO buffers or custom
 * implementations. The primary purpose of the DataBuffer and its primitive-specific subtypes is
 * provide a unified way of accessing bulk data regardless of an application's needs, which may
 * determine if arrays or buffers are more appropriate. To be future proof, DataBuffers use `long`
 * to represent length and index access.
 *
 * DataBuffer has two primary subtypes: {@link BitData} and {@link NumericData} that denote
 * different semantic interpretations of the data. BitData treats the primitives as a mean to store
 * bit patterns, which may be interpreted as the standard 2's complement interpretation of integers,
 * or IEEE floating point numbers. BitData is useful when operating at a low level for custom
 * numeric representations, such as packing multiple values into a single primitive. NumericData
 * maps underlying primitive data to the real numbers. The details of this mapping depends on the
 * specific representation of the data but NumericData provides a clean interface to read and write
 * `double` values regardless of underlying storage.
 *
 * @author Michael Ludwig
 */
public interface DataBuffer {
  /**
   * @return The length of the data, must be at least 0.
   */
  long getLength();

  /**
   * @return Whether or not underlying multi-byte data is stored as big Endian or little Endian.
   */
  boolean isBigEndian();

  /**
   * Get whether or not the data can be accessed or transferred directly to the GPU. The
   * requirements for this are open to change as GPU adapters for Java are developed further. The
   * current requirements are:
   *
   * 1. Underlying data is stored in NIO buffers.
   * 2. The NIO buffer is direct (e.g. continuous off-heap block of memory that can be referenced
   * as a pointer in JNI code).
   * 3. The byte order of the data must match the native system's byte order.
   *
   * @return The GPU accessibility of the underlying data
   */
  boolean isGPUAccessible();

  /**
   * @return The number of bits per primitive of the underlying data
   */
  int getBitSize();

  /**
   * Copy values from `data` into this buffer. Set the values of this data buffer, starting at
   * `writeIndex`, to the those in `data` starting at `readIndex`. `length` values will be copied.
   * An exception is thrown if there are not `length` primitives available in this buffer or `data`
   * based on the appropriate write and read indices. Similarly, an exception is thrown if the index
   * ranges would access any out-of-bounds index of the underlying data. These exceptions must be
   * thrown before any modifications are made to this buffer.
   *
   * The `data` argument is of type DataBuffer but that does not mean that every type of input
   * DataBuffer is supported for a given implementation. The following minimum support and behavior
   * is required:
   *
   * 1. A BitData implementation must support BitData input buffers of the same primitive type,
   * i.e. ByteData implementations must support all other ByteData implementations.
   * 3. Copying between BitData with different bit counts is not supported.
   * 4. A NumericData implementation must support copying values from any other NumericData
   * implementation.
   * 5. Copying between NumericData copies the effective numeric values (up to loss from
   * representation changes).
   * 6. Copying between a NumericData and a BitData (and vice versa) is not supported.
   *
   * @param writeIndex
   *     The index of this buffer that receives the first copied value
   * @param data
   *     The data buffer being copied into this buffer
   * @param readIndex
   *     The index into `data` for the first value that is copied
   * @param length
   *     The number of primitives to copy from `data` to `this`
   * @throws IndexOutOfBoundsException
   *     if the range from `writeIndex` to `writeIndex +
   *     length` or the range from `readIndex` to `readIndex + length` accesses bad data.
   */
  void set(long writeIndex, DataBuffer data, long readIndex, long length);
}
