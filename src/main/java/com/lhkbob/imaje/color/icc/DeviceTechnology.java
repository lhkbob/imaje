package com.lhkbob.imaje.color.icc;

/**
 *
 */
public enum DeviceTechnology {
  FILM_SCANNER("fscn"),
  DIGITAL_CAMERA("dcam"),
  REFLECTIVE_SCANNER("rscn"),
  INK_JET_PRINTER("ijet"),
  THERMAL_WAX_PRINTER("twax"),
  ELECTROPHOTOGRAPHIC_PRINTER("epho"),
  ELECTROSTATIC_PRINTER("esta"),
  DYE_SUBLIMATION_PRINTER("dsub"),
  PHOTOGRAPHIC_PAPER_PRINTER("rpho"),
  FILM_WRITER("fprn"),
  VIDEO_MONITOR("vidm"),
  VIDEO_CAMERA("vidc"),
  PROJECTION_TELEVISION("pjtv"),
  CATHODE_RAY_TUBE_DISPLAY("CRT"),
  PASSIVE_MATRIX_DISPLAY("PMD"),
  ACTIVE_MATRIX_DISPLAY("AMD"),
  PHOTO_CD("KPCD"),
  PHOTOGRAPHIC_IMAGE_SETTER("imgs"),
  GRAVURE("grav"),
  OFFSET_LITHOGRAPHY("offs"),
  SILKSCREEN("silk"),
  MOTION_PICTURE_FILM_SCANNER("mpfs"),
  MOTION_PICTURE_FILM_RECORDER("mpfr"),
  DIGITAL_MOTION_PICTURE_CAMERA("dmpc"),
  DIGITAL_CINEMA_PROJECTOR("dcpj");

  private final Signature signature;

  DeviceTechnology(String signature) {
    this.signature = Signature.fromName(signature);
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
