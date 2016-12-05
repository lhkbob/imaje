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
public enum DeviceTechnology {
  UNSPECIFIED(Signature.NULL),
  FILM_SCANNER(Signature.fromName("fscn")),
  DIGITAL_CAMERA(Signature.fromName("dcam")),
  REFLECTIVE_SCANNER(Signature.fromName("rscn")),
  INK_JET_PRINTER(Signature.fromName("ijet")),
  THERMAL_WAX_PRINTER(Signature.fromName("twax")),
  ELECTROPHOTOGRAPHIC_PRINTER(Signature.fromName("epho")),
  ELECTROSTATIC_PRINTER(Signature.fromName("esta")),
  DYE_SUBLIMATION_PRINTER(Signature.fromName("dsub")),
  PHOTOGRAPHIC_PAPER_PRINTER(Signature.fromName("rpho")),
  FILM_WRITER(Signature.fromName("fprn")),
  VIDEO_MONITOR(Signature.fromName("vidm")),
  VIDEO_CAMERA(Signature.fromName("vidc")),
  PROJECTION_TELEVISION(Signature.fromName("pjtv")),
  CATHODE_RAY_TUBE_DISPLAY(Signature.fromName("CRT")),
  PASSIVE_MATRIX_DISPLAY(Signature.fromName("PMD")),
  ACTIVE_MATRIX_DISPLAY(Signature.fromName("AMD")),
  PHOTO_CD(Signature.fromName("KPCD")),
  PHOTOGRAPHIC_IMAGE_SETTER(Signature.fromName("imgs")),
  GRAVURE(Signature.fromName("grav")),
  OFFSET_LITHOGRAPHY(Signature.fromName("offs")),
  SILKSCREEN(Signature.fromName("silk")),
  MOTION_PICTURE_FILM_SCANNER(Signature.fromName("mpfs")),
  MOTION_PICTURE_FILM_RECORDER(Signature.fromName("mpfr")),
  DIGITAL_MOTION_PICTURE_CAMERA(Signature.fromName("dmpc")),
  DIGITAL_CINEMA_PROJECTOR(Signature.fromName("dcpj"));

  private final Signature signature;

  DeviceTechnology(Signature signature) {
    this.signature = signature;
  }

  public static DeviceTechnology fromSignature(Signature sig) {
    for (DeviceTechnology v : values()) {
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
