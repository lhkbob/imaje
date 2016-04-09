package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.NumericDataSource;

/**
 *
 */
public class NormalizedUnsignedLongSource implements NumericDataSource, DataView<UnsignedLongSource> {
  private static final double TO_LONG_SCALAR = Math.pow(2, 64);
  private static final double TO_DOUBLE_SCALAR = 1.0 / TO_LONG_SCALAR;
  private static final long NEG_MASK = 1L << 63;
  private static final long POS_MASK = ~NEG_MASK;

  private final UnsignedLongSource source;

  public NormalizedUnsignedLongSource(UnsignedLongSource source) {
    this.source = source;
  }

  @Override
  public double getValue(long index) {
    // The UnsignedLongSource cannot actually represent a 1s complement 64bit number so it returns
    // the 2s complement signed version
    long svalue = source.get(index);

    if (svalue < 0) {
      // The value corresponds to the 0.5 to 1.0 normalized range
      // NOTE: since TO_DOUBLE_SCALAR = 2^-64, it's the same as 0.5 * 2^-63, which is what you'd
      // expect for first normalizing the first 63 bits to 0-1 and then scaling to 0-0.5
      return 0.5 + TO_DOUBLE_SCALAR * (svalue & POS_MASK);
    } else {
      // The value corresponds to the 0 to 0.5 normalized range
      return TO_DOUBLE_SCALAR * svalue;
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
  public int getBitSize() {
    return source.getBitSize();
  }

  @Override
  public void setValue(long index, double value) {
    // First clamp to the valid normalization range
    value = Math.max(0.0, Math.min(value, 1.0));

    long uvalue;
    if (value > 0.5) {
      // The value will actually appear to be a negative 2s-complement long
      // NOTE: must also shift left by 0.5 so that the first 63 bits make sense
      uvalue = NEG_MASK | Math.round((value - 0.5) * TO_LONG_SCALAR);
    } else {
      // Simply rescale and round
      uvalue = Math.round(value * TO_LONG_SCALAR);
    }
    source.set(index, uvalue);
  }

  @Override
  public UnsignedLongSource getSource() {
    return source;
  }
}
