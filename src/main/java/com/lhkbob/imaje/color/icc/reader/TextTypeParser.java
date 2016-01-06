package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextASCIIString;

/**
 *
 */
public class TextTypeParser implements TagParser<String> {
  public static final Signature SIGNATURE = Signature.fromName("text");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public String parse(Signature tag, Header header, ByteBuffer data) {
    // Length of the string is encoded in what's remaining of the tag
    // this assumes that the data's limit has been appropriately set.
    return nextASCIIString(data, data.remaining());
  }
}
