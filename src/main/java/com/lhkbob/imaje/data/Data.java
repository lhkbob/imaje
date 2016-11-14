/*
 * BSD 3-Clause License - imaJe
 *
 * Copyright (c) 2016, Michael Ludwig
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
import com.lhkbob.imaje.data.nio.BufferFactory;
import com.lhkbob.imaje.data.nio.ByteBufferData;
import com.lhkbob.imaje.data.nio.DirectBufferFactory;
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
import com.lhkbob.imaje.util.Arguments;

import java.nio.ByteOrder;

/**
 * Data
 * ====
 *
 * Data is a utility function class for allocating and interpreting data. In particular, it defines
 * several common, standardized {@link BinaryRepresentation types} for signed and unsigned integers
 * and floating point numbers. It defines a {@link Factory} interface for creating primitive-typed
 * DataBuffers and provides implementations that use either array-backed or NIO buffer-backed data
 * sources.
 *
 * Data is also the configuration point for the {@link BufferFactory} singleton that should be used
 * when creating any NIO buffer (regardless of purpose or if it's going to be passed onto a
 * DataBuffer).
 *
 * @author Michael Ludwig
 */
public final class Data {
  /**
   * Numeric representation for a signed 16-bit floating point number. Has a 5-bit exponent and
   * 10-bit mantissa. This is equivalent to the `half` primitive type used in OpenGL and Vulkan.
   */
  public static final BinaryRepresentation SFLOAT16 = new SignedFloatingPointNumber(5, 10);
  /**
   * Numeric representation for a signed 32-bit floating point number. Has a 8-bit exponent and
   * 23-bit mantissa. This is equivalent to Java's `float`, which is why it is deprecated.
   *
   * @deprecated Deprecated because Java has native support, which should be preferred when possible
   */
  @Deprecated public static final BinaryRepresentation SFLOAT32 = new SignedFloatingPointNumber(
      8, 23);
  /**
   * Numeric representation for a signed 64-bit floating point number. Has a 11-bit exponent and
   * 52-bit mantissa. This is equivalent to Java's `double`, which is why it is deprecated.
   *
   * @deprecated Deprecated because Java has native support, which should be preferred when possible
   */
  @Deprecated public static final BinaryRepresentation SFLOAT64 = new Signed64FloatingPointNumber();
  /**
   * Numeric representation for a 2's complement 16-bit signed integer, equivalent to Java's
   * `short`.
   *
   * @deprecated Deprecated because Java has native support, which should be preferred when possible
   */
  @Deprecated public static final BinaryRepresentation SINT16 = new SignedInteger(16);
  /**
   * Numeric representation for a 2's complement 32-bit signed integer, equivalent to Java's `int`.
   *
   * @deprecated Deprecated because Java has native support, which should be preferred when possible
   */
  @Deprecated public static final BinaryRepresentation SINT32 = new SignedInteger(32);
  /**
   * Numeric representation for a 2's complement 64-bit signed integer, equivalent to Java's `long`.
   *
   * @deprecated Deprecated because Java has native support, which should be preferred when possible
   */
  @Deprecated public static final BinaryRepresentation SINT64 = new SignedInteger(64);
  /**
   * Numeric representation for a 2's complement 8-bit signed integer, equivalent to Java's `byte`.
   *
   * @deprecated Deprecated because Java has native support, which should be preferred when possible
   */
  @Deprecated public static final BinaryRepresentation SINT8 = new SignedInteger(8);
  /**
   * Numeric representation for a signed, 16-bit fixed-point number where integer values between
   * `-Short.MAX_VALUE` and `{@link Short#MAX_VALUE}` are normalized to `[-1, 1]`.
   */
  public static final BinaryRepresentation SNORM16 = new SignedNormalizedInteger(16);
  /**
   * Numeric representation for a signed, 32-bit fixed-point number where integer values between
   * `-Integer.MAX_VALUE` and `{@link Integer#MAX_VALUE}` are normalized to `[-1, 1]`.
   */
  public static final BinaryRepresentation SNORM32 = new SignedNormalizedInteger(32);
  /**
   * Numeric representation for a signed, 64-bit fixed-point number where integer values between
   * `-Long.MAX_VALUE` and `{@link Long#MAX_VALUE}` are normalized to `[-1, 1]`.
   */
  public static final BinaryRepresentation SNORM64 = new SignedNormalizedInteger(64);
  /**
   * Numeric representation for a signed, 8-bit fixed-point number where integer values between
   * `-Byte.MAX_VALUE` and `{@link Byte#MAX_VALUE}` are normalized to `[-1, 1]`.
   */
  public static final BinaryRepresentation SNORM8 = new SignedNormalizedInteger(8);
  /**
   * Numeric representation for a 1's complement 16-bit unsigned integer.
   */
  public static final BinaryRepresentation UINT16 = new UnsignedInteger(16);
  /**
   * Numeric representation for a 1's complement 32-bit unsigned integer.
   */
  public static final BinaryRepresentation UINT32 = new UnsignedInteger(32);
  /**
   * Numeric representation for a 1's complement 64-bit unsigned integer.
   */
  public static final BinaryRepresentation UINT64 = new UnsignedInteger(64);
  /**
   * Numeric representation for a 1's complement 8-bit unsigned integer.
   */
  public static final BinaryRepresentation UINT8 = new UnsignedInteger(8);
  /**
   * Numeric representation for an unsigned, 16-bit fixed-point number where integer values between
   * `0` and `2^16-1` are normalized to `[0, 1]`.
   */
  public static final BinaryRepresentation UNORM16 = new UnsignedNormalizedInteger(16);
  /**
   * Numeric representation for an unsigned, 32-bit fixed-point number where integer values between
   * `0` and `2^32-1` are normalized to `[0, 1]`.
   */
  public static final BinaryRepresentation UNORM32 = new UnsignedNormalizedInteger(32);
  /**
   * Numeric representation for an unsigned, 64-bit fixed-point number where integer values between
   * `0` and `2^64-1` are normalized to `[0, 1]`.
   */
  public static final BinaryRepresentation UNORM64 = new UnsignedNormalizedInteger(64);
  /**
   * Numeric representation for an unsigned, 8-bit fixed-point number where integer values between
   * `0` and `255` are normalized to `[0, 1]`.
   */
  public static final BinaryRepresentation UNORM8 = new UnsignedNormalizedInteger(8);

  /**
   * Factory
   * =======
   *
   * Data.Factory is a factory interface for creating DataBuffers of each of the Java primitive
   * types or represented by a BinaryRepresentation. A given factory will use implementations that
   * rely on consistent data storage, i.e. all DataBuffers use arrays or will use NIO buffers.
   * When necessary, Factories should wrap appropriately sized DataBuffers in a large-variant from
   * the {@link com.lhkbob.imaje.data.large} package (e.g. multiple {@link ByteArrayData} instances
   * into a {@link LargeByteData}).
   *
   * Implementations are immutable and thread-safe. It is implementation dependent as to whether or
   * not the contents of the created DataBuffers are initialized to a particular value.
   *
   * @author Michael Ludwig
   */
  public interface Factory {
    /**
     * Create a new ByteData that will have the given length.
     *
     * @param length
     *     The length of the new data
     * @return A new ByteData
     */
    ByteData newByteData(long length);

    /**
     * Create a new DoubleData that will have the given length.
     *
     * @param length
     *     The length of the new data
     * @return A new DoubleData
     */
    DoubleData newDoubleData(long length);

    /**
     * Create a new FloatData that will have the given length.
     *
     * @param length
     *     The length of the new data
     * @return A new FloatData
     */
    FloatData newFloatData(long length);

    /**
     * Create a new IntData that will have the given length.
     *
     * @param length
     *     The length of the new data
     * @return A new IntData
     */
    IntData newIntData(long length);

    /**
     * Create a new LongData that will have the given length.
     *
     * @param length
     *     The length of the new data
     * @return A new LongData
     */
    LongData newLongData(long length);

    /**
     * Create a new ShortData that will have the given length.
     *
     * @param length
     *     The length of the new data
     * @return A new ShortData
     */
    ShortData newShortData(long length);

    /**
     * Create a new NumericData source based on the given binary representation, `format`. If
     * `format` is equivalent to a Java primitive, the appropriate `newXData()` function is invoked
     * instead. Otherwise {@link CustomBinaryData} wraps a BitData source of `length`; however, only
     * certain bit sizes are supported since the factory only knows how to create byte, short, int,
     * and long bit data buffers.
     *
     * @param format
     *     The binary representation of the data buffer
     * @param length
     *     The length of the new buffer
     * @return A new NumericData that stores numeric values based on `format`
     */
    default NumericData<?> newData(BinaryRepresentation format, long length) {
      // If the binary representation is natively supported, use them directly
      if (format.equals(SFLOAT32)) {
        return newFloatData(length);
      } else if (format.equals(SFLOAT64)) {
        return newDoubleData(length);
      } else if (format.equals(SINT8)) {
        return new ByteData.Numeric(newByteData(length));
      } else if (format.equals(SINT16)) {
        return new ShortData.Numeric(newShortData(length));
      } else if (format.equals(SINT32)) {
        return new IntData.Numeric(newIntData(length));
      } else if (format.equals(SINT64)) {
        return new LongData.Numeric(newLongData(length));
      }

      switch (format.getBitSize()) {
      case 64:
        return new CustomBinaryData<>(format, newLongData(length));
      case 32:
        return new CustomBinaryData<>(format, newIntData(length));
      case 16:
        return new CustomBinaryData<>(format, newShortData(length));
      case 8:
        return new CustomBinaryData<>(format, newByteData(length));
      default:
        throw new UnsupportedOperationException(
            "Only supports BinaryRepresentations that have a bit size of 8, 16, 32, or 64. Not: "
                + format.getBitSize());
      }
    }
  }

  private static volatile BufferFactory bufferFactory = DirectBufferFactory.nativeFactory();

  private Data() {}

  /**
   * Get the DataBuffer factory that uses the array-backed implementations defined in the {@link
   * com.lhkbob.imaje.data.array} package. This returns an internal singleton, so there is no
   * allocation cost associated with this function. The returned factory can be used even if it is
   * not assigned as the default data factory.
   *
   * @return The array-based data buffer factory
   */
  public static Factory arrayDataFactory() {
    return ARRAY_DATA_FACTORY;
  }

  /**
   * Get the DataBuffer factory that uses the array-backed implementations defined in the {@link
   * com.lhkbob.imaje.data.nio} package. This returns an internal singleton, so there is no
   * allocation cost associated with this function. The returned factory can be used even if it is
   * not assigned as the default data factory.
   *
   * The underlying NIO buffer instances the factory creates are instantiated by the {@link
   * #getBufferFactory() buffer factory} that is configured at the time of each DataBuffer creation.
   * This means this data factory always respects the up-to-date buffer creation policy.
   *
   * @return The NIO-buffer based data buffer factory
   */
  public static Factory nioDataFactory() {
    return BUFFER_DATA_FACTORY;
  }

  /**
   * Get the currently configured {@link BufferFactory}. The returned factory should be used for all
   * NIO buffer instantiation to ensure that the type of buffer optimized by the JVM is consistent
   * (i.e. array-backed or native, direct buffers).
   *
   * By default, if {@link #setBufferFactory(BufferFactory)} has not been called, the buffer
   * factory creates direct NIO buffers with the native byte ordering.
   *
   * @return The buffer factory
   */
  public static BufferFactory getBufferFactory() {
    return bufferFactory;
  }

  /**
   * Get the default data Factory implementation. Unlike the configured BufferFactory, the default
   * data factory is merely a hint or convenience for code that instantiates DataBuffers. It is
   * recommended that library code be configurable, where it can take an application-specified data
   * factory if necessary. If no such factory is specified, the library can then fall back onto the
   * default factory.
   *
   * By default, if {@link #setDefaultDataFactory(Factory)} has not been called, the data factory
   * is that returned by {@link #arrayDataFactory()}.
   *
   * @return The default data factory
   */
  public static Factory getDefaultDataFactory() {
    return dataFactory;
  }

  /**
   * Get the lowest level data source within `data`. This unwraps all {@link DataView} containers
   * until the root source is found. If `data` does not implement DataView then it is returned
   * as-is.
   *
   * @param data
   *     The data to unwrap
   * @return The root data, after having removed all nested DataViews
   */
  public static Object getViewedData(Object data) {
    while (data instanceof DataView) {
      data = ((DataView<?>) data).getSource();
    }
    return data;
  }

  /**
   * @return True if the system's byte order is big endian
   */
  public static boolean isNativeBigEndian() {
    return ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN);
  }

  /**
   * @param source
   *     The buffer to check
   * @return True if `source` has the same byte order as the system
   */
  public static boolean isNativeEndian(DataBuffer source) {
    return isNativeBigEndian() == source.isBigEndian();
  }

  /**
   * Configure the BufferFactory singleton that is returned by {@link #getBufferFactory()}. For
   * maximum effectiveness and to achieve the goals behind BufferFactory's definition, this should
   * be called a minimal number of times at the start of the application prior to any buffer
   * creation.
   *
   * @param factory
   *     The new buffer factory
   */
  public static void setBufferFactory(BufferFactory factory) {
    Arguments.notNull("factory", factory);
    bufferFactory = factory;
  }

  /**
   * Configure the default data Factory that will be returned by {@link #getDefaultDataFactory()}.
   *
   * @param factory
   *     The new default data factory
   */
  public static void setDefaultDataFactory(Factory factory) {
    Arguments.notNull("factory", factory);
    dataFactory = factory;
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
  private static final Factory ARRAY_DATA_FACTORY = new Factory() {
    @Override
    public ByteData newByteData(long length) {
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
    public DoubleData newDoubleData(long length) {
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
    public FloatData newFloatData(long length) {
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
    public IntData newIntData(long length) {
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
    public LongData newLongData(long length) {
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
    public ShortData newShortData(long length) {
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
  };
  private static volatile Factory dataFactory = ARRAY_DATA_FACTORY;
  private static final Factory BUFFER_DATA_FACTORY = new Factory() {
    @Override
    public ByteData newByteData(long length) {
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
    public DoubleData newDoubleData(long length) {
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
    public FloatData newFloatData(long length) {
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
    public IntData newIntData(long length) {
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
    public LongData newLongData(long length) {
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
    public ShortData newShortData(long length) {
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
  };
}
