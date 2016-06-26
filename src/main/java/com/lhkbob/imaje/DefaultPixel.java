package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.layout.ColorAdapter;

/**
 *
 */
public class DefaultPixel<T extends Color> implements Pixel<T> {
  private int x;
  private int y;
  private int layer;
  private int level;

  private final ColorAdapter<T> data;
  private final long[] channels;

  public DefaultPixel(ColorAdapter<T> data) {
    this.data = data;
    this.channels = data.createCompatibleChannelArray();
  }

  void setLevel(int level) {
    this.level = level;
  }

  void setLayer(int layer) {
    this.layer = layer;
  }

  void setPixel(int x, int y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public double get(T result) {
    return data.get(x, y, result, channels);
  }

  @Override
  public double getAlpha() {
    return data.getAlpha(x, y);
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
  public void set(T value) {
    data.set(x, y, value, data.getAlpha(x, y), channels);
  }

  @Override
  public void set(T value, double a) {
    data.set(x, y, value, a, channels);
  }

  @Override
  public void setAlpha(double a) {
    data.setAlpha(x, y, a);
  }
}
