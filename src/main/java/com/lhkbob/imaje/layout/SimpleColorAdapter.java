package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.Depth;
import com.lhkbob.imaje.color.SRGB;
import com.lhkbob.imaje.color.SimpleColor;
import com.lhkbob.imaje.data.FloatSource;
import com.lhkbob.imaje.data.adapter.NormalizedUnsignedShortSource;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.lhkbob.imaje.layout.GPUFormat.dataType;
import static com.sun.javafx.tools.resource.DeployResource.Type.data;

/**
 *
 */
public class SimpleColorAdapter<T extends SimpleColor> implements PixelAdapter<T> {
  private final PixelEncoder encoder;
  private final Class<T> type;

  public SimpleColorAdapter(Class<T> type, PixelEncoder encoder) {
    // SimpleColor mandates that all subclasses have a fixed number of logical channels, so all instances
    // of T will report the same number for getChannelCount(), and Color mandates that a public
    // default constructor is available
    try {
      if (type.newInstance().getChannelCount() != encoder.getColorChannelCount())
        throw new IllegalArgumentException("Logical channel count mismatch between data map and provided color type");
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException("Color type does not provide public default constructor", e);
    }

    this.type = type;
    this.encoder = encoder;
  }

  @Override
  public int getWidth() {
    return encoder.getLayout().getWidth();
  }

  @Override
  public int getHeight() {
    return encoder.getLayout().getHeight();
  }

  @Override
  public Iterator<ImageCoordinate> iterator() {
    return encoder.getLayout().iterator();
  }

  @Override
  public Spliterator<ImageCoordinate> spliterator() {
    return encoder.getLayout().spliterator();
  }

  @Override
  public double get(int x, int y, T result) {
    return encoder.get(x, y, result.getChannelData(), 0);
  }

  @Override
  public double get(int x, int y, T result, long[] channels) {
    return encoder.get(x, y, result.getChannelData(), 0, channels);
  }

  @Override
  public double getAlpha(int x, int y) {
    return encoder.getAlpha(x, y);
  }

  @Override
  public Class<T> getType() {
    return type;
  }

  @Override
  public void set(int x, int y, T value, double a) {
    encoder.set(x, y, value.getChannelData(), 0, a);
  }

  @Override
  public void set(int x, int y, T value, double a, long[] channels) {
    encoder.set(x, y, value.getChannelData(), 0, a, channels);
  }

  public void setAlpha(int x, int y, double alpha) {
    encoder.setAlpha(x, y, alpha);
  }

  @Override
  public GPUFormat getFormat() {
    if(Depth.class.isAssignableFrom(type) && !hasAlphaChannel()
        && encoder.getLayout().getChannelCount() == 1) {
      if (encoder.getData() instanceof NormalizedUnsignedShortSource)
        return GPUFormat.D16_UNORM;
      else if (encoder.getData() instanceof FloatSource)
        return GPUFormat.D32_SFLOAT;
    }

    Stream<GPUFormat> compat = GPUFormat.streamAll().filter(encoder.getFormatFilter());
    // If the color type is equal to SRGB try to further restrict it, but
    // since the GPU isn't consistent with all RGB orderings offering sRGB support,
    // don't skip the option of just returning a plain-typed format.
    Predicate<GPUFormat> isSRGB = GPUFormat.dataType(GPUFormat.Type.SRGB);
    if (getType().equals(SRGB.class) && compat.anyMatch(isSRGB)) {
      compat = compat.filter(isSRGB);
    } else {
      // But also exclude SRGB that was reported along with the UNORM data formats
      compat = compat.filter(isSRGB.negate());
    }

    if (compat.count() > 1) {
      // This should not happen given the current set of GPU formats and data source
      // implementations and their mappings onto data types, so is considered an error
      throw new RuntimeException("Ambiguous gpu format: " + Arrays.toString(compat.toArray()));
    } else {
      return compat.findAny().orElse(GPUFormat.UNDEFINED);
    }
  }

  @Override
  public boolean isGPUCompatible() {
    return encoder.getData().isGPUAccessible() && encoder.getLayout().isGPUCompatible();
  }

  @Override
  public boolean hasAlphaChannel() {
    return encoder.hasAlphaChannel();
  }

  @Override
  public long[] createCompatibleChannelArray() {
    return new long[encoder.getLayout().getChannelCount()];
  }
}
