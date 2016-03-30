package com.lhkbob.imaje.data;

import com.lhkbob.imaje.data.adapter.ByteToDoubleSource;
import com.lhkbob.imaje.data.adapter.FloatToDoubleSource;
import com.lhkbob.imaje.data.adapter.HalfFloatSource;
import com.lhkbob.imaje.data.adapter.IntToDoubleSource;
import com.lhkbob.imaje.data.adapter.LongToDoubleSource;
import com.lhkbob.imaje.data.adapter.MultiByteToDoubleSource;
import com.lhkbob.imaje.data.adapter.MultiByteToFloatSource;
import com.lhkbob.imaje.data.adapter.MultiByteToIntSource;
import com.lhkbob.imaje.data.adapter.MultiByteToLongSource;
import com.lhkbob.imaje.data.adapter.MultiByteToShortSource;
import com.lhkbob.imaje.data.adapter.NormalizedByteSource;
import com.lhkbob.imaje.data.adapter.NormalizedIntSource;
import com.lhkbob.imaje.data.adapter.NormalizedLongSource;
import com.lhkbob.imaje.data.adapter.NormalizedShortSource;
import com.lhkbob.imaje.data.adapter.NormalizedUnsignedByteSource;
import com.lhkbob.imaje.data.adapter.NormalizedUnsignedIntSource;
import com.lhkbob.imaje.data.adapter.NormalizedUnsignedLongSource;
import com.lhkbob.imaje.data.adapter.NormalizedUnsignedShortSource;
import com.lhkbob.imaje.data.adapter.ShortToDoubleSource;
import com.lhkbob.imaje.data.adapter.UnsignedByteSource;
import com.lhkbob.imaje.data.adapter.UnsignedByteToDoubleSource;
import com.lhkbob.imaje.data.adapter.UnsignedIntSource;
import com.lhkbob.imaje.data.adapter.UnsignedIntToDoubleSource;
import com.lhkbob.imaje.data.adapter.UnsignedLongSource;
import com.lhkbob.imaje.data.adapter.UnsignedLongToDoubleSource;
import com.lhkbob.imaje.data.adapter.UnsignedShortSource;
import com.lhkbob.imaje.data.adapter.UnsignedShortToDoubleSource;
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
import com.lhkbob.imaje.data.nio.ByteBufferSource;
import com.lhkbob.imaje.data.nio.DoubleBufferSource;
import com.lhkbob.imaje.data.nio.FloatBufferSource;
import com.lhkbob.imaje.data.nio.IntBufferSource;
import com.lhkbob.imaje.data.nio.LongBufferSource;
import com.lhkbob.imaje.data.nio.ShortBufferSource;

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
public final class DataSources {
  public interface Builder<S, A, B extends Buffer> {
    S ofArray(long length);

    S ofBuffer(long length);

    S wrapArray(A array);

    S wrapBuffer(B buffer);

    S wrapFile(Path path) throws IOException;

    S wrapFile(FileChannel channel, long offset, long length) throws IOException;
  }

  private DataSources() {}

  public static DataSource<?> getRootDataSource(DataSource<?> data) {
    while (data instanceof DataView) {
      data = ((DataView<?>) data).getSource();
    }
    return data;
  }

  public static boolean isNativeBigEndian() {
    return ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);
  }

  public static boolean isNativeEndian(DataSource<?> source) {
    return isNativeBigEndian() == source.isBigEndian();
  }

  public static Builder<ByteSource.Primitive, byte[], ByteBuffer> newByteSource() {
    return new Builder<ByteSource.Primitive, byte[], ByteBuffer>() {
      @Override
      public ByteSource.Primitive ofArray(long length) {
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
      public ByteSource.Primitive ofBuffer(long length) {
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
      public ByteSource.Primitive wrapArray(byte[] array) {
        return new ByteArray(array);
      }

      @Override
      public ByteSource.Primitive wrapBuffer(ByteBuffer buffer) {
        return new ByteBufferSource(buffer);
      }

      @Override
      public ByteSource.Primitive wrapFile(Path path) throws IOException {
        try (
            FileChannel channel = FileChannel
                .open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
          return wrapFile(channel, 0, channel.size());
        }
      }

      @Override
      public ByteSource.Primitive wrapFile(FileChannel channel, long offset, long length) throws
          IOException {
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

  public static Builder<DoubleSource.Primitive, double[], DoubleBuffer> newDoubleSource() {
    return new Builder<DoubleSource.Primitive, double[], DoubleBuffer>() {
      @Override
      public DoubleSource.Primitive ofArray(long length) {
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
      public DoubleSource.Primitive ofBuffer(long length) {
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
      public DoubleSource.Primitive wrapArray(double[] array) {
        return new DoubleArray(array);
      }

      @Override
      public DoubleSource.Primitive wrapBuffer(DoubleBuffer buffer) {
        return new DoubleBufferSource(buffer);
      }

      @Override
      public DoubleSource.Primitive wrapFile(Path path) throws IOException {
        try (
            FileChannel channel = FileChannel
                .open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
          return wrapFile(channel, 0, channel.size());
        }
      }

      @Override
      public DoubleSource.Primitive wrapFile(FileChannel channel, long offset, long length) throws
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

  public static Builder<FloatSource.Primitive, float[], FloatBuffer> newFloatSource() {
    return new Builder<FloatSource.Primitive, float[], FloatBuffer>() {
      @Override
      public FloatSource.Primitive ofArray(long length) {
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
      public FloatSource.Primitive ofBuffer(long length) {
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
      public FloatSource.Primitive wrapArray(float[] array) {
        return new FloatArray(array);
      }

      @Override
      public FloatSource.Primitive wrapBuffer(FloatBuffer buffer) {
        return new FloatBufferSource(buffer);
      }

      @Override
      public FloatSource.Primitive wrapFile(Path path) throws IOException {
        try (
            FileChannel channel = FileChannel
                .open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
          return wrapFile(channel, 0, channel.size());
        }
      }

      @Override
      public FloatSource.Primitive wrapFile(FileChannel channel, long offset, long length) throws
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

  public static Builder<IntSource.Primitive, int[], IntBuffer> newIntSource() {
    return new Builder<IntSource.Primitive, int[], IntBuffer>() {
      @Override
      public IntSource.Primitive ofArray(long length) {
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
      public IntSource.Primitive ofBuffer(long length) {
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
      public IntSource.Primitive wrapArray(int[] array) {
        return new IntArray(array);
      }

      @Override
      public IntSource.Primitive wrapBuffer(IntBuffer buffer) {
        return new IntBufferSource(buffer);
      }

      @Override
      public IntSource.Primitive wrapFile(Path path) throws IOException {
        try (
            FileChannel channel = FileChannel
                .open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
          return wrapFile(channel, 0, channel.size());
        }
      }

      @Override
      public IntSource.Primitive wrapFile(FileChannel channel, long offset, long length) throws
          IOException {
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

  public static Builder<LongSource.Primitive, long[], LongBuffer> newLongSource() {
    return new Builder<LongSource.Primitive, long[], LongBuffer>() {
      @Override
      public LongSource.Primitive ofArray(long length) {
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
      public LongSource.Primitive ofBuffer(long length) {
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
      public LongSource.Primitive wrapArray(long[] array) {
        return new LongArray(array);
      }

      @Override
      public LongSource.Primitive wrapBuffer(LongBuffer buffer) {
        return new LongBufferSource(buffer);
      }

      @Override
      public LongSource.Primitive wrapFile(Path path) throws IOException {
        try (
            FileChannel channel = FileChannel
                .open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
          return wrapFile(channel, 0, channel.size());
        }
      }

      @Override
      public LongSource.Primitive wrapFile(FileChannel channel, long offset, long length) throws
          IOException {
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

  public static Builder<ShortSource.Primitive, short[], ShortBuffer> newShortSource() {
    return new Builder<ShortSource.Primitive, short[], ShortBuffer>() {
      @Override
      public ShortSource.Primitive ofArray(long length) {
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
      public ShortSource.Primitive ofBuffer(long length) {
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
      public ShortSource.Primitive wrapArray(short[] array) {
        return new ShortArray(array);
      }

      @Override
      public ShortSource.Primitive wrapBuffer(ShortBuffer buffer) {
        return new ShortBufferSource(buffer);
      }

      @Override
      public ShortSource.Primitive wrapFile(Path path) throws IOException {
        try (
            FileChannel channel = FileChannel
                .open(path, StandardOpenOption.READ, StandardOpenOption.WRITE)) {
          return wrapFile(channel, 0, channel.size());
        }
      }

      @Override
      public ShortSource.Primitive wrapFile(FileChannel channel, long offset, long length) throws
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

  public static Builder<HalfFloatSource, short[], ShortBuffer> sfloat16() {
    return new WrappingBuilder<>(sint16(), HalfFloatSource::new);
  }

  public static Builder<FloatToDoubleSource, float[], FloatBuffer> sfloat32() {
    return new WrappingBuilder<>(newFloatSource(), FloatToDoubleSource::new);
  }

  public static Builder<DoubleSource.Primitive, double[], DoubleBuffer> sfloat64() {
    return newDoubleSource();
  }

  public static Builder<ShortSource.Primitive, short[], ShortBuffer> sint16() {
    return newShortSource();
  }

  public static Builder<IntSource.Primitive, int[], IntBuffer> sint32() {
    return newIntSource();
  }

  public static Builder<LongSource.Primitive, long[], LongBuffer> sint64() {
    return newLongSource();
  }

  public static Builder<ByteSource.Primitive, byte[], ByteBuffer> sint8() {
    return newByteSource();
  }

  public static Builder<NormalizedShortSource, short[], ShortBuffer> snorm16() {
    return new WrappingBuilder<>(sint16(), NormalizedShortSource::new);
  }

  public static Builder<NormalizedIntSource, int[], IntBuffer> snorm32() {
    return new WrappingBuilder<>(sint32(), NormalizedIntSource::new);
  }

  public static Builder<NormalizedLongSource, long[], LongBuffer> snorm64() {
    return new WrappingBuilder<>(sint64(), NormalizedLongSource::new);
  }

  public static Builder<NormalizedByteSource, byte[], ByteBuffer> snorm8() {
    return new WrappingBuilder<>(sint8(), NormalizedByteSource::new);
  }

  public static Builder<ShortToDoubleSource, short[], ShortBuffer> sscaled16() {
    return new WrappingBuilder<>(sint16(), ShortToDoubleSource::new);
  }

  public static Builder<IntToDoubleSource, int[], IntBuffer> sscaled32() {
    return new WrappingBuilder<>(sint32(), IntToDoubleSource::new);
  }

  public static Builder<LongToDoubleSource, long[], LongBuffer> sscaled64() {
    return new WrappingBuilder<>(sint64(), LongToDoubleSource::new);
  }

  public static Builder<ByteToDoubleSource, byte[], ByteBuffer> sscaled8() {
    return new WrappingBuilder<>(sint8(), ByteToDoubleSource::new);
  }

  public static Builder<UnsignedShortSource, short[], ShortBuffer> uint16() {
    return new WrappingBuilder<>(sint16(), UnsignedShortSource::new);
  }

  public static Builder<UnsignedIntSource, int[], IntBuffer> uint32() {
    return new WrappingBuilder<>(sint32(), UnsignedIntSource::new);
  }

  public static Builder<UnsignedLongSource, long[], LongBuffer> uint64() {
    return new WrappingBuilder<>(sint64(), UnsignedLongSource::new);
  }

  public static Builder<UnsignedByteSource, byte[], ByteBuffer> uint8() {
    return new WrappingBuilder<>(sint8(), UnsignedByteSource::new);
  }

  public static Builder<NormalizedUnsignedShortSource, short[], ShortBuffer> unorm16() {
    return new WrappingBuilder<>(uint16(), NormalizedUnsignedShortSource::new);
  }

  public static Builder<NormalizedUnsignedIntSource, int[], IntBuffer> unorm32() {
    return new WrappingBuilder<>(uint32(), NormalizedUnsignedIntSource::new);
  }

  public static Builder<NormalizedUnsignedLongSource, long[], LongBuffer> unorm64() {
    return new WrappingBuilder<>(uint64(), NormalizedUnsignedLongSource::new);
  }

  public static Builder<NormalizedUnsignedByteSource, byte[], ByteBuffer> unorm8() {
    return new WrappingBuilder<>(uint8(), NormalizedUnsignedByteSource::new);
  }

  public static Builder<UnsignedShortToDoubleSource, short[], ShortBuffer> uscaled16() {
    return new WrappingBuilder<>(uint16(), UnsignedShortToDoubleSource::new);
  }

  public static Builder<UnsignedIntToDoubleSource, int[], IntBuffer> uscaled32() {
    return new WrappingBuilder<>(uint32(), UnsignedIntToDoubleSource::new);
  }

  public static Builder<UnsignedLongToDoubleSource, long[], LongBuffer> uscaled64() {
    return new WrappingBuilder<>(uint64(), UnsignedLongToDoubleSource::new);
  }

  public static Builder<UnsignedByteToDoubleSource, byte[], ByteBuffer> uscaled8() {
    return new WrappingBuilder<>(uint8(), UnsignedByteToDoubleSource::new);
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

  private static class WrappingBuilder<I, O, A, B extends Buffer> implements Builder<O, A, B> {
    private final Builder<I, A, B> wrapped;
    private final Function<I, O> wrapper;

    public WrappingBuilder(Builder<I, A, B> wrapped, Function<I, O> wrapper) {
      this.wrapped = wrapped;
      this.wrapper = wrapper;
    }

    @Override
    public O ofArray(long length) {
      return wrapper.apply(wrapped.ofArray(length));
    }

    @Override
    public O ofBuffer(long length) {
      return wrapper.apply(wrapped.ofBuffer(length));
    }

    @Override
    public O wrapArray(A array) {
      return wrapper.apply(wrapped.wrapArray(array));
    }

    @Override
    public O wrapBuffer(B buffer) {
      return wrapper.apply(wrapped.wrapBuffer(buffer));
    }

    @Override
    public O wrapFile(Path path) throws IOException {
      return wrapper.apply(wrapped.wrapFile(path));
    }

    @Override
    public O wrapFile(FileChannel channel, long offset, long length) throws IOException {
      return wrapper.apply(wrapped.wrapFile(channel, offset, length));
    }
  }
}
