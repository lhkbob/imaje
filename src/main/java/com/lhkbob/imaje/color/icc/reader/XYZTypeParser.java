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
import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextXYZNumber;

/**
 *
 */
public final class XYZTypeParser implements TagParser<List<GenericColorValue>> {
  public static final Signature SIGNATURE = Signature.fromName("XYZ");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public List<GenericColorValue> parse(Signature tag, Header header, ByteBuffer data) {
    // A flaw in the ICC spec is that XYZNumber does not specify its color space and is inferred
    // from context. In almost all cases this is clearly defined. However, when stored in an
    // XYZType tag data, the class is not defined by the type so hypothetically it must be inferred
    // from tag.
    //
    // XYZType tags are used for the media white point, luminance, and matrix columns.
    // media white point is normalized CIEXYZ, luminance is CIEXYZ; matrix columns aren't really
    // a color, default to the PCS type corresponding to b-side's XYZ or LAB.
    GenericColorValue.ColorType type =
        header.getBSideColorSpace() == ColorSpace.CIEXYZ ? GenericColorValue.ColorType.PCSXYZ
            : GenericColorValue.ColorType.PCSLAB;
    Tag.Definition<?> def = Tag.fromSignature(tag);
    if (def == Tag.MEDIA_WHITE_POINT) {
      type = GenericColorValue.ColorType.NORMALIZED_CIEXYZ;
    } else if (def == Tag.LUMINANCE) {
      type = GenericColorValue.ColorType.CIEXYZ;
    }

    int count = data.remaining() / 12;
    List<GenericColorValue> colors = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      colors.add(nextXYZNumber(data, type));
    }

    return colors;
  }
}
