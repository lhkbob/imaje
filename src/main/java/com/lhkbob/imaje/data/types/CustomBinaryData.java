package com.lhkbob.imaje.data.types;

import com.lhkbob.imaje.data.BitData;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.NumericData;
import com.lhkbob.imaje.util.Arguments;

/**
 *
 */
public class CustomBinaryData<T extends BitData> implements NumericData<T>, DataView<T> {
  private final T source;
  private final BinaryRepresentation converter;

  public CustomBinaryData(BinaryRepresentation bitRep, T source) {
    Arguments.equals("bit size", bitRep.getBitSize(), source.getBitSize());
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
