package com.lhkbob.imaje.data;

/**
 *
 */
public interface ByteSource extends DataSource<Byte> {
  byte get(long index);

  void set(long index, byte value);

  @Override
  default Byte getElement(long index) {
    return get(index);
  }

  @Override
  default void setElement(long index, Byte value) {
    set(index, value);
  }

  @Override
  default Class<Byte> getBoxedElementType() {
    return Byte.class;
  }
}
