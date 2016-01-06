package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;

/**
 *
 */
public interface TagParser<T> {
  Signature getSignature();

  T parse(Signature tag, Header header, ByteBuffer data);
}
