package com.lhkbob.imaje.data;

import com.lhkbob.imaje.data.array.ByteArrayData;
import com.lhkbob.imaje.data.array.DoubleArrayData;
import com.lhkbob.imaje.data.array.FloatArrayData;
import com.lhkbob.imaje.data.array.IntArrayData;
import com.lhkbob.imaje.data.array.LongArrayData;
import com.lhkbob.imaje.data.array.ShortArrayData;
import com.lhkbob.imaje.data.large.LargeByteData;
import com.lhkbob.imaje.data.large.LargeDoubleData;
import com.lhkbob.imaje.data.large.LargeFloatData;
import com.lhkbob.imaje.data.large.LargeIntData;
import com.lhkbob.imaje.data.large.LargeLongData;
import com.lhkbob.imaje.data.large.LargeShortData;
import com.lhkbob.imaje.data.nio.ByteBufferData;
import com.lhkbob.imaje.data.nio.DoubleBufferData;
import com.lhkbob.imaje.data.nio.FloatBufferData;
import com.lhkbob.imaje.data.nio.IntBufferData;
import com.lhkbob.imaje.data.nio.LongBufferData;
import com.lhkbob.imaje.data.nio.ShortBufferData;
import com.lhkbob.imaje.data.types.BinaryRepresentation;
import com.lhkbob.imaje.data.types.CustomBinaryData;
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
import java.util.function.Function;

/**
 *
 */
public final class Data {
  public static final BinaryRepresentation SFLOAT16 = new SignedFloatingPointNumber(5, 10);
  @Deprecated public static final BinaryRepresentation SFLOAT32 = new SignedFloatingPointNumber(
      8, 23);
  @Deprecated public static final BinaryRepresentation SFLOAT64 = new Signed64FloatingPointNumber();
  @Deprecated public static final BinaryRepresentation SINT16 = new SignedInteger(16);
  @Deprecated public static final BinaryRepresentation SINT32 = new SignedInteger(32);
  @Deprecated public static final BinaryRepresentation SINT64 = new SignedInteger(64);
  @Deprecated public static final BinaryRepresentation SINT8 = new SignedInteger(8);
  public static final BinaryRepresentation SNORM16 = new SignedNormalizedInteger(16);
  public static final BinaryRepresentation SNORM32 = new SignedNormalizedInteger(32);
  public static final BinaryRepresentation SNORM64 = new SignedNormalizedInteger(64);
  public static final BinaryRepresentation SNORM8 = new SignedNormalizedInteger(8);
  public static final BinaryRepresentation UINT16 = new UnsignedInteger(16);
  public static final BinaryRepresentation UINT32 = new UnsignedInteger(32);
  public static final BinaryRepresentation UINT64 = new UnsignedInteger(64);
  public static final BinaryRepresentation UINT8 = new UnsignedInteger(8);
  public static final BinaryRepresentation UNORM16 = new UnsignedNormalizedInteger(16);
  public static final BinaryRepresentation UNORM32 = new UnsignedNormalizedInteger(32);
  public static final BinaryRepresentation UNORM64 = new UnsignedNormalizedInteger(64);
  public static final BinaryRepresentation UNORM8 = new UnsignedNormalizedInteger(8);

  // FIXME add endianness to file wrapping functions?
  public interface Builder<S extends DataBuffer, A, B extends Buffer> {
    Class<A> getArrayClass();

    Class<B> getBufferClass();

    Class<S> getDataBufferClass();

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

  public static boolean isNativeEndian(DataBuffer source) {
    return isNativeBigEndian() == source.isBigEndian();
  }

  public static Builder<ByteData, byte[], ByteBuffer> newByteData() {
    return new Builder<ByteData, byte[], ByteBuffer>() {
      @Override
      public Class<byte[]> getArrayClass() {
        return byte[].class;
      }

      @Override
      public Class<ByteBuffer> getBufferClass() {
        return ByteBuffer.class;
      }

      @Override
      public Class<ByteData> getDataBufferClass() {
        return ByteData.class;
      }

      @Override
      public ByteData ofArray(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          ByteArrayData[] backingData = new ByteArrayData[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new ByteArrayData(sizes[i]);
          }

          return new LargeByteData(backingData);
        } else {
          return new ByteArrayData((int) length);
        }
      }

      @Override
      public ByteData ofBuffer(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          ByteBufferData[] backingData = new ByteBufferData[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new ByteBufferData(sizes[i]);
          }

          return new LargeByteData(backingData);
        } else {
          return new ByteBufferData((int) length);
        }
      }

      @Override
      public ByteData wrapArray(byte[] array) {
        return new ByteArrayData(array);
      }

      @Override
      public ByteData wrapBuffer(ByteBuffer buffer) {
        return new ByteBufferData(buffer);
      }

      @Override
      public ByteData wrapFile(Path path) throws IOException {
        try (
            FileChannel channel = FileChannel
                .open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
          return wrapFile(channel, 0, channel.size());
        }
      }

      @Override
      public ByteData wrapFile(FileChannel channel, long offset, long length) throws IOException {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          ByteBufferData[] backingData = new ByteBufferData[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, sizes[i]);
            backingData[i] = new ByteBufferData(mapped);
            offset += sizes[i];
          }

          return new LargeByteData(backingData);
        } else {
          MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, length);
          return new ByteBufferData(mapped);
        }
      }
    };
  }

  public static Builder<DoubleData, double[], DoubleBuffer> newDoubleData() {
    return new Builder<DoubleData, double[], DoubleBuffer>() {
      @Override
      public Class<double[]> getArrayClass() {
        return double[].class;
      }

      @Override
      public Class<DoubleBuffer> getBufferClass() {
        return DoubleBuffer.class;
      }

      @Override
      public Class<DoubleData> getDataBufferClass() {
        return DoubleData.class;
      }

      @Override
      public DoubleData ofArray(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          DoubleArrayData[] backingData = new DoubleArrayData[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new DoubleArrayData(sizes[i]);
          }

          return new LargeDoubleData(backingData);
        } else {
          return new DoubleArrayData((int) length);
        }
      }

      @Override
      public DoubleData ofBuffer(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          DoubleBufferData[] backingData = new DoubleBufferData[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new DoubleBufferData(sizes[i]);
          }

          return new LargeDoubleData(backingData);
        } else {
          return new DoubleBufferData((int) length);
        }
      }

      @Override
      public DoubleData wrapArray(double[] array) {
        return new DoubleArrayData(array);
      }

      @Override
      public DoubleData wrapBuffer(DoubleBuffer buffer) {
        return new DoubleBufferData(buffer);
      }

      @Override
      public DoubleData wrapFile(Path path) throws IOException {
        try (
            FileChannel channel = FileChannel
                .open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
          return wrapFile(channel, 0, channel.size());
        }
      }

      @Override
      public DoubleData wrapFile(FileChannel channel, long offset, long length) throws IOException {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          DoubleData[] backingData = new DoubleData[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, sizes[i]);
            backingData[i] = new DoubleBufferData(mapped.asDoubleBuffer());
            offset += sizes[i];
          }

          return new LargeDoubleData(backingData);
        } else {
          MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, length);
          return new DoubleBufferData(mapped.asDoubleBuffer());
        }
      }
    };
  }

  public static Builder<FloatData, float[], FloatBuffer> newFloatSource() {
    return new Builder<FloatData, float[], FloatBuffer>() {
      @Override
      public Class<float[]> getArrayClass() {
        return float[].class;
      }

      @Override
      public Class<FloatBuffer> getBufferClass() {
        return FloatBuffer.class;
      }

      @Override
      public Class<FloatData> getDataBufferClass() {
        return FloatData.class;
      }

      @Override
      public FloatData ofArray(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          FloatArrayData[] backingData = new FloatArrayData[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new FloatArrayData(sizes[i]);
          }

          return new LargeFloatData(backingData);
        } else {
          return new FloatArrayData((int) length);
        }
      }

      @Override
      public FloatData ofBuffer(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          FloatBufferData[] backingData = new FloatBufferData[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new FloatBufferData(sizes[i]);
          }

          return new LargeFloatData(backingData);
        } else {
          return new FloatBufferData((int) length);
        }
      }

      @Override
      public FloatData wrapArray(float[] array) {
        return new FloatArrayData(array);
      }

      @Override
      public FloatData wrapBuffer(FloatBuffer buffer) {
        return new FloatBufferData(buffer);
      }

      @Override
      public FloatData wrapFile(Path path) throws IOException {
        try (
            FileChannel channel = FileChannel
                .open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
          return wrapFile(channel, 0, channel.size());
        }
      }

      @Override
      public FloatData wrapFile(FileChannel channel, long offset, long length) throws IOException {
        boolean bigEndian = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);

        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          FloatData[] backingData = new FloatData[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, sizes[i]);
            backingData[i] = new FloatBufferData(mapped.asFloatBuffer());
            offset += sizes[i];
          }

          return new LargeFloatData(backingData);
        } else {
          MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, length);
          return new FloatBufferData(mapped.asFloatBuffer());
        }
      }
    };
  }

  public static Builder<IntData, int[], IntBuffer> newIntData() {
    return new Builder<IntData, int[], IntBuffer>() {
      @Override
      public Class<int[]> getArrayClass() {
        return int[].class;
      }

      @Override
      public Class<IntBuffer> getBufferClass() {
        return IntBuffer.class;
      }

      @Override
      public Class<IntData> getDataBufferClass() {
        return IntData.class;
      }

      @Override
      public IntData ofArray(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          IntArrayData[] backingData = new IntArrayData[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new IntArrayData(sizes[i]);
          }

          return new LargeIntData(backingData);
        } else {
          return new IntArrayData((int) length);
        }
      }

      @Override
      public IntData ofBuffer(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          IntBufferData[] backingData = new IntBufferData[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new IntBufferData(sizes[i]);
          }

          return new LargeIntData(backingData);
        } else {
          return new IntBufferData((int) length);
        }
      }

      @Override
      public IntData wrapArray(int[] array) {
        return new IntArrayData(array);
      }

      @Override
      public IntData wrapBuffer(IntBuffer buffer) {
        return new IntBufferData(buffer);
      }

      @Override
      public IntData wrapFile(Path path) throws IOException {
        try (
            FileChannel channel = FileChannel
                .open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
          return wrapFile(channel, 0, channel.size());
        }
      }

      @Override
      public IntData wrapFile(FileChannel channel, long offset, long length) throws IOException {
        boolean bigEndian = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);

        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          IntData[] backingData = new IntData[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, sizes[i]);
            backingData[i] = new IntBufferData(mapped.asIntBuffer());
            offset += sizes[i];
          }

          return new LargeIntData(backingData);
        } else {
          MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, length);
          return new IntBufferData(mapped.asIntBuffer());
        }
      }
    };
  }

  public static Builder<LongData, long[], LongBuffer> newLongData() {
    return new Builder<LongData, long[], LongBuffer>() {
      @Override
      public Class<long[]> getArrayClass() {
        return long[].class;
      }

      @Override
      public Class<LongBuffer> getBufferClass() {
        return LongBuffer.class;
      }

      @Override
      public Class<LongData> getDataBufferClass() {
        return LongData.class;
      }

      @Override
      public LongData ofArray(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          LongArrayData[] backingData = new LongArrayData[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new LongArrayData(sizes[i]);
          }

          return new LargeLongData(backingData);
        } else {
          return new LongArrayData((int) length);
        }
      }

      @Override
      public LongData ofBuffer(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          LongBufferData[] backingData = new LongBufferData[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new LongBufferData(sizes[i]);
          }

          return new LargeLongData(backingData);
        } else {
          return new LongBufferData((int) length);
        }
      }

      @Override
      public LongData wrapArray(long[] array) {
        return new LongArrayData(array);
      }

      @Override
      public LongData wrapBuffer(LongBuffer buffer) {
        return new LongBufferData(buffer);
      }

      @Override
      public LongData wrapFile(Path path) throws IOException {
        try (
            FileChannel channel = FileChannel
                .open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
          return wrapFile(channel, 0, channel.size());
        }
      }

      @Override
      public LongData wrapFile(FileChannel channel, long offset, long length) throws IOException {
        boolean bigEndian = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);

        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          LongData[] backingData = new LongData[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, sizes[i]);
            backingData[i] = new LongBufferData(mapped.asLongBuffer());
            offset += sizes[i];
          }

          return new LargeLongData(backingData);
        } else {
          MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, length);
          return new LongBufferData(mapped.asLongBuffer());
        }
      }
    };
  }

  public static Builder<ShortData, short[], ShortBuffer> newShortData() {
    return new Builder<ShortData, short[], ShortBuffer>() {
      @Override
      public Class<short[]> getArrayClass() {
        return short[].class;
      }

      @Override
      public Class<ShortBuffer> getBufferClass() {
        return ShortBuffer.class;
      }

      @Override
      public Class<ShortData> getDataBufferClass() {
        return ShortData.class;
      }

      @Override
      public ShortData ofArray(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          ShortArrayData[] backingData = new ShortArrayData[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new ShortArrayData(sizes[i]);
          }

          return new LargeShortData(backingData);
        } else {
          return new ShortArrayData((int) length);
        }
      }

      @Override
      public ShortData ofBuffer(long length) {
        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          ShortBufferData[] backingData = new ShortBufferData[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            backingData[i] = new ShortBufferData(sizes[i]);
          }

          return new LargeShortData(backingData);
        } else {
          return new ShortBufferData((int) length);
        }
      }

      @Override
      public ShortData wrapArray(short[] array) {
        return new ShortArrayData(array);
      }

      @Override
      public ShortData wrapBuffer(ShortBuffer buffer) {
        return new ShortBufferData(buffer);
      }

      @Override
      public ShortData wrapFile(Path path) throws IOException {
        try (
            FileChannel channel = FileChannel
                .open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
          return wrapFile(channel, 0, channel.size());
        }
      }

      @Override
      public ShortData wrapFile(FileChannel channel, long offset, long length) throws IOException {
        boolean bigEndian = ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);

        if (length > MAX_ARRAY_SIZE) {
          int[] sizes = getLargeSourceSizes(length);
          ShortData[] backingData = new ShortData[sizes.length];
          for (int i = 0; i < sizes.length; i++) {
            MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, sizes[i]);
            backingData[i] = new ShortBufferData(mapped.asShortBuffer());
            offset += sizes[i];
          }

          return new LargeShortData(backingData);
        } else {
          MappedByteBuffer mapped = channel.map(FileChannel.MapMode.PRIVATE, offset, length);
          return new ShortBufferData(mapped.asShortBuffer());
        }
      }
    };
  }

  public static Builder<? extends NumericData<ShortData>, short[], ShortBuffer> sfloat16() {
    return new BinaryBuilder<>(newShortData(), SFLOAT16);
  }

  public static Builder<? extends NumericData<IntData>, float[], FloatBuffer> sfloat32() {
    return newFloatSource();
  }

  public static Builder<? extends NumericData<LongData>, double[], DoubleBuffer> sfloat64() {
    return newDoubleData();
  }

  public static Builder<? extends NumericData<ShortData>, short[], ShortBuffer> sint16() {
    return new NumericBuilder<>(ShortData.Numeric.class, ShortData.Numeric::new, newShortData());
  }

  public static Builder<? extends NumericData<IntData>, int[], IntBuffer> sint32() {
    return new NumericBuilder<>(IntData.Numeric.class, IntData.Numeric::new, newIntData());
  }

  public static Builder<? extends NumericData<LongData>, long[], LongBuffer> sint64() {
    return new NumericBuilder<>(LongData.Numeric.class, LongData.Numeric::new, newLongData());
  }

  public static Builder<? extends NumericData<ByteData>, byte[], ByteBuffer> sint8() {
    return new NumericBuilder<>(ByteData.Numeric.class, ByteData.Numeric::new, newByteData());
  }

  public static Builder<? extends NumericData<ShortData>, short[], ShortBuffer> snorm16() {
    return new BinaryBuilder<>(newShortData(), SNORM16);
  }

  public static Builder<? extends NumericData<IntData>, int[], IntBuffer> snorm32() {
    return new BinaryBuilder<>(newIntData(), SNORM32);
  }

  public static Builder<? extends NumericData<LongData>, long[], LongBuffer> snorm64() {
    return new BinaryBuilder<>(newLongData(), SNORM64);
  }

  public static Builder<? extends NumericData<ByteData>, byte[], ByteBuffer> snorm8() {
    return new BinaryBuilder<>(newByteData(), SNORM8);
  }

  public static Builder<? extends NumericData<ShortData>, short[], ShortBuffer> uint16() {
    return new BinaryBuilder<>(newShortData(), UINT16);
  }

  public static Builder<? extends NumericData<IntData>, int[], IntBuffer> uint32() {
    return new BinaryBuilder<>(newIntData(), UINT32);
  }

  public static Builder<? extends NumericData<LongData>, long[], LongBuffer> uint64() {
    return new BinaryBuilder<>(newLongData(), UINT64);
  }

  public static Builder<? extends NumericData<ByteData>, byte[], ByteBuffer> uint8() {
    return new BinaryBuilder<>(newByteData(), UINT8);
  }

  public static Builder<? extends NumericData<ShortData>, short[], ShortBuffer> unorm16() {
    return new BinaryBuilder<>(newShortData(), UNORM16);
  }

  public static Builder<? extends NumericData<IntData>, int[], IntBuffer> unorm32() {
    return new BinaryBuilder<>(newIntData(), UNORM32);
  }

  public static Builder<? extends NumericData<LongData>, long[], LongBuffer> unorm64() {
    return new BinaryBuilder<>(newLongData(), UNORM64);
  }

  public static Builder<? extends NumericData<ByteData>, byte[], ByteBuffer> unorm8() {
    return new BinaryBuilder<>(newByteData(), UNORM8);
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

  private static class BinaryBuilder<I extends BitData, A, B extends Buffer> implements Builder<CustomBinaryData<I>, A, B> {
    private final BinaryRepresentation bitRep;
    private final Builder<I, A, B> source;

    public BinaryBuilder(Builder<I, A, B> source, BinaryRepresentation bitRep) {
      this.source = source;
      this.bitRep = bitRep;
    }

    @Override
    public Class<A> getArrayClass() {
      return source.getArrayClass();
    }

    @Override
    public Class<B> getBufferClass() {
      return source.getBufferClass();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<CustomBinaryData<I>> getDataBufferClass() {
      return (Class) CustomBinaryData.class;
    }

    @Override
    public CustomBinaryData<I> ofArray(long length) {
      return new CustomBinaryData<>(bitRep, source.ofArray(length));
    }

    @Override
    public CustomBinaryData<I> ofBuffer(long length) {
      return new CustomBinaryData<>(bitRep, source.ofBuffer(length));
    }

    @Override
    public CustomBinaryData<I> wrapArray(A array) {
      return new CustomBinaryData<>(bitRep, source.wrapArray(array));
    }

    @Override
    public CustomBinaryData<I> wrapBuffer(B buffer) {
      return new CustomBinaryData<>(bitRep, source.wrapBuffer(buffer));
    }

    @Override
    public CustomBinaryData<I> wrapFile(Path path) throws IOException {
      return new CustomBinaryData<>(bitRep, source.wrapFile(path));
    }

    @Override
    public CustomBinaryData<I> wrapFile(FileChannel channel, long offset, long length) throws
        IOException {
      return new CustomBinaryData<>(bitRep, source.wrapFile(channel, offset, length));
    }
  }

  private static class NumericBuilder<I extends BitData, N extends NumericData<I>, A, B extends Buffer> implements Builder<N, A, B> {
    private final Function<I, N> ctor;
    private final Class<N> dataClass;
    private final Builder<I, A, B> source;

    public NumericBuilder(Class<N> dataClass, Function<I, N> ctor, Builder<I, A, B> source) {
      this.dataClass = dataClass;
      this.ctor = ctor;
      this.source = source;
    }

    @Override
    public Class<A> getArrayClass() {
      return source.getArrayClass();
    }

    @Override
    public Class<B> getBufferClass() {
      return source.getBufferClass();
    }

    @Override
    public Class<N> getDataBufferClass() {
      return dataClass;
    }

    @Override
    public N ofArray(long length) {
      return ctor.apply(source.ofArray(length));
    }

    @Override
    public N ofBuffer(long length) {
      return ctor.apply(source.ofBuffer(length));
    }

    @Override
    public N wrapArray(A array) {
      return ctor.apply(source.wrapArray(array));
    }

    @Override
    public N wrapBuffer(B buffer) {
      return ctor.apply(source.wrapBuffer(buffer));
    }

    @Override
    public N wrapFile(Path path) throws IOException {
      return ctor.apply(source.wrapFile(path));
    }

    @Override
    public N wrapFile(FileChannel channel, long offset, long length) throws IOException {
      return ctor.apply(source.wrapFile(channel, offset, length));
    }
  }
}
