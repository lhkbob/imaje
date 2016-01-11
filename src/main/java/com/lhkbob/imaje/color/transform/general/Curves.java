package com.lhkbob.imaje.color.transform.general;

import com.lhkbob.imaje.color.transform.curves.Curve;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Curves implements Transform {
  private final List<Curve> curves;

  public Curves(List<? extends Curve> curves) {
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
  public Curves inverted() {
    List<Curve> inverted = new ArrayList<>();
    for (Curve c: curves) {
      Curve invC = c.inverted();
      if (invC == null)
        return null;
      else
        inverted.add(invC);
    }
    return new Curves(inverted);
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    for (int i = 0; i < input.length; i++) {
      Curve c = curves.get(i);
      double inDomain = Math.max(c.getDomainMin(), Math.min(input[i], c.getDomainMax()));
      output[i] = c.evaluate(inDomain);
    }
  }

  @Override
  public Curves getLocallySafeInstance() {
    // This is purely functional (curve list is constant, and a curve is meant to be a constant
    // thread-safe function) so the instance can be used by any thread
    return this;
  }

  @Override
  public int hashCode() {
    return curves.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;
    if (!(o instanceof Curves))
      return false;
    return ((Curves) o).curves.equals(curves);
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
