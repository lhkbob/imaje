package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;

/**
 *
 */
public interface TagParser<T> {
  Signature getSignature();

  // FIXME provide current tag as context as well
  T parse(Header header, ByteBuffer data);
}
