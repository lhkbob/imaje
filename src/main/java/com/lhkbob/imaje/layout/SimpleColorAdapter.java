package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.Depth;
import com.lhkbob.imaje.color.DepthStencil;
import com.lhkbob.imaje.color.SRGB;
import com.lhkbob.imaje.color.Stencil;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 */
public class SimpleColorAdapter<T extends Color> implements ColorAdapter<T>, PixelArrayBackedAdapter {
  private final PixelArray image;
  private final Class<T> type;

  private transient volatile GPUFormat format;

  public SimpleColorAdapter(Class<T> type, PixelArray image) {
    // SimpleColor mandates that all subclasses have a fixed number of logical channels, so all instances
    // of T will report the same number for getChannelCount(), and Color mandates that a public
    // default constructor is available
    try {
      if (type.newInstance().getChannelCount() != image.getFormat().getColorChannelCount()) {
        throw new IllegalArgumentException(
            "Logical channel count mismatch between data map and provided color type");
      }
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException("Color type does not provide public default constructor", e);
    }

    this.type = type;
    this.image = image;
    format = null;
  }

  @Override
  public PixelArray getPixelArray() {
    return image;
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
  public Iterator<ImageCoordinate> iterator() {
    return image.getLayout().iterator();
  }

  @Override
  public Spliterator<ImageCoordinate> spliterator() {
    return image.getLayout().spliterator();
  }

  @Override
  public double get(int x, int y, T result) {
    return image.get(x, y, result.getChannels());
  }

  @Override
  public double get(int x, int y, T result, long[] channels) {
    return image.get(x, y, result.getChannels(), channels);
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
  public void set(int x, int y, T value, double a) {
    image.set(x, y, value.getChannels(), a);
  }

  @Override
  public void set(int x, int y, T value, double a, long[] channels) {
    image.set(x, y, value.getChannels(), a, channels);
  }

  public void setAlpha(int x, int y, double alpha) {
    image.setAlpha(x, y, alpha);
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
        // First color channel semantics are either R (for majority of colors), or D for depth and
        // depth-stencil types, and stencil for stencil only types.
        if (Depth.class.isAssignableFrom(type) || DepthStencil.class.isAssignableFrom(type)) {
          semantic = GPUFormat.Channel.D;
        } else if (Stencil.class.isAssignableFrom(type)) {
          semantic = GPUFormat.Channel.S;
        } else {
          semantic = GPUFormat.Channel.R;
        }
      } else if (i == 1) {
        if (DepthStencil.class.isAssignableFrom(type)) {
          semantic = GPUFormat.Channel.S;
        } else {
          semantic = GPUFormat.Channel.G;
        }
      } else if (i == 2) {
        semantic = GPUFormat.Channel.B;
      } else if (i == 3) {
        // 4th color channel maps to A so that CMYK and other 4 channel colors can still
        // be sent to the GPU even if it doesn't actually represent alpha values
        semantic = GPUFormat.Channel.A;
      } else {
        semantic = GPUFormat.Channel.X;
      }

      channels[pf.getColorChannelDataIndex(i)] = semantic;
    }

    // Include alpha semantic if necessary
    if (pf.hasAlphaChannel()) {
      channels[pf.getAlphaChannelDataIndex()] = GPUFormat.Channel.A;
    }

    // Fill in any skipped data channels with X
    for (int i = 0; i < channels.length; i++) {
      if (channels[i] == null) {
        channels[i] = GPUFormat.Channel.X;
      }
    }

    Stream<GPUFormat> formats = GPUFormat.streamAll().filter(image.getGPUFormatFilter())
        .filter(GPUFormat.channelLayout(channels));
    // Reduce the set of formats depending on if the color type is sRGB or not
    Predicate<GPUFormat> isSRGB = GPUFormat::isSRGB;
    if (SRGB.class.isAssignableFrom(type)) {
      // Reduce to an SRGB format if possible, but take a plain format if no SRGB is available
      return formats.reduce(GPUFormat.UNDEFINED, (a, b) -> {
        if (b.isSRGB() && !a.isSRGB()) {
          // Upgrade the non-SRGB format to b
          return b;
        } else if (a == GPUFormat.UNDEFINED && b != GPUFormat.UNDEFINED) {
          // Upgrade to a real format
          return b;
        } else {
          // Stick with a
          return a;
        }
      });
    } else {
      // Filter out sRGB typed formats and then report first
      return formats.filter(isSRGB.negate()).findFirst().orElse(GPUFormat.UNDEFINED);
    }
  }

  @Override
  public boolean isGPUCompatible() {
    return image.getData().isGPUAccessible() && image.getLayout().isGPUCompatible()
        && getFormat() != GPUFormat.UNDEFINED;
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
