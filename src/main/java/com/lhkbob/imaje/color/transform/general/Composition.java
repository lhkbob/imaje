package com.lhkbob.imaje.color.transform.general;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.transform.ColorTransform;
import com.lhkbob.imaje.util.Arguments;

/**
 *
 */
public class Composition<SA extends ColorSpace<A, SA>, A extends Color<A, SA>, SB extends ColorSpace<B, SB>, B extends Color<B, SB>, SC extends ColorSpace<C, SC>, C extends Color<C, SC>> implements ColorTransform<SA, A, SC, C> {
  private final ColorTransform<SA, A, SB, B> f;
  private final ColorTransform<SB, B, SC, C> g;

  private final Composition<SC, C, SB, B, SA, A> inverse;

  public Composition(ColorTransform<SA, A, SB, B> f, ColorTransform<SB, B, SC, C> g) {
    Arguments.notNull("f", f);
    Arguments.notNull("g", g);

    // Sanity check
    Arguments.equals("intermediate channel count", f.getOutputSpace().getChannelCount(),
        g.getInputSpace().getChannelCount());

    this.f = f;
    this.g = g;

    inverse = new Composition<>(this);
  }

  private Composition(Composition<SC, C, SB, B, SA, A> inverse) {
    f = inverse.g.inverse();
    g = inverse.f.inverse();
    this.inverse = inverse;
  }

  @Override
  public Composition<SC, C, SB, B, SA, A> inverse() {
    return inverse;
  }

  @Override
  public SA getInputSpace() {
    return f.getInputSpace();
  }

  @Override
  public SC getOutputSpace() {
    return g.getOutputSpace();
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    Arguments.equals("input.length", f.getInputSpace().getChannelCount(), input.length);
    Arguments.equals("output.length", g.getOutputSpace().getChannelCount(), output.length);

    double[] mid = new double[f.getOutputSpace().getChannelCount()];
    boolean r = f.applyUnchecked(input, mid);
    return g.applyUnchecked(mid, output) && r;
  }
}
