package com.lhkbob.imaje.color.icc;

import com.lhkbob.imaje.color.icc.transforms.NormalizeChannelTransform;

import java.util.Arrays;

/**
 *
 */
public enum ColorSpace {
  CIEXYZ("XYZ", 3, 0.0, 1.0 + 32767.0 / 32768.0),
  CIELAB("Lab", 3, new double[] { 0.0, -128.0, -128.0 }, new double[] { 100.0, 127.0, 127.0 }),
  CIELUV("Luv", 3, new double[] { 0.0, -128.0, -128.0 }, new double[] { 100.0, 127.0, 127.0 }),
  YCbCr("YCbr", 3, new double[] { 0.0, -0.5, -0.5 }, new double[] { 1.0, 0.5, 0.5 }),
  CIEYxy("Yxy", 3, 0.0, 1.0),
  RGB("RGB", 3, 0.0, 1.0),
  GRAY("GRAY", 1, 0.0, 1.0),
  HSV("HSV", 3, new double[] { 0.0, 0.0, 0.0 }, new double[] { 360.0, 1.0, 1.0 }),
  HLS("HLS", 3, new double[] { 0.0, 0.0, 0.0 }, new double[] { 360.0, 1.0, 1.0 }),
  CMYK("CMYK", 4, 0.0, 1.0),
  CMY("CMY", 3, 0.0, 1.0),
  TWO_COLOR("2CLR", 2, 0.0, 1.0),
  THREE_COLOR("3CLR", 3, 0.0, 1.0),
  FOUR_COLOR("4CLR", 4, 0.0, 1.0),
  FIVE_COLOR("5CLR", 5, 0.0, 1.0),
  SIX_COLOR("6CLR", 6, 0.0, 1.0),
  SEVEN_COLOR("7CLR", 7, 0.0, 1.0),
  EIGHT_COLOR("8CLR", 8, 0.0, 1.0),
  NINE_COLOR("9CLR", 9, 0.0, 1.0),
  TEN_COLOR("ACLR", 10, 0.0, 1.0),
  ELEVEN_COLOR("BCLR", 11, 0.0, 1.0),
  TWELVE_COLOR("CCLR", 12, 0.0, 1.0),
  THIRTEEN_COLOR("DCLR", 13, 0.0, 1.0),
  FOURTEEN_COLOR("ECLR", 14, 0.0, 1.0),
  FIFTEEN_COLOR("FCLR", 15, 0.0, 1.0);

  private final int channelCount;
  private final Signature signature;
  private final NormalizeChannelTransform normalizingFunction;

  ColorSpace(String signature, int channelCount, double min, double max) {
    this.signature = Signature.fromName(signature);
    this.channelCount = channelCount;
    double[] mins = new double[channelCount];
    double[] maxs = new double[channelCount];
    Arrays.fill(mins, min);
    Arrays.fill(maxs, max);
    normalizingFunction = new NormalizeChannelTransform(mins, maxs);
  }

  ColorSpace(String signature, int channelCount, double[] mins, double[] maxs) {
    if (mins.length != channelCount || maxs.length != channelCount) {
      throw new RuntimeException("CRITICAL: bad min/max array lengths");
    }

    this.signature = Signature.fromName(signature);
    this.channelCount = channelCount;
    normalizingFunction = new NormalizeChannelTransform(mins, maxs);
  }

  public static ColorSpace fromSignature(Signature s) {
    for (ColorSpace v : values()) {
      if (v.getSignature().equals(s)) {
        return v;
      }
    }

    throw new IllegalArgumentException("Unknown signature: " + s);
  }

  public NormalizeChannelTransform getNormalizingFunction() {
    return normalizingFunction;
  }

  public int getChannelCount() {
    return channelCount;
  }

  public Signature getSignature() {
    return signature;
  }
}
