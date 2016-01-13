package com.lhkbob.imaje.color.icc;

/**
 *
 */
public final class Signature {
  public static final Signature NULL = Signature.fromBitField(0L);

  private final String text;
  private final long uint32;

  private Signature(String text, long uint32) {
    this.text = text;
    this.uint32 = uint32;
  }

  public static Signature fromBitField(long uint32) {
    StringBuilder sb = new StringBuilder(4);
    byte b0 = (byte) (uint32 >> 24);
    byte b1 = (byte) (uint32 >> 16);
    byte b2 = (byte) (uint32 >> 8);
    byte b3 = (byte) (uint32);
    sb.append((char) b0).append((char) b1).append((char) b2).append((char) b3);
    return new Signature(sb.toString().trim(), (0xffffffffL & uint32));
  }

  public static Signature fromName(String name) {
    if (name.length() > SIGNATURE_BYTE_LENGTH) {
      throw new IllegalArgumentException("Signature names can be up to 4 characters, not: " + name);
    }

    long bits = 0;
    for (int i = 0; i < SIGNATURE_BYTE_LENGTH; i++) {
      char c = (i < name.length() ? name.charAt(i) : PADDING);
      int shift = (SIGNATURE_BYTE_LENGTH - i - 1) * 8;
      bits |= ((0xff & c) << shift);
    }

    return new Signature(name, bits);
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Signature && ((Signature) o).uint32 == uint32;
  }

  public long getBitField() {
    return uint32;
  }

  public String getName() {
    return text;
  }

  @Override
  public int hashCode() {
    return text.hashCode();
  }

  @Override
  public String toString() {
    return String.format("%s (%s)", text, Long.toHexString(uint32));
  }

  private static final char PADDING = ' ';
  private static final int SIGNATURE_BYTE_LENGTH = 4;
}
