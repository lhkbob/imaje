package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.LocalizedString;
import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 *
 */
public class StringToLocalizedStringAdapter implements TagParser<LocalizedString> {
  private final TagParser<String> parser;

  public StringToLocalizedStringAdapter(TagParser<String> parser) {
    this.parser = parser;
  }

  @Override
  public Signature getSignature() {
    return parser.getSignature();
  }

  @Override
  public LocalizedString parse(Header header, ByteBuffer data) {
    String text = parser.parse(header, data);
    // This assumes that the text parser is reading an ASCII text so the locale should be US
    Map<Locale, String> options = Collections.singletonMap(Locale.US, text);
    return new LocalizedString(options, Locale.US);
  }
}
