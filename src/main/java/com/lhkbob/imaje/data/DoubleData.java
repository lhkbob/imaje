package com.lhkbob.imaje.data;

/**
 *
 */
public interface DoubleData extends NumericData<LongData> {
  double get(long index);

  void set(long index, double value);

  @Override
  default int getBitSize() {
    return Double.SIZE;
  }

  @Override
  default double getValue(long index) {
    return get(index);
  }

  @Override
  default void setValue(long index, double value) {
    set(index, value);
  }

  @Override
  default LongData asBitData() {
    return new LongData() {
      @Override
      public long get(long index) {
        return Double.doubleToLongBits(DoubleData.this.get(index));
      }

      @Override
      public void set(long index, long value) {
        DoubleData.this.set(index, Double.longBitsToDouble(value));
      }

      @Override
      public long getLength() {
        return DoubleData.this.getLength();
      }

      @Override
      public boolean isBigEndian() {
        return DoubleData.this.isBigEndian();
      }

      @Override
      public boolean isGPUAccessible() {
        return DoubleData.this.isGPUAccessible();
      }
    };
  }
}
