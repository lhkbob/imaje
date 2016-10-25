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
import com.lhkbob.imaje.color.transform.curves.UnitGammaFunction;

import java.nio.ByteBuffer;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextS15Fixed16Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt16Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.skip;

/**
 *
 */
public final class ParametricCurveTypeParser implements TagParser<Curve> {
  public static final Signature SIGNATURE = Signature.fromName("para");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public Curve parse(Signature tag, Header header, ByteBuffer data) {
    int functionType = nextUInt16Number(data);
    skip(data, 2);

    switch (functionType) {
    case 0: {
      double gamma = nextS15Fixed16Number(data);
      return UnitGammaFunction.newSimpleCurve(gamma);
    }
    case 1: {
      double gamma = nextS15Fixed16Number(data);
      double a = nextS15Fixed16Number(data);
      double b = nextS15Fixed16Number(data);
      return UnitGammaFunction.newCIE122_1996Curve(gamma, a, b);
    }
    case 2: {
      double gamma = nextS15Fixed16Number(data);
      double a = nextS15Fixed16Number(data);
      double b = nextS15Fixed16Number(data);
      double c = nextS15Fixed16Number(data);
      return UnitGammaFunction.newIEC61966_3Curve(gamma, a, b, c);
    }
    case 3: {
      double gamma = nextS15Fixed16Number(data);
      double a = nextS15Fixed16Number(data);
      double b = nextS15Fixed16Number(data);
      double c = nextS15Fixed16Number(data);
      double d = nextS15Fixed16Number(data);
      return UnitGammaFunction.newIEC61966_2_1Curve(gamma, a, b, c, d);
    }
    case 4: {
      double gamma = nextS15Fixed16Number(data);
      double a = nextS15Fixed16Number(data);
      double b = nextS15Fixed16Number(data);
      double c = nextS15Fixed16Number(data);
      double d = nextS15Fixed16Number(data);
      double e = nextS15Fixed16Number(data);
      double f = nextS15Fixed16Number(data);
      return new UnitGammaFunction(gamma, a, b, c, e, f, d);
    }
    default:
      throw new IllegalStateException("Unknown parametric curve type: " + functionType);
    }
  }
}
