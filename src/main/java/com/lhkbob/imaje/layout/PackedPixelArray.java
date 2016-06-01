package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.data.BitDataSource;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.DataSource;
import com.lhkbob.imaje.data.types.BinaryRepresentation;
import com.lhkbob.imaje.data.types.SignedInteger;
import com.lhkbob.imaje.data.types.SignedNormalizedInteger;
import com.lhkbob.imaje.data.types.UnsignedInteger;
import com.lhkbob.imaje.data.types.UnsignedNormalizedInteger;
import com.lhkbob.imaje.util.Functions;

import java.util.function.Predicate;

/**
 *
 */
public class PackedPixelArray implements PixelArray {
  private final PixelLayout layout;
  private final PixelFormat format;
  private final BitDataSource data;

  private final BinaryRepresentation[] fields;
  private final long[] fieldMasks;
  private final long[] fieldShifts;

  public PackedPixelArray(PixelFormat format, PixelLayout layout, BitDataSource data) {
    if (layout.getChannelCount() != 1) {
      throw new IllegalArgumentException(
          "All color channel values must be packed into a single data channel");
    }
    if (data.getLength() < layout.getRequiredDataElements()) {
      throw new IllegalArgumentException(
          "Data source does not have sufficient elements for image layout");
    }
    if (format.getTotalBitSize() != data.getBitSize())
      throw new IllegalArgumentException("Total bit size of format does not match data source");

    fields = new BinaryRepresentation[format.getDataChannelCount()];
    fieldMasks = new long[fields.length];
    fieldShifts = new long[fields.length];

    // Count from the back so we can track total shift from right to left
    int shift = 0;
    for (int i = fields.length - 1; i >= 0; i--) {
      fields[i] = getRepresentation(format.getDataChannelType(i), format.getDataChannelBitSize(i));
      fieldMasks[i] = Functions.maskLong(format.getDataChannelBitSize(i));
      fieldShifts[i] = shift;
      shift += format.getDataChannelBitSize(i);
    }

    this.layout = layout;
    this.format = format;
    this.data = data;
  }

  private static BinaryRepresentation getRepresentation(PixelFormat.Type type, int size) {
    switch(type) {
    case UINT: case USCALED:
      return new UnsignedInteger(size);
    case SINT:
    case SSCALED:
      return new SignedInteger(size);
    case UNORM:
      return new UnsignedNormalizedInteger(size);
    case SNORM:
      return new SignedNormalizedInteger(size);
    case SFLOAT:
      if (size == 64)
        return Data.SFLOAT64;
      else if (size == 32)
        return Data.SFLOAT32;
      else if (size == 16)
        return Data.SFLOAT16;
      else
        throw new UnsupportedOperationException("Unexpected bit size for SFLOAT, unsure of mantissa and exponent bit allocations for size: " + size);
    case UFLOAT:
      throw new UnsupportedOperationException("UFLOAT channels are currently unsupported");
    default:
      throw new UnsupportedOperationException("Unknown type: " + type);
    }
  }

  @Override
  public PixelLayout getLayout() {
    return layout;
  }

  @Override
  public DataSource getData() {
    return data;
  }

  @Override
  public PixelFormat getFormat() {
    return format;
  }

  @Override
  public double get(int x, int y, double[] channelValues, int offset) {
    long index = layout.getChannelIndex(x, y, 0);
    return unpack(data.getBits(index), channelValues, offset);
  }

  @Override
  public double get(int x, int y, double[] channelValues, int offset, long[] channels) {
    layout.getChannelIndices(x, y, channels);
    return unpack(data.getBits(channels[0]), channelValues, offset);
  }

  @Override
  public double getAlpha(int x, int y) {
    if (format.hasAlphaChannel()) {
      long index = layout.getChannelIndex(x, y, 0);
      return bitFieldToDouble(data.getBits(index), format.getAlphaChannelDataIndex());
    } else {
      return 1.0;
    }
  }

  @Override
  public void set(int x, int y, double[] channelValues, int offset, double a) {
    long index = layout.getChannelIndex(x, y, 0);
    data.setBits(index, pack(channelValues, offset, a));
  }

  @Override
  public void set(int x, int y, double[] channelValues, int offset, double a, long[] channels) {
    layout.getChannelIndices(x, y, channels);
    data.setBits(channels[0], pack(channelValues, offset, a));
  }

  @Override
  public void setAlpha(int x, int y, double alpha) {
    if (format.hasAlphaChannel()) {
      long index = layout.getChannelIndex(x, y, 0);
      long bits = data.getBits(index);

      // Zero out original bit field
      int alphaDataChannelIndex = format.getAlphaChannelDataIndex();
      long alphaMask = fieldMasks[alphaDataChannelIndex] << fieldShifts[alphaDataChannelIndex];
      bits &= ~alphaMask;

      // Calculate new alpha bit field and or it into the remaining value, store into data source
      data.setBits(index, bits | doubleToBitField(alpha, alphaDataChannelIndex));
    } // else ignore set request
  }

  private long pack(double[] values, int offset, double a) {
    long bits = 0;
    for (int i = 0; i < format.getColorChannelCount(); i++) {
      int dataChannel = format.getColorChannelDataIndex(i);
      bits |= doubleToBitField(values[offset + i], dataChannel);
    }
    if (format.hasAlphaChannel()) {
      bits |= doubleToBitField(a, format.getAlphaChannelDataIndex());
    }

    return bits;
  }

  private double unpack(long bits, double[] values, int offset) {
    for (int i = 0; i < format.getColorChannelCount(); i++) {
      int dataChannel = format.getColorChannelDataIndex(i);
      values[offset + i] = bitFieldToDouble(bits, dataChannel);
    }

    if (format.hasAlphaChannel()) {
      return bitFieldToDouble(bits, format.getAlphaChannelDataIndex());
    } else {
      return 1.0;
    }
  }

  private double bitFieldToDouble(long bits, int field) {
    // First shift everything over so the LSB of the field is at bit 0
    bits >>= fieldShifts[field];
    // Next mask off everything to the right of the MSB of the field
    bits &= fieldMasks[field];

    return fields[field].toNumericValue(bits);
  }

  private long doubleToBitField(double value, int field) {
    long bits = fields[field].toBits(value);
    // Shift to the left so the bit field is aligned with the rest of the packed pixel
    // (Masking is not necessary assuming the binary representation contract is correct)
    return bits << fieldShifts[field];
  }

  @Override
  public Predicate<GPUFormat> getGPUFormatFilter() {
    return GPUFormat.bitSize(data.getBitSize()).and(GPUFormat.format(format)).and(GPUFormat::isPacked);
  }
}
