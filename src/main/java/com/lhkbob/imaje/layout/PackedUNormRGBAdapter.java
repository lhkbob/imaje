package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.SRGB;
import com.lhkbob.imaje.data.IntSource;
import com.lhkbob.imaje.data.ShortSource;

/**
 *
 */
public abstract class PackedUNormRGBAdapter<T extends RGB> implements PixelAdapter<T> {
  private final Class<T> rgbType;
  protected final PixelLayout layout;
  private final RGBOrder order;
  private final GPUFormat format;

  private final int[] masks; // Also the scaling factor to convert normalized double to fixed point
  private final int[] shifts;
  private final double[] toNormScalars;

  protected PackedUNormRGBAdapter(Class<T> rgbType, RGBOrder order, PixelLayout layout, int[] channelBits, long dataLength,
      GPUFormat format) {
    if (layout.getChannelCount() != 1) {
      throw new IllegalArgumentException("All color channel values must be packed into a single data channel");
    }
    if (channelBits.length != 3 && channelBits.length != 4) {
      throw new IllegalArgumentException("Channel bits specification must be length 3 or 4");
    }
    if (layout.getRequiredDataElements() > dataLength) {
      throw new IllegalArgumentException("Insufficient data provided in data source for specified PixelLayout");
    }

    // RGB or BGR when channelBits.length == 3 (e.g. no alpha)
    if (channelBits.length == 3 && order != RGBOrder.RGB && order != RGBOrder.BGR) {
      throw new IllegalArgumentException("Order must be RGB or BGR for alpha-less data");
    }
    // Anything but RGB or BGR when channelBits.length == 4 (e.g. has alpha)
    if (channelBits.length == 4 && (order == RGBOrder.RGB || order == RGBOrder.BGR)) {
      throw new IllegalArgumentException("Order must include an alpha channel");
    }

    // Convert sequential channel bit allocations into masks and shifts to extract those ranges
    masks = new int[channelBits.length];
    shifts = new int[channelBits.length];
    toNormScalars = new double[channelBits.length];

    // Validate the channel bits (count from the back so we can use the intermediate totalBits
    // value as the shift for that channel)
    int totalBits = 0;
    for (int i = channelBits.length - 1; i >= 0; i--) {
      shifts[i] = totalBits;
      masks[i] = (1 << channelBits[i]) - 1;
      toNormScalars[i] = 1.0 / masks[i];
      totalBits += channelBits[i];
    }

    if (totalBits != 16 && totalBits != 32)
      throw new IllegalArgumentException("Channel bit specification must total 16 or 32 bits, not: " + totalBits);

    this.rgbType = rgbType;
    this.layout = layout;
    this.order = order;
    this.format = format;
  }

  public static <T extends RGB> PackedUNormRGBAdapter<T> newR5G6B5Adapter(Class<T> rgbType, PixelLayout layout, ShortSource data) {
    return new ShortSourceAdapter<>(rgbType, RGBOrder.RGB, layout, new int[] { 5, 6, 5}, data, GPUFormat.R5G6B5_UNORM_PACK16);
  }

  public static <T extends RGB> PackedUNormRGBAdapter<T> newB5G6R5Adapter(Class<T> rgbType, PixelLayout layout, ShortSource data) {
    return new ShortSourceAdapter<>(rgbType, RGBOrder.BGR, layout, new int[] { 5, 6, 5 }, data, GPUFormat.B5G6R5_UNORM_PACK16);
  }

  public static <T extends RGB> PackedUNormRGBAdapter<T> newR4G4B4A4Adapter(Class<T> rgbType, PixelLayout layout, ShortSource data) {
    return new ShortSourceAdapter<>(rgbType, RGBOrder.RGBA, layout, new int[] {4, 4, 4, 4}, data, GPUFormat.R4G4B4A4_UNORM_PACK16);
  }

  public static <T extends RGB> PackedUNormRGBAdapter<T> newB4G4R4A4Adapter(Class<T> rgbType, PixelLayout layout, ShortSource data) {
    return new ShortSourceAdapter<>(rgbType, RGBOrder.BGRA, layout, new int[] { 4, 4, 4, 4 }, data,
        GPUFormat.B4G4R4A4_UNORM_PACK16);
  }

  public static <T extends RGB> PackedUNormRGBAdapter<T> newR5G5B5A1Adapter(
      Class<T> rgbType, PixelLayout layout, ShortSource data) {
    return new ShortSourceAdapter<>(rgbType, RGBOrder.RGBA, layout, new int[] { 5, 5, 5, 1}, data,
        GPUFormat.R5G5B5A1_UNORM_PACK16);
  }

  public static <T extends RGB> PackedUNormRGBAdapter<T> newB5G5R5A1Adapter(
      Class<T> rgbType, PixelLayout layout, ShortSource data) {
    return new ShortSourceAdapter<>(rgbType, RGBOrder.BGRA, layout, new int[] { 5, 5, 5, 1 }, data,
        GPUFormat.B5G5R5A1_UNORM_PACK16);
  }

  public static <T extends RGB> PackedUNormRGBAdapter<T> newA1R5G5B5Adapter(
      Class<T> rgbType, PixelLayout layout, ShortSource data) {
    return new ShortSourceAdapter<>(rgbType, RGBOrder.ARGB, layout, new int[] { 1, 5, 5, 5 }, data,
        GPUFormat.A1R5G5B5_UNORM_PACK16);
  }

  public static <T extends RGB> PackedUNormRGBAdapter<T> newA8B8G8R8Adapter(
      Class<T> rgbType, PixelLayout layout, IntSource data) {
    GPUFormat format = (rgbType.equals(SRGB.class) ? GPUFormat.A8B8G8R8_SRGB_PACK32 : GPUFormat.A8B8G8R8_UNORM_PACK32);
    return new IntSourceAdapter<>(rgbType, RGBOrder.ABGR, layout, new int[] { 8, 8, 8, 8}, data, format);
  }

  public static <T extends RGB> PackedUNormRGBAdapter<T> newA2R10G10B10Adapter(
      Class<T> rgbType, PixelLayout layout, IntSource data) {
    return new IntSourceAdapter<>(rgbType, RGBOrder.ARGB, layout, new int[] {2, 10, 10, 10 }, data, GPUFormat.A2R10G10B10_UNORM_PACK32);
  }

  public static <T extends RGB> PackedUNormRGBAdapter<T> newA2B10G10R10Adapter(
      Class<T> rgbType, PixelLayout layout, IntSource data) {
    return new IntSourceAdapter<>(rgbType, RGBOrder.ABGR, layout, new int[] { 2, 10, 10, 10 }, data,
        GPUFormat.A2B10G10R10_UNORM_PACK32);
  }

  @Override
  public double get(int x, int y, T result) {
    int bitfield = getPackedValue(layout.getChannelIndex(x, y, 0));
    return unpack(bitfield, result);
  }

  @Override
  public double get(int x, int y, T result, long[] channels) {
    layout.getChannelIndices(x, y, channels);
    return unpack(getPackedValue(channels[0]), result);
  }

  @Override
  public Class<T> getType() {
    return rgbType;
  }

  @Override
  public void set(int x, int y, T value, double a) {
    int bitfield = pack(value, a);
    setPackedValue(layout.getChannelIndex(x, y, 0), bitfield);
  }

  @Override
  public void set(int x, int y, T value, double a, long[] channels) {
    layout.getChannelIndices(x, y, channels);
    setPackedValue(channels[0], pack(value, a));
  }

  @Override
  public GPUFormat getFormat() {
    return format;
  }

  @Override
  public boolean hasAlphaChannel() {
    return order != RGBOrder.RGB && order != RGBOrder.BGR;
  }

  @Override
  public long[] createCompatibleChannelArray() {
    return new long[1];
  }

  protected abstract int getPackedValue(long index);

  protected abstract void setPackedValue(long index, int value);

  private double unpack(int packedValue, T result) {
    double c1 = ((packedValue >> shifts[0]) & masks[0]) * toNormScalars[0];
    double c2 = ((packedValue >> shifts[1]) & masks[1]) * toNormScalars[1];
    double c3 = ((packedValue >> shifts[2]) & masks[2]) * toNormScalars[2];

    if (order == RGBOrder.BGR || order == RGBOrder.RGB) {
      // Only need 3 channels and alpha defaults to 1.0
      if (order == RGBOrder.BGR) {
        result.b(c1);
        result.g(c2);
        result.r(c3);
      } else {
        result.r(c1);
        result.g(c2);
        result.b(c3);
      }
      return 1.0;
    } else {
      // Must extract 4 channels, 1 of which is the returned alpha
      double c4 = ((packedValue >> shifts[3]) & masks[3]) * toNormScalars[3];
      switch(order) {
      case RGBA:
        result.r(c1);
        result.g(c2);
        result.b(c3);
        return c4;
      case ARGB:
        result.r(c2);
        result.g(c3);
        result.b(c4);
        return c1;
      case BGRA:
        result.b(c1);
        result.g(c2);
        result.r(c3);
        return c4;
      case ABGR:
        result.b(c2);
        result.g(c3);
        result.r(c4);
        return c1;
      default:
        throw new UnsupportedOperationException("Unexpected RGBOrder value");
      }
    }
  }

  private int pack(T value, double a) {
    double c1, c2, c3, c4;
    switch(order) {
    case RGB:
    case RGBA:
      c1 = value.r();
      c2 = value.g();
      c3 = value.b();
      c4 = a;
      break;
    case ARGB:
      c1 = a;
      c2 = value.r();
      c3 = value.g();
      c4 = value.b();
      break;
    case BGR:
    case BGRA:
      c1 = value.b();
      c2 = value.g();
      c3 = value.r();
      c4 = a;
      break;
    case ABGR:
      c1 = a;
      c2 = value.b();
      c3 = value.g();
      c4 = value.r();
      break;
    default:
      throw new UnsupportedOperationException("Unexpected RGBOrder value");
    }

    // Clamp all values to be between 0 and 1 and convert to shifted bit fields
    int d1 = (((int) (Math.max(0.0, Math.min(c1, 1.0))) * masks[0]) & masks[0]) << shifts[0];
    int d2 = (((int) (Math.max(0.0, Math.min(c2, 1.0))) * masks[1]) & masks[1]) << shifts[1];
    int d3 = (((int) (Math.max(0.0, Math.min(c3, 1.0))) * masks[2]) & masks[2]) << shifts[2];

    if (order == RGBOrder.BGR || order == RGBOrder.RGB) {
      // Only use d1, d2, d3
      return d1 | d2 | d3;
    } else {
      int d4 = (((int) (Math.max(0.0, Math.min(c4, 1.0))) * masks[3]) & masks[3]) << shifts[3];
      return d1 | d2 | d3 | d4;
    }
  }

  private static class ShortSourceAdapter<T extends RGB> extends PackedUNormRGBAdapter<T> {
    private final ShortSource data;

    protected ShortSourceAdapter(
        Class<T> rgbType, RGBOrder order, PixelLayout layout, int[] channelBits, ShortSource data,
        GPUFormat format) {
      super(rgbType, order, layout, channelBits, data.getLength(), format);
      this.data = data;
    }

    @Override
    protected int getPackedValue(long index) {
      return data.get(index);
    }

    @Override
    protected void setPackedValue(long index, int value) {
      data.set(index, (short) value);
    }

    @Override
    public boolean isGPUCompatible() {
      return data.isGPUAccessible() && layout.isGPUCompatible();
    }
  }

  private static class IntSourceAdapter<T extends RGB> extends PackedUNormRGBAdapter<T> {
    private final IntSource data;

    protected IntSourceAdapter(
        Class<T> rgbType, RGBOrder order, PixelLayout layout, int[] channelBits, IntSource data,
        GPUFormat format) {
      super(rgbType, order, layout, channelBits, data.getLength(), format);
      this.data = data;
    }

    @Override
    protected int getPackedValue(long index) {
      return data.get(index);
    }

    @Override
    protected void setPackedValue(long index, int value) {
      data.set(index, value);
    }

    @Override
    public boolean isGPUCompatible() {
      return data.isGPUAccessible() && layout.isGPUCompatible();
    }
  }
}
