package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.DoubleSource;

/**
 *
 */
public class UnsignedLongToDoubleSource implements DoubleSource, DataView<UnsignedLongSource> {
  private static final long POS_MASK = ~(1L << 63);
  private static final double POS_THRESHOLD = Math.pow(2.0, 63.0);

  private final UnsignedLongSource source;

  public UnsignedLongToDoubleSource(UnsignedLongSource source) {
    this.source = source;
  }

  @Override
  public double get(long index) {
    // The UnsignedLongSource cannot actually represent a 1s complement 64bit number so it returns
    // the 2s complement signed version
    long svalue = source.get(index);

    if (svalue < 0) {
      // Mask out the upper most bit and add 2^63 to it
      return POS_THRESHOLD + (svalue & POS_MASK);
    } else {
      // The value is in the range 0 to 2^63-1 so it can be lifted to double without issue
      return svalue;
    }
  }

  @Override
  public long getLength() {
    return source.getLength();
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
  public void set(long index, double value) {
    if (value > POS_THRESHOLD - 1.0) {
      // Cast the lower 63 bits to a long and then add the highest sign bit
      long lower = (long) (value - POS_THRESHOLD);
      source.set(index, (1L << 63) | lower);
    } else {
      // Below the unsigned threshold so we can cast safely
      source.set(index, (long) value);
    }
  }

  @Override
  public UnsignedLongSource getSource() {
    return source;
  }
}
