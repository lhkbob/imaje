package com.lhkbob.imaje.color.icc;

/**
 *
 */
public enum MeasurementUnit {
  // ISO 5-3 densitometer response. This is the accepted standard for
  // reflection densitometers for measuring photographic color prints.
  STATUS_A("StaA"),
  // ISO 5-3 densitometer response which is the accepted standard in
  // Europe for color reflection densitometers
  STATUS_E("StaE"),
  // ISO 5-3 densitometer response commonly referred to as narrow band or
  // interference-type response
  STATUS_I("StaI"),
  // ISO 5-3 wide band color reflection densitometer response which is
  // the accepted standard in the United States for color reflection
  // densitometers
  STATUS_T("StaT"),
  // ISO 5-3 densitometer response for measuring color negatives
  STATUS_M("StaM"),
  // DIN 16536-2 densitometer response, with no polarizing filter.
  DIN_E_UNPOLARIZED("DN"),
  // DIN 16536-2 densitometer response, with polarizing filter.
  DIN_E_POLARIZED("DN P"),
  // DIN 16536-2 narrow band densitometer response, with no
  // polarizing filter.
  DIN_I_UNPOLARIZED("DNN"),
  // DIN 16536-2 narrow band densitometer response, with polarizing filter.
  DIN_I_POLARIZED("DNNP");

  private final Signature signature;

  MeasurementUnit(String name) {
    signature = Signature.fromName(name);
  }

  public static MeasurementUnit fromSignature(Signature s) {
    for (MeasurementUnit v : values()) {
      if (v.getSignature().equals(s)) {
        return v;
      }
    }

    throw new IllegalArgumentException("Unknown signature: " + s);
  }

  public Signature getSignature() {
    return signature;
  }
}
