package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.LocalizedString;
import com.lhkbob.imaje.color.icc.ProfileID;
import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.PositionNumber;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextPositionNumber;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt32Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.require;
import static com.lhkbob.imaje.color.icc.reader.ProfileSequenceDescriptionTypeParser.readEmbeddedTextTag;

/**
 *
 */
public class ProfileSequenceIdentifierTypeParser implements TagParser<LinkedHashMap<ProfileID, LocalizedString>> {
  public static final Signature SIGNATURE = Signature.fromName("psid");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public LinkedHashMap<ProfileID, LocalizedString> parse(
      Signature tag, Header header, ByteBuffer data) {
    int dataStart = data.position() - 8;

    int count = Math.toIntExact(nextUInt32Number(data));
    PositionNumber[] table = new PositionNumber[count];
    for (int i = 0; i < count; i++) {
      table[i] = nextPositionNumber(data);
    }

    byte[] id = new byte[16];
    int dataEnd = data.position();

    // FIXME this type won't work if the profile sequence contains multiple profiles with the same id.
    LinkedHashMap<ProfileID, LocalizedString> seq = new LinkedHashMap<>();
    for (int i = 0; i < count; i++) {
      PositionNumber p = table[i];
      // Set window into data based on the position number, relative to the start of the tag
      p.configureBuffer(data, dataStart);
      // Update maximum end of the data
      dataEnd = Math.max(dataEnd, data.limit());

      require(data, 16);
      for (int j = 0; j < id.length; j++) {
        id[j] = data.get();
      }

      ProfileID key = new ProfileID(id);
      LocalizedString value = readEmbeddedTextTag(tag, header, data);
      seq.put(key, value);
    }

    data.position(dataEnd);
    return seq;
  }
}
