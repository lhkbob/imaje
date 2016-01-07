package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextU16Fixed16Number;

/**
 *
 */
public final class U16Fixed16ArrayTypeParser implements TagParser<double[]> {
  public static final Signature SIGNATURE = Signature.fromName("uf32");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public double[] parse(Signature tag, Header header, ByteBuffer data) {
    int count = data.remaining() / 4;
    double[] values = new double[count];
    for (int i = 0; i < count; i++) {
      values[i] = nextU16Fixed16Number(data);
    }

    return values;
  }
}
