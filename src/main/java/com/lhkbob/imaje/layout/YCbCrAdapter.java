package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.YCbCr;
import com.lhkbob.imaje.data.DoubleSource;

import java.util.LinkedHashMap;

/**
 *
 */
public class YCbCrAdapter<T extends YCbCr> implements PixelAdapter<T> {
  private final DoubleSource y;
  private final DoubleSource cb;
  private final DoubleSource cr;

  public YCbCrAdapter(DoubleSource y, DoubleSource cb, DoubleSource cr) {
    this.y = y;
    this.cb = cb;
    this.cr = cr;
  }

  @Override
  public void get(long pixelIndex, T result) {
    result.y(y.get(pixelIndex));
    result.cb(cb.get(pixelIndex));
    result.cr(cr.get(pixelIndex));
  }

  @Override
  public void set(long pixelIndex, T value) {
    y.set(pixelIndex, value.y());
    cb.set(pixelIndex, value.cb());
    cr.set(pixelIndex, value.cr());
  }

  @Override
  public LinkedHashMap<String, DoubleSource> getChannels() {
    LinkedHashMap<String, DoubleSource> channels = new LinkedHashMap<>();
    channels.put(YCbCr.Y, y);
    channels.put(YCbCr.CB, cb);
    channels.put(YCbCr.CR, cr);
    return channels;
  }
}
