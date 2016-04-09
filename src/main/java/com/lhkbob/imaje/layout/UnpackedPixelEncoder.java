package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.data.ByteSource;
import com.lhkbob.imaje.data.IntSource;
import com.lhkbob.imaje.data.LongSource;
import com.lhkbob.imaje.data.NumericDataSource;
import com.lhkbob.imaje.data.ShortSource;
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

import java.util.function.Predicate;

import static com.lhkbob.imaje.layout.GPUFormat.Type.SFLOAT;
import static com.lhkbob.imaje.layout.GPUFormat.Type.SNORM;
import static com.lhkbob.imaje.layout.GPUFormat.Type.SRGB;
import static com.lhkbob.imaje.layout.GPUFormat.Type.SSCALED;
import static com.lhkbob.imaje.layout.GPUFormat.Type.UNORM;
import static com.lhkbob.imaje.layout.GPUFormat.Type.USCALED;
import static com.lhkbob.imaje.layout.GPUFormat.bitSize;
import static com.lhkbob.imaje.layout.GPUFormat.dataType;

/**
 *
 */
public class UnpackedPixelEncoder extends PixelEncoder {
  private final NumericDataSource data;

  public UnpackedPixelEncoder(
      int[] dataToLogicalChannelMap, PixelLayout layout, NumericDataSource data) {
    super(dataToLogicalChannelMap, layout);
    if (dataToLogicalChannelMap.length != layout.getChannelCount()) {
      throw new IllegalArgumentException(
          "PixelLayout is incompatible with channel mapping, requires "
              + dataToLogicalChannelMap.length + " data channels, but layout provides " + layout
              .getChannelCount());
    }
    if (data.getLength() < layout.getRequiredDataElements()) {
      throw new IllegalArgumentException(
          "Data source does not have sufficient elements for image layout");
    }

    this.data = data;
  }

  @Override
  public NumericDataSource getData() {
    return data;
  }

  @Override
  public double get(int x, int y, double[] channelValues, int offset) {
    for (int i = 0; i < logicalToDataChannelMap.length; i++) {
      int dataChannel = logicalToDataChannelMap[i];
      channelValues[offset + i] = data.getValue(layout.getChannelIndex(x, y, dataChannel));
    }

    if (hasAlphaChannel()) {
      return data.getValue(layout.getChannelIndex(x, y, alphaDataChannelIndex));
    } else {
      return 1.0;
    }
  }

  @Override
  public double get(int x, int y, double[] channelValues, int offset, long[] channels) {
    layout.getChannelIndices(x, y, channels);
    for (int i = 0; i < logicalToDataChannelMap.length; i++) {
      int dataChannel = logicalToDataChannelMap[i];
      channelValues[offset + i] = data.getValue(channels[dataChannel]);
    }

    if (hasAlphaChannel()) {
      return data.getValue(channels[alphaDataChannelIndex]);
    } else {
      return 1.0;
    }
  }

  @Override
  public double getAlpha(int x, int y) {
    if (hasAlphaChannel()) {
      return data.getValue(layout.getChannelIndex(x, y, alphaDataChannelIndex));
    } else {
      // No alpha channel
      return 1.0;
    }
  }

  @Override
  public void set(int x, int y, double[] channelValues, int offset, double a) {
    for (int i = 0; i < logicalToDataChannelMap.length; i++) {
      int dataChannel = logicalToDataChannelMap[i];
      data.setValue(layout.getChannelIndex(x, y, dataChannel), channelValues[offset + i]);
    }

    if (hasAlphaChannel()) {
      data.setValue(layout.getChannelIndex(x, y, alphaDataChannelIndex), a);
    } // otherwise no alpha channel so ignore the set request
  }

  @Override
  public void set(int x, int y, double[] channelValues, int offset, double a, long[] channels) {
    layout.getChannelIndices(x, y, channels);
    for (int i = 0; i < logicalToDataChannelMap.length; i++) {
      int dataChannel = logicalToDataChannelMap[i];
      data.setValue(channels[dataChannel], channelValues[offset + i]);
    }

    if (hasAlphaChannel()) {
      data.setValue(channels[alphaDataChannelIndex], a);
    } // otherwise no alpha channel so ignore the set request
  }

  @Override
  public void setAlpha(int x, int y, double alpha) {
    if (hasAlphaChannel()) {
      data.setValue(layout.getChannelIndex(x, y, alphaDataChannelIndex), alpha);
    } // otherwise no alpha channel so ignore the set request
  }

  @Override
  public Predicate<GPUFormat> getFormatFilter() {
    Predicate<GPUFormat> typeFilter;
    if (data instanceof NormalizedUnsignedByteSource || data instanceof NormalizedUnsignedIntSource
        || data instanceof NormalizedUnsignedShortSource
        || data instanceof NormalizedUnsignedLongSource) {
      typeFilter = dataType(UNORM).or(dataType(SRGB));
    } else if (data instanceof NormalizedByteSource || data instanceof NormalizedIntSource
        || data instanceof NormalizedShortSource || data instanceof NormalizedLongSource) {
      typeFilter = dataType(SNORM);
    } else if (data instanceof UnsignedByteSource || data instanceof UnsignedIntSource
        || data instanceof UnsignedShortSource || data instanceof UnsignedLongSource) {
      typeFilter = dataType(USCALED);
    } else if (data instanceof ByteSource || data instanceof IntSource
        || data instanceof ShortSource || data instanceof LongSource) {
      typeFilter = dataType(SSCALED);
    } else {
      typeFilter = dataType(SFLOAT);
    }

    return super.getFormatFilter().and(GPUFormat::isUnpackedLayout).and(bitSize(data.getBitSize()))
        .and(typeFilter);
  }
}
