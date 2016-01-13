package com.lhkbob.imaje.color.icc;

import com.lhkbob.imaje.color.transform.general.NormalizeChannels;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public enum ColorSpace {
  CIEXYZ("XYZ", 3, 0.0, 1.0 + 32767.0 / 32768.0, "X tristimulus", "Y tristimulus", "Z tristimulus"),
  CIELAB("Lab", 3, new double[] { 0.0, -128.0, -128.0 }, new double[] { 100.0, 127.0, 127.0 }, "L*",
      "a*", "b*"),
  CIELUV("Luv", 3, new double[] { 0.0, -128.0, -128.0 }, new double[] { 100.0, 127.0, 127.0 }, "L*",
      "u*", "v*"),
  YCbCr("YCbr", 3, new double[] { 0.0, -0.5, -0.5 }, new double[] { 1.0, 0.5, 0.5 },
      "Y (luminance)", "Cb", "Cr"),
  CIEYxy("Yxy", 3, 0.0, 1.0, "Y (luminance)", "x chromaticity", "y chromaticity"),
  RGB("RGB", 3, 0.0, 1.0, "Red", "Green", "Blue "),
  GRAY("GRAY", 1, 0.0, 1.0, "Luminance"),
  HSV("HSV", 3, new double[] { 0.0, 0.0, 0.0 }, new double[] { 360.0, 1.0, 1.0 }, "Hue",
      "Saturation", "Value"),
  HLS("HLS", 3, new double[] { 0.0, 0.0, 0.0 }, new double[] { 360.0, 1.0, 1.0 }, "Hue",
      "Lightness", "Saturation"),
  CMYK("CMYK", 4, 0.0, 1.0, "Cyan", "Magenta", "Yellow", "Black"),
  CMY("CMY", 3, 0.0, 1.0, "Cyan", "Magenta", "Yellow"),
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
  private final List<String> channelNames;
  private final NormalizeChannels normalizingFunction;
  private final Signature signature;

  ColorSpace(String signature, int channelCount, double min, double max, String... channelNames) {
    if (channelNames.length > 0 && channelNames.length != channelCount) {
      throw new RuntimeException("CRITICAL: incorrect number of channel names");
    }

    this.signature = Signature.fromName(signature);
    this.channelCount = channelCount;
    this.channelNames = Collections.unmodifiableList(Arrays.asList(channelNames));

    double[] mins = new double[channelCount];
    double[] maxs = new double[channelCount];
    Arrays.fill(mins, min);
    Arrays.fill(maxs, max);
    normalizingFunction = new NormalizeChannels(mins, maxs);
  }

  ColorSpace(
      String signature, int channelCount, double[] mins, double[] maxs, String... channelNames) {
    if (mins.length != channelCount || maxs.length != channelCount) {
      throw new RuntimeException("CRITICAL: bad min/max array lengths");
    }
    if (channelNames.length > 0 && channelNames.length != channelCount) {
      throw new RuntimeException("CRITICAL: incorrect number of channel names");
    }

    this.channelNames = Collections.unmodifiableList(Arrays.asList(channelNames));

    this.signature = Signature.fromName(signature);
    this.channelCount = channelCount;
    normalizingFunction = new NormalizeChannels(mins, maxs);
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

  public String getChannelName(int channel) {
    if (channelNames.isEmpty()) {
      return "";
    } else {
      return channelNames.get(channel);
    }
  }

  public List<String> getChannelNames() {
    return channelNames;
  }

  public NormalizeChannels getNormalizingFunction() {
    return normalizingFunction;
  }

  public Signature getSignature() {
    return signature;
  }

  public boolean hasChannelNames() {
    return !channelNames.isEmpty();
  }
}
