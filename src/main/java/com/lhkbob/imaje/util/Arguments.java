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

  public static void checkArrayRange(String name, long arrayLength, long rangeOffset, long rangeLength) {
    if (rangeOffset < 0L) {
      throw new IndexOutOfBoundsException(String.format("%s range offset must be at least 0: %d", name, rangeOffset));
    }
    if (rangeLength < 1L) {
      throw new IndexOutOfBoundsException(String.format("%s range length must be at least 1: %d", name, rangeLength));
    }
    if (rangeOffset + rangeLength > arrayLength) {
      throw new IndexOutOfBoundsException(String.format("%s range length (%d) too long for size %d", name, rangeLength, arrayLength));
    }
  }

  public static void notEmpty(String name, Collection<?> collection) {
    notNull(name, collection);
    if (collection.isEmpty()) {
      throw new IllegalArgumentException(String.format("%s cannot be empty", name));
    }
  }

  public  static void notNull(String name, Object toCheck) {
    if (toCheck == null) {
      throw new NullPointerException(String.format("%s cannot be null", name));
    }
  }

  public static void equals(String name, Object expected, Object actual) {
    if (!Objects.equals(expected, actual)) {
      throw new IllegalArgumentException(String.format("%s (%s) must equal %s", name, actual, expected));
    }
  }

  public static void equals(String name, double expected, double actual) {
    if (Math.abs(expected - actual) < 1e-23) {
      throw new IllegalArgumentException(String.format("%s (%.4f) must equal %.4f", name, actual, expected));
    }
  }

  public static void equals(String name, long expected, long actual) {
    if (expected != actual) {
      throw new IllegalArgumentException(String.format("%s (%d) must equal %d", name, actual, expected));
    }
  }

  public static void isPositive(String name, double value) {
    if (value <= 0.0) {
      throw new IllegalArgumentException(String.format("%s (%.4f) must be greater than 0", name, value));
    }
  }

  public static void isNegative(String name, double value) {
    if (value >= 0.0) {
      throw new IllegalArgumentException(String.format("%s (%.4f) must be less than 0", name, value));
    }
  }

  public static void isGreaterThanOrEqualToZero(String name, double value) {
    if (value < 0.0) {
      throw new IllegalArgumentException(String.format("%s (%.4f) must be greater than or equal to 0", name, value));
    }
  }

  public static void isLessThanOrEqualToZero(String name, double value) {
    if (value > 0.0) {
      throw new IllegalArgumentException(String.format("%s (%.4f) must be less than or equal to 0", name, value));
    }
  }

  public static void inRangeInclusive(String name, double min, double max, double value) {
    if (value < min || value > max) {
      throw new IllegalArgumentException(String.format("%s (%.4f) must be in range [%.4f, %.4f]", name, value, min, max));
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
