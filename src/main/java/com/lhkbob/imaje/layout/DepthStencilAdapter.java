package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.DepthStencil;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class DepthStencilAdapter<T extends DepthStencil> implements ColorAdapter<T> {
  private final PixelArray image;
  private final Class<T> type;

  private final AtomicReference<double[]> tempChannels;
  private transient volatile GPUFormat format;

  public DepthStencilAdapter(Class<T> type, PixelArray image) {
    this.type = type;
    this.image = image;
    tempChannels = new AtomicReference<>(new double[2]);
  }

  @Override
  public int getWidth() {
    return image.getLayout().getWidth();
  }

  @Override
  public int getHeight() {
    return image.getLayout().getHeight();
  }

  @Override
  public double get(int x, int y, T result) {
    double[] temp = getTempChannelData();
    try {
      double alpha = image.get(x, y, temp, 0);
      result.setDepth(temp[0]);
      result.setStencil((int) temp[1]);
      return alpha;
    } finally {
      returnTempChannelData(temp);
    }
  }

  @Override
  public double get(int x, int y, T result, long[] channels) {
    double[] temp = getTempChannelData();
    try {
      double alpha = image.get(x, y, temp, 0, channels);
      result.setDepth(temp[0]);
      result.setStencil((int) temp[1]);
      return alpha;
    } finally {
      returnTempChannelData(temp);
    }
  }

  @Override
  public double getAlpha(int x, int y) {
    return image.getAlpha(x, y);
  }

  @Override
  public Class<T> getType() {
    return type;
  }

  @Override
  public Iterator<ImageCoordinate> iterator() {
    return image.getLayout().iterator();
  }

  @Override
  public Spliterator<ImageCoordinate> spliterator() {
    return image.getLayout().spliterator();
  }

  @Override
  public void set(int x, int y, T value, double a) {
    double[] temp = getTempChannelData();
    try {
      temp[0] = value.getDepth();
      temp[1] = value.getStencil();
      image.set(x, y, temp, 0, a);
    } finally {
      returnTempChannelData(temp);
    }
  }

  @Override
  public void set(int x, int y, T value, double a, long[] channels) {
    double[] temp = getTempChannelData();
    try {
      temp[0] = value.getDepth();
      temp[1] = value.getStencil();
      image.set(x, y, temp, 0, a, channels);
    } finally {
      returnTempChannelData(temp);
    }
  }

  private double[] getTempChannelData() {
    double[] temp = tempChannels.getAndSet(null);
    if (temp == null) {
      // Another thread already grabbed the cached array so just allocate a new one; this is unlikely
      // to happen and the atomic reference offers better performance characteristics than
      // maintaining an entire ThreadLocal map for such a cache.
      temp = new double[2];
    }
    return temp;
  }

  private void returnTempChannelData(double[] data) {
    // If it is null, store the provided 2-element array, which may have been the original or
    // one allocated but released sooner than the original. It doesn't really matter since the
    // only contract is that one array be made available. When the reference is already null, the
    // provided data is quietly discarded since it is no longer needed and can be reclaimed.
    tempChannels.compareAndSet(null, data);
  }

  @Override
  public void setAlpha(int x, int y, double a) {
    image.setAlpha(x, y, a);
  }

  @Override
  public GPUFormat getFormat() {
    // The GPUFormat selection should always be consistent, so even in the event of a race condition
    // to assign the value to format, it will be the same enum value and only cost duplicated effort.
    if (format != null) {
      return format;
    }

    PixelFormat pf = image.getFormat();
    GPUFormat.Channel[] channels = new GPUFormat.Channel[pf.getDataChannelCount()];

    // Map integer based color channels to GPU semantics
    for (int i = 0; i < pf.getColorChannelCount(); i++) {
      GPUFormat.Channel semantic;

      if (i == 0) {
        semantic = GPUFormat.Channel.D;
      } else if (i == 1) {
        semantic = GPUFormat.Channel.S;
      } else {
        semantic = GPUFormat.Channel.X;
      }

      channels[pf.getColorChannelDataIndex(i)] = semantic;
    }

    // Include alpha semantic if necessary
    if (pf.hasAlphaChannel()) {
      channels[pf.getAlphaChannelDataIndex()] = GPUFormat.Channel.A;
    }

    // Fill in any skipped data channels with X (which for most depth stencil formats will be at
    // least one field of padding)
    for (int i = 0; i < channels.length; i++) {
      if (channels[i] == null)
        channels[i] = GPUFormat.Channel.X;
    }

    Stream<GPUFormat> formats = GPUFormat.streamAll();
    // First filter based on the pixel format and array
    formats = formats.filter(image.getGPUFormatFilter());
    // Next filter based on channel semantics
    formats = formats.filter(GPUFormat.channelLayout(channels));

    if (formats.count() > 1) {
      // This should not happen given the current set of GPU formats and data source
      // implementations and their mappings onto data types, so is considered an error
      throw new RuntimeException("Ambiguous gpu format: " + Arrays.toString(formats.toArray()));
    } else {
      // Cache the calculated format since it cannot change as image format and layout are final
      format = formats.findAny().orElse(GPUFormat.UNDEFINED);
      return format;
    }
  }

  @Override
  public boolean isGPUCompatible() {
    return image.getLayout().isGPUCompatible() && image.getData().isGPUAccessible() && getFormat() != GPUFormat.UNDEFINED;
  }

  @Override
  public boolean hasAlphaChannel() {
    return image.getFormat().hasAlphaChannel();
  }

  @Override
  public long[] createCompatibleChannelArray() {
    return new long[image.getLayout().getChannelCount()];
  }
}
