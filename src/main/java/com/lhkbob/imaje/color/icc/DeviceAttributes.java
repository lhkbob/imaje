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

/**
 *
 */
public final class DeviceAttributes {
  public static final long COLOR_MEDIA_MASK = 1 << 3;
  public static final long GLOSSY_MATTE_MASK = 1 << 1;
  public static final long POLARITY_MASK = 1 << 2;
  public static final long REFLECTIVITY_TRANSPARENCY_MASK = 1;
  private final long bitfield;

  public DeviceAttributes(long bitfield) {
    this.bitfield = bitfield;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    return o instanceof DeviceAttributes && ((DeviceAttributes) o).bitfield == bitfield;
  }

  public long getBitField() {
    return bitfield;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(bitfield);
  }

  public boolean isMatte() {
    return isBitSet(GLOSSY_MATTE_MASK);
  }

  public boolean isMediaBlackAndWhite() {
    return isBitSet(COLOR_MEDIA_MASK);
  }

  public boolean isPolarityNegative() {
    return isBitSet(POLARITY_MASK);
  }

  public boolean isTransparent() {
    return isBitSet(REFLECTIVITY_TRANSPARENCY_MASK);
  }

  @Override
  public String toString() {
    return String.format(
        "DeviceAttributes (bits: %s, material: %s + %s, polarity: %s, media: %s)",
        Long.toHexString(bitfield), (isTransparent() ? "transparent" : "reflective"),
        (isMatte() ? "matte" : "glossy"), (isPolarityNegative() ? "negative" : "positive"),
        (isMediaBlackAndWhite() ? "black & white" : "color"));
  }

  private boolean isBitSet(long mask) {
    return (bitfield & mask) != 0;
  }
}
