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
package com.lhkbob.imaje.op;

import com.lhkbob.imaje.color.Color;

/**
 *
 */
public final class ColorOps {
  private ColorOps() {

  }

  public static <T extends Color> void zero(T result) {
    double[] c = result.getChannels();
    for (int i = 0; i < c.length; i++) {
      c[i] = 0.0;
    }
  }

  public static <T extends Color> void mul(T color, double c, T result) {
    double[] in = color.getChannels();
    double[] out = result.getChannels();
    for (int i = 0; i < in.length; i++) {
      out[i] = in[i] * c;
    }
  }

  public static <T extends Color> void add(T a, T b, T result) {
    double[] inA = a.getChannels();
    double[] inB = b.getChannels();
    double[] out = result.getChannels();
    for (int i = 0; i < inA.length; i++) {
      out[i] = inA[i] + inB[i];
    }
  }

  public static <T extends Color> void sub(T a, T b, T result) {
    double[] inA = a.getChannels();
    double[] inB = b.getChannels();
    double[] out = result.getChannels();
    for (int i = 0; i < inA.length; i++) {
      out[i] = inA[i] - inB[i];
    }
  }

  public static <T extends Color> void mul(T a, T b, T result) {
    double[] inA = a.getChannels();
    double[] inB = b.getChannels();
    double[] out = result.getChannels();
    for (int i = 0; i < inA.length; i++) {
      out[i] = inA[i] * inB[i];
    }
  }

  public static <T extends Color> void add(T color, double c, T result) {
    double[] in = color.getChannels();
    double[] out = result.getChannels();
    for (int i = 0; i < in.length; i++) {
      out[i] = in[i] + c;
    }
  }

  public static <T extends Color> void sub(T color, double c, T result) {
    double[] in = color.getChannels();
    double[] out = result.getChannels();
    for (int i = 0; i < in.length; i++) {
      out[i] = in[i] - c;
    }
  }

  public static <T extends Color> void addScaled(T a, T b, double scaleB, T result) {
    double[] inA = a.getChannels();
    double[] inB = b.getChannels();
    double[] out = result.getChannels();
    for (int i = 0; i < inA.length; i++) {
      out[i] = inA[i] + scaleB * inB[i];
    }
  }
}
