package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.GenericColorValue;
import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextXYZNumber;

/**
 *
 */
public class XYZTypeParser implements TagParser<List<GenericColorValue>> {
  public static final Signature SIGNATURE = Signature.fromName("XYZ");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public List<GenericColorValue> parse(Signature tag, Header header, ByteBuffer data) {
    int count = data.remaining() / 12;
    List<GenericColorValue> colors = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      // A flaw in the ICC spec is that XYZNumber does not specify its color space and is inferred
      // from context. In almost all cases this is clearly defined. However, when stored in an
      // XYZType tag data, the class is not defined by the type so hypothetically it must be inferred
      // from tag. XYZType tags are only used as columns for color matrix transformations so they
      // don't even represent color in that case.
      //
      // Due to this we select PCSXYZ as it's a reasonable end point of the transformation matrix.
      colors.add(nextXYZNumber(data, GenericColorValue.ColorType.PCSXYZ));
    }

    return colors;
  }
}
