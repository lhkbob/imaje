package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.Signature;
import com.lhkbob.imaje.color.transform.curves.Curve;
import com.lhkbob.imaje.color.transform.curves.DomainWindow;
import com.lhkbob.imaje.color.transform.curves.LinearFunction;
import com.lhkbob.imaje.color.transform.curves.UniformlySampledCurve;
import com.lhkbob.imaje.color.transform.curves.UnitGammaFunction;

import java.nio.ByteBuffer;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextU8Fixed8Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt16Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt32Number;

/**
 *
 */
public class CurveTypeParser implements TagParser<Curve> {
  public static final Signature SIGNATURE = Signature.fromName("curv");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public Curve parse(Signature tag, Header header, ByteBuffer data) {
    int tableLength = Math.toIntExact(nextUInt32Number(data));
    if (tableLength == 0) {
      // Identity response, but limited to a 0-1 range
      return new DomainWindow(new LinearFunction(1.0, 0.0), 0.0, 1.0);
    } else if (tableLength == 1) {
      // Next two bytes are a u8f8 representing a gamma exponent
      double gamma = nextU8Fixed8Number(data);
      return UnitGammaFunction.newSimpleCurve(gamma);
    } else {
      // It's a table of normalized uint16's
      double[] table = new double[tableLength];
      for (int i = 0; i < tableLength; i++) {
        table[i] = nextUInt16Number(data) / 65535.0;
      }
      return new UniformlySampledCurve(0.0, 1.0, table);
    }
  }
}
