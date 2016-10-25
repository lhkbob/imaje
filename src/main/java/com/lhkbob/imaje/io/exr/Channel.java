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

import com.lhkbob.imaje.data.Bytes;
import com.lhkbob.imaje.io.IO;
import com.lhkbob.imaje.io.InvalidImageException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 *
 */
public class Channel {
  private final String name;
  private final ChannelFormat type;
  // pLinear is merely a hint for compression of a channel, and does not specify whether or not
  // the actual values have been gamma-encoded, etc.
  private final boolean linear;
  private final int xSampling;
  private final int ySampling;

  public Channel(String name, ChannelFormat type, boolean linear, int xSampling, int ySampling) {
    this.name = name;
    this.type = type;
    this.linear = linear;
    this.xSampling = xSampling;
    this.ySampling = ySampling;
  }

  public String getFullName() {
    return name;
  }

  public ChannelFormat getFormat() {
    return type;
  }

  public boolean isLinear() {
    return linear;
  }

  public int getXSampling() {
    return xSampling;
  }

  public int getYSampling() {
    return ySampling;
  }

  public String getGroupName() {
    int lastDot = name.lastIndexOf(".");
    if (lastDot < 0) {
      // No group
      return "";
    } else {
      // Group name is everything in front of the period
      return name.substring(0, lastDot);
    }
  }

  public String getName() {
    int lastDot = name.lastIndexOf(".");
    if (lastDot < 0) {
      // No group
      return name;
    } else {
      // Channel name is everything after the period
      return name.substring(lastDot + 1);
    }
  }

  public static Channel read(SeekableByteChannel in, ByteBuffer work) throws IOException {
    String name = TypeReader.readNullTerminatedString(in, work);

    if (!IO.read(in, work, 16)) {
      throw new InvalidImageException("Unexpected EOF while reading Channel definition");
    }
    int pf = Bytes.bytesToIntLE(work);
    boolean linear = Bytes.bytesToIntLE(work) != 0;
    int xs = Bytes.bytesToIntLE(work);
    int ys = Bytes.bytesToIntLE(work);

    return new Channel(name, ChannelFormat.values()[pf], linear, xs, ys);
  }
}
