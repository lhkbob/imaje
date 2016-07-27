package com.lhkbob.imaje.color.icc;

import com.lhkbob.imaje.util.Arguments;

/**
 *
 */
public final class Measurement {
  private final double flare;
  private final MeasurementGeometry geometry;
  private final StandardIlluminant illuminant;
  private final GenericColorValue measurement;
  private final StandardObserver observer;

  public Measurement(
      StandardObserver observer, MeasurementGeometry geometry, StandardIlluminant illuminant,
      double flare, GenericColorValue measurement) {
    Arguments.equals("measurement.getType()", GenericColorValue.ColorType.NORMALIZED_CIEXYZ, measurement.getType());

    this.observer = observer;
    this.geometry = geometry;
    this.illuminant = illuminant;
    this.flare = flare;
    this.measurement = measurement;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Measurement)) {
      return false;
    }
    Measurement m = (Measurement) o;
    return m.geometry.equals(geometry) && m.illuminant.equals(illuminant) && m.measurement
        .equals(measurement) && m.observer.equals(observer) && Double.compare(m.flare, flare) == 0;
  }

  public double getFlareValue() {
    return flare;
  }

  public MeasurementGeometry getGeometry() {
    return geometry;
  }

  public StandardIlluminant getIlluminant() {
    return illuminant;
  }

  public GenericColorValue getMeasurement() {
    return measurement;
  }

  public StandardObserver getObserver() {
    return observer;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + Double.hashCode(flare);
    result = 31 * result + geometry.hashCode();
    result = 31 * result + illuminant.hashCode();
    result = 31 * result + measurement.hashCode();
    result = 31 * result + observer.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return String.format(
        "Measurement (geometry: %s, illuminant: %s, observer: %s, flare: %.4f, value: %s)",
        geometry, illuminant, observer, flare, measurement);
  }
}
