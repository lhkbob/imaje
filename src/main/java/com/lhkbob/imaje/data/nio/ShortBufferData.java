package com.lhkbob.imaje.data.nio;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.ShortData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 *
 */
public class ShortBufferData implements ShortData, DataView<ShortBuffer> {
  private final ShortBuffer buffer;

  public ShortBufferData(int length) {
    this(ByteBuffer.allocateDirect(length << 1).order(ByteOrder.nativeOrder()).asShortBuffer());
  }

  public ShortBufferData(ShortBuffer buffer) {
    Arguments.notNull("buffer", buffer);
    this.buffer = buffer;
  }

  @Override
  public short get(long index) {
    return buffer.get(Math.toIntExact(index));
  }

  @Override
  public ShortBuffer getSource() {
    return buffer;
  }

  @Override
  public long getLength() {
    return buffer.capacity();
  }

  @Override
  public boolean isBigEndian() {
    return buffer.order().equals(ByteOrder.BIG_ENDIAN);
  }

  @Override
  public void set(long index, short value) {
    buffer.put(Math.toIntExact(index), value);
  }

  @Override
  public boolean isGPUAccessible() {
    return buffer.isDirect() && buffer.order().equals(ByteOrder.nativeOrder());
  }

  @Override
  public void set(long dataIndex, short[] values, int offset, int length) {
    // Optimize with bulk put in ShortBuffer
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("ShortBufferData", getLength(), dataIndex, length);

    // Preserve buffer state since we have to manipulate position when making bulk get call
    int oldPos = buffer.position();
    int oldLimit = buffer.limit();

    int bufferOffset = Math.toIntExact(dataIndex);
    buffer.limit(bufferOffset + length).position(bufferOffset);
    buffer.put(values, offset, length);

    // Restore buffer state
    buffer.limit(oldLimit).position(oldPos);
  }

  @Override
  public void set(long dataIndex, ShortBuffer values, int offset, int length) {
    // Optimize with ShortBuffer put
    copy(values, offset, buffer, Math.toIntExact(dataIndex), length);
  }

  @Override
  public void get(long dataIndex, short[] values, int offset, int length) {
    // Optimize with bulk get in ShortBuffer
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("ShortBufferData", getLength(), dataIndex, length);

    // Preserve buffer state since we have to manipulate position when making bulk get call
    int oldPos = buffer.position();
    int oldLimit = buffer.limit();

    int bufferOffset = Math.toIntExact(dataIndex);
    buffer.limit(bufferOffset + length).position(bufferOffset);
    buffer.get(values, offset, length);

    // Restore buffer state
    buffer.limit(oldLimit).position(oldPos);
  }

  @Override
  public void get(long dataIndex, ShortBuffer values, int offset, int length) {
    // Optimize with ShortBuffer put
    copy(buffer, Math.toIntExact(dataIndex), values, offset, length);
  }

  private static void copy(
      ShortBuffer src, int srcOffset, ShortBuffer dst, int dstOffset, int length) {
    Arguments.checkArrayRange("source buffer", src.capacity(), srcOffset, length);
    Arguments.checkArrayRange("dest buffer", dst.capacity(), dstOffset, length);

    // Preserve buffer state since we have to manipulate position when making bulk get call
    int oldSrcPos = src.position();
    int oldSrcLimit = src.limit();

    int oldDstPos = dst.position();
    int oldDstLimit = dst.limit();

    // The bulk put stores all remaining values in src, using relative position of dst, so configure
    // position and limit to match the requested range
    src.limit(srcOffset + length).position(srcOffset);
    dst.limit(dstOffset + length).position(dstOffset);
    dst.put(src);

    // Restore buffer state
    src.limit(oldSrcLimit).position(oldSrcPos);
    dst.limit(oldDstLimit).position(oldDstPos);
  }
}
