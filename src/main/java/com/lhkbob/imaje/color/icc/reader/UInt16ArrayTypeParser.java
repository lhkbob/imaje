package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt16Number;

/**
 *
 */
public class UInt16ArrayTypeParser implements TagParser<int[]> {
  public static final Signature SIGNATURE = Signature.fromName("ui16");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public int[] parse(Header header, ByteBuffer data) {
    int count = data.remaining() / 2;
    int[] values = new int[count];
    for (int i = 0; i < count; i++) {
      values[i] = nextUInt16Number(data);
    }

    return values;
  }
}
