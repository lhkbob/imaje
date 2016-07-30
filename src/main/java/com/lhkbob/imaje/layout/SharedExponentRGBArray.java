package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.data.BitData;
import com.lhkbob.imaje.data.types.UnsignedSharedExponent;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.Functions;

/**
 *
 */
public class SharedExponentRGBArray implements PixelArray {
  private final PixelFormat format;
  private final PixelLayout layout;
  private final BitData data;
  private final long offset;

  private final UnsignedSharedExponent exp;

  public SharedExponentRGBArray(PixelFormat format, PixelLayout layout, BitData data, long offset) {
    Arguments.isGreaterThanOrEqualToZero("offset", offset);
    Arguments.equals("layout.getChannelCount()", 1, layout.getChannelCount());
    Arguments.equals("bit size", format.getTotalBitSize(), data.getBitSize());
    Arguments
        .checkArrayRange("data length", data.getLength(), offset, layout.getRequiredDataElements());

    if (format.hasAlphaChannel()) {
      throw new IllegalArgumentException(
          "Alpha channels are not supported for shared exponent formats");
    }
    if (format.getColorChannelCount() < 3) {
      throw new IllegalArgumentException(
          "Must have at least 3 color channels, where one is reserved for the exponent field");
    }

    long exponentMask = 0L;
    long[] mantissaMasks = new long[format.getColorChannelCount() - 1];
    // Count from the back so we can track total shift from right to left
    int shift = 0;
    for (int i = format.getDataChannelCount() - 1; i >= 0; i--) {

      if (!format.isDataChannelSkipped(i)) {
        long mask = Functions.maskLong(format.getDataChannelBitSize(i)) << shift;
        int channel = format.getDataChannelColorIndex(i);
        if (channel == mantissaMasks.length) {
          // This is the exponent
          exponentMask = mask;
        } else {
          mantissaMasks[channel] = mask;
        }
      }

      shift += format.getDataChannelBitSize(i);
    }

    exp = new UnsignedSharedExponent(exponentMask, mantissaMasks);
    this.format = format;
    this.layout = layout;
    this.data = data;
    this.offset = offset;
  }

  @Override
  public PixelLayout getLayout() {
    return layout;
  }

  @Override
  public BitData getData() {
    return data;
  }

  @Override
  public PixelFormat getFormat() {
    return format;
  }

  public UnsignedSharedExponent getTypeConverter() {
    return exp;
  }

  @Override
  public long getDataOffset() {
    return offset;
  }

  @Override
  public double get(int x, int y, double[] channelValues) {
    long bits = data.getBits(offset + layout.getChannelIndex(x, y, 0));
    // Expand all floating point values from bits into the given channel array
    exp.toNumericValues(bits, channelValues);

    // Always return 1.0 since there is never an alpha channel
    return 1.0;
  }

  @Override
  public double get(int x, int y, double[] channelValues, long[] channels) {
    layout.getChannelIndices(x, y, channels);
    // Expand all floating point values from bits into the given channel array
    exp.toNumericValues(data.getBits(offset + channels[0]), channelValues);

    // Always return 1.0 since there is never an alpha channel
    return 1.0;
  }

  @Override
  public double getAlpha(int x, int y) {
    // Always return 1.0 since there is never an alpha channel
    return 1.0;
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a) {
    // Ignore alpha value
    long encodedBits = exp.toBits(channelValues);
    data.setBits(offset + layout.getChannelIndex(x, y, 0), encodedBits);
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a, long[] channels) {
    // Ignore alpha value
    long encodedBits = exp.toBits(channelValues);
    layout.getChannelIndices(x, y, channels);
    data.setBits(offset + channels[0], encodedBits);
  }

  @Override
  public void setAlpha(int x, int y, double alpha) {
    // Do nothing, there is never an alpha channel
  }
}
