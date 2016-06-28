package com.lhkbob.imaje.color;

import com.lhkbob.imaje.color.annot.Channels;

/**
 *
 */
@Channels(value = "Stencil Mask", shortNames = "S")
public class Stencil extends Color {
  public Stencil() {
    this(0);
  }

  public Stencil(int stencilMask) {
    setStencil(stencilMask);
  }

  @Override
  public Stencil clone() {
    return (Stencil) super.clone();
  }

  public int getStencil() {
    return (int) get(0);
  }

  public void setStencil(int stencil) {
    set(0, stencil);
  }

  public int stencil() {
    return getStencil();
  }

  public void stencil(int stencil) {
    setStencil(stencil);
  }
}
