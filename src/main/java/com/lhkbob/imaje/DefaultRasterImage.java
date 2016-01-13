package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.data.DataSource;
import com.lhkbob.imaje.data.DoubleSource;
import com.lhkbob.imaje.layout.ImageCoordinate;
import com.lhkbob.imaje.layout.PixelAdapter;
import com.lhkbob.imaje.layout.PixelLayout;

import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 *
 */
public class DefaultRasterImage<T extends Color> implements RasterImage<T> {
  private final DoubleSource alphas;
  private final PixelAdapter<T> colors;
  private final PixelLayout layout;

  public DefaultRasterImage(PixelLayout layout, PixelAdapter<T> colors) {
    this(layout, colors, null);
  }

  public DefaultRasterImage(PixelLayout layout, PixelAdapter<T> colors, DoubleSource alphas) {
    long numPixels = layout.getWidth() * layout.getHeight();
    if (alphas != null && alphas.getLength() != numPixels) {
      throw new IllegalArgumentException(
          "Alpha channel does not have expected number of values, was: " + alphas.getLength()
              + ", expected: " + numPixels);
    }
    for (Map.Entry<String, ? extends DataSource<?>> c : colors.getChannels().entrySet()) {
      if (c.getValue().getLength() != numPixels) {
        throw new IllegalArgumentException(
            c.getKey() + " channel does not have expected number of values, was: " + c.getValue()
                .getLength() + ", expected: " + numPixels);
      }
    }

    this.layout = layout;
    this.colors = colors;
    this.alphas = alphas;
  }

  @Override
  public void get(int x, int y, T result) {
    colors.get(getPixelIndex(x, y), result);
  }

  @Override
  public double getAlpha(int x, int y) {
    return alphas.get(getPixelIndex(x, y));
  }

  @Override
  public Class<T> getColorType() {
    return colors.getType();
  }

  @Override
  public int getHeight() {
    return layout.getHeight();
  }

  @Override
  public int getLayerCount() {
    return 1;
  }

  @Override
  public Map<String, String> getMetadata() {
    throw new UnsupportedOperationException("NOT IMPLEMENTED");
  }

  @Override
  public int getMipmapCount() {
    return 1;
  }

  @Override
  public Pixel<T> getPixel(int x, int y) {
    checkImageCoordinates(x, y);
    RasterPixel p = new RasterPixel();
    p.x = x;
    p.y = y;
    return p;
  }

  @Override
  public Pixel<T> getPixel(int x, int y, int level, int layer) {
    // Accept level and layer as 0s (e.g. a single raster image)
    if (level == 0 && layer == 0) {
      return getPixel(x, y);
    } else {
      throw new IndexOutOfBoundsException(
          "Mipmap level and array layer do not exist: " + level + ", " + layer);
    }
  }

  @Override
  public int getWidth() {
    return layout.getWidth();
  }

  @Override
  public boolean hasAlphaChannel() {
    return alphas != null;
  }

  @Override
  public Iterator<Pixel<T>> iterator() {
    return new RasterIterator();
  }

  @Override
  public void set(int x, int y, T value) {
    colors.set(getPixelIndex(x, y), value);
  }

  @Override
  public void setAlpha(int x, int y, double alpha) {
    alphas.set(getPixelIndex(x, y), alpha);
  }

  @Override
  public Spliterator<Pixel<T>> spliterator() {
    return new RasterSpliterator();
  }

  private void checkImageCoordinates(int x, int y) {
    if (x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) {
      throw new IndexOutOfBoundsException(
          "(x, y) coordinates are outside of image dimensions: " + x + ", " + y);
    }
  }

  private long getPixelIndex(int x, int y) {
    checkImageCoordinates(x, y);
    return layout.getIndex(x, y);
  }

  private class RasterIterator implements Iterator<Pixel<T>> {
    private final Iterator<ImageCoordinate> coords;
    private final RasterPixel pixel;

    public RasterIterator() {
      coords = layout.iterator();
      pixel = new RasterPixel();
    }

    @Override
    public boolean hasNext() {
      return coords.hasNext();
    }

    @Override
    public Pixel<T> next() {
      ImageCoordinate c = coords.next();
      pixel.x = c.getX();
      pixel.y = c.getY();
      return pixel;
    }
  }

  private class RasterPixel implements Pixel<T> {
    private int x;
    private int y;

    @Override
    public void get(T result) {
      DefaultRasterImage.this.get(x, y, result);
    }

    @Override
    public double getAlpha() {
      return DefaultRasterImage.this.getAlpha(x, y);
    }

    @Override
    public int getLayer() {
      return 0;
    }

    @Override
    public int getLevel() {
      return 0;
    }

    @Override
    public int getX() {
      return x;
    }

    @Override
    public int getY() {
      return y;
    }

    @Override
    public void set(T value) {
      DefaultRasterImage.this.set(x, y, value);
    }

    @Override
    public void setAlpha(double a) {
      DefaultRasterImage.this.setAlpha(x, y, a);
    }
  }

  private class RasterSpliterator implements Spliterator<Pixel<T>> {
    private final Spliterator<ImageCoordinate> coords;
    private final RasterPixel pixel;

    public RasterSpliterator() {
      this(layout.spliterator());
    }

    public RasterSpliterator(Spliterator<ImageCoordinate> coords) {
      this.coords = coords;
      pixel = new RasterPixel();
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
        pixel.x = coord.getX();
        pixel.y = coord.getY();
        action.accept(pixel);
      });
    }

    @Override
    public Spliterator<Pixel<T>> trySplit() {
      Spliterator<ImageCoordinate> split = coords.trySplit();
      if (split == null) {
        return null;
      } else {
        return new RasterSpliterator(split);
      }
    }
  }
}
