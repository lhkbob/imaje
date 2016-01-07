package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextSignature;

/**
 *
 */
public final class SignatureTypeParser implements TagParser<Signature> {
  public static final Signature SIGNATURE = Signature.fromName("sig");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public Signature parse(Signature tag, Header header, ByteBuffer data) {
    return nextSignature(data);
  }
}
