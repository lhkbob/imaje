package com.lhkbob.imaje.color.icc;

import com.lhkbob.imaje.color.transform.curves.Curve;
import com.lhkbob.imaje.util.Arguments;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public final class ResponseCurveSet {
  public static class Builder {
    private final int channelCount;
    private final Map<MeasurementUnit, UnitBuilder> unitResponses;

    private Builder(int channelCount) {
      Arguments.isPositive("channelCount", channelCount);

      this.channelCount = channelCount;
      unitResponses = new HashMap<>();
    }

    public ResponseCurveSet build() {
      // FIXME this needs some more validation, what if someone never completed a unit builder?
      Map<MeasurementUnit, ResponseCurves> responseCurves = new HashMap<>();
      for (Map.Entry<MeasurementUnit, UnitBuilder> e : unitResponses.entrySet()) {
        responseCurves.put(e.getKey(), e.getValue().build());
      }
      return new ResponseCurveSet(channelCount, responseCurves);
    }

    public UnitBuilder forUnit(MeasurementUnit units) {
      Arguments.notNull("units", units);

      UnitBuilder b = unitResponses.get(units);
      if (b == null) {
        b = new UnitBuilder(channelCount);
        unitResponses.put(units, b);
      }
      return b;
    }
  }

  public static class UnitBuilder {
    private final GenericColorValue[] measurements;
    private final Curve[] responses;

    private UnitBuilder(int channelCount) {
      measurements = new GenericColorValue[channelCount];
      responses = new Curve[channelCount];
    }

    public UnitBuilder setChannelMeasurement(int channel, GenericColorValue value) {
      Arguments.notNull("value", value);

      if (value.getType() != GenericColorValue.ColorType.PCSXYZ) {
        throw new IllegalArgumentException(
            "Channel measurements must be in PCSXYZ, not: " + value.getType());
      }
      measurements[channel] = value;
      return this;
    }

    public UnitBuilder setResponseCurve(int channel, Curve curve) {
      Arguments.notNull("curve", curve);

      if (curve.getDomainMin() != 0.0 && curve.getDomainMax() != 1.0) {
        throw new IllegalArgumentException("Response curve domain must be [0, 1]");
      }
      responses[channel] = curve;
      return this;
    }

    private ResponseCurves build() {
      return new ResponseCurves(measurements, responses);
    }
  }

  private final int channelCount;
  private final Map<MeasurementUnit, ResponseCurves> responseCurves;

  private ResponseCurveSet(int channelCount, Map<MeasurementUnit, ResponseCurves> responseCurves) {
    this.channelCount = channelCount;
    // The builder creates a new map with each instantiation so we don't need to do a
    // defensive copy.
    this.responseCurves = responseCurves;
  }

  public static Builder newResponseCurveSet(int channelCount) {
    return new Builder(channelCount);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ResponseCurveSet)) {
      return false;
    }
    return ((ResponseCurveSet) o).responseCurves.equals(responseCurves);
  }

  public int getChannelCount() {
    return channelCount;
  }

  public GenericColorValue getChannelMeasurement(MeasurementUnit unit, int channel) {
    ResponseCurves c = responseCurves.get(unit);
    if (c != null) {
      return c.channelMeasurements[channel];
    } else {
      return null;
    }
  }

  public Curve getResponseCurve(MeasurementUnit unit, int channel) {
    ResponseCurves c = responseCurves.get(unit);
    if (c != null) {
      return c.channelResponseCurves[channel];
    } else {
      return null;
    }
  }

  @Override
  public int hashCode() {
    // channelCount is implicit to the array lengths stored in the response curve objects
    return responseCurves.hashCode();
  }

  @Override
  public String toString() {
    return "Response curve set (" + responseCurves + ")";
  }

  private static class ResponseCurves {
    private final GenericColorValue[] channelMeasurements;
    private final Curve[] channelResponseCurves;

    public ResponseCurves(GenericColorValue[] channelMeasurements, Curve[] channelResponseCurves) {
      // Do defensive copies here in case the builder object is reused.
      this.channelMeasurements = Arrays.copyOf(channelMeasurements, channelMeasurements.length);
      this.channelResponseCurves = Arrays
          .copyOf(channelResponseCurves, channelResponseCurves.length);
    }

    @Override
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof ResponseCurves)) {
        return false;
      }
      ResponseCurves c = (ResponseCurves) o;
      return Arrays.equals(c.channelResponseCurves, channelResponseCurves) && Arrays
          .equals(c.channelMeasurements, channelMeasurements);
    }

    @Override
    public int hashCode() {
      return Arrays.hashCode(channelMeasurements) ^ Arrays.hashCode(channelResponseCurves);
    }

    @Override
    public String toString() {
      return String.format("channel measurements: %s, channel curves: %s",
          Arrays.toString(channelMeasurements), Arrays.toString(channelResponseCurves));
    }
  }
}
