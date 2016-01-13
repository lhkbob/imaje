package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.YUV;
import com.lhkbob.imaje.data.DoubleSource;

import java.util.LinkedHashMap;

/**
 *
 */
public class YUVAdapter<T extends YUV> implements PixelAdapter<T> {
  private final Class<T> type;
  private final DoubleSource u;
  private final DoubleSource v;
  private final DoubleSource y;

  public YUVAdapter(Class<T> type, DoubleSource y, DoubleSource u, DoubleSource v) {
    this.type = type;
    this.y = y;
    this.u = u;
    this.v = v;
  }

  @Override
  public void get(long pixelIndex, T result) {
    result.y(y.get(pixelIndex));
    result.u(u.get(pixelIndex));
    result.v(v.get(pixelIndex));
  }

  @Override
  public LinkedHashMap<String, DoubleSource> getChannels() {
    LinkedHashMap<String, DoubleSource> channels = new LinkedHashMap<>();
    channels.put(YUV.Y, y);
    channels.put(YUV.U, u);
    channels.put(YUV.V, v);
    return channels;
  }

  @Override
  public Class<T> getType() {
    return type;
  }

  @Override
  public void set(long pixelIndex, T value) {
    y.set(pixelIndex, value.y());
    u.set(pixelIndex, value.u());
    v.set(pixelIndex, value.v());
  }
}
