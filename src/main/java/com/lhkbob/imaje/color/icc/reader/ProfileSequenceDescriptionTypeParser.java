package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.DeviceAttributes;
import com.lhkbob.imaje.color.icc.DeviceTechnology;
import com.lhkbob.imaje.color.icc.LocalizedString;
import com.lhkbob.imaje.color.icc.ProfileDescription;
import com.lhkbob.imaje.color.icc.ProfileID;
import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextSignature;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt32Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt64Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.skip;

/**
 *
 */
public final class ProfileSequenceDescriptionTypeParser implements TagParser<List<ProfileDescription>> {
  public static final Signature SIGNATURE = Signature.fromName("pseq");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public List<ProfileDescription> parse(Signature tag, Header header, ByteBuffer data) {
    int descCount = Math.toIntExact(nextUInt32Number(data));
    List<ProfileDescription> profiles = new ArrayList<>();
    for (int i = 0; i < descCount; i++) {
      Signature manufacturerSig = nextSignature(data);
      Signature modelSig = nextSignature(data);
      DeviceAttributes attrs = new DeviceAttributes(nextUInt64Number(data));
      long techSig = nextUInt32Number(data);
      DeviceTechnology tech = (techSig == 0 ? null
          : DeviceTechnology.fromSignature(Signature.fromBitField(techSig)));

      LocalizedString manufacturerDesc = readEmbeddedTextTag(tag, header, data);
      if (manufacturerDesc == null) {
        // unsupported text type in tag
        return null;
      }
      LocalizedString modelDesc = readEmbeddedTextTag(tag, header, data);
      if (modelDesc == null) {
        // unsupported text type in tag
        return null;
      }

      // The profile sequence description tag data doesn't actually contain an id
      // or text description; it must be combined with the sequenceIdentifier tag type
      profiles.add(new ProfileDescription(new ProfileID(new byte[16]), new LocalizedString(),
          manufacturerSig, modelSig, attrs, tech, manufacturerDesc, modelDesc));
    }

    return profiles;
  }

  static LocalizedString readEmbeddedTextTag(Signature tag, Header header, ByteBuffer data) {
    Signature type = nextSignature(data);
    skip(data, 4); // reserved

    if (type.equals(TextDescriptionTagParser.SIGNATURE)) {
      return new TextDescriptionTagParser().parse(tag, header, data);
    } else if (type.equals(MultiLocalizedUnicodeTypeParser.SIGNATURE)) {
      return new MultiLocalizedUnicodeTypeParser().parse(tag, header, data);
    } else {
      return null;
    }
  }
}
