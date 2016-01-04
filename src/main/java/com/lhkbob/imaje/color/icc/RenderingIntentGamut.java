package com.lhkbob.imaje.color.icc;

/**
 *
 */
public enum RenderingIntentGamut {
  UNDEFINED(Signature.NULL),
  PERCEPTUAL_REFERENCE_MEDIUM_GAMUT(Signature.fromName("prmg"));

  private final Signature signature;

  RenderingIntentGamut(Signature signature) {
    this.signature = signature;
  }

  public Signature getSignature() {
    return signature;
  }

  public static RenderingIntentGamut fromSignature(Signature sig) {
    for (RenderingIntentGamut v : values()) {
      if (v.getSignature().equals(sig)) {
        return v;
      }
    }

    throw new IllegalArgumentException("Unknown signature: " + sig);
  }
}
