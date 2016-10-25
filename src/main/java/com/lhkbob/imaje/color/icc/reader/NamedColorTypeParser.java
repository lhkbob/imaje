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

import com.lhkbob.imaje.color.icc.ColorSpace;
import com.lhkbob.imaje.color.icc.GenericColorValue;
import com.lhkbob.imaje.color.icc.NamedColor;
import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextASCIIString;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextLABNumberLegacy16;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt16Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt32Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextXYZNumber16;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.require;

/**
 *
 */
public final class NamedColorTypeParser implements TagParser<List<NamedColor>> {
  public static final Signature SIGNATURE = Signature.fromName("ncl2");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public List<NamedColor> parse(Signature tag, Header header, ByteBuffer data) {
    long vendorFlags = nextUInt32Number(data); // ignored for now
    int colorCount = Math.toIntExact(nextUInt32Number(data));
    int deviceCoords = Math.toIntExact(nextUInt32Number(data));
    String namePrefix = nextASCIIString(data, 32);
    String nameSuffix = nextASCIIString(data, 32);

    require(data, colorCount * (2 * deviceCoords
        + 38)); // 2 bytes per device coords, 6 bytes for PCS, 32 bytes for name
    List<NamedColor> colors = new ArrayList<>();
    for (int i = 0; i < colorCount; i++) {
      String rootName = nextASCIIString(data, 32);
      String finalName = namePrefix + rootName + nameSuffix;

      GenericColorValue pcs = (header.getBSideColorSpace() == ColorSpace.CIEXYZ ? nextXYZNumber16(
          data) : nextLABNumberLegacy16(data));
      double[] device = new double[deviceCoords];
      for (int j = 0; j < deviceCoords; j++) {
        device[j] = nextUInt16Number(data);
      }

      colors.add(new NamedColor(finalName, pcs, GenericColorValue.genericColor(device)));
    }

    return colors;
  }
}
