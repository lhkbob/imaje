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

import java.util.Objects;

/**
 *
 */
public enum ProfileClass {
  /**
   * A profile describing how to transform device colors to the PCS, where the device
   * is an input device such as a camera or scanner.
   */
  INPUT_DEVICE_PROFILE("scnr"),
  /**
   * A profile describing how a device that emits or displays colors dynamically (e.g.
   * TV or monitor) relates to the PCS.
   */
  DISPLAY_DEVICE_PROFILE("mntr"),
  /**
   * A profile describing how printers and other non-emitting output devices relate to the
   * PCS.
   */
  OUTPUT_DEVICE_PROFILE("prtr"),
  /**
   * An optimized transformation profile between two devices that bypasses or implicitly
   * encodes the intermediate transformation to and from the PCS.
   */
  DEVICE_LINK_PROFILE("link"),
  /**
   * A transformation profile that specifies the relationship between a defined color space
   * and the PCS.
   */
  COLOR_SPACE_PROFILE("spac"),
  /**
   * A transformation within the PCS that can be used to perform image manipulations and other effects.
   */
  ABSTRACT_PROFILE("abst"),
  /**
   * A profile that provides explicit mappings of certain colors in device coordinates to their
   * PCS values.
   */
  NAMED_COLOR_PROFILE("nmcl");

  private final Signature signature;

  ProfileClass(String signature) {
    this.signature = Signature.fromName(signature);
  }

  public static ProfileClass fromSignature(Signature sig) {
    for (ProfileClass v : values()) {
      if (Objects.equals(v.getSignature(), sig)) {
        return v;
      }
    }

    throw new IllegalArgumentException("Unknown signature: " + sig);
  }

  public Signature getSignature() {
    return signature;
  }
}
