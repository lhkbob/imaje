package com.lhkbob.imaje.data;

/**
 *
 */
public interface FloatData extends NumericData<IntData> {
  float get(long index);

  void set(long index, float value);

  @Override
  default int getBitSize() {
    return Float.SIZE;
  }

  default double getValue(long index) {
    return get(index);
  }

  default void setValue(long index, double value) {
    set(index, (float) value);
  }

  @Override
  default IntData asBitData() {
    return new IntData() {
      @Override
      public int get(long index) {
        return Float.floatToIntBits(FloatData.this.get(index));
      }

      @Override
      public void set(long index, int value) {
        FloatData.this.set(index, Float.intBitsToFloat(value));
      }

      @Override
      public long getLength() {
        return FloatData.this.getLength();
      }

      @Override
      public boolean isBigEndian() {
        return FloatData.this.isBigEndian();
      }

      @Override
      public boolean isGPUAccessible() {
        return FloatData.this.isGPUAccessible();
      }
    };
  }
}
