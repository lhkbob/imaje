package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.Pixel;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.util.Arguments;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 *
 */
public class ArrayBackedPixel<T extends Color> implements Pixel<T> {
  private int x;
  private int y;

  private final int[] otherCoords;
  private final int layer;
  private final int level;

  private final PixelArray data;
  private final long[] channels;

  private final T cachedColor;
  private transient double cachedAlpha;

  public ArrayBackedPixel(
      Class<T> colorType, PixelArray data, int layer, int mipmap, int... fixedCoords) {
    Arguments.notNull("colorType", colorType);
    Arguments.notNull("data", data);

    this.data = data;
    this.channels = new long[data.getLayout().getChannelCount()];
    cachedColor = Color.newInstance(colorType);
    otherCoords = fixedCoords.clone();
    level = mipmap;
    this.layer = layer;

    // Final validation to make sure channel counts are compatible
    Arguments.equals("channel count", cachedColor.getChannelCount(),
        data.getFormat().getColorChannelCount());
  }

  @Override
  public T getColor() {
    return cachedColor;
  }

  @Override
  public double getAlpha() {
    return cachedAlpha;
  }

  @Override
  public int getLayerIndex() {
    return layer;
  }

  @Override
  public int getMipmapLevel() {
    return level;
  }

  @Override
  public int getDimensionality() {
    return 2 + otherCoords.length;
  }

  @Override
  public int getCoordinate(int dim) {
    if (dim == 0) {
      return x;
    } else if (dim == 1) {
      return y;
    } else if (dim - 2 < otherCoords.length) {
      return otherCoords[dim - 2];
    } else {
      return 0;
    }
  }


  @Override
  public void setColor(T value) {
    setColor(value, cachedAlpha);
  }

  @Override
  public void setColor(T value, double a) {
    Arguments.notNull("value", value);

    if (value != cachedColor) {
      // Copy the state of value into the internal cachedColor instance so that future calls to getColor
      // are accurate
      cachedColor.set(value.getChannels());
    }

    // Now that cachedColor matches value, persist will correctly store the new value
    persist(a);
  }

  @Override
  public void setAlpha(double a) {
    cachedAlpha = a;
    data.setAlpha(x, y, a);
  }

  @Override
  public void persist() {
    persist(cachedAlpha);
  }

  @Override
  public void persist(double alpha) {
    cachedAlpha = alpha;
    data.set(x, y, cachedColor.getChannels(), alpha, channels);
  }

  @Override
  public void refresh() {
    refreshAt(x, y);
  }

  public void refreshAt(int x, int y) {
    cachedAlpha = data.get(x, y, cachedColor.getChannels(), channels);
    // The PixelArray validates x and y, so if code reaches here it was a valid coordinate and we
    // can update the pixel's location
    this.x = x;
    this.y = y;
  }

  public static <T extends Color> Iterator<Pixel<T>> iterator(
      Class<T> colorType, PixelArray data, int layer, int mipmap, int... fixedDims) {
    return new DefaultIterator<>(colorType, data, layer, mipmap, fixedDims);
  }

  public static <T extends Color> Spliterator<Pixel<T>> spliterator(
      Class<T> colorType, PixelArray data, int layer, int mipmap, int... fixedDims) {
    return new DefaultSpliterator<>(colorType, data, layer, mipmap, fixedDims);
  }

  private static class DefaultIterator<T extends Color> implements Iterator<Pixel<T>> {
    private final Iterator<ImageCoordinate> coords;
    private final ArrayBackedPixel<T> pixel;

    DefaultIterator(Class<T> colorType, PixelArray data, int layer, int mipmap, int... fixedDims) {
      this.coords = data.getLayout().iterator();
      pixel = new ArrayBackedPixel<>(colorType, data, layer, mipmap, fixedDims);
    }

    @Override
    public boolean hasNext() {
      return coords.hasNext();
    }

    @Override
    public Pixel<T> next() {
      ImageCoordinate c = coords.next();
      pixel.refreshAt(c.getX(), c.getY());
      return pixel;
    }
  }

  private static class DefaultSpliterator<T extends Color> implements Spliterator<Pixel<T>> {
    private final Spliterator<ImageCoordinate> coords;
    private final ArrayBackedPixel<T> pixel;
    private final Class<T> colorType;

    DefaultSpliterator(Class<T> colorType, PixelArray data, int layer, int mipmap, int... fixedDims) {
      this(colorType, data, data.getLayout().spliterator(), layer, mipmap, fixedDims);
    }

    DefaultSpliterator(
        Class<T> colorType, PixelArray data, Spliterator<ImageCoordinate> coords, int layer, int mipmap, int... fixedDims) {
      this.coords = coords;
      this.colorType = colorType;
      pixel = new ArrayBackedPixel<>(colorType, data, layer, mipmap, fixedDims);
    }

    @Override
    public int characteristics() {
      return coords.characteristics();
    }

    @Override
    public long estimateSize() {
      return coords.estimateSize();
    }

    @Override
    public boolean tryAdvance(Consumer<? super Pixel<T>> action) {
      return coords.tryAdvance(coord -> {
        pixel.refreshAt(coord.getX(), coord.getY());
        action.accept(pixel);
      });
    }

    @Override
    public Spliterator<Pixel<T>> trySplit() {
      Spliterator<ImageCoordinate> split = coords.trySplit();
      if (split == null) {
        return null;
      } else {
        return new DefaultSpliterator<>(
            colorType, pixel.data, split, pixel.layer, pixel.level, pixel.otherCoords);
      }
    }
  }
}
