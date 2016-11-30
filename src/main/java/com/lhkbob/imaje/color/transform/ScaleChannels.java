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
package com.lhkbob.imaje.color.transform;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.Functions;

import java.util.Arrays;

/**
 *
 */
public class ScaleChannels<S extends ColorSpace<T, S>, T extends Color<T, S>> implements ColorTransform<S, T, S, T> {
  private final S space;

  private final double[] domainMax;
  private final double[] domainMin;

  private final double[] rangeMax;
  private final double[] rangeMin;

  private final ScaleChannels<S, T> inverse;

  public ScaleChannels(
      S space, double[] domainMin, double[] domainMax, double[] rangeMin, double[] rangeMax) {
    Arguments.equals("domainMin.length", space.getChannelCount(), domainMin.length);
    Arguments.equals("domainMax.length", space.getChannelCount(), domainMax.length);
    Arguments.equals("rangeMin.length", space.getChannelCount(), domainMin.length);
    Arguments.equals("rangeMax.length", space.getChannelCount(), domainMax.length);

    this.space = space;

    this.domainMax = new double[domainMax.length];
    this.domainMin = new double[domainMin.length];
    this.rangeMax = new double[rangeMax.length];
    this.rangeMin = new double[rangeMin.length];

    for (int i = 0; i < domainMax.length; i++) {
      if (domainMin[i] >= domainMax[i]) {
        throw new IllegalArgumentException(
            "Minimum domain value in channel must be less than max value");
      }
      this.domainMax[i] = domainMax[i];
      this.domainMin[i] = domainMin[i];

      if (rangeMin[i] >= rangeMax[i]) {
        throw new IllegalArgumentException(
            "Minimum range value in channel must be less than max value");
      }
      this.rangeMax[i] = rangeMax[i];
      this.rangeMin[i] = rangeMin[i];
    }

    inverse = new ScaleChannels<>(this);
  }

  public static <S extends ColorSpace<T, S>, T extends Color<T, S>> ScaleChannels<S, T> normalize(
      S space, double[] channelMin, double[] channelMax) {
    double[] rangeMin = new double[channelMin.length];
    double[] rangeMax = new double[channelMax.length];
    Arrays.fill(rangeMax, 1.0);
    return new ScaleChannels<>(space, channelMin, channelMax, rangeMin, rangeMax);
  }

  private ScaleChannels(ScaleChannels<S, T> inverse) {
    space = inverse.space;
    domainMax = inverse.rangeMax;
    domainMin = inverse.rangeMin;
    rangeMax = inverse.domainMax;
    rangeMin = inverse.domainMin;
    this.inverse = inverse;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ScaleChannels)) {
      return false;
    }
    ScaleChannels c = (ScaleChannels) o;
    return c.space.equals(space) && Arrays.equals(c.domainMin, domainMin) && Arrays
        .equals(c.domainMax, domainMax) && Arrays.equals(c.rangeMin, rangeMin) && Arrays
        .equals(c.rangeMax, rangeMax);
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + space.hashCode();
    result = 31 * result + Arrays.hashCode(domainMin);
    result = 31 * result + Arrays.hashCode(domainMax);
    result = 31 * result + Arrays.hashCode(rangeMin);
    result = 31 * result + Arrays.hashCode(rangeMax);
    return result;
  }

  @Override
  public ScaleChannels<S, T> inverse() {
    return inverse;
  }

  @Override
  public S getInputSpace() {
    return space;
  }

  @Override
  public S getOutputSpace() {
    return space;
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    Arguments.equals("input.length", space.getChannelCount(), input.length);
    Arguments.equals("output.length", space.getChannelCount(), output.length);

    for (int i = 0; i < domainMax.length; i++) {
      double inDomain = Functions.clamp(input[i], domainMin[i], domainMax[i]);
      double alpha = (inDomain - domainMin[i]) / (domainMax[i] - domainMin[i]);
      output[i] = alpha * (rangeMax[i] - rangeMin[i]) + rangeMin[i];
    }

    return true;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Scale Channels (dim: ").append(domainMax.length)
        .append("):");
    for (int i = 0; i < domainMax.length; i++) {
      sb.append("\n  channel ").append(i + 1).append(": ").append(String
          .format("[%.3f, %.3f] -> [%.3f, %.3f]", domainMin[i], domainMax[i], rangeMin[i],
              rangeMax[i]));
    }
    return sb.toString();
  }
}
