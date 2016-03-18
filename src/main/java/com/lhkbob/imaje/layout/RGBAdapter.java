package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.SRGB;
import com.lhkbob.imaje.data.DoubleSource;

import java.util.stream.Stream;

/**
 *
 */
public class RGBAdapter<T extends RGB> extends AbstractSingleSource3ComponentAdapter<T> {
  private final RGBOrder order;

  private RGBAdapter(
      Class<T> type, RGBOrder order, PixelLayout layout, DoubleSource data) {
    super(type, layout, order == RGBOrder.ARGB || order == RGBOrder.ABGR, data);
    if (layout.getChannelCount() == 3 && order != RGBOrder.RGB && order != RGBOrder.BGR) {
      throw new IllegalArgumentException(
          "Pixel layout with 3 data channels must have an order of RGB or BGR (no alpha)");
    }
    this.order = order;
  }

  public static <T extends RGB> RGBAdapter<T> newABGRAdapter(
      Class<T> type, PixelLayout layout, DoubleSource data) {
    return new RGBAdapter<>(type, RGBOrder.ABGR, layout, data);
  }

  public static <T extends RGB> RGBAdapter<T> newARGBAdapter(
      Class<T> type, PixelLayout layout, DoubleSource data) {
    return new RGBAdapter<>(type, RGBOrder.ARGB, layout, data);
  }

  public static <T extends RGB> RGBAdapter<T> newBGRAAdapter(
      Class<T> type, PixelLayout layout, DoubleSource data) {
    return new RGBAdapter<>(type, RGBOrder.BGRA, layout, data);
  }

  public static <T extends RGB> RGBAdapter<T> newBGRAdapter(
      Class<T> type, PixelLayout layout, DoubleSource data) {
    return new RGBAdapter<>(type, RGBOrder.BGR, layout, data);
  }

  public static <T extends RGB> RGBAdapter<T> newRGBAAdapter(
      Class<T> type, PixelLayout layout, DoubleSource data) {
    return new RGBAdapter<>(type, RGBOrder.RGBA, layout, data);
  }

  public static <T extends RGB> RGBAdapter<T> newRGBAdapter(
      Class<T> type, PixelLayout layout, DoubleSource data) {
    return new RGBAdapter<>(type, RGBOrder.RGB, layout, data);
  }

  @Override
  protected Stream<GPUFormat> filterFormats(Stream<GPUFormat> compat) {
    // First filter based on explicitly requested component ordering
    switch (order) {
    case RGB:
      compat = compat.filter(GPUFormats.RGB);
      break;
    case RGBA:
      compat = compat.filter(GPUFormats.RGBA);
      break;
    case ARGB:
      compat = compat.filter(GPUFormats.ARGB);
      break;
    case BGR:
      compat = compat.filter(GPUFormats.BGR);
      break;
    case BGRA:
      compat = compat.filter(GPUFormats.BGRA);
      break;
    case ABGR:
      compat = compat.filter(GPUFormats.ABGR);
      break;
    }

    // If the color type is equal to SRGB try to further restrict it, but
    // since the GPU isn't consistent with all RGB orderings offering sRGB support,
    // don't skip the option of just returning a plain-typed format.
    if (getType().equals(SRGB.class) && compat.anyMatch(GPUFormats.SRGB)) {
      return compat.filter(GPUFormats.SRGB);
    } else {
      return compat;
    }
  }

  @Override
  protected void get(double c1, double c2, double c3, T result) {
    switch (order) {
    case RGB:
    case RGBA:
    case ARGB:
      result.r(c1);
      result.g(c2);
      result.b(c3);
      break;
    case BGR:
    case BGRA:
    case ABGR:
      result.b(c1);
      result.g(c2);
      result.r(c3);
      break;
    default:
      throw new UnsupportedOperationException("Unknown RGBOrder value: " + order);
    }
  }

  @Override
  protected void set(T value, long i1, long i2, long i3, DoubleSource data) {
    switch (order) {

    case RGB:
    case RGBA:
    case ARGB:
      data.set(i1, value.r());
      data.set(i2, value.g());
      data.set(i3, value.b());
      break;
    case BGR:
    case BGRA:
    case ABGR:
      data.set(i1, value.b());
      data.set(i2, value.g());
      data.set(i3, value.r());
      break;
    default:
      throw new UnsupportedOperationException("Unknown RGBOrder value: " + order);
    }
  }
}
