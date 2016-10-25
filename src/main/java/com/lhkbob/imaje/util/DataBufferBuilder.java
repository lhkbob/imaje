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
package com.lhkbob.imaje.util;

import com.lhkbob.imaje.data.ByteData;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.DataBuffer;
import com.lhkbob.imaje.data.DoubleData;
import com.lhkbob.imaje.data.FloatData;
import com.lhkbob.imaje.data.IntData;
import com.lhkbob.imaje.data.LongData;
import com.lhkbob.imaje.data.NumericData;
import com.lhkbob.imaje.data.ShortData;
import com.lhkbob.imaje.data.array.ByteArrayData;
import com.lhkbob.imaje.data.array.DoubleArrayData;
import com.lhkbob.imaje.data.array.FloatArrayData;
import com.lhkbob.imaje.data.array.IntArrayData;
import com.lhkbob.imaje.data.array.LongArrayData;
import com.lhkbob.imaje.data.array.ShortArrayData;
import com.lhkbob.imaje.data.nio.ByteBufferData;
import com.lhkbob.imaje.data.nio.DoubleBufferData;
import com.lhkbob.imaje.data.nio.FloatBufferData;
import com.lhkbob.imaje.data.nio.IntBufferData;
import com.lhkbob.imaje.data.nio.LongBufferData;
import com.lhkbob.imaje.data.nio.ShortBufferData;
import com.lhkbob.imaje.data.types.CustomBinaryData;
import com.lhkbob.imaje.io.UnsupportedImageFormatException;
import com.lhkbob.imaje.layout.PixelFormat;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

import static com.lhkbob.imaje.layout.PixelFormat.Type.UINT;

/**
 *
 */
public class DataBufferBuilder implements Cloneable {
  private PixelFormat.Type type;
  private int bitSize;

  private long length;
  private Object existingData;

  public DataBufferBuilder() {
    length = 0;
    existingData = null;
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

  private NumericData<?> buildDataSource() {
    if (bitSize == 8 && (type == UINT || type == PixelFormat.Type.USCALED)) {
      if (existingData instanceof CustomBinaryData) {
        CustomBinaryData<?> d = (CustomBinaryData<?>) existingData;
        if (d.getBinaryRepresentation().equals(Data.UINT8)) {
          return (CustomBinaryData<?>) existingData;
        }
      } else if (existingData instanceof ByteData) {
        return new CustomBinaryData<>(Data.UINT8, (ByteData) existingData);
      } else if (existingData instanceof ByteBuffer) {
        return new CustomBinaryData<>(Data.UINT8, new ByteBufferData((ByteBuffer) existingData));
      } else if (existingData instanceof byte[]) {
        return new CustomBinaryData<>(Data.UINT8, new ByteArrayData((byte[]) existingData));
      } else if (existingData instanceof Data.Factory) {
        return new CustomBinaryData<>(Data.UINT8, ((Data.Factory) existingData).newByteData(length));
      } else if (existingData == null) {
        return new CustomBinaryData<>(Data.UINT8, Data.getDefaultDataFactory().newByteData(length));
      }
    } else if (bitSize == 16 && (type == UINT || type == PixelFormat.Type.USCALED)) {
      if (existingData instanceof CustomBinaryData) {
        CustomBinaryData<?> d = (CustomBinaryData<?>) existingData;
        if (d.getBinaryRepresentation().equals(Data.UINT16)) {
          return (CustomBinaryData<?>) existingData;
        }
      } else if (existingData instanceof ShortData) {
        return new CustomBinaryData<>(Data.UINT16, (ShortData) existingData);
      } else if (existingData instanceof ShortBuffer) {
        return new CustomBinaryData<>(Data.UINT16, new ShortBufferData((ShortBuffer) existingData));
      } else if (existingData instanceof short[]) {
        return new CustomBinaryData<>(Data.UINT16, new ShortArrayData((short[]) existingData));
      } else if (existingData instanceof Data.Factory) {
        return new CustomBinaryData<>(Data.UINT16, ((Data.Factory) existingData).newShortData(length));
      } else if (existingData == null) {
        return new CustomBinaryData<>(Data.UINT16, Data.getDefaultDataFactory().newShortData(length));
      }
    } else if (bitSize == 32 && (type == UINT || type == PixelFormat.Type.USCALED)) {
      if (existingData instanceof CustomBinaryData) {
        CustomBinaryData<?> d = (CustomBinaryData<?>) existingData;
        if (d.getBinaryRepresentation().equals(Data.UINT32)) {
          return (CustomBinaryData<?>) existingData;
        }
      } else if (existingData instanceof IntData) {
        return new CustomBinaryData<>(Data.UINT32, (IntData) existingData);
      } else if (existingData instanceof IntBuffer) {
        return new CustomBinaryData<>(Data.UINT32, new IntBufferData((IntBuffer) existingData));
      } else if (existingData instanceof int[]) {
        return new CustomBinaryData<>(Data.UINT32, new IntArrayData((int[]) existingData));
      } else if (existingData instanceof Data.Factory) {
        return new CustomBinaryData<>(Data.UINT32, ((Data.Factory) existingData).newIntData(length));
      } else if (existingData == null) {
        return new CustomBinaryData<>(Data.UINT32, Data.getDefaultDataFactory().newIntData(length));
      }
    } else if (bitSize == 64 && (type == UINT || type == PixelFormat.Type.USCALED)) {
      if (existingData instanceof CustomBinaryData) {
        CustomBinaryData<?> d = (CustomBinaryData<?>) existingData;
        if (d.getBinaryRepresentation().equals(Data.UINT64)) {
          return (CustomBinaryData<?>) existingData;
        }
      } else if (existingData instanceof LongData) {
        return new CustomBinaryData<>(Data.UINT64, (LongData) existingData);
      } else if (existingData instanceof LongBuffer) {
        return new CustomBinaryData<>(Data.UINT64, new LongBufferData((LongBuffer) existingData));
      } else if (existingData instanceof long[]) {
        return new CustomBinaryData<>(Data.UINT64, new LongArrayData((long[]) existingData));
      } else if (existingData instanceof Data.Factory) {
        return new CustomBinaryData<>(Data.UINT64, ((Data.Factory) existingData).newLongData(length));
      } else if (existingData == null) {
        return new CustomBinaryData<>(Data.UINT64, Data.getDefaultDataFactory().newLongData(length));
      }
    } else if (bitSize == 8 && (type == PixelFormat.Type.SINT || type == PixelFormat.Type.SSCALED)) {
      if (existingData instanceof ByteData.Numeric) {
        return (ByteData.Numeric) existingData;
      } else if (existingData instanceof CustomBinaryData) {
        CustomBinaryData<?> d = (CustomBinaryData<?>) existingData;
        if (d.getBinaryRepresentation().equals(Data.SINT8)) {
          return (CustomBinaryData<?>) existingData;
        }
      } else if (existingData instanceof ByteData) {
        return new ByteData.Numeric((ByteData) existingData);
      } else if (existingData instanceof ByteBuffer) {
        return new ByteData.Numeric(new ByteBufferData((ByteBuffer) existingData));
      } else if (existingData instanceof byte[]) {
        return new ByteData.Numeric(new ByteArrayData((byte[]) existingData));
      } else if (existingData instanceof Data.Factory) {
        return new ByteData.Numeric(((Data.Factory) existingData).newByteData(length));
      } else if (existingData == null) {
        return new ByteData.Numeric(Data.getDefaultDataFactory().newByteData(length));
      }
    } else if (bitSize == 16 && (type == PixelFormat.Type.SINT || type == PixelFormat.Type.SSCALED)) {
      if (existingData instanceof ShortData.Numeric) {
        return (ShortData.Numeric) existingData;
      } else if (existingData instanceof CustomBinaryData) {
        CustomBinaryData<?> d = (CustomBinaryData<?>) existingData;
        if (d.getBinaryRepresentation().equals(Data.SINT16)) {
          return (CustomBinaryData<?>) existingData;
        }
      } else if (existingData instanceof ShortData) {
        return new ShortData.Numeric((ShortData) existingData);
      } else if (existingData instanceof ShortBuffer) {
        return new ShortData.Numeric(new ShortBufferData((ShortBuffer) existingData));
      } else if (existingData instanceof short[]) {
        return new ShortData.Numeric(new ShortArrayData((short[]) existingData));
      } else if (existingData instanceof Data.Factory) {
        return new ShortData.Numeric(((Data.Factory) existingData).newShortData(length));
      } else if (existingData == null) {
        return new ShortData.Numeric(Data.getDefaultDataFactory().newShortData(length));
      }
    } else if (bitSize == 32 && (type == PixelFormat.Type.SINT || type == PixelFormat.Type.SSCALED)) {
      if (existingData instanceof IntData.Numeric) {
        return (IntData.Numeric) existingData;
      } else if (existingData instanceof CustomBinaryData) {
        CustomBinaryData<?> d = (CustomBinaryData<?>) existingData;
        if (d.getBinaryRepresentation().equals(Data.SINT32)) {
          return (CustomBinaryData<?>) existingData;
        }
      } else if (existingData instanceof IntData) {
        return new IntData.Numeric((IntData) existingData);
      } else if (existingData instanceof IntBuffer) {
        return new IntData.Numeric(new IntBufferData((IntBuffer) existingData));
      } else if (existingData instanceof int[]) {
        return new IntData.Numeric(new IntArrayData((int[]) existingData));
      } else if (existingData instanceof Data.Factory) {
        return new IntData.Numeric(((Data.Factory) existingData).newIntData(length));
      } else if (existingData == null) {
        return new IntData.Numeric(Data.getDefaultDataFactory().newIntData(length));
      }
    } else if (bitSize == 64 && (type == PixelFormat.Type.SINT || type == PixelFormat.Type.SSCALED)) {
      if (existingData instanceof LongData.Numeric) {
        return (LongData.Numeric) existingData;
      } else if (existingData instanceof CustomBinaryData) {
        CustomBinaryData<?> d = (CustomBinaryData<?>) existingData;
        if (d.getBinaryRepresentation().equals(Data.SINT64)) {
          return (CustomBinaryData<?>) existingData;
        }
      }else if (existingData instanceof LongData) {
        return new LongData.Numeric((LongData) existingData);
      } else if (existingData instanceof LongBuffer) {
        return new LongData.Numeric(new LongBufferData((LongBuffer) existingData));
      } else if (existingData instanceof long[]) {
        return new LongData.Numeric(new LongArrayData((long[]) existingData));
      } else if (existingData instanceof Data.Factory) {
        return new LongData.Numeric(((Data.Factory) existingData).newLongData(length));
      } else if (existingData == null) {
        return new LongData.Numeric(Data.getDefaultDataFactory().newLongData(length));
      }
    } else if (bitSize == 8 && type == PixelFormat.Type.UNORM) {
      if (existingData instanceof CustomBinaryData) {
        CustomBinaryData<?> d = (CustomBinaryData<?>) existingData;
        if (d.getBinaryRepresentation().equals(Data.UNORM8)) {
          return (CustomBinaryData<?>) existingData;
        }
      } else if (existingData instanceof ByteData) {
        return new CustomBinaryData<>(Data.UNORM8, (ByteData) existingData);
      } else if (existingData instanceof ByteBuffer) {
        return new CustomBinaryData<>(Data.UNORM8, new ByteBufferData((ByteBuffer) existingData));
      } else if (existingData instanceof byte[]) {
        return new CustomBinaryData<>(Data.UNORM8, new ByteArrayData((byte[]) existingData));
      } else if (existingData instanceof Data.Factory) {
        return new CustomBinaryData<>(Data.UNORM8, ((Data.Factory) existingData).newByteData(length));
      } else if (existingData == null) {
        return new CustomBinaryData<>(Data.UNORM8, Data.getDefaultDataFactory().newByteData(length));
      }
    } else if (bitSize == 16 && type == PixelFormat.Type.UNORM) {
      if (existingData instanceof CustomBinaryData) {
        CustomBinaryData<?> d = (CustomBinaryData<?>) existingData;
        if (d.getBinaryRepresentation().equals(Data.UNORM8)) {
          return (CustomBinaryData<?>) existingData;
        }
      } else if (existingData instanceof ShortData) {
        return new CustomBinaryData<>(Data.UNORM16, (ShortData) existingData);
      } else if (existingData instanceof ShortBuffer) {
        return new CustomBinaryData<>(Data.UNORM16, new ShortBufferData((ShortBuffer) existingData));
      } else if (existingData instanceof short[]) {
        return new CustomBinaryData<>(Data.UNORM16, new ShortArrayData((short[]) existingData));
      } else if (existingData instanceof Data.Factory) {
        return new CustomBinaryData<>(Data.UNORM16, ((Data.Factory) existingData).newShortData(length));
      } else if (existingData == null) {
        return new CustomBinaryData<>(Data.UNORM16, Data.getDefaultDataFactory().newShortData(length));
      }
    } else if (bitSize == 32 && type == PixelFormat.Type.UNORM) {
      if (existingData instanceof CustomBinaryData) {
        CustomBinaryData<?> d = (CustomBinaryData<?>) existingData;
        if (d.getBinaryRepresentation().equals(Data.UNORM8)) {
          return (CustomBinaryData<?>) existingData;
        }
      } else if (existingData instanceof IntData) {
        return new CustomBinaryData<>(Data.UNORM32, (IntData) existingData);
      } else if (existingData instanceof IntBuffer) {
        return new CustomBinaryData<>(Data.UNORM32, new IntBufferData((IntBuffer) existingData));
      } else if (existingData instanceof int[]) {
        return new CustomBinaryData<>(Data.UNORM32, new IntArrayData((int[]) existingData));
      } else if (existingData instanceof Data.Factory) {
        return new CustomBinaryData<>(Data.UNORM32, ((Data.Factory) existingData).newIntData(length));
      } else if (existingData == null) {
        return new CustomBinaryData<>(Data.UNORM32, Data.getDefaultDataFactory().newIntData(length));
      }
    } else if (bitSize == 64 && type == PixelFormat.Type.UNORM) {
      if (existingData instanceof CustomBinaryData) {
        CustomBinaryData<?> d = (CustomBinaryData<?>) existingData;
        if (d.getBinaryRepresentation().equals(Data.UNORM8)) {
          return (CustomBinaryData<?>) existingData;
        }
      } else if (existingData instanceof LongData) {
        return new CustomBinaryData<>(Data.UNORM64, (LongData) existingData);
      } else if (existingData instanceof LongBuffer) {
        return new CustomBinaryData<>(Data.UNORM64, new LongBufferData((LongBuffer) existingData));
      } else if (existingData instanceof long[]) {
        return new CustomBinaryData<>(Data.UNORM64, new LongArrayData((long[]) existingData));
      } else if (existingData instanceof Data.Factory) {
        return new CustomBinaryData<>(Data.UNORM64, ((Data.Factory) existingData).newLongData(length));
      } else if (existingData == null) {
        return new CustomBinaryData<>(Data.UNORM64, Data.getDefaultDataFactory().newLongData(length));
      }
    } else if (bitSize == 8 && type == PixelFormat.Type.SNORM) {
      if (existingData instanceof CustomBinaryData) {
        CustomBinaryData<?> d = (CustomBinaryData<?>) existingData;
        if (d.getBinaryRepresentation().equals(Data.SNORM8)) {
          return (CustomBinaryData<?>) existingData;
        }
      } else if (existingData instanceof ByteData) {
        return new CustomBinaryData<>(Data.SNORM8, (ByteData) existingData);
      } else if (existingData instanceof ByteBuffer) {
        return new CustomBinaryData<>(Data.SNORM8, new ByteBufferData((ByteBuffer) existingData));
      } else if (existingData instanceof byte[]) {
        return new CustomBinaryData<>(Data.SNORM8, new ByteArrayData((byte[]) existingData));
      } else if (existingData instanceof Data.Factory) {
        return new CustomBinaryData<>(Data.SNORM8, ((Data.Factory) existingData).newByteData(length));
      } else if (existingData == null) {
        return new CustomBinaryData<>(Data.SNORM8, Data.getDefaultDataFactory().newByteData(length));
      }
    } else if (bitSize == 16 && type == PixelFormat.Type.SNORM) {
      if (existingData instanceof CustomBinaryData) {
        CustomBinaryData<?> d = (CustomBinaryData<?>) existingData;
        if (d.getBinaryRepresentation().equals(Data.SNORM16)) {
          return (CustomBinaryData<?>) existingData;
        }
      } else if (existingData instanceof ShortData) {
        return new CustomBinaryData<>(Data.SNORM16, (ShortData) existingData);
      } else if (existingData instanceof ShortBuffer) {
        return new CustomBinaryData<>(Data.SNORM16, new ShortBufferData((ShortBuffer) existingData));
      } else if (existingData instanceof short[]) {
        return new CustomBinaryData<>(Data.SNORM16, new ShortArrayData((short[]) existingData));
      } else if (existingData instanceof Data.Factory) {
        return new CustomBinaryData<>(Data.SNORM16, ((Data.Factory) existingData).newShortData(length));
      } else if (existingData == null) {
        return new CustomBinaryData<>(Data.SNORM16, Data.getDefaultDataFactory().newShortData(length));
      }
    } else if (bitSize == 32 && type == PixelFormat.Type.SNORM) {
      if (existingData instanceof CustomBinaryData) {
        CustomBinaryData<?> d = (CustomBinaryData<?>) existingData;
        if (d.getBinaryRepresentation().equals(Data.SNORM32)) {
          return (CustomBinaryData<?>) existingData;
        }
      } else if (existingData instanceof IntData) {
        return new CustomBinaryData<>(Data.SNORM32, (IntData) existingData);
      } else if (existingData instanceof IntBuffer) {
        return new CustomBinaryData<>(Data.SNORM32, new IntBufferData((IntBuffer) existingData));
      } else if (existingData instanceof int[]) {
        return new CustomBinaryData<>(Data.SNORM32, new IntArrayData((int[]) existingData));
      } else if (existingData instanceof Data.Factory) {
        return new CustomBinaryData<>(Data.SNORM32, ((Data.Factory) existingData).newIntData(length));
      } else if (existingData == null) {
        return new CustomBinaryData<>(Data.SNORM32, Data.getDefaultDataFactory().newIntData(length));
      }
    } else if (bitSize == 64 && type == PixelFormat.Type.SNORM) {
      if (existingData instanceof CustomBinaryData) {
        CustomBinaryData<?> d = (CustomBinaryData<?>) existingData;
        if (d.getBinaryRepresentation().equals(Data.SNORM64)) {
          return (CustomBinaryData<?>) existingData;
        }
      } else if (existingData instanceof LongData) {
        return new CustomBinaryData<>(Data.SNORM64, (LongData) existingData);
      } else if (existingData instanceof LongBuffer) {
        return new CustomBinaryData<>(Data.SNORM64, new LongBufferData((LongBuffer) existingData));
      } else if (existingData instanceof long[]) {
        return new CustomBinaryData<>(Data.SNORM64, new LongArrayData((long[]) existingData));
      } else if (existingData instanceof Data.Factory) {
        return new CustomBinaryData<>(Data.SNORM64, ((Data.Factory) existingData).newLongData(length));
      } else if (existingData == null) {
        return new CustomBinaryData<>(Data.SNORM64, Data.getDefaultDataFactory().newLongData(length));
      }
    } else if (bitSize == 16 && type == PixelFormat.Type.SFLOAT) {
      if (existingData instanceof CustomBinaryData) {
        CustomBinaryData<?> d = (CustomBinaryData<?>) existingData;
        if (d.getBinaryRepresentation().equals(Data.SFLOAT16)) {
          return (CustomBinaryData<?>) existingData;
        }
      } else if (existingData instanceof ShortData) {
        return new CustomBinaryData<>(Data.SFLOAT16, (ShortData) existingData);
      } else if (existingData instanceof ShortBuffer) {
        return new CustomBinaryData<>(Data.SFLOAT16, new ShortBufferData((ShortBuffer) existingData));
      } else if (existingData instanceof short[]) {
        return new CustomBinaryData<>(Data.SFLOAT16, new ShortArrayData((short[]) existingData));
      } else if (existingData instanceof Data.Factory) {
        return new CustomBinaryData<>(Data.SFLOAT16, ((Data.Factory) existingData).newShortData(length));
      } else if (existingData == null) {
        return new CustomBinaryData<>(Data.SFLOAT16, Data.getDefaultDataFactory().newShortData(length));
      }
    } else if (bitSize == 32 && type == PixelFormat.Type.SFLOAT) {
      if (existingData instanceof CustomBinaryData) {
        CustomBinaryData<?> d = (CustomBinaryData<?>) existingData;
        if (d.getBinaryRepresentation().equals(Data.SFLOAT32)) {
          return (CustomBinaryData<?>) existingData;
        }
      } else if (existingData instanceof FloatData) {
        return (FloatData) existingData;
      } else if (existingData instanceof FloatBuffer) {
        return new FloatBufferData((FloatBuffer) existingData);
      } else if (existingData instanceof float[]) {
        return new FloatArrayData((float[]) existingData);
      } else if (existingData instanceof IntData) {
        return new CustomBinaryData<>(Data.SFLOAT32, (IntData) existingData);
      } else if (existingData instanceof IntBuffer) {
        return new CustomBinaryData<>(Data.SFLOAT32, new IntBufferData((IntBuffer) existingData));
      } else if (existingData instanceof int[]) {
        return new CustomBinaryData<>(Data.SFLOAT32, new IntArrayData((int[]) existingData));
      } else if (existingData instanceof Data.Factory) {
        return ((Data.Factory) existingData).newFloatData(length);
      } else if (existingData == null) {
        return Data.getDefaultDataFactory().newFloatData(length);
      }
    } else if (bitSize == 64 && type == PixelFormat.Type.SFLOAT) {
      if (existingData instanceof CustomBinaryData) {
        CustomBinaryData<?> d = (CustomBinaryData<?>) existingData;
        if (d.getBinaryRepresentation().equals(Data.SFLOAT64)) {
          return (CustomBinaryData<?>) existingData;
        }
      } else if (existingData instanceof DoubleData) {
        return (DoubleData) existingData;
      } else if (existingData instanceof DoubleBuffer) {
        return new DoubleBufferData((DoubleBuffer) existingData);
      } else if (existingData instanceof double[]) {
        return new DoubleArrayData((double[]) existingData);
      } else if (existingData instanceof LongData) {
        return new CustomBinaryData<>(Data.SFLOAT64, (LongData) existingData);
      } else if (existingData instanceof LongBuffer) {
        return new CustomBinaryData<>(Data.SFLOAT64, new LongBufferData((LongBuffer) existingData));
      } else if (existingData instanceof long[]) {
        return new CustomBinaryData<>(Data.SFLOAT64, new LongArrayData((long[]) existingData));
      } else if (existingData instanceof Data.Factory) {
        return ((Data.Factory) existingData).newDoubleData(length);
      } else if (existingData == null) {
        return Data.getDefaultDataFactory().newDoubleData(length);
      }
    } else {
      // If we get here, the bit size and pixel interpretation type combination is unknown
      throw new UnsupportedOperationException(String.format("Unknown bit size (%d) and pixel type combination (%s)", bitSize, type));
    }

    // If we get here, the bit size and type combination were known, but the existing data
    // provided was not compatible
    throw new UnsupportedOperationException(String.format("Existing data is incompatible with pixel format (%s, %d): %s", type, bitSize, existingData));
  }

  public NumericData<?> build() {
    if (length < 0) {
      throw new UnsupportedOperationException("DataSource length must be at least 0: " + length);
    }

    NumericData<?> source = buildDataSource();
    if (source.getLength() != length) {
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

  public DataBufferBuilder allocateNewData(Data.Factory factory) {
    existingData = factory;
    return this;
  }

  public DataBufferBuilder wrapDataBuffer(DataBuffer existing) {
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
}
