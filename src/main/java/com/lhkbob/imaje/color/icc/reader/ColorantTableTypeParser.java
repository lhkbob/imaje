package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.ColorSpace;
import com.lhkbob.imaje.color.icc.GenericColorValue;
import com.lhkbob.imaje.color.icc.NamedColor;
import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextASCIIString;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextLABNumber16;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt32Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextXYZNumber16;


/**
 *
 */
public class ColorantTableTypeParser implements TagParser<List<NamedColor>> {
  public static final Signature SIGNATURE = Signature.fromName("clrt");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public List<NamedColor> parse(Signature tag, Header header, ByteBuffer data) {
    boolean pcsIsXYZ = header.getBSideColorSpace() == ColorSpace.CIEXYZ;
    int colorantCount = Math.toIntExact(nextUInt32Number(data));

    List<NamedColor> colorants = new ArrayList<>(colorantCount);
    for (int i = 0; i < colorantCount; i++) {
      String name = nextASCIIString(data, 32);
      GenericColorValue pcs = (pcsIsXYZ ? nextXYZNumber16(data) : nextLABNumber16(data));

      double[] device = new double[colorantCount];
      device[i] = 1.0;
      colorants.add(new NamedColor(name, pcs, GenericColorValue.genericColor(device)));
    }
    return colorants;
  }
}
