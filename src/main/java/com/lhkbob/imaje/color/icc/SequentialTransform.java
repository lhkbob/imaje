package com.lhkbob.imaje.color.icc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class SequentialTransform implements ColorTransform {
  private final List<ColorTransform> transforms;
  private final List<double[]> workVectors;

  public SequentialTransform(List<ColorTransform> transforms) {
    if (transforms.size() < 1) {
      throw new IllegalArgumentException(
          "Sequential transform must have at least one transform element");
    }
    this.transforms = Collections.unmodifiableList(new ArrayList<>(transforms));
    workVectors = new ArrayList<>(transforms.size() - 1);
    for (int i = 0; i < transforms.size() - 1; i++) {
      ColorTransform in = transforms.get(i);
      ColorTransform out = transforms.get(i + 1);
      if (in.getOutputChannels() != out.getInputChannels()) {
        throw new IllegalArgumentException(
            "Adjacent transform elements do not have compatible output and input channel counts");
      }

      workVectors.add(new double[in.getOutputChannels()]);
    }
  }

  @Override
  public int getInputChannels() {
    return transforms.get(0).getInputChannels();
  }

  @Override
  public int getOutputChannels() {
    return transforms.get(transforms.size() - 1).getOutputChannels();
  }

  public List<ColorTransform> getSequence() {
    return transforms;
  }

  @Override
  public ColorTransform inverted() {
    List<ColorTransform> inv = new ArrayList<>(transforms.size());
    for (int i = transforms.size() - 1; i >= 0; i++) {
      ColorTransform invE = transforms.get(i).inverted();
      if (invE == null) {
        return null;
      }
      inv.add(invE);
    }
    return new SequentialTransform(inv);
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

    double[] in = input;
    double[] out;
    for (int i = 0; i < transforms.size(); i++) {
      out = i == transforms.size() - 1 ? output : workVectors.get(i);
      transforms.get(i).transform(in, out);
      in = out;
    }
  }
}
