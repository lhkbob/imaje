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

import java.util.Arrays;
import java.util.Objects;

/**
 *
 */
public final class Colorant {
  public static final Colorant EBU_TECH_3213_E = new Colorant(
      new double[] { 0.640, 0.290, 0.150 }, new double[] { 0.330, 0.600, 0.060 });
  public static final Colorant ITU_R_BT_709_2 = new Colorant(
      new double[] { 0.640, 0.300, 0.150 }, new double[] { 0.330, 0.600, 0.060 });
  public static final Colorant P22 = new Colorant(
      new double[] { 0.625, 0.280, 0.155 }, new double[] { 0.340, 0.605, 0.070 });
  public static final Colorant SMPTE_RP145 = new Colorant(
      new double[] { 0.630, 0.310, 0.155 }, new double[] { 0.340, 0.595, 0.070 });
  private final double[] xs;
  private final double[] ys;

  public Colorant(double[] xs, double[] ys) {
    Arguments.equals("array lengths", xs.length, ys.length);

    this.xs = xs;
    this.ys = ys;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Colorant)) {
      return false;
    }

    Colorant c = (Colorant) o;
    return Arrays.equals(c.xs, xs) && Arrays.equals(c.ys, ys);
  }

  public int getChannelCount() {
    return xs.length;
  }

  public String getName() {
    if (Objects.equals(ITU_R_BT_709_2, this)) {
      return "ITU_R_BT_709_2";
    } else if (Objects.equals(SMPTE_RP145, this)) {
      return "SMPTE_RP145";
    } else if (Objects.equals(EBU_TECH_3213_E, this)) {
      return "EBU_TECH_3213_E";
    } else if (Objects.equals(P22, this)) {
      return "P22";
    } else {
      return null;
    }
  }

  public double getXChromaticity(int channel) {
    return xs[channel];
  }

  public double getYChromaticity(int channel) {
    return ys[channel];
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(xs) ^ Arrays.hashCode(ys);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Colorant (");

    String name = getName();
    if (name != null) {
      sb.append(name).append(", ");
    }

    for (int i = 0; i < getChannelCount(); i++) {
      if (i > 0) {
        sb.append(", ");
      }
      sb.append('c').append(i + 1).append(": [").append(String.format("%.4f", getXChromaticity(i)))
          .append(", ").append(String.format("%.4f", getYChromaticity(i))).append("]");
    }
    sb.append(")");
    return sb.toString();
  }
}
