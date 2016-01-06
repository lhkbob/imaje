package com.lhkbob.imaje.color.icc.reader;


import com.lhkbob.imaje.color.icc.GenericColorValue;
import com.lhkbob.imaje.color.icc.Measurement;
import com.lhkbob.imaje.color.icc.MeasurementGeometry;
import com.lhkbob.imaje.color.icc.Signature;
import com.lhkbob.imaje.color.icc.StandardIlluminant;
import com.lhkbob.imaje.color.icc.StandardObserver;

import java.nio.ByteBuffer;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextU16Fixed16Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt32Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextXYZNumber;

/**
 *
 */
public class MeasurementTypeParser implements TagParser<Measurement> {
  public static final Signature SIGNATURE = Signature.fromName("meas");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public Measurement parse(Signature tag, Header header, ByteBuffer data) {
    int observer = Math.toIntExact(nextUInt32Number(data));
    GenericColorValue measure = nextXYZNumber(data, GenericColorValue.ColorType.NORMALIZED_CIEXYZ);
    int geometry = Math.toIntExact(nextUInt32Number(data));
    double flare = nextU16Fixed16Number(data);
    int illuminant = Math.toIntExact(nextUInt32Number(data));

    return new Measurement(StandardObserver.values()[observer],
        MeasurementGeometry.values()[geometry], StandardIlluminant.values()[illuminant], flare,
        measure);
  }
}
