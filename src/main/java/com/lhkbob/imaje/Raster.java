package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.layout.ColorAdapter;
import com.lhkbob.imaje.layout.ImageCoordinate;
import com.lhkbob.imaje.layout.PixelArray;
import com.lhkbob.imaje.layout.PixelArrayBackedAdapter;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 *
 */
public class Raster<T extends Color> implements Image<T> {
  private final ColorAdapter<T> data;

  public Raster(ColorAdapter<T> data) {
    this.data = data;
  }

  public double get(int x, int y, T result) {
    return data.get(x, y, result);
  }

  public double getAlpha(int x, int y) {
    return data.getAlpha(x, y);
  }

  @Override
  public int getWidth() {
    return data.getWidth();
  }

  @Override
  public int getHeight() {
    return data.getHeight();
  }

  @Override
  public Class<T> getColorType() {
    return data.getType();
  }

  public Pixel<T> getPixel(int x, int y) {
    return getPixelForMipmapArray(x, y, 0, 0);
  }

  @Override
  public boolean hasAlphaChannel() {
    return data.hasAlphaChannel();
  }

  @Override
  public Iterator<Pixel<T>> iterator() {
    return iteratorForMipmapArray(0, 0);
  }

  public void set(int x, int y, T value) {
    data.set(x, y, value, data.getAlpha(x, y));
  }

  public void set(int x, int y, T value, double alpha) {
    data.set(x, y, value, alpha);
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

  ColorAdapter<T> getColorAdapter() {
    return data;
  }

  PixelArray getPixelArray() {
    if (data instanceof PixelArrayBackedAdapter) {
      return ((PixelArrayBackedAdapter) data).getPixelArray();
    } else {
      return null;
    }
  }

  Pixel<T> getPixelForMipmapArray(int x, int y, int level, int layer) {
    checkImageCoordinates(x, y);
    DefaultPixel<T> p = new DefaultPixel<>(data);
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
      coords = data.iterator();
      pixel = new DefaultPixel<>(data);
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
      this(data.spliterator(), level, layer);
    }

    public RasterSpliterator(Spliterator<ImageCoordinate> coords, int level, int layer) {
      this.coords = coords;
      pixel = new DefaultPixel<>(data);
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
