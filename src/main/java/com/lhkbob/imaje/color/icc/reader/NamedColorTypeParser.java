package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.ColorSpace;
import com.lhkbob.imaje.color.icc.GenericColorValue;
import com.lhkbob.imaje.color.icc.NamedColor;
import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextASCIIString;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextLABNumberLegacy16;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt16Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt32Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextXYZNumber16;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.require;

/**
 *
 */
public final class NamedColorTypeParser implements TagParser<List<NamedColor>> {
  public static final Signature SIGNATURE = Signature.fromName("ncl2");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public List<NamedColor> parse(Signature tag, Header header, ByteBuffer data) {
    long vendorFlags = nextUInt32Number(data); // ignored for now
    int colorCount = Math.toIntExact(nextUInt32Number(data));
    int deviceCoords = Math.toIntExact(nextUInt32Number(data));
    String namePrefix = nextASCIIString(data, 32);
    String nameSuffix = nextASCIIString(data, 32);

    require(data, colorCount * (2 * deviceCoords
        + 38)); // 2 bytes per device coords, 6 bytes for PCS, 32 bytes for name
    List<NamedColor> colors = new ArrayList<>();
    for (int i = 0; i < colorCount; i++) {
      String rootName = nextASCIIString(data, 32);
      String finalName = namePrefix + rootName + nameSuffix;

      GenericColorValue pcs = (header.getBSideColorSpace() == ColorSpace.CIEXYZ ? nextXYZNumber16(
          data) : nextLABNumberLegacy16(data));
      double[] device = new double[deviceCoords];
      for (int j = 0; j < deviceCoords; j++) {
        device[j] = nextUInt16Number(data);
      }

      colors.add(new NamedColor(finalName, pcs, GenericColorValue.genericColor(device)));
    }

    return colors;
  }
}
