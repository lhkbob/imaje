package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt32Number;

/**
 *
 */
public class UInt32ArrayTypeParser implements TagParser<long[]> {
  public static final Signature SIGNATURE = Signature.fromName("ui32");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public long[] parse(Signature tag, Header header, ByteBuffer data) {
    int count = data.remaining() / 4;
    long[] values = new long[count];
    for (int i = 0; i < count; i++) {
      values[i] = nextUInt32Number(data);
    }

    return values;
  }
}
