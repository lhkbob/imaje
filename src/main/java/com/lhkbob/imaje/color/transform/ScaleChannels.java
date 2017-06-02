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

import com.lhkbob.imaje.color.Vector;
import com.lhkbob.imaje.color.VectorSpace;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.Functions;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * ScaleChannels
 * =============
 *
 * ScaleChannels is a simple transform that linearly maps an input vector component from a defined
 * domain to a value in another finite continuous interval. The transform supports separate domain
 * and range minimum and maximum values for each channel of the input and output vector spaces.
 *
 * The ScaleChannels transform behaves similarly to {@link Identity} in how it handles vector spaces
 * with different dimensionalities. If the two spaces have the same dimensionality, each channel is
 * linearly interpolated from its configured domain interval to its range interval. If the two
 * spaces have different dimensionality, then one of the two must be only a single channel.
 *
 * If the input space has a single channel, component values in its domain interval are first mapped
 * to the unit `[0, 1]` interval and then this intermediate value is mapped to each of the output
 * space's channels separately (with their potentially independent range intervals). If the output
 * space has a single channel, each component value of the input vector is mapped independently to
 * the unit `[0, 1]` interval and then averaged; this average uniform value is then mapped to the
 * single output channel's configured range interval.
 *
 * @author Michael Ludwig
 */
public class ScaleChannels<I extends Vector<I, SI>, SI extends VectorSpace<I, SI>, O extends Vector<O, SO>, SO extends VectorSpace<O, SO>> implements Transform<I, SI, O, SO> {
  private final SI inputSpace;
  private final SO outputSpace;

  private final double[] domainMax;
  private final double[] domainMin;

  private final double[] rangeMax;
  private final double[] rangeMin;

  private final ScaleChannels<O, SO, I, SI> inverse;

  /**
   * Create a new ScaleChannels transform that linearly interpolates an input vector's component
   * values from a specific domain range to an output range. The domain range is specified in two
   * parallel arrays: `domainMin` and `domainMax`. These must have the same length as each other and
   * their lengths must either be equal to the channel count of the input space, or exactly 1. If
   * the domain range arrays' lengths are the input channel count then each index specifies the
   * minimum and maximum value for the input component with the same index. If the length of the
   * range arrays' is 1, then that specifies the minimum and maximum value for all input components.
   *
   * The output range is specified equivalently with the `rangeMin` and `rangeMax` arrays, except
   * that these range arrays must have lengths equal to 1 or the output space's channel count.
   *
   * Depending on the dimensionality of the input and output spaces, the mapping from input
   * component to output component is handled differently. If both spaces have dimensionality above
   * one, then they must have the same dimensionality and the mapping is one-to-one. If either space
   * has a dimensionality of one, then the identity transformation behaves in a modified form, as
   * described above.
   *
   * @param inputSpace
   *     The input vector space
   * @param outputSpace
   *     The output vector space
   * @param domainMin
   *     The array of minimum values for input components (either length 1 to define all channels,
   *     or equal to the input channel count)
   * @param domainMax
   *     The array of maximum values for input components (same length as `domainMin`)
   * @param rangeMin
   *     The array of minimum values for the output components (either length 1 to define all
   *     channels, or equal to the output channel count)
   * @param rangeMax
   *     The array of maximum values for the output components (same length as `rangeMin`)
   * @throws IllegalArgumentException
   *     if the lengths of the domain range arrays do match the specification and vector spaces'
   *     channel counts
   */
  public ScaleChannels(
      SI inputSpace, SO outputSpace, double[] domainMin, double[] domainMax, double[] rangeMin,
      double[] rangeMax) {
    // Domain min and max, and range min and max much have paired lengths
    Arguments.equals("domainMax.length", domainMax.length, domainMin.length);
    Arguments.equals("rangeMax.length", rangeMax.length, rangeMin.length);

    if (domainMax.length != 1) {
      // If more than a single value is given (which is assumed to be representative of all channels)
      // then all channel ranges must be provided
      Arguments.equals("domain channel count", inputSpace.getChannelCount(), domainMax.length);
    }
    if (rangeMax.length != 1) {
      Arguments.equals("range channel count", outputSpace.getChannelCount(), rangeMax.length);
    }

    // And if input/output space channel counts are not 1s, they must be equal
    if (inputSpace.getChannelCount() != 1 && outputSpace.getChannelCount() != 1) {
      Arguments
          .equals("channel count", inputSpace.getChannelCount(), outputSpace.getChannelCount());
    }

    this.inputSpace = inputSpace;
    this.outputSpace = outputSpace;

    this.domainMax = new double[domainMax.length];
    this.domainMin = new double[domainMin.length];
    this.rangeMax = new double[rangeMax.length];
    this.rangeMin = new double[rangeMin.length];

    // Validate ranges
    for (int i = 0; i < domainMax.length; i++) {
      if (domainMin[i] >= domainMax[i]) {
        throw new IllegalArgumentException(
            "Minimum domain value in channel must be less than max value");
      }
      this.domainMax[i] = domainMax[i];
      this.domainMin[i] = domainMin[i];
    }
    for (int i = 0; i < rangeMax.length; i++) {
      if (rangeMin[i] >= rangeMax[i]) {
        throw new IllegalArgumentException(
            "Minimum range value in channel must be less than max value");
      }
      this.rangeMax[i] = rangeMax[i];
      this.rangeMin[i] = rangeMin[i];
    }

    inverse = new ScaleChannels<>(this);
  }

  private ScaleChannels(ScaleChannels<O, SO, I, SI> inverse) {
    inputSpace = inverse.getOutputSpace();
    outputSpace = inverse.getInputSpace();

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
    return Objects.equals(c.inputSpace, inputSpace) && Objects.equals(c.outputSpace, outputSpace)
        && Arrays.equals(c.domainMin, domainMin) && Arrays.equals(c.domainMax, domainMax) && Arrays
        .equals(c.rangeMin, rangeMin) && Arrays.equals(c.rangeMax, rangeMax);
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + inputSpace.hashCode();
    result = 31 * result + outputSpace.hashCode();
    result = 31 * result + Arrays.hashCode(domainMin);
    result = 31 * result + Arrays.hashCode(domainMax);
    result = 31 * result + Arrays.hashCode(rangeMin);
    result = 31 * result + Arrays.hashCode(rangeMax);
    return result;
  }

  @Override
  public Optional<ScaleChannels<O, SO, I, SI>> inverse() {
    return Optional.of(inverse);
  }

  @Override
  public SI getInputSpace() {
    return inputSpace;
  }

  @Override
  public SO getOutputSpace() {
    return outputSpace;
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    Arguments.equals("input.length", inputSpace.getChannelCount(), input.length);
    Arguments.equals("output.length", outputSpace.getChannelCount(), output.length);

    if (input.length > 1 && output.length == 1) {
      // Normalize, then average and then set to output range
      double avgAlpha = 0.0;
      for (int i = 0; i < input.length; i++) {
        double inDomain = Functions.clamp(input[i], getDomainMin(i), getDomainMax(i));
        avgAlpha += (inDomain - getDomainMin(i)) / (getDomainMax(i) - getDomainMin(i));
      }

      output[0] = (avgAlpha / input.length) * (getRangeMax(0) - getRangeMin(0)) + getRangeMin(0);
    } else if (input.length == 1 && output.length > 1) {
      // Calculate single alpha value and then scale to each output range
      double inDomain = Functions.clamp(input[0], getDomainMin(0), getDomainMax(0));
      double alpha = (inDomain - getDomainMin(0)) / (getDomainMax(0) - getDomainMin(0));
      for (int i = 0; i < output.length; i++) {
        output[i] = alpha * (getRangeMax(i) - getRangeMin(i)) + getRangeMin(i);
      }
    } else {
      // Normalize and set per channel
      for (int i = 0; i < input.length; i++) {
        double inDomain = Functions.clamp(input[i], getDomainMin(i), getDomainMax(i));
        double alpha = (inDomain - getDomainMin(i)) / (getDomainMax(i) - getDomainMin(i));
        output[i] = alpha * (getRangeMax(i) - getRangeMin(i)) + getRangeMin(i);
      }
    }

    return true;
  }

  private double getDomainMin(int channel) {
    if (domainMin.length == 1) {
      return domainMin[0];
    } else {
      return domainMin[channel];
    }
  }

  private double getDomainMax(int channel) {
    if (domainMax.length == 1) {
      return domainMax[0];
    } else {
      return domainMax[channel];
    }
  }

  private double getRangeMin(int channel) {
    if (rangeMin.length == 1) {
      return rangeMin[0];
    } else {
      return rangeMin[channel];
    }
  }

  private double getRangeMax(int channel) {
    if (rangeMax.length == 1) {
      return rangeMax[0];
    } else {
      return rangeMax[channel];
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Scale Channels:");
    if (inputSpace.getChannelCount() > 1 && outputSpace.getChannelCount() == 1) {
      // Normalize each input channel based on domain, then average, and then scale to single output
      sb.append(" (");
      for (int i = 0; i < inputSpace.getChannelCount(); i++) {
        if (i > 0) {
          sb.append(" + ");
        }
        sb.append(String.format("([%.3f, %.3f] -> [0, 1])", getDomainMin(i), getDomainMax(i)));
      }
      sb.append(String.format(") / 3 -> [%.3f, %.3f]", getRangeMin(0), getRangeMax(0)));
    } else if (inputSpace.getChannelCount() == 1 && outputSpace.getChannelCount() > 1) {
      // Copy normalized input channel to every output channel
      for (int i = 0; i < outputSpace.getChannelCount(); i++) {
        sb.append("\n channel ").append(i + 1).append(": ").append(String
            .format("[%.3f, %.3f] -> [%.3f, %.3f]", getDomainMin(0), getDomainMax(0),
                getRangeMin(i), getRangeMax(i)));
      }
    } else {
      // Do each channel individually
      for (int i = 0; i < inputSpace.getChannelCount(); i++) {
        sb.append("\n  channel ").append(i + 1).append(": ").append(String
            .format("[%.3f, %.3f] -> [%.3f, %.3f]", getDomainMin(i), getDomainMax(i),
                getRangeMin(i), getRangeMax(i)));
      }
    }

    return sb.toString();
  }
}
