package com.lhkbob.imaje.data.adapter;

import com.lhkbob.imaje.data.ByteSource;
import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.IntSource;

/**
 *
 */
public class PackedIntToByteSource implements ByteSource, DataView<IntSource> {
  private final IntSource source;
  private final boolean bigEndian;
  private final int indexScale;

  public PackedIntToByteSource(IntSource source, boolean bigEndian, boolean ignoreFourthByte) {
    this.source = source;
    this.bigEndian = bigEndian;
    indexScale = (ignoreFourthByte ? 3 : 4);
  }

  @Override
  public IntSource getSource() {
    return source;
  }

  @Override
  public long getLength() {
    return indexScale * source.getLength();
  }

  @Override
  public byte get(long index) {
    long sourceIndex = index / indexScale;
    int wordByteIndex = (int) (index % indexScale);

    int shift;
    if (bigEndian) {
      shift = 24 - 8 * wordByteIndex;
    } else {
      shift = 8 * wordByteIndex;
    }

    return (byte) (source.get(sourceIndex) >> shift);
  }

  @Override
  public void set(long index, byte value) {
    long sourceIndex = index / indexScale;
    int wordByteIndex = (int) (index % indexScale);

    int shift;
    if (bigEndian) {
      shift = 24 - 8 * wordByteIndex;
    } else {
      shift = 8 * wordByteIndex;
    }

    int word = source.get(sourceIndex);
    int mask = 0xff << shift;
    word = (mask & (value << shift)) | (word & ~mask);
    source.set(sourceIndex, word);
  }
}
