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
