package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.ColorSpace;
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
    // A flaw in the ICC spec is that XYZNumber does not specify its color space and is inferred
    // from context. In almost all cases this is clearly defined. However, when stored in an
    // XYZType tag data, the class is not defined by the type so hypothetically it must be inferred
    // from tag.
    //
    // XYZType tags are used for the media white point, luminance, and matrix columns.
    // media white point is normalized CIEXYZ, luminance is CIEXYZ; matrix columns aren't really
    // a color, default to the PCS type corresponding to b-side's XYZ or LAB.
    GenericColorValue.ColorType type =
        header.getBSideColorSpace() == ColorSpace.CIEXYZ ? GenericColorValue.ColorType.PCSXYZ
            : GenericColorValue.ColorType.PCSLAB;
    Tag.Definition<?> def = Tag.fromSignature(tag);
    if (def == Tag.MEDIA_WHITE_POINT) {
      type = GenericColorValue.ColorType.NORMALIZED_CIEXYZ;
    } else if (def == Tag.LUMINANCE) {
      type = GenericColorValue.ColorType.CIEXYZ;
    }

    int count = data.remaining() / 12;
    List<GenericColorValue> colors = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      colors.add(nextXYZNumber(data, type));
    }

    return colors;
  }
}
