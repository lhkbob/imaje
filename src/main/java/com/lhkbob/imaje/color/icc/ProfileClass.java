package com.lhkbob.imaje.color.icc;

/**
 *
 */
public enum ProfileClass {
  INPUT_DEVICE_PROFILE("scnr"),
  DISPLAY_DEVICE_PROFILE("mntr"),
  OUTPUT_DEVICE_PROFILE("prtr"),
  DEVICE_LINK_PROFILE("link"),
  COLOR_SPACE_PROFILE("spac"),
  ABSTRACT_PROFILE("abst"),
  NAMED_COLOR_PROFILE("nmcl");

  private final Signature signature;

  ProfileClass(String signature) {
    this.signature = Signature.fromName(signature);
  }

  public static ProfileClass fromSignature(Signature sig) {
    for (ProfileClass v : values()) {
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
