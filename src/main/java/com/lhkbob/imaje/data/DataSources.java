package com.lhkbob.imaje.data;

import com.lhkbob.imaje.data.adapter.FloatToDoubleSource;
import com.lhkbob.imaje.data.adapter.HalfFloatSource;
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
import com.lhkbob.imaje.data.adapter.UnsignedByteSource;
import com.lhkbob.imaje.data.adapter.UnsignedIntSource;
import com.lhkbob.imaje.data.adapter.UnsignedLongSource;
import com.lhkbob.imaje.data.adapter.UnsignedShortSource;
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

  public static DoubleSource asDoubleSource(final DataSource<?> source) {
    if (source instanceof DoubleSource) {
      return (DoubleSource) source;
    } else if (source instanceof FloatSource) {
      return new FloatToDoubleSource((FloatSource) source);
    } else if (source instanceof UnsignedByteSource) {
      return new NormalizedUnsignedByteSource((UnsignedByteSource) source);
    } else if (source instanceof UnsignedShortSource) {
      return new NormalizedUnsignedShortSource((UnsignedShortSource) source);
    } else if (source instanceof UnsignedIntSource) {
      return new NormalizedUnsignedIntSource((UnsignedIntSource) source);
    } else if (source instanceof UnsignedLongSource) {
      return new NormalizedUnsignedLongSource((UnsignedLongSource) source);
    } else if (source instanceof ByteSource) {
      return new NormalizedByteSource((ByteSource) source);
    } else if (source instanceof ShortSource) {
      return new NormalizedShortSource((ShortSource) source);
    } else if (source instanceof IntSource) {
      return new NormalizedIntSource((IntSource) source);
    } else if (source instanceof LongSource) {
      return new NormalizedLongSource((LongSource) source);
    } else {
      // Unknown subclass of data source
      throw new UnsupportedOperationException(
          "Unknown subclass of DataSource, can't convert to DoubleSource: " + source);
    }
  }

  public static DataSource<?> getRootDataSource(DataSource<?> data) {
    while(data instanceof DataView) {
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

  public static Builder<ByteSource, byte[], ByteBuffer> newByteSource() {
    return BYTE_BUILDER;
  }

  public static Builder<DoubleSource, double[], DoubleBuffer> newDoubleSource() {
    return DOUBLE_BUILDER;
  }

  public static Builder<FloatSource, float[], FloatBuffer> newFloatSource() {
    return FLOAT_BUILDER;
  }

  public static Builder<HalfFloatSource, short[], ShortBuffer> newHalfFloatSource() {
    return HALF_BUILDER;
  }

  public static Builder<IntSource, int[], IntBuffer> newIntSource() {
    return INT_BUILDER;
  }

  public static Builder<LongSource, long[], LongBuffer> newLongSource() {
    return LONG_BUILDER;
  }

  public static Builder<ShortSource, short[], ShortBuffer> newShortSource() {
    return SHORT_BUILDER;
  }

  public static Builder<UnsignedByteSource, byte[], ByteBuffer> newUnsignedByteSource() {
    return UNSIGNED_BYTE_BUILDER;
  }

  public static Builder<UnsignedIntSource, int[], IntBuffer> newUnsignedIntSource() {
    return UNSIGNED_INT_BUILDER;
  }

  public static Builder<UnsignedShortSource, short[], ShortBuffer> newUnsignedShortSource() {
    return UNSIGNED_SHORT_BUILDER;
  }

  public static Builder<UnsignedLongSource, long[], LongBuffer> newUnsignedLongSource() {
    return UNSIGNED_LONG_BUILDER;
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

  private static final Builder<IntSource, int[], IntBuffer> INT_BUILDER = new Builder<IntSource, int[], IntBuffer>() {
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
  private static final Builder<LongSource, long[], LongBuffer> LONG_BUILDER = new Builder<LongSource, long[], LongBuffer>() {
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
  private static final Builder<FloatSource, float[], FloatBuffer> FLOAT_BUILDER = new Builder<FloatSource, float[], FloatBuffer>() {
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
    public FloatSource wrapFile(FileChannel channel, long offset, long length) throws IOException {
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
  private static final Builder<DoubleSource, double[], DoubleBuffer> DOUBLE_BUILDER = new Builder<DoubleSource, double[], DoubleBuffer>() {
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
    public DoubleSource wrapFile(FileChannel channel, long offset, long length) throws IOException {
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
  private static final Builder<ByteSource, byte[], ByteBuffer> BYTE_BUILDER = new Builder<ByteSource, byte[], ByteBuffer>() {
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
  private static final Builder<ShortSource, short[], ShortBuffer> SHORT_BUILDER = new Builder<ShortSource, short[], ShortBuffer>() {
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
    public ShortSource wrapFile(FileChannel channel, long offset, long length) throws IOException {
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
  private static final Builder<HalfFloatSource, short[], ShortBuffer> HALF_BUILDER = new Builder<HalfFloatSource, short[], ShortBuffer>() {
    @Override
    public HalfFloatSource ofArray(long length) {
      return new HalfFloatSource(SHORT_BUILDER.ofArray(length));
    }

    @Override
    public HalfFloatSource ofBuffer(long length) {
      return new HalfFloatSource(SHORT_BUILDER.ofBuffer(length));
    }

    @Override
    public HalfFloatSource wrapArray(short[] array) {
      return new HalfFloatSource(SHORT_BUILDER.wrapArray(array));
    }

    @Override
    public HalfFloatSource wrapBuffer(ShortBuffer buffer) {
      return new HalfFloatSource(SHORT_BUILDER.wrapBuffer(buffer));
    }

    @Override
    public HalfFloatSource wrapFile(Path path) throws IOException {
      return new HalfFloatSource(SHORT_BUILDER.wrapFile(path));
    }

    @Override
    public HalfFloatSource wrapFile(FileChannel channel, long offset, long length) throws IOException {
      return new HalfFloatSource(SHORT_BUILDER.wrapFile(channel, offset, length));
    }
  };

  private static final Builder<UnsignedByteSource, byte[], ByteBuffer> UNSIGNED_BYTE_BUILDER = new Builder<UnsignedByteSource, byte[], ByteBuffer>() {
    @Override
    public UnsignedByteSource ofArray(long length) {
      return new UnsignedByteSource(BYTE_BUILDER.ofArray(length));
    }

    @Override
    public UnsignedByteSource ofBuffer(long length) {
      return new UnsignedByteSource(BYTE_BUILDER.ofBuffer(length));
    }

    @Override
    public UnsignedByteSource wrapArray(byte[] array) {
      return new UnsignedByteSource(BYTE_BUILDER.wrapArray(array));
    }

    @Override
    public UnsignedByteSource wrapBuffer(ByteBuffer buffer) {
      return new UnsignedByteSource(BYTE_BUILDER.wrapBuffer(buffer));
    }

    @Override
    public UnsignedByteSource wrapFile(Path path) throws IOException {
      return new UnsignedByteSource(BYTE_BUILDER.wrapFile(path));
    }

    @Override
    public UnsignedByteSource wrapFile(FileChannel channel, long offset, long length) throws
        IOException {
      return new UnsignedByteSource(BYTE_BUILDER.wrapFile(channel, offset, length));
    }
  };
  private static final Builder<UnsignedIntSource, int[], IntBuffer> UNSIGNED_INT_BUILDER = new Builder<UnsignedIntSource, int[], IntBuffer>() {
    @Override
    public UnsignedIntSource ofArray(long length) {
      return new UnsignedIntSource(INT_BUILDER.ofArray(length));
    }

    @Override
    public UnsignedIntSource ofBuffer(long length) {
      return new UnsignedIntSource(INT_BUILDER.ofBuffer(length));
    }

    @Override
    public UnsignedIntSource wrapArray(int[] array) {
      return new UnsignedIntSource(INT_BUILDER.wrapArray(array));
    }

    @Override
    public UnsignedIntSource wrapBuffer(IntBuffer buffer) {
      return new UnsignedIntSource(INT_BUILDER.wrapBuffer(buffer));
    }

    @Override
    public UnsignedIntSource wrapFile(Path path) throws IOException {
      return new UnsignedIntSource(INT_BUILDER.wrapFile(path));
    }

    @Override
    public UnsignedIntSource wrapFile(FileChannel channel, long offset, long length) throws
        IOException {
      return new UnsignedIntSource(INT_BUILDER.wrapFile(channel, offset, length));
    }
  };
  private static final Builder<UnsignedLongSource, long[], LongBuffer> UNSIGNED_LONG_BUILDER = new Builder<UnsignedLongSource, long[], LongBuffer>() {
    @Override
    public UnsignedLongSource ofArray(long length) {
      return new UnsignedLongSource(LONG_BUILDER.ofArray(length));
    }

    @Override
    public UnsignedLongSource ofBuffer(long length) {
      return new UnsignedLongSource(LONG_BUILDER.ofBuffer(length));
    }

    @Override
    public UnsignedLongSource wrapArray(long[] array) {
      return new UnsignedLongSource(LONG_BUILDER.wrapArray(array));
    }

    @Override
    public UnsignedLongSource wrapBuffer(LongBuffer buffer) {
      return new UnsignedLongSource(LONG_BUILDER.wrapBuffer(buffer));
    }

    @Override
    public UnsignedLongSource wrapFile(Path path) throws IOException {
      return new UnsignedLongSource(LONG_BUILDER.wrapFile(path));
    }

    @Override
    public UnsignedLongSource wrapFile(FileChannel channel, long offset, long length) throws
        IOException {
      return new UnsignedLongSource(LONG_BUILDER.wrapFile(channel, offset, length));
    }
  };
  private static final Builder<UnsignedShortSource, short[], ShortBuffer> UNSIGNED_SHORT_BUILDER = new Builder<UnsignedShortSource, short[], ShortBuffer>() {
    @Override
    public UnsignedShortSource ofArray(long length) {
      return new UnsignedShortSource(SHORT_BUILDER.ofArray(length));
    }

    @Override
    public UnsignedShortSource ofBuffer(long length) {
      return new UnsignedShortSource(SHORT_BUILDER.ofBuffer(length));
    }

    @Override
    public UnsignedShortSource wrapArray(short[] array) {
      return new UnsignedShortSource(SHORT_BUILDER.wrapArray(array));
    }

    @Override
    public UnsignedShortSource wrapBuffer(ShortBuffer buffer) {
      return new UnsignedShortSource(SHORT_BUILDER.wrapBuffer(buffer));
    }

    @Override
    public UnsignedShortSource wrapFile(Path path) throws IOException {
      return new UnsignedShortSource(SHORT_BUILDER.wrapFile(path));
    }

    @Override
    public UnsignedShortSource wrapFile(FileChannel channel, long offset, long length) throws
        IOException {
      return new UnsignedShortSource(SHORT_BUILDER.wrapFile(channel, offset, length));
    }
  };
}
