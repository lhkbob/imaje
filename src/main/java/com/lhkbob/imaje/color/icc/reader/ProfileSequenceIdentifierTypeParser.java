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

import com.lhkbob.imaje.color.icc.LocalizedString;
import com.lhkbob.imaje.color.icc.ProfileID;
import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.PositionNumber;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextPositionNumber;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt32Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.require;
import static com.lhkbob.imaje.color.icc.reader.ProfileSequenceDescriptionTypeParser.readEmbeddedTextTag;

/**
 *
 */
public final class ProfileSequenceIdentifierTypeParser implements TagParser<LinkedHashMap<ProfileID, LocalizedString>> {
  public static final Signature SIGNATURE = Signature.fromName("psid");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public LinkedHashMap<ProfileID, LocalizedString> parse(
      Signature tag, Header header, ByteBuffer data) {
    int dataStart = data.position() - 8;

    int count = Math.toIntExact(nextUInt32Number(data));
    PositionNumber[] table = new PositionNumber[count];
    for (int i = 0; i < count; i++) {
      table[i] = nextPositionNumber(data);
    }

    byte[] id = new byte[16];
    int dataEnd = data.position();

    // FIXME this type won't work if the profile sequence contains multiple profiles with the same id.
    LinkedHashMap<ProfileID, LocalizedString> seq = new LinkedHashMap<>();
    for (int i = 0; i < count; i++) {
      PositionNumber p = table[i];
      // Set window into data based on the position number, relative to the start of the tag
      p.configureBuffer(data, dataStart);
      // Update maximum end of the data
      dataEnd = Math.max(dataEnd, data.limit());

      require(data, 16);
      for (int j = 0; j < id.length; j++) {
        id[j] = data.get();
      }

      ProfileID key = new ProfileID(id);
      LocalizedString value = readEmbeddedTextTag(tag, header, data);
      seq.put(key, value);
    }

    data.position(dataEnd);
    return seq;
  }
}
