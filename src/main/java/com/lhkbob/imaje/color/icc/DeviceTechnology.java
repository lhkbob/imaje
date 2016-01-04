package com.lhkbob.imaje.color.icc;

/**
 *
 */
public enum DeviceTechnology {
  UNSPECIFIED(Signature.NULL),
  FILM_SCANNER(Signature.fromName("fscn")),
  DIGITAL_CAMERA(Signature.fromName("dcam")),
  REFLECTIVE_SCANNER(Signature.fromName("rscn")),
  INK_JET_PRINTER(Signature.fromName("ijet")),
  THERMAL_WAX_PRINTER(Signature.fromName("twax")),
  ELECTROPHOTOGRAPHIC_PRINTER(Signature.fromName("epho")),
  ELECTROSTATIC_PRINTER(Signature.fromName("esta")),
  DYE_SUBLIMATION_PRINTER(Signature.fromName("dsub")),
  PHOTOGRAPHIC_PAPER_PRINTER(Signature.fromName("rpho")),
  FILM_WRITER(Signature.fromName("fprn")),
  VIDEO_MONITOR(Signature.fromName("vidm")),
  VIDEO_CAMERA(Signature.fromName("vidc")),
  PROJECTION_TELEVISION(Signature.fromName("pjtv")),
  CATHODE_RAY_TUBE_DISPLAY(Signature.fromName("CRT")),
  PASSIVE_MATRIX_DISPLAY(Signature.fromName("PMD")),
  ACTIVE_MATRIX_DISPLAY(Signature.fromName("AMD")),
  PHOTO_CD(Signature.fromName("KPCD")),
  PHOTOGRAPHIC_IMAGE_SETTER(Signature.fromName("imgs")),
  GRAVURE(Signature.fromName("grav")),
  OFFSET_LITHOGRAPHY(Signature.fromName("offs")),
  SILKSCREEN(Signature.fromName("silk")),
  MOTION_PICTURE_FILM_SCANNER(Signature.fromName("mpfs")),
  MOTION_PICTURE_FILM_RECORDER(Signature.fromName("mpfr")),
  DIGITAL_MOTION_PICTURE_CAMERA(Signature.fromName("dmpc")),
  DIGITAL_CINEMA_PROJECTOR(Signature.fromName("dcpj"));

  private final Signature signature;

  DeviceTechnology(Signature signature) {
    this.signature = signature;
  }

  public static DeviceTechnology fromSignature(Signature sig) {
    for (DeviceTechnology v : values()) {
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
