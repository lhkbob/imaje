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

import java.util.Arrays;

/**
 *
 */
public class NormalizeChannels implements Transform {
  private final double[] channelMaxs;
  private final double[] channelMins;

  public NormalizeChannels(double[] channelMins, double[] channelMaxs) {
    Arguments.equals("channel lengths", channelMaxs.length, channelMins.length);

    this.channelMaxs = new double[channelMaxs.length];
    this.channelMins = new double[channelMins.length];
    for (int i = 0; i < channelMaxs.length; i++) {
      if (channelMins[i] > channelMaxs[i]) {
        throw new IllegalArgumentException(
            "Minimum value in channel must be less than or equal to max value");
      }
      this.channelMaxs[i] = channelMaxs[i];
      this.channelMins[i] = channelMins[i];
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof NormalizeChannels)) {
      return false;
    }
    NormalizeChannels c = (NormalizeChannels) o;
    return Arrays.equals(c.channelMins, channelMins) && Arrays.equals(c.channelMaxs, channelMaxs);
  }

  @Override
  public int getInputChannels() {
    return channelMaxs.length;
  }

  @Override
  public NormalizeChannels getLocallySafeInstance() {
    // This is purely functional (with constant parameters) so the instance can be used by any thread
    return this;
  }

  @Override
  public int getOutputChannels() {
    return channelMaxs.length;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + Arrays.hashCode(channelMaxs);
    result = 31 * result + Arrays.hashCode(channelMins);
    return result;
  }

  @Override
  public Transform inverted() {
    return new DenormalizeChannelTransform();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Normalize Channels Transform (dim: ")
        .append(channelMaxs.length).append("):");
    for (int i = 0; i < channelMaxs.length; i++) {
      sb.append("\n  channel ").append(i + 1).append(": ")
          .append(String.format("[%.3f, %.3f] -> [0.0, 1.0]", channelMins[i], channelMaxs[i]));
    }
    return sb.toString();
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    for (int i = 0; i < channelMins.length; i++) {
      double unclipped = (input[i] - channelMins[i]) / (channelMaxs[i] - channelMins[i]);
      output[i] = Math.max(0.0, Math.min(unclipped, 1.0));
    }
  }

  private class DenormalizeChannelTransform implements Transform {
    @Override
    public boolean equals(Object o) {
      if (o == this) {
        return true;
      }
      if (!(o instanceof DenormalizeChannelTransform)) {
        return false;
      }
      return ((DenormalizeChannelTransform) o).getParent().equals(getParent());
    }

    @Override
    public int getInputChannels() {
      return channelMaxs.length;
    }

    @Override
    public DenormalizeChannelTransform getLocallySafeInstance() {
      // This is purely functional so the instance can be used by any thread
      return this;
    }

    @Override
    public int getOutputChannels() {
      return channelMaxs.length;
    }

    @Override
    public int hashCode() {
      return DenormalizeChannelTransform.class.hashCode() ^ NormalizeChannels.this.hashCode();
    }

    @Override
    public Transform inverted() {
      return NormalizeChannels.this;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("De-normalize Channels Transform (dim: ")
          .append(channelMaxs.length).append("):");
      for (int i = 0; i < channelMaxs.length; i++) {
        sb.append("\n  channel ").append(i + 1).append(": ")
            .append(String.format("[0.0, 1.0] -> [%.3f, %.3f]", channelMins[i], channelMaxs[i]));
      }
      return sb.toString();
    }

    @Override
    public void transform(double[] input, double[] output) {
      Transform.validateDimensions(this, input, output);

      for (int i = 0; i < channelMaxs.length; i++) {
        double clipped = Math.max(0.0, Math.min(input[i], 1.0));
        output[i] = clipped * (channelMaxs[i] - channelMins[i]) + channelMins[i];
      }
    }

    private NormalizeChannels getParent() {
      return NormalizeChannels.this;
    }
  }
}
