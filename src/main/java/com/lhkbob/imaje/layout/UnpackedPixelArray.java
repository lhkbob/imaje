package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.data.ByteSource;
import com.lhkbob.imaje.data.DoubleSource;
import com.lhkbob.imaje.data.FloatSource;
import com.lhkbob.imaje.data.IntSource;
import com.lhkbob.imaje.data.LongSource;
import com.lhkbob.imaje.data.NumericDataSource;
import com.lhkbob.imaje.data.ShortSource;
import com.lhkbob.imaje.data.types.BinaryNumericSource;
import com.lhkbob.imaje.data.types.SignedInteger;
import com.lhkbob.imaje.data.types.SignedNormalizedInteger;
import com.lhkbob.imaje.data.types.UnsignedInteger;
import com.lhkbob.imaje.data.types.UnsignedNormalizedInteger;

import java.util.function.Predicate;

/**
 *
 */
public class UnpackedPixelArray implements PixelArray {
  private final PixelLayout layout;
  private final PixelFormat format;
  private final NumericDataSource data;

  public UnpackedPixelArray(PixelFormat format, PixelLayout layout, NumericDataSource data) {
    if (format.getDataChannelCount() != layout.getChannelCount()) {
      throw new IllegalArgumentException(
          "PixelLayout is incompatible with PixelFormat, requires " + format.getDataChannelCount()
              + " data channels, but layout provides " + layout.getChannelCount());
    }
    if (data.getLength() < layout.getRequiredDataElements()) {
      throw new IllegalArgumentException(
          "Data source does not have sufficient elements for image layout");
    }

    // Now verify that the format fits the mold expected of unpacked types
    // i.e. bit size for each channel is the same and type of each channel is the same.
    // - because data access goes directly to the numeric data source, this checks all data channels
    //   even those that are skipped by the format (although only bit size is validated then).

    // There's always at least one color channel, which has a bit size and type
    // (unlike with randomly accessing a data channel, which could have a null type if skipped)
    int channelBitSize = format.getColorChannelBitSize(0);
    PixelFormat.Type channelType = format.getColorChannelType(0);
    for (int i = 0; i < format.getDataChannelCount(); i++) {
      if (format.getDataChannelType(i) != null && format.getDataChannelType(i) != channelType) {
        throw new IllegalArgumentException(
            "Data channel " + i + " has incorrect type for an unpacked encoder, expected "
                + channelType + " but was " + format.getDataChannelType(i));
      }
      if (format.getDataChannelBitSize(i) != channelBitSize) {
        throw new IllegalArgumentException(
            "Data channel " + i + " has incorrect bit size for an unpacked encoder, expected "
                + channelBitSize + " but was " + format.getDataChannelBitSize(i));
      }
    }

    // At this point the format is self-consistent with an unpacked format but make sure the
    // numeric data source provides a compatible type
    if (channelBitSize != data.getBitSize()) {
      throw new IllegalArgumentException(
          "Channel bit size is incompatible with data source bit size, expected " + channelBitSize
              + " but was " + data.getBitSize());
    }

    boolean badType = true;
    switch (channelType) {
    case UINT:
    case USCALED:
      // Must go through a known type with UINT semantics
      if (data instanceof BinaryNumericSource && ((BinaryNumericSource) data)
          .getBinaryRepresentation() instanceof UnsignedInteger) {
        badType = false;
      }
    case SINT:
    case SSCALED:
      // Native short, int, long, byte is preferred
      if (data instanceof ByteSource || data instanceof ShortSource || data instanceof IntSource
          || data instanceof LongSource) {
        badType = false;
      } else if (data instanceof BinaryNumericSource && ((BinaryNumericSource) data)
          .getBinaryRepresentation() instanceof SignedInteger) {
        badType = false;
      }
      break;
    case UNORM:
      // Must go through a known type with UNORM semantics
      if (data instanceof BinaryNumericSource && ((BinaryNumericSource) data)
          .getBinaryRepresentation() instanceof UnsignedNormalizedInteger) {
        badType = false;
      }
      break;
    case SNORM:
      // Must go through a known type with SNORM semantics
      if (data instanceof BinaryNumericSource && ((BinaryNumericSource) data)
          .getBinaryRepresentation() instanceof SignedNormalizedInteger) {
        badType = false;
      }
      break;
    case SFLOAT:
      // Native float or double is preferred
      if (data instanceof FloatSource || data instanceof DoubleSource) {
        badType = false;
      } else if (data instanceof BinaryNumericSource) {
        // Only other valid source type is a BinaryNumericSource with one of the known SFLOAT types
        BinaryNumericSource d = (BinaryNumericSource) data;
        if (d.getBinaryRepresentation().isFloatingPoint() && !d.getBinaryRepresentation()
            .isUnsigned()) {
          badType = false;
        }
      }
      break;
    case UFLOAT:
      if (data instanceof BinaryNumericSource) {
        BinaryNumericSource d = (BinaryNumericSource) data;
        if (d.getBinaryRepresentation().isFloatingPoint() && d.getBinaryRepresentation()
            .isUnsigned()) {
          badType = false;
        }
      }
      break;
    }

    if (badType) {
      throw new IllegalArgumentException(
          "Unsupported DataSource type (" + data + ") for type " + channelType);
    }

    // Validation complete
    this.data = data;
    this.format = format;
    this.layout = layout;
  }

  @Override
  public PixelLayout getLayout() {
    return layout;
  }

  @Override
  public PixelFormat getFormat() {
    return format;
  }

  @Override
  public NumericDataSource getData() {
    return data;
  }

  @Override
  public double get(int x, int y, double[] channelValues, int offset) {
    for (int i = 0; i < format.getColorChannelCount(); i++) {
      int dataChannel = format.getColorChannelDataIndex(i);
      channelValues[offset + i] = data.getValue(layout.getChannelIndex(x, y, dataChannel));
    }

    if (format.hasAlphaChannel()) {
      return data.getValue(layout.getChannelIndex(x, y, format.getAlphaChannelDataIndex()));
    } else {
      return 1.0;
    }
  }

  @Override
  public double get(int x, int y, double[] channelValues, int offset, long[] channels) {
    layout.getChannelIndices(x, y, channels);
    for (int i = 0; i < format.getColorChannelCount(); i++) {
      int dataChannel = format.getColorChannelDataIndex(i);
      channelValues[offset + i] = data.getValue(channels[dataChannel]);
    }

    if (format.hasAlphaChannel()) {
      return data.getValue(channels[format.getAlphaChannelDataIndex()]);
    } else {
      return 1.0;
    }
  }

  @Override
  public double getAlpha(int x, int y) {
    if (format.hasAlphaChannel()) {
      return data.getValue(layout.getChannelIndex(x, y, format.getAlphaChannelDataIndex()));
    } else {
      // No alpha channel
      return 1.0;
    }
  }

  @Override
  public void set(int x, int y, double[] channelValues, int offset, double a) {
    for (int i = 0; i < format.getColorChannelCount(); i++) {
      int dataChannel = format.getColorChannelDataIndex(i);
      data.setValue(layout.getChannelIndex(x, y, dataChannel), channelValues[offset + i]);
    }

    if (format.hasAlphaChannel()) {
      data.setValue(layout.getChannelIndex(x, y, format.getAlphaChannelDataIndex()), a);
    } // otherwise no alpha channel so ignore the set request
  }

  @Override
  public void set(int x, int y, double[] channelValues, int offset, double a, long[] channels) {
    layout.getChannelIndices(x, y, channels);
    for (int i = 0; i < format.getColorChannelCount(); i++) {
      int dataChannel = format.getColorChannelDataIndex(i);
      data.setValue(channels[dataChannel], channelValues[offset + i]);
    }

    if (format.hasAlphaChannel()) {
      data.setValue(channels[format.getAlphaChannelDataIndex()], a);
    } // otherwise no alpha channel so ignore the set request
  }

  @Override
  public void setAlpha(int x, int y, double alpha) {
    if (format.hasAlphaChannel()) {
      data.setValue(layout.getChannelIndex(x, y, format.getAlphaChannelDataIndex()), alpha);
    } // otherwise no alpha channel so ignore the set request
  }

  @Override
  public Predicate<GPUFormat> getGPUFormatFilter() {
    return GPUFormat.bitSize(data.getBitSize()).and(GPUFormat.format(format)).and(GPUFormat::isUnpackedLayout);
  }
}
