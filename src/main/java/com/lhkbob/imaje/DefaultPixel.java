package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.layout.PixelArray;

/**
 *
 */
public class DefaultPixel<T extends Color> implements Pixel<T> {
  private int x;
  private int y;
  private int layer;
  private int level;

  private final PixelArray data;
  private final long[] channels;

  private final T color;
  private transient double cachedAlpha;

  public DefaultPixel(Class<T> colorType, PixelArray data) {
    this.data = data;
    this.channels = new long[data.getLayout().getChannelCount()];
    color = Color.newInstance(colorType);
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public void setLayer(int layer) {
    this.layer = layer;
  }

  public void setPixel(int x, int y) {
    this.x = x;
    this.y = y;
    refresh();
  }

  @Override
  public T getColor() {
    return color;
  }

  @Override
  public double getAlpha() {
    return cachedAlpha;
  }

  @Override
  public int getLayer() {
    return layer;
  }

  @Override
  public int getLevel() {
    return level;
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
  public void setColor(T value) {
    setColor(value, cachedAlpha);
  }

  @Override
  public void setColor(T value, double a) {
    if (value != color) {
      // Copy the state of value into the internal color instance so that future calls to getColor
      // are accurate
      color.set(value.getChannels());
    }

    // Now that color matches value, persist will correctly store the new value
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
    data.set(x, y, color.getChannels(), alpha, channels);
  }

  @Override
  public void refresh() {
    cachedAlpha = data.get(x, y, color.getChannels(), channels);
  }
}
