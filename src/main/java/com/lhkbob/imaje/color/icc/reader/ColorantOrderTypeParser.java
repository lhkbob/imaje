package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt32Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt8Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.require;

/**
 *
 */
public final class ColorantOrderTypeParser implements TagParser<int[]> {
  public static final Signature SIGNATURE = Signature.fromName("clro");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public int[] parse(Signature tag, Header header, ByteBuffer data) {
    int colorantCount = Math.toIntExact(nextUInt32Number(data));
    int[] colorants = new int[colorantCount];

    require(data, colorantCount);
    for (int i = 0; i < colorantCount; i++) {
      colorants[i] = nextUInt8Number(data);
    }

    return colorants;
  }
}
