package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.Colorant;
import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextU16Fixed16Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt16Number;

/**
 *
 */
public class ChromaticityTypeParser implements TagParser<Colorant> {
  public static final Signature SIGNATURE = Signature.fromName("chrm");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public Colorant parse(Signature tag, Header header, ByteBuffer data) {
    int channels = nextUInt16Number(data);
    int colorant = nextUInt16Number(data);

    if (colorant != 0) {
      // predefined colorant
      if (channels != 3 && channels != 0) {
        throw new IllegalStateException(
            "Unexpected channel count for predefined colorant type: " + channels);
      }
      switch (colorant) {
      case 1:
        return Colorant.ITU_R_BT_709_2;
      case 2:
        return Colorant.SMPTE_RP145;
      case 3:
        return Colorant.EBU_TECH_3213_E;
      case 4:
        return Colorant.P22;
      default:
        throw new IllegalStateException("Uknonwn colorant type value: " + colorant);
      }
    }

    double[] xs = new double[channels];
    double[] ys = new double[channels];
    for (int i = 0; i < channels; i++) {
      xs[i] = nextU16Fixed16Number(data);
      ys[i] = nextU16Fixed16Number(data);
    }

    return new Colorant(xs, ys);
  }
}
