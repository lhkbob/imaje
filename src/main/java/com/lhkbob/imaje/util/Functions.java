package com.lhkbob.imaje.util;

/**
 *
 */
public final class Functions {
  private Functions() {}

  public static double clamp(double value, double min, double max) {
    if (value > max)
      return max;
    else if (value < min)
      return min;
    else
      return value;
  }

  public static long clamp(long value, long min, long max) {
    if (value > max)
      return max;
    else if (value < min)
      return min;
    else
      return value;
  }

  public static int clamp(int value, int min, int max) {
    if (value > max)
      return max;
    else if (value < min)
      return min;
    else
      return value;
  }

  public static int maskInt(int bits) {
    return (~0) >>> (32 - bits);
  }

  public static long maskLong(int bits) {
    return (~0L) >>> (64 - bits);
  }
}
