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

import com.lhkbob.imaje.color.icc.Colorant;
import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextU16Fixed16Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt16Number;

/**
 *
 */
public final class ChromaticityTypeParser implements TagParser<Colorant> {
  public static final Signature SIGNATURE = Signature.fromName("chrm");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public Colorant parse(Signature tag, Header header, ByteBuffer data) {
    int channels = nextUInt16Number(data);
    int colorant = nextUInt16Number(data);

    if (colorant != 0) {
      // predefined colorant
      if (channels != 3 && channels != 0) {
        throw new IllegalStateException(
            "Unexpected channel count for predefined colorant type: " + channels);
      }
      switch (colorant) {
      case 1:
        return Colorant.ITU_R_BT_709_2;
      case 2:
        return Colorant.SMPTE_RP145;
      case 3:
        return Colorant.EBU_TECH_3213_E;
      case 4:
        return Colorant.P22;
      default:
        throw new IllegalStateException("Uknonwn colorant type value: " + colorant);
      }
    }

    double[] xs = new double[channels];
    double[] ys = new double[channels];
    for (int i = 0; i < channels; i++) {
      xs[i] = nextU16Fixed16Number(data);
      ys[i] = nextU16Fixed16Number(data);
    }

    return new Colorant(xs, ys);
  }
}
