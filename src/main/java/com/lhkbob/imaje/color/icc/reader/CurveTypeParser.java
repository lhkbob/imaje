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
package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.Signature;
import com.lhkbob.imaje.color.transform.curves.Curve;
import com.lhkbob.imaje.color.transform.curves.DomainWindow;
import com.lhkbob.imaje.color.transform.curves.LinearFunction;
import com.lhkbob.imaje.color.transform.curves.UniformlySampledCurve;
import com.lhkbob.imaje.color.transform.curves.UnitGammaFunction;

import java.nio.ByteBuffer;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextU8Fixed8Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt16Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt32Number;

/**
 *
 */
public final class CurveTypeParser implements TagParser<Curve> {
  public static final Signature SIGNATURE = Signature.fromName("curv");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public Curve parse(Signature tag, Header header, ByteBuffer data) {
    int tableLength = Math.toIntExact(nextUInt32Number(data));
    if (tableLength == 0) {
      // Identity response, but limited to a 0-1 range
      return new DomainWindow(new LinearFunction(1.0, 0.0), 0.0, 1.0);
    } else if (tableLength == 1) {
      // Next two bytes are a u8f8 representing a gamma exponent
      double gamma = nextU8Fixed8Number(data);
      return UnitGammaFunction.newSimpleCurve(gamma);
    } else {
      // It's a table of normalized uint16's
      double[] table = new double[tableLength];
      for (int i = 0; i < tableLength; i++) {
        table[i] = nextUInt16Number(data) / 65535.0;
      }
      return new UniformlySampledCurve(0.0, 1.0, table);
    }
  }
}
