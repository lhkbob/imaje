package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.data.DoubleSource;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 *
 */
public abstract class AbstractSingleSource3ComponentAdapter<T extends Color> implements PixelAdapter<T> {
  private final DoubleSource data;
  private final PixelLayout layout;
  private final Class<T> type;
  private final boolean alphaIsFirst;

  public AbstractSingleSource3ComponentAdapter(Class<T> type, PixelLayout layout, boolean alphaIsFirst, DoubleSource data) {
    if (layout.getChannelCount() != 3 && layout.getChannelCount() != 4) {
      throw new IllegalArgumentException("PixelLayout must have 3 or 4 data channels");
    }
    if (data.getLength() < layout.getRequiredDataElements()) {
      throw new IllegalArgumentException("Data source does not have sufficient elements for image layout");
    }

    this.alphaIsFirst = alphaIsFirst;
    this.type = type;
    this.layout = layout;
    this.data = data;
  }

  @Override
  public double get(int x, int y, T result) {
    long i1 = layout.getChannelIndex(x, y, 0);
    long i2 = layout.getChannelIndex(x, y, 1);
    long i3 = layout.getChannelIndex(x, y, 2);

    if (layout.getChannelCount() == 3) {
      // No alpha to worry about
      get(data.get(i1), data.get(i2), data.get(i3), result);
      return 1.0;
    } else {
      // Compute 4th channel index and permute channels based on specified alpha order
      long i4 = layout.getChannelIndex(x, y, 3);
      if (alphaIsFirst) {
        get(data.get(i2), data.get(i3), data.get(i4), result);
        return data.get(i1);
      } else {
        get(data.get(i1), data.get(i2), data.get(i3), result);
        return data.get(i4);
      }
    }
  }

  @Override
  public double get(int x, int y, T result, long[] channels) {
    layout.getChannelIndices(x, y, channels);
    if (layout.getChannelCount() == 3) {
      // No alpha to worry about
      get(data.get(channels[0]), data.get(channels[1]), data.get(channels[2]), result);
      return 1.0;
    } else {
      // 4th channel index was already calculated so just permute channel order as needed
      if (alphaIsFirst) {
        get(data.get(channels[1]), data.get(channels[2]), data.get(channels[3]), result);
        return data.get(channels[0]);
      } else {
        get(data.get(channels[0]), data.get(channels[1]), data.get(channels[2]), result);
        return data.get(channels[3]);
      }
    }
  }

  @Override
  public Class<T> getType() {
    return type;
  }

  @Override
  public void set(int x, int y, T value, double a) {
    long i1 = layout.getChannelIndex(x, y, 0);
    long i2 = layout.getChannelIndex(x, y, 1);
    long i3 = layout.getChannelIndex(x, y, 2);

    if (layout.getChannelCount() == 3) {
      // No alpha to worry about
      set(value, i1, i2, i3, data);
    } else {
      // Compute 4th channel and permute index based on alpha order; save alpha channel explicitly
      long i4 = layout.getChannelIndex(x, y, 3);
      if (alphaIsFirst) {
        data.set(i1, a);
        set(value, i2, i3, i4, data);
      } else {
        data.set(i4, a);
        set(value, i1, i2, i3, data);
      }
    }
  }

  @Override
  public void set(int x, int y, T value, double a, long[] channels) {
    layout.getChannelIndices(x, y, channels);

    if (layout.getChannelCount() == 3) {
      // No alpha to worry about
      set(value, channels[0], channels[1], channels[2], data);
    } else {
      // 4th channel already computed, just permute index based on alpha order; save alpha channel explicitly
      if (alphaIsFirst) {
        data.set(channels[0], a);
        set(value, channels[1], channels[2], channels[3], data);
      } else {
        data.set(channels[3], a);
        set(value, channels[0], channels[1], channels[2], data);
      }
    }
  }

  @Override
  public GPUFormat getFormat() {
    Stream<GPUFormat> compat = GPUFormats.streamCompatible(data);
    compat = filterFormats(compat);
    if (compat.count() > 1) {
      // This should not happen given the current set of GPU formats and data source
      // implementations and their mappings onto data types, so is considered an error
      throw new RuntimeException("Ambiguous gpu format: " + Arrays.toString(compat.toArray()));
    } else {
      return compat.findAny().orElse(null);
    }
  }

  @Override
  public boolean isGPUCompatible() {
    return data.isGPUAccessible() && layout.isGPUCompatible();
  }

  @Override
  public boolean hasAlphaChannel() {
    return layout.getChannelCount() == 4;
  }

  @Override
  public long[] createCompatibleChannelArray() {
    return new long[layout.getChannelCount()];
  }

  protected Stream<GPUFormat> filterFormats(Stream<GPUFormat> compatible) {
    if (hasAlphaChannel()) {
      if (alphaIsFirst) {
        return compatible.filter(GPUFormats.ARGB);
      } else {
        return compatible.filter(GPUFormats.RGBA);
      }
    } else {
      return compatible.filter(GPUFormats.RGB);
    }
  }

  protected abstract void get(double c1, double c2, double c3, T result);
  protected abstract void set(T value, long i1, long i2, long i3, DoubleSource data);
}
