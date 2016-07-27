package com.lhkbob.imaje.data.nio;

import com.lhkbob.imaje.data.DataView;
import com.lhkbob.imaje.data.IntData;
import com.lhkbob.imaje.util.Arguments;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

/**
 *
 */
public class IntBufferData implements IntData, DataView<IntBuffer> {
  private final IntBuffer buffer;

  public IntBufferData(int length) {
    this(ByteBuffer.allocateDirect(length << 2).order(ByteOrder.nativeOrder()).asIntBuffer());
  }

  public IntBufferData(IntBuffer buffer) {
    Arguments.notNull("buffer", buffer);
    this.buffer = buffer;
  }

  @Override
  public int get(long index) {
    return buffer.get(Math.toIntExact(index));
  }

  @Override
  public IntBuffer getSource() {
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
  public void set(long index, int value) {
    buffer.put(Math.toIntExact(index), value);
  }

  @Override
  public boolean isGPUAccessible() {
    return buffer.isDirect() && buffer.order().equals(ByteOrder.nativeOrder());
  }

  @Override
  public void set(long dataIndex, int[] values, int offset, int length) {
    // Optimize with bulk put in IntBuffer
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("IntBufferData", getLength(), dataIndex, length);

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
  public void set(long dataIndex, IntBuffer values, int offset, int length) {
    // Optimize with IntBuffer put
    copy(values, offset, buffer, Math.toIntExact(dataIndex), length);
  }

  @Override
  public void get(long dataIndex, int[] values, int offset, int length) {
    // Optimize with bulk get in IntBuffer
    Arguments.checkArrayRange("values array", values.length, offset, length);
    Arguments.checkArrayRange("IntBufferData", getLength(), dataIndex, length);

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
  public void get(long dataIndex, IntBuffer values, int offset, int length) {
    // Optimize with IntBuffer put
    copy(buffer, Math.toIntExact(dataIndex), values, offset, length);
  }

  private static void copy(
      IntBuffer src, int srcOffset, IntBuffer dst, int dstOffset, int length) {
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
