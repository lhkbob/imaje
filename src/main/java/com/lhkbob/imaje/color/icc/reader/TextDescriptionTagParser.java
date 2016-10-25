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
import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextASCIIString;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt32Number;

/**
 *
 */
public final class TextDescriptionTagParser implements TagParser<LocalizedString> {
  public static final Signature SIGNATURE = Signature.fromName("desc");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public LocalizedString parse(Signature tag, Header header, ByteBuffer data) {
    // First read in the ASCII invariant
    int asciiLength = Math.toIntExact(nextUInt32Number(data));
    String ascii = nextASCIIString(data, asciiLength);

    // Next read in the UNICODE localization, the ICC says the unicode language
    // code is represented as a uint32, presumably this is 4 ascii chars representing
    // the language and country code? They do not specify any standard and
    // "Unicode Language Code" is not a thing.
    byte[] code = new byte[2];
    data.get(code);
    String language = new String(code, StandardCharsets.US_ASCII);
    data.get(code);
    String country = new String(code, StandardCharsets.US_ASCII);
    Locale locale = new Locale(language, country);

    int charCount = Math.toIntExact(nextUInt32Number(data));
    byte[] unicodeChars = new byte[charCount * 2];
    data.get(unicodeChars);
    String unicode = new String(unicodeChars, StandardCharsets.UTF_16BE);

    Map<Locale, String> variants = new HashMap<>();
    variants.put(Locale.US, ascii);
    variants.put(locale, unicode);

    return new LocalizedString(variants, Locale.US);
  }
}
