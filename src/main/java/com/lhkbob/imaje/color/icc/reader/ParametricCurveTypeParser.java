package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.Signature;
import com.lhkbob.imaje.color.icc.curves.Curve;
import com.lhkbob.imaje.color.icc.curves.UnitGammaCurve;

import java.nio.ByteBuffer;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextS15Fixed16Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt16Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.skip;

/**
 *
 */
public class ParametricCurveTypeParser implements TagParser<Curve> {
  public static final Signature SIGNATURE = Signature.fromName("para");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public Curve parse(Signature tag, Header header, ByteBuffer data) {
    int functionType = nextUInt16Number(data);
    skip(data, 2);

    switch (functionType) {
    case 0: {
      double gamma = nextS15Fixed16Number(data);
      return UnitGammaCurve.newSimpleCurve(gamma);
    }
    case 1: {
      double gamma = nextS15Fixed16Number(data);
      double a = nextS15Fixed16Number(data);
      double b = nextS15Fixed16Number(data);
      return UnitGammaCurve.newCIE122_1996Curve(gamma, a, b);
    }
    case 2: {
      double gamma = nextS15Fixed16Number(data);
      double a = nextS15Fixed16Number(data);
      double b = nextS15Fixed16Number(data);
      double c = nextS15Fixed16Number(data);
      return UnitGammaCurve.newIEC61966_3Curve(gamma, a, b, c);
    }
    case 3: {
      double gamma = nextS15Fixed16Number(data);
      double a = nextS15Fixed16Number(data);
      double b = nextS15Fixed16Number(data);
      double c = nextS15Fixed16Number(data);
      double d = nextS15Fixed16Number(data);
      return UnitGammaCurve.newIEC61966_2_1Curve(gamma, a, b, c, d);
    }
    case 4: {
      double gamma = nextS15Fixed16Number(data);
      double a = nextS15Fixed16Number(data);
      double b = nextS15Fixed16Number(data);
      double c = nextS15Fixed16Number(data);
      double d = nextS15Fixed16Number(data);
      double e = nextS15Fixed16Number(data);
      double f = nextS15Fixed16Number(data);
      return new UnitGammaCurve(gamma, a, b, c, e, f, d);
    }
    default:
      throw new IllegalStateException("Unknown parametric curve type: " + functionType);
    }
  }
}
