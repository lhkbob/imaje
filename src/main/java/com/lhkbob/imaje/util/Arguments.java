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

import java.lang.annotation.Documented;
import java.util.Collection;
import java.util.Objects;

/**
 *
 */
public final class Arguments {
  @Documented
  public @interface Nullable {

  }

  private Arguments() {}

  public static void checkIndex(String name, long arrayLength, long index) {
    inRangeExcludeMax(name, 0, arrayLength, index);
  }

  public static void checkArrayRange(
      String name, long arrayLength, long rangeOffset, long rangeLength) {
    if (rangeOffset < 0L) {
      throw new IndexOutOfBoundsException(
          String.format("%s range offset must be at least 0: %d", name, rangeOffset));
    }
    if (rangeLength < 1L) {
      throw new IndexOutOfBoundsException(
          String.format("%s range length must be at least 1: %d", name, rangeLength));
    }
    if (rangeOffset + rangeLength > arrayLength) {
      throw new IndexOutOfBoundsException(String
          .format("%s range length (%d) too long for size %d", name, rangeLength, arrayLength));
    }
  }

  public static void notEmpty(String name, Collection<?> collection) {
    notNull(name, collection);
    if (collection.isEmpty()) {
      throw new IllegalArgumentException(String.format("%s cannot be empty", name));
    }
  }

  public static void notNull(String name, Object toCheck) {
    if (toCheck == null) {
      throw new NullPointerException(String.format("%s cannot be null", name));
    }
  }

  public static void equals(String name, Object expected, Object actual) {
    if (!Objects.equals(expected, actual)) {
      throw new IllegalArgumentException(
          String.format("%s (%s) must equal %s", name, actual, expected));
    }
  }

  public static void equals(String name, double expected, double actual) {
    equals(name, expected, actual, 1e-23);
  }

  public static void equals(String name, long expected, long actual) {
    if (expected != actual) {
      throw new IllegalArgumentException(
          String.format("%s (%d) must equal %d", name, actual, expected));
    }
  }

  public static void equals(String name, double expected, double value, double precision) {
    if (Math.abs(expected - value) > precision) {
      throw new IllegalArgumentException(String
          .format("%s must equal (up to %g) %g, but was: %g", name, precision, expected, value));
    }
  }

  public static void isPositive(String name, double value) {
    if (value <= 0.0) {
      throw new IllegalArgumentException(
          String.format("%s (%.4f) must be greater than 0", name, value));
    }
  }

  public static void isNegative(String name, double value) {
    if (value >= 0.0) {
      throw new IllegalArgumentException(
          String.format("%s (%.4f) must be less than 0", name, value));
    }
  }

  public static void isGreaterThanOrEqualToZero(String name, double value) {
    if (value < 0.0) {
      throw new IllegalArgumentException(
          String.format("%s (%.4f) must be greater than or equal to 0", name, value));
    }
  }

  public static void isLessThanOrEqualToZero(String name, double value) {
    if (value > 0.0) {
      throw new IllegalArgumentException(
          String.format("%s (%.4f) must be less than or equal to 0", name, value));
    }
  }

  public static void inRangeInclusive(String name, double min, double max, double value) {
    if (value < min || value > max) {
      throw new IllegalArgumentException(
          String.format("%s (%.4f) must be in range [%.4f, %.4f]", name, value, min, max));
    }
  }

  public static void inRangeExclusive(String name, double min, double max, double value) {
    if (value <= min || value >= max) {
      throw new IllegalArgumentException(
          String.format("%s (%.4f) must be in range (%.4f, %.4f)", name, value, min, max));
    }
  }

  public static void inRangeExcludeMax(String name, double min, double max, double value) {
    if (value < min || value >= max) {
      throw new IllegalArgumentException(
          String.format("%s (%.4f) must be in range [%.4f, %.4f)", name, value, min, max));
    }
  }

  public static void inRangeExcludeMin(String name, double min, double max, double value) {
    if (value <= min || value > max) {
      throw new IllegalArgumentException(
          String.format("%s (%.4f) must be in range (%.4f, %.4f]", name, value, min, max));
    }
  }

  public static void isPositive(String name, long value) {
    if (value <= 0L) {
      throw new IllegalArgumentException(
          String.format("%s (%d) must be greater than 0", name, value));
    }
  }

  public static void isNegative(String name, long value) {
    if (value >= 0L) {
      throw new IllegalArgumentException(String.format("%s (%d) must be less than 0", name, value));
    }
  }

  public static void isGreaterThanOrEqualToZero(String name, long value) {
    if (value < 0L) {
      throw new IllegalArgumentException(
          String.format("%s (%d) must be greater than or equal to 0", name, value));
    }
  }

  public static void isLessThanOrEqualToZero(String name, long value) {
    if (value > 0L) {
      throw new IllegalArgumentException(
          String.format("%s (%d) must be less than or equal to 0", name, value));
    }
  }

  public static void isGreaterThanOrEqualTo(String name, double min, double value) {
    if (value < min) {
      throw new IllegalArgumentException(
          String.format("%s (%.4f) must be greater than or equal to %.4f", name, value, min));
    }
  }

  public static void isGreaterThanOrEqualTo(String name, long min, long value) {
    if (value < min) {
      throw new IllegalArgumentException(
          String.format("%s (%d) must be greater than or equal to %d", name, value, min));
    }
  }

  public static void isGreaterThan(String name, double min, double value) {
    if (value <= min) {
      throw new IllegalArgumentException(
          String.format("%s (%.4f) must be greater than %.4f", name, value, min));
    }
  }

  public static void isGreaterThan(String name, long min, long value) {
    if (value <= min) {
      throw new IllegalArgumentException(
          String.format("%s (%d) must be greater than %d", name, value, min));
    }
  }

  public static void isLessThanOrEqualTo(String name, double max, double value) {
    if (value > max) {
      throw new IllegalArgumentException(
          String.format("%s (%.4f) must be less than or equal to %.4f", name, value, max));
    }
  }

  public static void isLessThanOrEqualTo(String name, long max, long value) {
    if (value > max) {
      throw new IllegalArgumentException(
          String.format("%s (%d) must be less than or equal to %d", name, value, max));
    }
  }

  public static void isLessThan(String name, double max, double value) {
    if (value >= max) {
      throw new IllegalArgumentException(
          String.format("%s (%.4f) must be less than %.4f", name, value, max));
    }
  }

  public static void isLessThan(String name, long max, long value) {
    if (value >= max) {
      throw new IllegalArgumentException(
          String.format("%s (%d) must be less than %d", name, value, max));
    }
  }

  public static void inRangeInclusive(String name, long min, long max, long value) {
    if (value < min || value > max) {
      throw new IllegalArgumentException(
          String.format("%s (%d) must be in range [%d, %d]", name, value, min, max));
    }
  }

  public static void inRangeExclusive(String name, long min, long max, long value) {
    if (value <= min || value >= max) {
      throw new IllegalArgumentException(
          String.format("%s (%d) must be in range (%d, %d)", name, value, min, max));
    }
  }

  public static void inRangeExcludeMax(String name, long min, long max, long value) {
    if (value < min || value >= max) {
      throw new IllegalArgumentException(
          String.format("%s (%d) must be in range [%d, %d)", name, value, min, max));
    }
  }

  public static void inRangeExcludeMin(String name, long min, long max, long value) {
    if (value <= min || value > max) {
      throw new IllegalArgumentException(
          String.format("%s (%d) must be in range (%d, %d]", name, value, min, max));
    }
  }
}
