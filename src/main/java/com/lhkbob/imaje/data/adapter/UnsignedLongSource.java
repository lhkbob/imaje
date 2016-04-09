package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.LongSource;
import com.lhkbob.imaje.data.NumericDataSource;
import com.lhkbob.imaje.util.Functions;

/**
 *
 */
public class UnsignedLongSource implements NumericDataSource, DataView<LongSource> {
  public static final double MAX_VALUE = Math.pow(2.0, 64.0);

  private static final long SIGN_MASK = ~(1L << 63);
  private static final double SIGN_OFFSET = Math.pow(2.0, 63.0);
  // The highest double value that can be represented as a regular signed long--e.g. Long.MAX_VALUE--
  // which is equal to 2^63 - 1, but since we round the double value anything higher than
  // 2^63-0.5 will round to 2^63 and then overflow, requiring special handling.
  private static final double SIGN_THRESHOLD = SIGN_OFFSET - 0.5;

  private final LongSource source;

  public UnsignedLongSource(LongSource source) {
    this.source = source;
  }

  public long get(long index) {
    return source.get(index);
  }

  @Override
  public long getLength() {
    return source.getLength();
  }

  @Override
  public double getValue(long index) {
    // The UnsignedLongSource cannot actually represent a 1s complement 64bit number so it returns
    // the 2s complement signed version
    long svalue = source.get(index);

    if (svalue < 0) {
      // Mask out the upper most bit and add 2^63 to it
      return SIGN_OFFSET + (svalue & SIGN_MASK);
    } else {
      // The value is in the range 0 to 2^63-1 so it can be lifted to double without issue
      return svalue;
    }
  }

  @Override
  public void setValue(long index, double value) {
    value = Functions.clamp(value, 0.0, MAX_VALUE);

    if (value > SIGN_THRESHOLD) {
      // Cast the lower 63 bits to a long and then add the highest sign bit
      long lower = Math.round(value - SIGN_THRESHOLD);
      source.set(index, (1L << 63) | lower);
    } else {
      // Below the unsigned threshold so we can cast safely
      source.set(index, Math.round(value));
    }
  }

  @Override
  public boolean isBigEndian() {
    return source.isBigEndian();
  }

  @Override
  public boolean isGPUAccessible() {
    return source.isGPUAccessible();
  }

  @Override
  public int getBitSize() {
    return source.getBitSize();
  }

  public void set(long index, long value) {
    source.set(index, value);
  }

  @Override
  public LongSource getSource() {
    return source;
  }
}
