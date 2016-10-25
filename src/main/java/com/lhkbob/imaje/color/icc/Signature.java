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
package com.lhkbob.imaje.color.icc;

import com.lhkbob.imaje.util.Arguments;

/**
 *
 */
public final class Signature {
  public static final Signature NULL = Signature.fromBitField(0L);

  private final String text;
  private final long uint32;

  private Signature(String text, long uint32) {
    Arguments.notNull("text", text);

    this.text = text;
    this.uint32 = uint32;
  }

  public static Signature fromBitField(long uint32) {
    StringBuilder sb = new StringBuilder(4);
    byte b0 = (byte) (uint32 >> 24);
    byte b1 = (byte) (uint32 >> 16);
    byte b2 = (byte) (uint32 >> 8);
    byte b3 = (byte) (uint32);
    sb.append((char) b0).append((char) b1).append((char) b2).append((char) b3);
    return new Signature(sb.toString().trim(), (0xffffffffL & uint32));
  }

  public static Signature fromName(String name) {
    Arguments.notNull("name", name);
    if (name.length() > SIGNATURE_BYTE_LENGTH) {
      throw new IllegalArgumentException("Signature names can be up to 4 characters, not: " + name);
    }

    long bits = 0;
    for (int i = 0; i < SIGNATURE_BYTE_LENGTH; i++) {
      char c = (i < name.length() ? name.charAt(i) : PADDING);
      int shift = (SIGNATURE_BYTE_LENGTH - i - 1) * 8;
      bits |= ((0xff & c) << shift);
    }

    return new Signature(name, bits);
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Signature && ((Signature) o).uint32 == uint32;
  }

  public long getBitField() {
    return uint32;
  }

  public String getName() {
    return text;
  }

  @Override
  public int hashCode() {
    return text.hashCode();
  }

  @Override
  public String toString() {
    return String.format("%s (%s)", text, Long.toHexString(uint32));
  }

  private static final char PADDING = ' ';
  private static final int SIGNATURE_BYTE_LENGTH = 4;
}
