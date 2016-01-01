package com.lhkbob.imaje.color.icc;

/**
 *
 */
public enum ColorSpace {
  CIEXYZ("XYZ", 3),
  CIELAB("Lab", 3),
  CIELUV("Luv", 3),
  YCbCr("YCbr", 3),
  CIEYxy("Yxy", 3),
  RGB("RGB", 3),
  GRAY("GRAY", 1), // FIXME is gray a single channel?
  HSV("HSV", 3),
  HLS("HLS", 3),
  CMYK("CMYK", 4),
  CMY("CMY", 3),
  TWO_COLOR("2CLR", 2),
  THREE_COLOR("3CLR", 3),
  FOUR_COLOR("4CLR", 4),
  FIVE_COLOR("5CLR", 5),
  SIX_COLOR("6CLR", 6),
  SEVEN_COLOR("7CLR", 7),
  EIGHT_COLOR("8CLR", 8),
  NINE_COLOR("9CLR", 9),
  TEN_COLOR("ACLR", 10),
  ELEVEN_COLOR("BCLR", 11),
  TWELVE_COLOR("CCLR", 12),
  THIRTEEN_COLOR("DCLR", 13),
  FOURTEEN_COLOR("ECLR", 14),
  FIFTEEN_COLOR("FCLR", 15);

  private final int channelCount;
  private final Signature signature;

  ColorSpace(String signature, int channelCount) {
    this.signature = Signature.fromName(signature);
    this.channelCount = channelCount;
  }

  public static ColorSpace fromSignature(Signature s) {
    for (ColorSpace v : values()) {
      if (v.getSignature().equals(s)) {
        return v;
      }
    }

    throw new IllegalArgumentException("Unknown signature: " + s);
  }

  public int getChannelCount() {
    return channelCount;
  }

  public Signature getSignature() {
    return signature;
  }
}
