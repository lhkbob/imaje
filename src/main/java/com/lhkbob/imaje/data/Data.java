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
import com.lhkbob.imaje.data.types.Signed64FloatingPointNumber;
import com.lhkbob.imaje.data.types.SignedFloatingPointNumber;
import com.lhkbob.imaje.data.types.SignedInteger;
import com.lhkbob.imaje.data.types.SignedNormalizedInteger;
import com.lhkbob.imaje.data.types.UnsignedInteger;
import com.lhkbob.imaje.data.types.UnsignedNormalizedInteger;
import com.lhkbob.imaje.util.Arguments;

import java.nio.ByteOrder;

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

  public interface Factory {
    ByteData newByteData(long length);

    DoubleData newDoubleData(long length);

    FloatData newFloatData(long length);

    IntData newIntData(long length);

    LongData newLongData(long length);

    ShortData newShortData(long length);
  }

  private static volatile BufferFactory bufferFactory = DirectBufferFactory.nativeFactory();

  private Data() {}

  public static Factory arrayDataFactory() {
    return ARRAY_DATA_FACTORY;
  }

  public static Factory bufferDataFactory() {
    return BUFFER_DATA_FACTORY;
  }

  public static BufferFactory getBufferFactory() {
    return bufferFactory;
  }

  public static Factory getDefaultDataFactory() {
    return dataFactory;
  }

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

  public static void setBufferFactory(BufferFactory factory) {
    Arguments.notNull("factory", factory);
    bufferFactory = factory;
  }

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
