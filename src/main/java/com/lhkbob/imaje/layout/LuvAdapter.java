package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.color.Luv;
import com.lhkbob.imaje.data.DoubleSource;

import java.util.LinkedHashMap;

/**
 *
 */
public class LuvAdapter implements PixelAdapter<Luv> {
  private final DoubleSource l;
  private final DoubleSource u;
  private final DoubleSource v;

  public LuvAdapter(DoubleSource l, DoubleSource u, DoubleSource v) {
    this.l = l;
    this.u = u;
    this.v = v;
  }

  @Override
  public void get(long pixelIndex, Luv result) {
    result.l(l.get(pixelIndex));
    result.u(u.get(pixelIndex));
    result.v(v.get(pixelIndex));
  }

  @Override
  public void set(long pixelIndex, Luv value) {
    l.set(pixelIndex, value.l());
    u.set(pixelIndex, value.u());
    v.set(pixelIndex, value.v());
  }

  @Override
  public LinkedHashMap<String, DoubleSource> getChannels() {
    LinkedHashMap<String, DoubleSource> channels = new LinkedHashMap<>();
    channels.put(Luv.L, l);
    channels.put(Luv.U, u);
    channels.put(Luv.V, v);
    return channels;
  }

  @Override
  public Class<Luv> getType() {
    return Luv.class;
  }
}
