package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.DeviceAttributes;
import com.lhkbob.imaje.color.icc.DeviceTechnology;
import com.lhkbob.imaje.color.icc.LocalizedString;
import com.lhkbob.imaje.color.icc.ProfileDescription;
import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextSignature;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt32Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt64Number;

/**
 *
 */
public class ProfileSequenceTypeParser implements TagParser<List<ProfileDescription>> {
  public static final Signature SIGNATURE = Signature.fromName("pseq");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public List<ProfileDescription> parse(Header header, ByteBuffer data) {
    MultiLocalizedUnicodeTypeParser textParser = new MultiLocalizedUnicodeTypeParser();

    int descCount = Math.toIntExact(nextUInt32Number(data));
    List<ProfileDescription> profiles = new ArrayList<>();
    for (int i = 0; i < descCount; i++) {
      Signature manufacturerSig = nextSignature(data);
      Signature modelSig = nextSignature(data);
      DeviceAttributes attrs = new DeviceAttributes(nextUInt64Number(data));
      long techSig = nextUInt32Number(data);
      DeviceTechnology tech = (techSig == 0 ? null
          : DeviceTechnology.fromSignature(Signature.fromBitField(techSig)));

      // FIXME are these packed or must they be adjusted to 4 byte boundaries? and if so should that
      // be the responsbility of MLUTD or this class?
      // FIXME the spec says that this is the entire deviceEtcTag, including tag type.
      // Does that mean it includes a tag signature that specifies the size, or does that just mean
      // it includes the MLUTD signature and 4 bytes of zeros
      LocalizedString manufacturerDesc = textParser.parse(header, data);
      LocalizedString modelDesc = textParser.parse(header, data);

      profiles.add(new ProfileDescription(manufacturerSig, modelSig, attrs, tech, manufacturerDesc,
          modelDesc));
    }

    return profiles;
  }
}
