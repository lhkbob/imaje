package com.lhkbob.imaje.util;

import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.DataSource;
import com.lhkbob.imaje.layout.PixelFormat;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.file.Path;

/**
 *
 */
public class DataSourceBuilder implements Cloneable {
  private PixelFormat.Type type;
  private int bitSize;

  private long length;
  private Object existingData;
  private boolean useNIOBuffersForNewData;

  public DataSourceBuilder() {
    length = 0;
    existingData = null;
    useNIOBuffersForNewData = true;
    type = PixelFormat.Type.SINT;
    bitSize = 8;
  }

  @Override
  public DataSourceBuilder clone() {
    try {
      // All fields are immutable or primitive, except existingData which should be a shallow clone
      // so nothing needs to be done after calling super.clone().
      return (DataSourceBuilder) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen", e);
    }
  }

  private <A, B extends Buffer> DataSource buildDataSource(Data.Builder<?, A, B> builder) {
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
    } else if (builder.getDataSourceClass().isInstance(existingData)) {
      return builder.getDataSourceClass().cast(existingData);
    } else if (existingData == null) {
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

  public DataSource build() {
    if (length < 0) {
      throw new UnsupportedOperationException("DataSource length must be at least 0: " + length);
    }

    DataSource source = null;
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

  public DataSourceBuilder length(long length) {
    this.length = length;
    return this;
  }

  public DataSourceBuilder type(PixelFormat.Type type) {
    this.type = type;
    return this;
  }

  public DataSourceBuilder bitSize(int bitSize) {
    this.bitSize = bitSize;
    return this;
  }

  public DataSourceBuilder useBuffersForNewData(boolean useNIO) {
    useNIOBuffersForNewData = true;
    return this;
  }

  public DataSourceBuilder allocateNewData() {
    existingData = null;
    return this;
  }

  public DataSourceBuilder wrapDataSource(DataSource existing) {
    existingData = existing;
    return this;
  }

  public DataSourceBuilder wrapBuffer(Buffer existing) {
    existingData = existing;
    return this;
  }

  public DataSourceBuilder wrapArray(float[] existing) {
    existingData = existing;
    return this;
  }

  public DataSourceBuilder wrapArray(double[] existing) {
    existingData = existing;
    return this;
  }

  public DataSourceBuilder wrapArray(int[] existing) {
    existingData = existing;
    return this;
  }

  public DataSourceBuilder wrapArray(short[] existing) {
    existingData = existing;
    return this;
  }

  public DataSourceBuilder wrapArray(long[] existing) {
    existingData = existing;
    return this;
  }

  public DataSourceBuilder wrapArray(byte[] existing) {
    existingData = existing;
    return this;
  }

  public DataSourceBuilder mapFile(Path path) {
    existingData = path;
    return this;
  }
}
