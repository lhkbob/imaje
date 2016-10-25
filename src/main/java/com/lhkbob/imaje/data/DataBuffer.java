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
 * How to add bulk operations and updates? What types of updates are necessary:
 *
 * 1. Copy into the source from an array, buffer, stream, channel that are assumed to exactly match
 *    the expected bit pattern of the source
 * 2. Copy into the source from an array, buffer, stream, channel that are numeric and must be
 *    re-encoded into the source's format --> Unnecessary, just use the DataSource copy then
 * 3. Copy back and forth between two sources, either optimized if bit compatible or by decoding
 *    and recoding as necessary
 * 4. All of these copies should have source and dest offsets and length, for streams it should have length
 *    but no dest offset, and length can be implicit (e.g. read as much as possible, either until source
 *    is full or stream is empty)
 *
 *    Other things to think about:
 *    The specific types that these things support require using the concrete type, or at least the
 *    more concrete interface like ByteSource. But this doesn't apply when you want to send values
 *    to
 */
public interface DataBuffer {
  long getLength();

  boolean isBigEndian();

  boolean isGPUAccessible();

  int getBitSize();
}
