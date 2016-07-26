package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.layout.ImageCoordinate;
import com.lhkbob.imaje.layout.PixelArray;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 *
 */
public class Raster<T extends Color> implements Image<T> {
  private final PixelArray data;
  private final Class<T> colorType;

  public Raster(Class<T> colorType, PixelArray data) {
    if (Color.getChannelCount(colorType) != data.getFormat().getColorChannelCount()) {
      throw new IllegalArgumentException(String.format(
          "PixelArray color channel count(%d) does not equal channel count of color(%d, %s)",
          data.getFormat().getColorChannelCount(), Color.getChannelCount(colorType), colorType));
    } this.colorType = colorType;
    this.data = data;
  }

  public double get(int x, int y, T result) {
    return data.get(x, y, result.getChannels());
  }

  public double getAlpha(int x, int y) {
    return data.getAlpha(x, y);
  }

  @Override
  public int getWidth() {
    return data.getLayout().getWidth();
  }

  @Override
  public int getHeight() {
    return data.getLayout().getHeight();
  }

  @Override
  public int getLayerCount() {
    return 1;
  }

  @Override
  public int getMipmapCount() {
    return 1;
  }

  @Override
  public Pixel<T> getPixel(int x, int y, int mipmapLevel, int layer) {
    if (mipmapLevel != 0) {
      throw new IllegalArgumentException(
          "Image is not mipmapped, expected mipmap level of 0, not: " + mipmapLevel);
    }
    if (layer != 0) {
      throw new IllegalArgumentException(
          "Image is not multilayered, layer must be 0, not: " + layer);
    }
    return getPixel(x, y);
  }

  @Override
  public Class<T> getColorType() {
    return colorType;
  }

  public Pixel<T> getPixel(int x, int y) {
    return getPixelForMipmapArray(x, y, 0, 0);
  }

  @Override
  public boolean hasAlphaChannel() {
    return data.getFormat().hasAlphaChannel();
  }

  @Override
  public Iterator<Pixel<T>> iterator() {
    return iteratorForMipmapArray(0, 0);
  }

  public void set(int x, int y, T value) {
    data.set(x, y, value.getChannels(), data.getAlpha(x, y));
  }

  public void set(int x, int y, T value, double alpha) {
    data.set(x, y, value.getChannels(), alpha);
  }

  public void setAlpha(int x, int y, double alpha) {
    data.setAlpha(x, y, alpha);
  }

  @Override
  public Spliterator<Pixel<T>> spliterator() {
    return spliteratorForMipmapArray(0, 0);
  }

  private void checkImageCoordinates(int x, int y) {
    if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
      throw new IndexOutOfBoundsException(
          "(x, y) coordinates are outside of image dimensions: " + x + ", " + y);
    }
  }

  public PixelArray getPixelArray() {
    return data;
  }

  Pixel<T> getPixelForMipmapArray(int x, int y, int level, int layer) {
    checkImageCoordinates(x, y);
    DefaultPixel<T> p = new DefaultPixel<>(colorType, data);
    p.setPixel(x, y);
    p.setLevel(level);
    p.setLayer(layer);
    return p;
  }

  Iterator<Pixel<T>> iteratorForMipmapArray(int level, int layer) {
    return new RasterIterator(level, layer);
  }

  Spliterator<Pixel<T>> spliteratorForMipmapArray(int level, int layer) {
    return new RasterSpliterator(level, layer);
  }

  private class RasterIterator implements Iterator<Pixel<T>> {
    private final Iterator<ImageCoordinate> coords;
    private final DefaultPixel<T> pixel;

    public RasterIterator(int level, int layer) {
      coords = data.getLayout().iterator();
      pixel = new DefaultPixel<>(colorType, data);
      pixel.setLevel(level);
      pixel.setLayer(layer);
    }

    @Override
    public boolean hasNext() {
      return coords.hasNext();
    }

    @Override
    public Pixel<T> next() {
      ImageCoordinate c = coords.next();
      pixel.setPixel(c.getX(), c.getY());
      return pixel;
    }
  }

  private class RasterSpliterator implements Spliterator<Pixel<T>> {
    private final Spliterator<ImageCoordinate> coords;
    private final DefaultPixel<T> pixel;

    public RasterSpliterator(int level, int layer) {
      this(data.getLayout().spliterator(), level, layer);
    }

    public RasterSpliterator(Spliterator<ImageCoordinate> coords, int level, int layer) {
      this.coords = coords;
      pixel = new DefaultPixel<>(colorType, data);
      pixel.setLevel(level);
      pixel.setLayer(layer);
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
        pixel.setPixel(coord.getX(), coord.getY());
        action.accept(pixel);
      });
    }

    @Override
    public Spliterator<Pixel<T>> trySplit() {
      Spliterator<ImageCoordinate> split = coords.trySplit();
      if (split == null) {
        return null;
      } else {
        return new RasterSpliterator(split, pixel.getLevel(), pixel.getLayer());
      }
    }
  }
}
