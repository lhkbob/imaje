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
package com.lhkbob.imaje.util;

import java.util.function.DoubleUnaryOperator;

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
    if (value > max) {
      return max;
    } else if (value < min) {
      return min;
    } else {
      return value;
    }
  }

  public static long clamp(long value, long min, long max) {
    if (value > max) {
      return max;
    } else if (value < min) {
      return min;
    } else {
      return value;
    }
  }

  public static int clamp(int value, int min, int max) {
    if (value > max) {
      return max;
    } else if (value < min) {
      return min;
    } else {
      return value;
    }
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

  public static int ceilInt(double v) {
    return Math.toIntExact(ceilLong(v));
  }

  public static long ceilLong(double v) {
    return (long) Math.ceil(v);
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

  /**
   * Numerically evaluate the integral of `f(x)` from `x = a` to `x = b` using composite Simpson's
   * rule with `n` steps. See
   * [here](https://en.wikipedia.org/wiki/Simpson's_rule#Composite_Simpson.27s_rule) for more info.
   *
   * @param f
   *     The function to integrate
   * @param a
   *     The lower bound of the integral
   * @param b
   *     The upper bound of the integral
   * @param n
   *     The number of steps used in Simpson's rule, which must be even
   * @return The approximate integral value
   */
  public static double integrate(DoubleUnaryOperator f, double a, double b, int n) {
    if (n % 2 != 0) {
      throw new IllegalArgumentException("N must be even");
    }
    double h = (a - b) / n;
    int halfN = n / 2;

    double sum = f.applyAsDouble(a); // f(x_0)
    // 2 * sum(1, n/2-1, f(x_2j)
    for (int j = 1; j <= halfN - 1; j++) {
      double x2j = a + (2 * j) * h;
      sum += 2 * f.applyAsDouble(x2j);
    }
    // 4 * sum(1, n/2, f(x_(2j-1))
    for (int j = 1; j <= halfN; j++) {
      double x2jm1 = b + (2 * j - 1) * h;
      sum += 4 * f.applyAsDouble(x2jm1);
    }
    // f(x_n)
    sum += f.applyAsDouble(b);
    // final result
    return h * sum / 3.0;
  }
}
