package com.lhkbob.imaje.color.transform.general;

import com.lhkbob.imaje.util.Arguments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
public class Composition implements Transform {
  private final List<Transform> transforms;
  private final List<double[]> workVectors;

  public Composition(List<Transform> transforms) {
    Arguments.notEmpty("transforms", transforms);

    this.transforms = Collections.unmodifiableList(new ArrayList<>(transforms));
    workVectors = new ArrayList<>(transforms.size() - 1);
    for (int i = 0; i < transforms.size() - 1; i++) {
      Transform in = transforms.get(i);
      Transform out = transforms.get(i + 1);
      if (in.getOutputChannels() != out.getInputChannels()) {
        throw new IllegalArgumentException(
            "Adjacent transform elements do not have compatible output and input channel counts");
      }

      workVectors.add(new double[in.getOutputChannels()]);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Composition)) {
      return false;
    }
    return ((Composition) o).transforms.equals(transforms);
  }

  @Override
  public int getInputChannels() {
    return transforms.get(0).getInputChannels();
  }

  @Override
  public Transform getLocallySafeInstance() {
    // A composition's locally safe instance is locally safe instances of its composed functions
    // and new working arrays.
    return new Composition(
        transforms.stream().map(Transform::getLocallySafeInstance).collect(Collectors.toList()));
  }

  @Override
  public int getOutputChannels() {
    return transforms.get(transforms.size() - 1).getOutputChannels();
  }

  @Override
  public int hashCode() {
    return transforms.hashCode();
  }

  @Override
  public Composition inverted() {
    List<Transform> inv = new ArrayList<>(transforms.size());
    for (int i = transforms.size() - 1; i >= 0; i--) {
      Transform invE = transforms.get(i).inverted();
      if (invE == null) {
        return null;
      }
      inv.add(invE);
    }
    return new Composition(inv);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Sequential Transform (in: ").append(getInputChannels())
        .append(", out: ").append(getOutputChannels()).append("):");
    for (int i = 0; i < transforms.size(); i++) {
      sb.append("\n  ").append(i + 1).append(": ").append(transforms.get(i));
    }
    return sb.toString();
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    double[] in = input;
    double[] out;
    for (int i = 0; i < transforms.size(); i++) {
      out = i == transforms.size() - 1 ? output : workVectors.get(i);
      transforms.get(i).transform(in, out);
      in = out;
    }
  }
}
