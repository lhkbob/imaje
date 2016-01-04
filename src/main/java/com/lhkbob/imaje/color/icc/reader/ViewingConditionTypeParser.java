package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.GenericColorValue;
import com.lhkbob.imaje.color.icc.LocalizedString;
import com.lhkbob.imaje.color.icc.Signature;
import com.lhkbob.imaje.color.icc.StandardIlluminant;
import com.lhkbob.imaje.color.icc.ViewingCondition;

import java.nio.ByteBuffer;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt32Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextXYZNumber;

/**
 *
 */
public class ViewingConditionTypeParser implements TagParser<ViewingCondition> {
  public static final long HEX_ENCODED_SIGNATURE = 0x76686577L;
  public static final Signature SIGNATURE = Signature.fromName("view");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public ViewingCondition parse(Header header, ByteBuffer data) {
    GenericColorValue illuminant = nextXYZNumber(data, GenericColorValue.ColorType.CIEXYZ);
    GenericColorValue surround = nextXYZNumber(data, GenericColorValue.ColorType.CIEXYZ);
    int type = Math.toIntExact(nextUInt32Number(data));
    StandardIlluminant illuminantType = StandardIlluminant.values()[type];
    // No description is provided in this tag, a separate tag must be combined later
    return new ViewingCondition(illuminant, illuminantType, surround, new LocalizedString());
  }
}
