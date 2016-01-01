package com.lhkbob.imaje.color.icc;

/**
 *
 */
public enum PrimaryPlatform {
  APPLE_COMPUTER_INC("APPL"),
  MICROSOFT_CORPORATION("MSFT"),
  SILICON_GRAPHICS_INC("SGI"),
  SUN_MICROSYSTEMS_INC("SUNW");

  private final Signature signature;

  PrimaryPlatform(String signature) {
    this.signature = Signature.fromName(signature);
  }

  public static PrimaryPlatform fromSignature(Signature sig) {
    for (PrimaryPlatform v : values()) {
      if (v.getSignature().equals(sig)) {
        return v;
      }
    }

    throw new IllegalArgumentException("Uknonwn signature: " + sig);
  }

  public Signature getSignature() {
    return signature;
  }
}
