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
