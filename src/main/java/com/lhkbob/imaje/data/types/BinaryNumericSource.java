package com.lhkbob.imaje.data.types;

import com.lhkbob.imaje.data.BitDataSource;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.NumericDataSource;

/**
 *
 */
public class BinaryNumericSource implements NumericDataSource, BitDataSource, DataView<BitDataSource> {
  private final BitDataSource source;
  private final BinaryRepresentation converter;

  public BinaryNumericSource(BinaryRepresentation bitRep, BitDataSource source) {
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
  public long getBits(long index) {
    return source.getBits(index);
  }

  @Override
  public BitDataSource getSource() {
    return source;
  }

  @Override
  public void setBits(long index, long value) {
    source.setBits(index, value);
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
