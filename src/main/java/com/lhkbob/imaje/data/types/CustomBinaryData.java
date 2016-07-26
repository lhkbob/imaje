package com.lhkbob.imaje.data.types;

import com.lhkbob.imaje.data.BitData;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.NumericData;

/**
 *
 */
public class CustomBinaryData<T extends BitData> implements NumericData<T>, DataView<T> {
  private final T source;
  private final BinaryRepresentation converter;

  public CustomBinaryData(BinaryRepresentation bitRep, T source) {
    if (bitRep.getBitSize() != source.getBitSize()) {
      throw new IllegalArgumentException("Binary representation is incompatible with bit size of data source; expected " + bitRep.getBitSize() + " but was " + source.getBitSize());
    }
    this.source = source;
    converter = bitRep;
  }

  public BinaryRepresentation getBinaryRepresentation() {
    return converter;
  }

  @Override
  public T getSource() {
    return source;
  }

  @Override
  public long getLength() {
    return source.getLength();
  }

  @Override
  public double getValue(long index) {
    long bits = source.getBits(index);
    return converter.toNumericValue(bits);
  }

  @Override
  public void setValue(long index, double value) {
    // This assumes the converter handles any clamping to the range of allowable values for the representation
    long bits = converter.toBits(value);
    source.setBits(index, bits);
  }

  @Override
  public T asBitData() {
    return source;
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
}
