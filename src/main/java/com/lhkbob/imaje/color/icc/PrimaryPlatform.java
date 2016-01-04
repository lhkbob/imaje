package com.lhkbob.imaje.color.icc;

/**
 *
 */
public enum PrimaryPlatform {
  UNSPECIFIED(Signature.NULL),
  APPLE_COMPUTER_INC(Signature.fromName("APPL")),
  MICROSOFT_CORPORATION(Signature.fromName("MSFT")),
  SILICON_GRAPHICS_INC(Signature.fromName("SGI")),
  SUN_MICROSYSTEMS_INC(Signature.fromName("SUNW"));

  private final Signature signature;

  PrimaryPlatform(Signature signature) {
    this.signature = signature;
  }

  public static PrimaryPlatform fromSignature(Signature sig) {
    for (PrimaryPlatform v : values()) {
      if (v.getSignature().equals(sig)) {
        return v;
      }
    }

    throw new IllegalArgumentException("Unknown signature: " + sig);
  }

  public Signature getSignature() {
    return signature;
  }
}
