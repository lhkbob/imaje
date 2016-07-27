package com.lhkbob.imaje.util;

import com.lhkbob.imaje.data.ByteData;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.DataBuffer;
import com.lhkbob.imaje.data.IntData;
import com.lhkbob.imaje.data.LongData;
import com.lhkbob.imaje.data.NumericData;
import com.lhkbob.imaje.data.ShortData;
import com.lhkbob.imaje.layout.PixelFormat;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.file.Path;

/**
 *
 */
public class DataBufferBuilder implements Cloneable {
  private PixelFormat.Type type;
  private int bitSize;

  private long length;
  private Object existingData;
  private boolean useNIOBuffersForNewData;

  public DataBufferBuilder() {
    length = 0;
    existingData = null;
    useNIOBuffersForNewData = true;
    type = PixelFormat.Type.SINT;
    bitSize = 8;
  }

  @Override
  public DataBufferBuilder clone() {
    try {
      // All fields are immutable or primitive, except existingData which should be a shallow clone
      // so nothing needs to be done after calling super.clone().
      return (DataBufferBuilder) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen", e);
    }
  }

  private <A, B extends Buffer> NumericData<?> buildDataSource(Data.Builder<? extends NumericData<?>, A, B> builder) {
    if (builder.getArrayClass().isInstance(existingData)) {
      return builder.wrapArray(builder.getArrayClass().cast(existingData));
    } else if (builder.getBufferClass().isInstance(existingData)) {
      return builder.wrapBuffer(builder.getBufferClass().cast(existingData));
    } else if (existingData instanceof Path) {
      try {
        return builder.wrapFile((Path) existingData);
      } catch(IOException e) {
        throw new UnsupportedOperationException("Unable to wrap Path instance in DataSource", e);
      }
    } else if (builder.getDataBufferClass().isInstance(existingData)) {
      return builder.getDataBufferClass().cast(existingData);
    } else if (existingData instanceof IntData && IntData.Numeric.class.equals(builder.getDataBufferClass())) {
      return new IntData.Numeric((IntData) existingData);
    } else if (existingData instanceof ShortData && ShortData.Numeric.class.equals(builder.getDataBufferClass())) {
      return new ShortData.Numeric((ShortData) existingData);
    } else if (existingData instanceof LongData && LongData.Numeric.class.equals(builder.getDataBufferClass())) {
      return new LongData.Numeric((LongData) existingData);
    } else if (existingData instanceof ByteData && ByteData.Numeric.class.equals(builder.getDataBufferClass())) {
      return new ByteData.Numeric((ByteData) existingData);
    }else if (existingData == null) {
      // Allocate new data
      if (useNIOBuffersForNewData) {
        return builder.ofBuffer(length);
      } else {
        return builder.ofArray(length);
      }
    } else {
      throw new UnsupportedOperationException("Existing data is incompatible with pixel format: " + existingData);
    }
  }

  public NumericData<?> build() {
    if (length < 0) {
      throw new UnsupportedOperationException("DataSource length must be at least 0: " + length);
    }

    NumericData<?> source = null;
    switch(type) {
    case UINT:
    case USCALED:
      switch(bitSize) {
      case 8:
        source = buildDataSource(Data.uint8());
        break;
      case 16:
        source = buildDataSource(Data.uint16());
        break;
      case 32:
        source = buildDataSource(Data.uint32());
        break;
      case 64:
        source = buildDataSource(Data.uint64());
        break;
      }
      break;
    case SINT:
    case SSCALED:
      switch (bitSize) {
      case 8:
        source = buildDataSource(Data.sint8());
        break;
      case 16:
        source = buildDataSource(Data.sint16());
        break;
      case 32:
        source = buildDataSource(Data.sint32());
        break;
      case 64:
        source = buildDataSource(Data.sint64());
        break;
      }
      break;
    case UNORM:
      switch (bitSize) {
      case 8:
        source = buildDataSource(Data.unorm8());
        break;
      case 16:
        source = buildDataSource(Data.unorm16());
        break;
      case 32:
        source = buildDataSource(Data.unorm32());
        break;
      case 64:
        source = buildDataSource(Data.unorm64());
        break;
      }
      break;
    case SNORM:
      switch (bitSize) {
      case 8:
        source = buildDataSource(Data.snorm8());
        break;
      case 16:
        source = buildDataSource(Data.snorm16());
        break;
      case 32:
        source = buildDataSource(Data.snorm32());
        break;
      case 64:
        source = buildDataSource(Data.snorm64());
        break;
      }
      break;
    case SFLOAT:
      switch (bitSize) {
      case 16:
        source = buildDataSource(Data.sfloat16());
        break;
      case 32:
        source = buildDataSource(Data.sfloat32());
        break;
      case 64:
        source = buildDataSource(Data.sfloat64());
        break;
      }
      break;
    case UFLOAT:
      // FIXME currently not implemented
      break;
    }

    if (source == null) {
      throw new UnsupportedOperationException("Unable to create or wrap data to match requested type and bit size (" + type + ", " + bitSize + ")");
    } else if (source.getLength() != length) {
      throw new UnsupportedOperationException("Wrapped data expected to have a length of " + length + " but was " + source.getLength());
    } else if (source.getBitSize() != bitSize) {
      throw new UnsupportedOperationException("Wrapped data expected to have a bit size of " + bitSize + " but was " + source.getBitSize());
    } else {
      return source;
    }
  }

  public DataBufferBuilder length(long length) {
    this.length = length;
    return this;
  }

  public DataBufferBuilder type(PixelFormat.Type type) {
    this.type = type;
    return this;
  }

  public DataBufferBuilder bitSize(int bitSize) {
    this.bitSize = bitSize;
    return this;
  }

  public DataBufferBuilder useBuffersForNewData(boolean useNIO) {
    useNIOBuffersForNewData = useNIO;
    return this;
  }

  public DataBufferBuilder allocateNewData() {
    existingData = null;
    return this;
  }

  public DataBufferBuilder wrapDataSource(DataBuffer existing) {
    existingData = existing;
    return this;
  }

  public DataBufferBuilder wrapBuffer(Buffer existing) {
    existingData = existing;
    return this;
  }

  public DataBufferBuilder wrapArray(float[] existing) {
    existingData = existing;
    return this;
  }

  public DataBufferBuilder wrapArray(double[] existing) {
    existingData = existing;
    return this;
  }

  public DataBufferBuilder wrapArray(int[] existing) {
    existingData = existing;
    return this;
  }

  public DataBufferBuilder wrapArray(short[] existing) {
    existingData = existing;
    return this;
  }

  public DataBufferBuilder wrapArray(long[] existing) {
    existingData = existing;
    return this;
  }

  public DataBufferBuilder wrapArray(byte[] existing) {
    existingData = existing;
    return this;
  }

  public DataBufferBuilder mapFile(Path path) {
    existingData = path;
    return this;
  }
}
