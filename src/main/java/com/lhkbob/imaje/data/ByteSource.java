package com.lhkbob.imaje.data;

/**
 *
 */
public interface ByteSource extends DataSource<Byte> {
  byte get(long index);

  @Override
  default Class<Byte> getBoxedElementType() {
    return Byte.class;
  }

  @Override
  default Byte getElement(long index) {
    return get(index);
  }

  void set(long index, byte value);

  @Override
  default void setElement(long index, Byte value) {
    set(index, value);
  }
}
