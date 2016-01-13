package com.lhkbob.imaje.color.icc;

/**
 *
 */
public enum ColorimetricIntent {
  PICTURE_REFERRED(Signature.NULL),
  SCENE_COLORIMETRY_ESTIMATES(Signature.fromName("scoe")),
  SCENE_APPEARANCE_ESTIMATES(Signature.fromName("sape")),
  FOCAL_PLANE_COLORIMETRY_ESTIMATES(Signature.fromName("fpce")),
  REFLECTION_HARDCOPY_ORIGINAL_COLORIMETRY(Signature.fromName("rhoc")),
  REFLECTION_PRINT_OUTPUT_COLORIMETRY(Signature.fromName("rpoc"));

  private final Signature signature;

  ColorimetricIntent(Signature signature) {
    this.signature = signature;
  }

  public static ColorimetricIntent fromSignature(Signature sig) {
    for (ColorimetricIntent v : values()) {
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
