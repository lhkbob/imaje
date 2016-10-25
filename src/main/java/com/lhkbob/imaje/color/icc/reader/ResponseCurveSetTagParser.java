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

import com.lhkbob.imaje.color.icc.GenericColorValue;
import com.lhkbob.imaje.color.icc.MeasurementUnit;
import com.lhkbob.imaje.color.icc.ResponseCurveSet;
import com.lhkbob.imaje.color.icc.Signature;
import com.lhkbob.imaje.color.transform.curves.SampledCurve;

import java.nio.ByteBuffer;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextS15Fixed16Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextSignature;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt16Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt32Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextXYZNumber;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.skip;

/**
 *
 */
public final class ResponseCurveSetTagParser implements TagParser<ResponseCurveSet> {
  public static final Signature SIGNATURE = Signature.fromName("rcs2");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public ResponseCurveSet parse(Signature tag, Header header, ByteBuffer data) {
    int tagStart = data.position() - 8;
    int channelCount = nextUInt16Number(data);
    int measurementCount = nextUInt16Number(data);

    int[] offsets = new int[measurementCount];
    for (int i = 0; i < offsets.length; i++) {
      offsets[i] = Math.toIntExact(nextUInt32Number(data));
    }

    int tagEnd = data.position();
    ResponseCurveSet.Builder builder = ResponseCurveSet.newResponseCurveSet(channelCount);
    for (int i = 0; i < measurementCount; i++) {
      data.position(tagStart + offsets[i]);
      readMeasurementCurve(data, channelCount, builder);
      tagEnd = Math.max(tagEnd, data.position());
    }
    data.position(tagEnd);
    return builder.build();
  }

  private void readMeasurementCurve(
      ByteBuffer data, int channelCount, ResponseCurveSet.Builder builder) {
    MeasurementUnit units = MeasurementUnit.fromSignature(nextSignature(data));
    int[] responseCurveSampleSizes = new int[channelCount];
    // Read number of response samples for each channel's curve
    for (int i = 0; i < channelCount; i++) {
      responseCurveSampleSizes[i] = Math.toIntExact(nextUInt32Number(data));
    }
    // Read PCS values for each channel
    for (int i = 0; i < channelCount; i++) {
      builder.forUnit(units)
          .setChannelMeasurement(i, nextXYZNumber(data, GenericColorValue.ColorType.PCSXYZ));
    }
    // Read variable response sample counts for each channel, representing it
    // as a SampledCurve with both xs and ys provided.
    for (int i = 0; i < channelCount; i++) {
      double[] xs = new double[responseCurveSampleSizes[i]];
      double[] ys = new double[responseCurveSampleSizes[i]];

      for (int j = 0; j < xs.length; j++) {
        // Read a response16Number, which is a uint16 normalized device coordinate
        // 2 bytes of padding, and then a s15Fixed16 response value. The NDC
        // represents the x axis of the curve.
        xs[j] = nextUInt16Number(data) / 65535.0;
        skip(data, 2);
        ys[j] = nextS15Fixed16Number(data);
      }

      builder.forUnit(units).setResponseCurve(i, new SampledCurve(xs, ys));
    }
  }
}
