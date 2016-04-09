package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.LongSource;
import com.lhkbob.imaje.data.NumericDataSource;

/**
 *
 */
public class UnsignedLongSource implements NumericDataSource, DataView<LongSource> {
  private static final long SIGN_MASK = ~(1L << 63);
  private static final double SIGN_THRESHOLD = Math.pow(2.0, 63.0);

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
      return SIGN_THRESHOLD + (svalue & SIGN_MASK);
    } else {
      // The value is in the range 0 to 2^63-1 so it can be lifted to double without issue
      return svalue;
    }
  }

  @Override
  public void setValue(long index, double value) {
    if (value > SIGN_THRESHOLD - 1.0) {
      // Cast the lower 63 bits to a long and then add the highest sign bit
      long lower = (long) (value - SIGN_THRESHOLD);
      source.set(index, (1L << 63) | lower);
    } else {
      // Below the unsigned threshold so we can cast safely
      source.set(index, (long) value);
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
