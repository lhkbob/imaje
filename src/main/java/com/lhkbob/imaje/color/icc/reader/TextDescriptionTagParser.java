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
public class TextDescriptionTagParser implements TagParser<LocalizedString> {
  public static final Signature SIGNATURE = Signature.fromName("desc");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public LocalizedString parse(Header header, ByteBuffer data) {
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
