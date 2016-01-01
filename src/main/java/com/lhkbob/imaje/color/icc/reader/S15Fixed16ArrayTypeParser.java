package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextS15Fixed16Number;

/**
 *
 */
public class S15Fixed16ArrayTypeParser implements TagParser<double[]> {
  public static final Signature SIGNATURE = Signature.fromName("sf32");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public double[] parse(Header header, ByteBuffer data) {
    int count = data.remaining() / 4;
    double[] values = new double[count];
    for (int i = 0; i < count; i++) {
      values[i] = nextS15Fixed16Number(data);
    }

    return values;
  }
}
