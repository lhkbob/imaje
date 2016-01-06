package com.lhkbob.imaje.color.icc.transforms;

import com.lhkbob.imaje.color.icc.curves.Curve;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CurveTransform implements ColorTransform {
  private final List<Curve> curves;

  public CurveTransform(List<? extends Curve> curves) {
    this.curves = new ArrayList<>(curves);
  }

  @Override
  public int getInputChannels() {
    return curves.size();
  }

  @Override
  public int getOutputChannels() {
    return curves.size();
  }

  @Override
  public CurveTransform inverted() {
    List<Curve> inverted = new ArrayList<>();
    for (Curve c: curves) {
      Curve invC = c.inverted();
      if (invC == null)
        return null;
      else
        inverted.add(invC);
    }
    return new CurveTransform(inverted);
  }

  @Override
  public void transform(double[] input, double[] output) {
    if (input.length != getInputChannels()) {
      throw new IllegalArgumentException(
          "Input vector must have " + getInputChannels() + " channels, but has " + input.length);
    }
    if (output.length != getOutputChannels()) {
      throw new IllegalArgumentException(
          "Output vector must have " + getOutputChannels() + " channels, but has " + output.length);
    }

    for (int i = 0; i < input.length; i++) {
      Curve c = curves.get(i);
      double inDomain = Math.max(c.getDomainMin(), Math.min(input[i], c.getDomainMax()));
      output[i] = c.evaluate(inDomain);
    }
  }

  @Override
  public int hashCode() {
    return curves.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;
    if (!(o instanceof CurveTransform))
      return false;
    return ((CurveTransform) o).curves.equals(curves);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Curve Transform (dim: ").append(curves.size()).append("):");
    for (int i = 0; i < curves.size(); i++) {
      sb.append("\n  channel ").append(i + 1).append(": ").append(curves.get(i).toString());
    }
    return sb.toString();
  }
}
