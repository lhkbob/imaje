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

import com.lhkbob.imaje.color.icc.DeviceAttributes;
import com.lhkbob.imaje.color.icc.DeviceTechnology;
import com.lhkbob.imaje.color.icc.LocalizedString;
import com.lhkbob.imaje.color.icc.ProfileDescription;
import com.lhkbob.imaje.color.icc.ProfileID;
import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextSignature;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt32Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt64Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.skip;

/**
 *
 */
public final class ProfileSequenceDescriptionTypeParser implements TagParser<List<ProfileDescription>> {
  public static final Signature SIGNATURE = Signature.fromName("pseq");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public List<ProfileDescription> parse(Signature tag, Header header, ByteBuffer data) {
    int descCount = Math.toIntExact(nextUInt32Number(data));
    List<ProfileDescription> profiles = new ArrayList<>();
    for (int i = 0; i < descCount; i++) {
      Signature manufacturerSig = nextSignature(data);
      Signature modelSig = nextSignature(data);
      DeviceAttributes attrs = new DeviceAttributes(nextUInt64Number(data));
      long techSig = nextUInt32Number(data);
      DeviceTechnology tech = (techSig == 0 ? null
          : DeviceTechnology.fromSignature(Signature.fromBitField(techSig)));

      LocalizedString manufacturerDesc = readEmbeddedTextTag(tag, header, data);
      if (manufacturerDesc == null) {
        // unsupported text type in tag
        return null;
      }
      LocalizedString modelDesc = readEmbeddedTextTag(tag, header, data);
      if (modelDesc == null) {
        // unsupported text type in tag
        return null;
      }

      // The profile sequence description tag data doesn't actually contain an id
      // or text description; it must be combined with the sequenceIdentifier tag type
      profiles.add(new ProfileDescription(new ProfileID(new byte[16]), new LocalizedString(),
          manufacturerSig, modelSig, attrs, tech, manufacturerDesc, modelDesc));
    }

    return profiles;
  }

  static LocalizedString readEmbeddedTextTag(Signature tag, Header header, ByteBuffer data) {
    Signature type = nextSignature(data);
    skip(data, 4); // reserved

    if (type.equals(TextDescriptionTagParser.SIGNATURE)) {
      return new TextDescriptionTagParser().parse(tag, header, data);
    } else if (type.equals(MultiLocalizedUnicodeTypeParser.SIGNATURE)) {
      return new MultiLocalizedUnicodeTypeParser().parse(tag, header, data);
    } else {
      return null;
    }
  }
}
