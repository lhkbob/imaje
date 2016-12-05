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
public enum MeasurementUnit {
  // ISO 5-3 densitometer response. This is the accepted standard for
  // reflection densitometers for measuring photographic color prints.
  STATUS_A("StaA"),
  // ISO 5-3 densitometer response which is the accepted standard in
  // Europe for color reflection densitometers
  STATUS_E("StaE"),
  // ISO 5-3 densitometer response commonly referred to as narrow band or
  // interference-type response
  STATUS_I("StaI"),
  // ISO 5-3 wide band color reflection densitometer response which is
  // the accepted standard in the United States for color reflection
  // densitometers
  STATUS_T("StaT"),
  // ISO 5-3 densitometer response for measuring color negatives
  STATUS_M("StaM"),
  // DIN 16536-2 densitometer response, with no polarizing filter.
  DIN_E_UNPOLARIZED("DN"),
  // DIN 16536-2 densitometer response, with polarizing filter.
  DIN_E_POLARIZED("DN P"),
  // DIN 16536-2 narrow band densitometer response, with no
  // polarizing filter.
  DIN_I_UNPOLARIZED("DNN"),
  // DIN 16536-2 narrow band densitometer response, with polarizing filter.
  DIN_I_POLARIZED("DNNP");

  private final Signature signature;

  MeasurementUnit(String name) {
    signature = Signature.fromName(name);
  }

  public static MeasurementUnit fromSignature(Signature s) {
    for (MeasurementUnit v : values()) {
      if (Objects.equals(v.getSignature(), s)) {
        return v;
      }
    }

    throw new IllegalArgumentException("Unknown signature: " + s);
  }

  public Signature getSignature() {
    return signature;
  }
}
