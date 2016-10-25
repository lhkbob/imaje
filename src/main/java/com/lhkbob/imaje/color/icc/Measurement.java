/*
 * BSD 3-Clause License - imaJe
 *
 * Copyright (c) 2016, Michael Ludwig
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
