package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.data.DataSource;

import java.util.LinkedHashMap;

/**
 *
 */
public interface PixelAdapter<T extends Color> {
  void get(long pixelIndex, T result);

  LinkedHashMap<String, ? extends DataSource<?>> getChannels();

  Class<T> getType();

  void set(long pixelIndex, T value);
}
