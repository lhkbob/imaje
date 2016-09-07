package com.lhkbob.imaje.util;

/**
 *
 */
public final class Functions {
  private Functions() {}

  private static final double INV_LOG_2 = 1.0 / Math.log(2.0);

  public static double log2(double value) {
    return Math.log(value) * INV_LOG_2;
  }

  /**
   * <p/>
   * Compute the smallest power-of-two that is greater than or equal to the given integer. If num is less
   * than or equal to 0, 1 is always returned. If num is already a power-of-two, num is returned.
   * <p/>
   * This runs in constant time.
   *
   * @param num
   *     The input
   * @return Smallest power-of-two greater than or equal to num
   */
  public static int ceilPowerOfTwo(int num) {
    if (num <= 0) {
      return 1;
    }

    num--;
    num |= (num >> 1);
    num |= (num >> 2);
    num |= (num >> 4);
    num |= (num >> 8);
    num |= (num >> 16);
    num++;

    return num;
  }

  /**

   * @return The log base 2 of the number, after rounding up to the nearest power of two
   */
  public static long ceilLog2(long num) {
    return 64 - Long.numberOfLeadingZeros(num - 1);
  }

  public static int ceilLog2(int num) {
    return 32 - Integer.numberOfLeadingZeros(num - 1);
  }

  /**
   * Return true if the given integer is a power of two. This is an efficient, constant time implementation.
   * Numbers less than or equal to 0 will always return false.
   *
   * @param num
   *     The number to check
   * @return True if num is a power of two
   */
  public static boolean isPowerOfTwo(int num) {
    if (num <= 0) {
      return false;
    }
    return (num & (num - 1)) == 0;
  }

  // floor(log(x)/log(2))
  public static int floorLog2(int x) {
    return 31 - Integer.numberOfLeadingZeros(x);
  }

  public static long floorLog2(long x) {
    return 63 - Long.numberOfLeadingZeros(x);
  }

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

  public static int floorInt(double v) {
    return Math.toIntExact(floorLong(v));
  }

  public static long floorLong(double v) {
    return (long) Math.floor(v);
  }

  public static double frac(double value) {
    return value - (long) value;
  }

  public static int roundToInt(double value) {
    return Math.toIntExact(Math.round(value));
  }
}
