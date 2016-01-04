package com.lhkbob.imaje.color.icc;

/**
 *
 */
public enum ProfileClass {
  /**
   * A profile describing how to transform device colors to the PCS, where the device
   * is an input device such as a camera or scanner.
   */
  INPUT_DEVICE_PROFILE("scnr"),
  /**
   * A profile describing how a device that emits or displays colors dynamically (e.g.
   * TV or monitor) relates to the PCS.
   */
  DISPLAY_DEVICE_PROFILE("mntr"),
  /**
   * A profile describing how printers and other non-emitting output devices relate to the
   * PCS.
   */
  OUTPUT_DEVICE_PROFILE("prtr"),
  /**
   * An optimized transformation profile between two devices that bypasses or implicitly
   * encodes the intermediate transformation to and from the PCS.
   */
  DEVICE_LINK_PROFILE("link"),
  /**
   * A transformation profile that specifies the relationship between a defined color space
   * and the PCS.
   */
  COLOR_SPACE_PROFILE("spac"),
  /**
   * A transformation within the PCS that can be used to perform image manipulations and other effects.
   */
  ABSTRACT_PROFILE("abst"),
  /**
   * A profile that provides explicit mappings of certain colors in device coordinates to their
   * PCS values.
   */
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

    throw new IllegalArgumentException("Unknown signature: " + sig);
  }

  public Signature getSignature() {
    return signature;
  }
}
