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
package com.lhkbob.imaje.data.large;

import com.lhkbob.imaje.data.DataBuffer;

import java.util.Arrays;

/**
 *
 */
public abstract class AbstractLargeDataSource<S extends DataBuffer> implements DataBuffer {
  private final S[] sources;
  private final long totalLength;
  private final long repeatedLength;
  private final boolean bigEndian;

  public AbstractLargeDataSource(S[] sources) {
    this.sources = Arrays.copyOf(sources, sources.length);
    repeatedLength = sources[0].getLength();
    bigEndian = sources[0].isBigEndian();

    long total = repeatedLength;
    for (int i = 1; i < sources.length; i++) {
      if (i < sources.length - 1) {
        if (sources[i].getLength() != repeatedLength) {
          throw new IllegalArgumentException(
              "All but last source must have the same size, expected: " + repeatedLength + ", but was " + sources[i].getLength());
        }
      } else {
        if (sources[i].getLength() > repeatedLength) {
          throw new IllegalArgumentException("Last source can have at most size " + repeatedLength + ", but was " + sources[i].getLength());
        }
      }

      if (bigEndian != sources[i].isBigEndian()) {
        throw new IllegalArgumentException("Endianness of sources are not all the same");
      }
      total += sources[i].getLength();
    }
    totalLength = total;
  }

  public S[] getSources() {
    return Arrays.copyOf(sources, sources.length);
  }

  @Override
  public long getLength() {
    return totalLength;
  }

  @Override
  public boolean isBigEndian() {
    return bigEndian;
  }

  @Override
  public boolean isGPUAccessible() {
    // Although the GPU might be able to support data sets that have more than a 32 bit index,
    // because Java can't allocate a contiguous array that long there is no way to have such a long
    // data source represented by a single pointer; thus this form of large data source cannot be
    // GPU accessible
    return false;
  }

  protected long getIndexInSource(long index) {
    return index % repeatedLength;
  }

  protected S getSource(long index) {
    return sources[(int) (index / repeatedLength)];
  }

  protected <T> void bulkOperation(
      BulkOperation<S, T> op, long dataIndex, T values, int offset, int length) {
    long dataLength = dataIndex + length;
    while (dataIndex < dataLength) {
      // Get subsource and location within subsource for the copy
      S source = getSource(dataIndex);
      long inSourceIndex = getIndexInSource(dataIndex);

      // Get the number of elements from values that this subsource can receive
      long remainingSource = source.getLength() - inSourceIndex;
      int consumed = Math.toIntExact(Math.min(length, remainingSource));

      // Copy this new range into the subsource
      op.run(source, inSourceIndex, values, offset, consumed);

      // Update current index and range for the remainder of the values array
      dataIndex += consumed;
      offset += consumed;
      length -= consumed;
    }
  }
}
