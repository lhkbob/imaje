package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt64Number;

/**
 *
 */
public class UInt64ArrayTypeParser implements TagParser<long[]> {
  public static final Signature SIGNATURE = Signature.fromName("ui64");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public long[] parse(Header header, ByteBuffer data) {
    int count = data.remaining() / 8;
    long[] values = new long[count];
    for (int i = 0; i < count; i++) {
      values[i] = nextUInt64Number(data);
    }

    return values;
  }
}
