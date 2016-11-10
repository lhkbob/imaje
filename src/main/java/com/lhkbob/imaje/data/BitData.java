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
 * BitData
 * =======
 *
 * BitData is a DataBuffer that stores bit fields in each primitive, e.g. the underlying elements
 * will be `int`, `short`, `byte`, or `long`. However, the bit fields may be interpreted differently
 * than the standard Java interpretation of those primitive types.
 *
 * As an interface, BitData exposes all of its values as `long`. However, only the least significant
 * bits (determined by {@link #getBitSize()} are valid. When setting values, bits beyond that are
 * discarded, and when getting values the high bits are set to 0.
 *
 * @author Michael Ludwig
 */
public interface BitData extends DataBuffer {
  /**
   * Get the bit field at `index` in this buffer. The returned `long` will have valid bits stored
   * in bits 0 to `getBitSize() - 1` and 0s in all higher bits.
   *
   * @param index
   *     The primitive element to access, 0-based.
   * @return The bit field at `index`
   *
   * @throws IndexOutOfBoundsException
   *     if `index` is less than 0 or greater than or equal to {@link #getLength()}
   */
  long getBits(long index);

  /**
   * Set the bit field at `index` in this buffer to the bits represented by `value`. Only the
   * bits 0 to `getBitSize() - 1` of `value` will be stored, all other bits are ignored.
   *
   * @param index
   *     The primitive element to modify, 0-based
   * @param value
   *     The new bit field value
   * @throws IndexOutOfBoundsException
   *     if `index` is less than 0 or greater than or equal to {@link #getLength()}
   */
  void setBits(long index, long value);
}
