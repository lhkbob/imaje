package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;
import java.time.ZonedDateTime;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextDateTimeNumber;

/**
 *
 */
public final class DateTimeTypeParser implements TagParser<ZonedDateTime> {
  public static final Signature SIGNATURE = Signature.fromName("dtim");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public ZonedDateTime parse(Signature tag, Header header, ByteBuffer data) {
    return nextDateTimeNumber(data);
  }
}
