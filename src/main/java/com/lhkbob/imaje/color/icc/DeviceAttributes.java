package com.lhkbob.imaje.color.icc;

/**
 *
 */
public final class DeviceAttributes {
  public static final long COLOR_MEDIA_MASK = 1 << 3;
  public static final long GLOSSY_MATTE_MASK = 1 << 1;
  public static final long POLARITY_MASK = 1 << 2;
  public static final long REFLECTIVITY_TRANSPARENCY_MASK = 1;
  private final long bitfield;

  public DeviceAttributes(long bitfield) {
    this.bitfield = bitfield;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    return o instanceof DeviceAttributes && ((DeviceAttributes) o).bitfield == bitfield;
  }

  public long getBitField() {
    return bitfield;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(bitfield);
  }

  public boolean isMatte() {
    return isBitSet(GLOSSY_MATTE_MASK);
  }

  public boolean isMediaBlackAndWhite() {
    return isBitSet(COLOR_MEDIA_MASK);
  }

  public boolean isPolarityNegative() {
    return isBitSet(POLARITY_MASK);
  }

  public boolean isTransparent() {
    return isBitSet(REFLECTIVITY_TRANSPARENCY_MASK);
  }

  @Override
  public String toString() {
    return String.format(
        "DeviceAttributes (bits: %s, material: %s + %s, polarity: %s, media: %s)",
        Long.toHexString(bitfield), (isTransparent() ? "transparent" : "reflective"),
        (isMatte() ? "matte" : "glossy"), (isPolarityNegative() ? "negative" : "positive"),
        (isMediaBlackAndWhite() ? "black & white" : "color"));
  }

  private boolean isBitSet(long mask) {
    return (bitfield & mask) != 0;
  }
}
