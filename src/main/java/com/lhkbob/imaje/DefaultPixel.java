package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.layout.PixelArray;
import com.lhkbob.imaje.util.Arguments;

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

  private final T cachedColor;
  private transient double cachedAlpha;

  public DefaultPixel(Class<T> colorType, PixelArray data) {
    Arguments.notNull("colorType", colorType);
    Arguments.notNull("data", data);

    this.data = data;
    this.channels = new long[data.getLayout().getChannelCount()];
    cachedColor = Color.newInstance(colorType);

    // Final validation to make sure channel counts are compatible
    Arguments.equals("channel count", cachedColor.getChannelCount(), data.getFormat().getColorChannelCount());
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public void setLayer(int layer) {
    this.layer = layer;
  }

  public void setPixel(int x, int y) {
    refresh(x, y);
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
    refresh(x, y);
  }

  private void refresh(int x, int y) {
    cachedAlpha = data.get(x, y, cachedColor.getChannels(), channels);
    // The PixelArray validates x and y, so if code reaches here it was a valid coordinate and we
    // can update the pixel's location
    this.x = x;
    this.y = y;
  }
}
