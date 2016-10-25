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
public final class NamedColor {
  private final GenericColorValue device;
  private final String name;
  private final GenericColorValue pcs;

  public NamedColor(String name, GenericColorValue pcs, @Arguments.Nullable GenericColorValue device) {
    Arguments.notNull("name", name);
    Arguments.notNull("pcs", pcs);

    if (pcs.getType() != GenericColorValue.ColorType.PCSLAB
        && pcs.getType() != GenericColorValue.ColorType.PCSXYZ) {
      throw new IllegalArgumentException("PCS color must be of type PCSLAB or PCSXYZ");
    }

    this.name = name;
    this.pcs = pcs;
    this.device = device;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NamedColor)) {
      return false;
    }

    NamedColor that = (NamedColor) o;
    return name.equals(that.name) && pcs.equals(that.pcs) && !(device != null ? !device
        .equals(that.device) : that.device != null);
  }

  public GenericColorValue getDeviceColor() {
    return device;
  }

  public String getName() {
    return name;
  }

  public GenericColorValue getPCSColor() {
    return pcs;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + pcs.hashCode();
    result = 31 * result + (device != null ? device.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return String.format("Named color (name: %s, pcs: %s, device: %s)", name, pcs, device);
  }
}
