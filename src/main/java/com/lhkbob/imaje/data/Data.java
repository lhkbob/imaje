package com.lhkbob.imaje.data;

import com.lhkbob.imaje.data.array.ByteArray;
import com.lhkbob.imaje.data.array.DoubleArray;
import com.lhkbob.imaje.data.array.FloatArray;
import com.lhkbob.imaje.data.array.IntArray;
import com.lhkbob.imaje.data.array.LongArray;
import com.lhkbob.imaje.data.array.ShortArray;
import com.lhkbob.imaje.data.large.LargeByteSource;
import com.lhkbob.imaje.data.large.LargeDoubleSource;
import com.lhkbob.imaje.data.large.LargeFloatSource;
import com.lhkbob.imaje.data.large.LargeIntSource;
import com.lhkbob.imaje.data.large.LargeLongSource;
import com.lhkbob.imaje.data.large.LargeShortSource;
import com.lhkbob.imaje.data.multi.MultiByteToDoubleSource;
import com.lhkbob.imaje.data.multi.MultiByteToFloatSource;
import com.lhkbob.imaje.data.multi.MultiByteToIntSource;
import com.lhkbob.imaje.data.multi.MultiByteToLongSource;
import com.lhkbob.imaje.data.multi.MultiByteToShortSource;
import com.lhkbob.imaje.data.nio.ByteBufferSource;
import com.lhkbob.imaje.data.nio.DoubleBufferSource;
import com.lhkbob.imaje.data.nio.FloatBufferSource;
import com.lhkbob.imaje.data.nio.IntBufferSource;
import com.lhkbob.imaje.data.nio.LongBufferSource;
import com.lhkbob.imaje.data.nio.ShortBufferSource;
import com.lhkbob.imaje.data.types.BinaryNumericSource;
import com.lhkbob.imaje.data.types.BinaryRepresentation;
import com.lhkbob.imaje.data.types.Signed64FloatingPointNumber;
import com.lhkbob.imaje.data.types.SignedFloatingPointNumber;
import com.lhkbob.imaje.data.types.SignedInteger;
import com.lhkbob.imaje.data.types.SignedNormalizedInteger;
import com.lhkbob.imaje.data.types.UnsignedInteger;
import com.lhkbob.imaje.data.types.UnsignedNormalizedInteger;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 *
 */
public final class Data {
  public static final BinaryRepresentation UINT8 = new UnsignedInteger(8);
  public static final BinaryRepresentation UINT16 = new UnsignedInteger(16);
  public static final BinaryRepresentation UINT32 = new UnsignedInteger(32);
  public static final BinaryRepresentation UINT64 = new UnsignedInteger(64);

  public static final BinaryRepresentation UNORM8 = new UnsignedNormalizedInteger(8);
  public static final BinaryRepresentation UNORM16 = new UnsignedNormalizedInteger(16);
  public static final BinaryRepresentation UNORM32 = new UnsignedNormalizedInteger(32);
  public static final BinaryRepresentation UNORM64 = new UnsignedNormalizedInteger(64);

  @Deprecated public static final BinaryRepresentation SINT8 = new SignedInteger(8);
  @Deprecated public static final BinaryRepresentation SINT16 = new SignedInteger(16);
  @Deprecated public static final BinaryRepresentation SINT32 = new SignedInteger(32);
  @Deprecated public static final BinaryRepresentation SINT64 = new SignedInteger(64);

  public static final BinaryRepresentation SNORM8 = new SignedNormalizedInteger(8);
  public static final BinaryRepresentation SNORM16 = new SignedNormalizedInteger(16);
  public static final BinaryRepresentation SNORM32 = new SignedNormalizedInteger(32);
  public static final BinaryRepresentation SNORM64 = new SignedNormalizedInteger(64);

  public static final BinaryRepresentation SFLOAT16 = new SignedFloatingPointNumber(5, 10);
  @Deprecated public static final BinaryRepresentation SFLOAT32 = new SignedFloatingPointNumber(
      8, 23);
  @Deprecated public static final BinaryRepresentation SFLOAT64 = new Signed64FloatingPointNumber();

  public interface Builder<S, A, B extends Buffer> {
    S ofArray(long length);

    S ofBuffer(long length);

    S wrapArray(A array);

    S wrapBuffer(B buffer);

    S wrapFile(Path path) throws IOException;

    S wrapFile(FileChannel channel, long offset, long length) throws IOException;
  }

  private Data() {}

  public static Object getViewedData(Object data) {
    while (data instanceof DataView) {
      data = ((DataView<?>) data).getSource();
    }
    return data;
  }

  public static boolean isNativeBigEndian() {
    return ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);
  }

  public static boolean isNativeEndian(DataSource source) {
    return isNativeBigEndian() == source.isBigEndian();
  }

  public static Builder<ByteSource, byte[], ByteBuffer> newByteSource() {
    return new Builder<ByteSource, byte[], ByteBuffer>() {
      @Override
      public ByteSource ofArray(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          ByteArray[] backingData = new ByteArray[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new ByteArray(sizes[i]);
          }

          return new LargeByteSource(backingData);
        } else {
          return new ByteArray((int) length);
        }
      }

      @Override
      public ByteSource ofBuffer(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          ByteBufferSource[] backingData = new ByteBufferSource[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new ByteBufferSource(sizes[i]);
          }

          return new LargeByteSource(backingData);
        } else {
          return new ByteBufferSource((int) length);
        }
      }

      @Override
      public ByteSource wrapArray(byte[] array) {
        return new ByteArray(array);
      }

      @Override
      public ByteSource wrapBuffer(ByteBuffer buffer) {
        return new ByteBufferSource(buffer);
      }

      @Override
      public ByteSource wrapFile(Path path) throws IOException {
        try (
            FileChannel channel = FileChannel
                .open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
          return wrapFile(channel, 0, channel.size());
        }
      }

      @Override
      public ByteSource wrapFile(FileChannel channel, long offset, long length) throws IOException {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          ByteBufferSource[] backingData = new ByteBufferSource[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, sizes[i]);
            backingData[i] = new ByteBufferSource(mapped);
            offset += sizes[i];
          }

          return new LargeByteSource(backingData);
        } else {
          MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, length);
          return new ByteBufferSource(mapped);
        }
      }
    };
  }

  public static Builder<DoubleSource, double[], DoubleBuffer> newDoubleSource() {
    return new Builder<DoubleSource, double[], DoubleBuffer>() {
      @Override
      public DoubleSource ofArray(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          DoubleArray[] backingData = new DoubleArray[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new DoubleArray(sizes[i]);
          }

          return new LargeDoubleSource(backingData);
        } else {
          return new DoubleArray((int) length);
        }
      }

      @Override
      public DoubleSource ofBuffer(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          DoubleBufferSource[] backingData = new DoubleBufferSource[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new DoubleBufferSource(sizes[i]);
          }

          return new LargeDoubleSource(backingData);
        } else {
          return new DoubleBufferSource((int) length);
        }
      }

      @Override
      public DoubleSource wrapArray(double[] array) {
        return new DoubleArray(array);
      }

      @Override
      public DoubleSource wrapBuffer(DoubleBuffer buffer) {
        return new DoubleBufferSource(buffer);
      }

      @Override
      public DoubleSource wrapFile(Path path) throws IOException {
        try (
            FileChannel channel = FileChannel
                .open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
          return wrapFile(channel, 0, channel.size());
        }
      }

      @Override
      public DoubleSource wrapFile(FileChannel channel, long offset, long length) throws
          IOException {
        boolean bigEndian = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);

        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          MultiByteToDoubleSource[] backingData = new MultiByteToDoubleSource[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, sizes[i]);
            backingData[i] = new MultiByteToDoubleSource(new ByteBufferSource(mapped), bigEndian);
            offset += sizes[i];
          }

          return new LargeDoubleSource(backingData);
        } else {
          MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, length);
          return new MultiByteToDoubleSource(new ByteBufferSource(mapped), bigEndian);
        }
      }
    };
  }

  public static Builder<FloatSource, float[], FloatBuffer> newFloatSource() {
    return new Builder<FloatSource, float[], FloatBuffer>() {
      @Override
      public FloatSource ofArray(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          FloatArray[] backingData = new FloatArray[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new FloatArray(sizes[i]);
          }

          return new LargeFloatSource(backingData);
        } else {
          return new FloatArray((int) length);
        }
      }

      @Override
      public FloatSource ofBuffer(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          FloatBufferSource[] backingData = new FloatBufferSource[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new FloatBufferSource(sizes[i]);
          }

          return new LargeFloatSource(backingData);
        } else {
          return new FloatBufferSource((int) length);
        }
      }

      @Override
      public FloatSource wrapArray(float[] array) {
        return new FloatArray(array);
      }

      @Override
      public FloatSource wrapBuffer(FloatBuffer buffer) {
        return new FloatBufferSource(buffer);
      }

      @Override
      public FloatSource wrapFile(Path path) throws IOException {
        try (
            FileChannel channel = FileChannel
                .open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
          return wrapFile(channel, 0, channel.size());
        }
      }

      @Override
      public FloatSource wrapFile(FileChannel channel, long offset, long length) throws
          IOException {
        boolean bigEndian = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);

        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          MultiByteToFloatSource[] backingData = new MultiByteToFloatSource[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, sizes[i]);
            backingData[i] = new MultiByteToFloatSource(new ByteBufferSource(mapped), bigEndian);
            offset += sizes[i];
          }

          return new LargeFloatSource(backingData);
        } else {
          MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, length);
          return new MultiByteToFloatSource(new ByteBufferSource(mapped), bigEndian);
        }
      }
    };
  }

  public static Builder<IntSource, int[], IntBuffer> newIntSource() {
    return new Builder<IntSource, int[], IntBuffer>() {
      @Override
      public IntSource ofArray(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          IntArray[] backingData = new IntArray[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new IntArray(sizes[i]);
          }

          return new LargeIntSource(backingData);
        } else {
          return new IntArray((int) length);
        }
      }

      @Override
      public IntSource ofBuffer(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          IntBufferSource[] backingData = new IntBufferSource[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new IntBufferSource(sizes[i]);
          }

          return new LargeIntSource(backingData);
        } else {
          return new IntBufferSource((int) length);
        }
      }

      @Override
      public IntSource wrapArray(int[] array) {
        return new IntArray(array);
      }

      @Override
      public IntSource wrapBuffer(IntBuffer buffer) {
        return new IntBufferSource(buffer);
      }

      @Override
      public IntSource wrapFile(Path path) throws IOException {
        try (
            FileChannel channel = FileChannel
                .open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
          return wrapFile(channel, 0, channel.size());
        }
      }

      @Override
      public IntSource wrapFile(FileChannel channel, long offset, long length) throws IOException {
        boolean bigEndian = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);

        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          MultiByteToIntSource[] backingData = new MultiByteToIntSource[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, sizes[i]);
            backingData[i] = new MultiByteToIntSource(new ByteBufferSource(mapped), bigEndian);
            offset += sizes[i];
          }

          return new LargeIntSource(backingData);
        } else {
          MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, length);
          return new MultiByteToIntSource(new ByteBufferSource(mapped), bigEndian);
        }
      }
    };
  }

  public static Builder<LongSource, long[], LongBuffer> newLongSource() {
    return new Builder<LongSource, long[], LongBuffer>() {
      @Override
      public LongSource ofArray(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          LongArray[] backingData = new LongArray[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new LongArray(sizes[i]);
          }

          return new LargeLongSource(backingData);
        } else {
          return new LongArray((int) length);
        }
      }

      @Override
      public LongSource ofBuffer(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          LongBufferSource[] backingData = new LongBufferSource[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new LongBufferSource(sizes[i]);
          }

          return new LargeLongSource(backingData);
        } else {
          return new LongBufferSource((int) length);
        }
      }

      @Override
      public LongSource wrapArray(long[] array) {
        return new LongArray(array);
      }

      @Override
      public LongSource wrapBuffer(LongBuffer buffer) {
        return new LongBufferSource(buffer);
      }

      @Override
      public LongSource wrapFile(Path path) throws IOException {
        try (
            FileChannel channel = FileChannel
                .open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
          return wrapFile(channel, 0, channel.size());
        }
      }

      @Override
      public LongSource wrapFile(FileChannel channel, long offset, long length) throws IOException {
        boolean bigEndian = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);

        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          MultiByteToLongSource[] backingData = new MultiByteToLongSource[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, sizes[i]);
            backingData[i] = new MultiByteToLongSource(new ByteBufferSource(mapped), bigEndian);
            offset += sizes[i];
          }

          return new LargeLongSource(backingData);
        } else {
          MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, length);
          return new MultiByteToLongSource(new ByteBufferSource(mapped), bigEndian);
        }
      }
    };
  }

  public static Builder<ShortSource, short[], ShortBuffer> newShortSource() {
    return new Builder<ShortSource, short[], ShortBuffer>() {
      @Override
      public ShortSource ofArray(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          ShortArray[] backingData = new ShortArray[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new ShortArray(sizes[i]);
          }

          return new LargeShortSource(backingData);
        } else {
          return new ShortArray((int) length);
        }
      }

      @Override
      public ShortSource ofBuffer(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          ShortBufferSource[] backingData = new ShortBufferSource[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new ShortBufferSource(sizes[i]);
          }

          return new LargeShortSource(backingData);
        } else {
          return new ShortBufferSource((int) length);
        }
      }

      @Override
      public ShortSource wrapArray(short[] array) {
        return new ShortArray(array);
      }

      @Override
      public ShortSource wrapBuffer(ShortBuffer buffer) {
        return new ShortBufferSource(buffer);
      }

      @Override
      public ShortSource wrapFile(Path path) throws IOException {
        try (
            FileChannel channel = FileChannel
                .open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
          return wrapFile(channel, 0, channel.size());
        }
      }

      @Override
      public ShortSource wrapFile(FileChannel channel, long offset, long length) throws
          IOException {
        boolean bigEndian = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);

        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          MultiByteToShortSource[] backingData = new MultiByteToShortSource[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, sizes[i]);
            backingData[i] = new MultiByteToShortSource(new ByteBufferSource(mapped), bigEndian);
            offset += sizes[i];
          }

          return new LargeShortSource(backingData);
        } else {
          MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, length);
          return new MultiByteToShortSource(new ByteBufferSource(mapped), bigEndian);
        }
      }
    };
  }

  public static Builder<? extends NumericDataSource, short[], ShortBuffer> sfloat16() {
    return new BinaryBuilder<>(newShortSource(), SFLOAT16);
  }

  public static Builder<? extends NumericDataSource, float[], FloatBuffer> sfloat32() {
    return newFloatSource();
  }


  public static Builder<? extends NumericDataSource, double[], DoubleBuffer> sfloat64() {
    return newDoubleSource();
  }

  public static Builder<? extends NumericDataSource, short[], ShortBuffer> sint16() {
    return newShortSource();
  }

  public static Builder<? extends NumericDataSource, int[], IntBuffer> sint32() {
    return newIntSource();
  }

  public static Builder<? extends NumericDataSource, long[], LongBuffer> sint64() {
    return newLongSource();
  }

  public static Builder<? extends NumericDataSource, byte[], ByteBuffer> sint8() {
    return newByteSource();
  }

  public static Builder<? extends NumericDataSource, short[], ShortBuffer> snorm16() {
    return new BinaryBuilder<>(newShortSource(), SNORM16);
  }

  public static Builder<? extends NumericDataSource, int[], IntBuffer> snorm32() {
    return new BinaryBuilder<>(newIntSource(), SNORM32);
  }

  public static Builder<? extends NumericDataSource, long[], LongBuffer> snorm64() {
    return new BinaryBuilder<>(newLongSource(), SNORM64);
  }

  public static Builder<? extends NumericDataSource, byte[], ByteBuffer> snorm8() {
    return new BinaryBuilder<>(newByteSource(), SNORM8);
  }

  public static Builder<? extends NumericDataSource, short[], ShortBuffer> uint16() {
    return new BinaryBuilder<>(newShortSource(), UINT16);
  }

  public static Builder<? extends NumericDataSource, int[], IntBuffer> uint32() {
    return new BinaryBuilder<>(newIntSource(), UINT32);
  }

  public static Builder<? extends NumericDataSource, long[], LongBuffer> uint64() {
    return new BinaryBuilder<>(newLongSource(), UINT64);
  }

  public static Builder<? extends NumericDataSource, byte[], ByteBuffer> uint8() {
    return new BinaryBuilder<>(newByteSource(), UINT8);
  }

  public static Builder<? extends NumericDataSource, short[], ShortBuffer> unorm16() {
    return new BinaryBuilder<>(newShortSource(), UNORM16);
  }

  public static Builder<? extends NumericDataSource, int[], IntBuffer> unorm32() {
    return new BinaryBuilder<>(newIntSource(), UNORM32);
  }

  public static Builder<? extends NumericDataSource, long[], LongBuffer> unorm64() {
    return new BinaryBuilder<>(newLongSource(), UNORM64);
  }

  public static Builder<? extends NumericDataSource, byte[], ByteBuffer> unorm8() {
    return new BinaryBuilder<>(newByteSource(), UNORM8);
  }

  private static int[] getLargeSourceSizes(long length) {
    if (length <= MAX_ARRAY_SIZE) {
      // Ideally shouldn't be called with this since there's no need to wrap with a large source,
      // but it shouldn't fail either.
      return new int[] { (int) length };
    } else {
      long numMaxSizedArrays = length / MAX_ARRAY_SIZE;
      long remainderArray = length % MAX_ARRAY_SIZE;
      int[] sizes = new int[Math.toIntExact(numMaxSizedArrays) + (remainderArray > 0 ? 1 : 0)];

      for (int i = 0; i < numMaxSizedArrays; i++) {
        sizes[i] = MAX_ARRAY_SIZE;
      }
      if (remainderArray > 0) {
        sizes[sizes.length - 1] = Math.toIntExact(remainderArray);
      }

      return sizes;
    }
  }

  private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE >> 1;

  private static class BinaryBuilder<I extends BitDataSource, A, B extends Buffer> implements Builder<BinaryNumericSource, A, B> {
    private final Builder<I, A, B> source;
    private final BinaryRepresentation bitRep;

    public BinaryBuilder(Builder<I, A, B> source, BinaryRepresentation bitRep) {
      this.source = source;
      this.bitRep = bitRep;
    }

    @Override
    public BinaryNumericSource ofArray(long length) {
      return new BinaryNumericSource(bitRep, source.ofArray(length));
    }

    @Override
    public BinaryNumericSource ofBuffer(long length) {
      return new BinaryNumericSource(bitRep, source.ofBuffer(length));
    }

    @Override
    public BinaryNumericSource wrapArray(A array) {
      return new BinaryNumericSource(bitRep, source.wrapArray(array));
    }

    @Override
    public BinaryNumericSource wrapBuffer(B buffer) {
      return new BinaryNumericSource(bitRep, source.wrapBuffer(buffer));
    }

    @Override
    public BinaryNumericSource wrapFile(Path path) throws IOException {
      return new BinaryNumericSource(bitRep, source.wrapFile(path));
    }

    @Override
    public BinaryNumericSource wrapFile(FileChannel channel, long offset, long length) throws
        IOException {
      return new BinaryNumericSource(bitRep, source.wrapFile(channel, offset, length));
    }
  }
}
