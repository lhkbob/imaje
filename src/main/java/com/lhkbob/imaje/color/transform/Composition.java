package com.lhkbob.imaje.color.transform;

import com.lhkbob.imaje.color.Vector;
import com.lhkbob.imaje.color.VectorSpace;
import com.lhkbob.imaje.util.Arguments;

import java.util.Optional;

/**
 * Composition
 * ===========
 *
 * A Transform implementation that embodies the mathematical composition of two other functions.
 *
 * @author Michael Ludwig
 */
public class Composition<A extends Vector<A, SA>, SA extends VectorSpace<A, SA>, B extends Vector<B, SB>, SB extends VectorSpace<B, SB>, C extends Vector<C, SC>, SC extends VectorSpace<C, SC>> implements Transform<A, SA, C, SC> {
  private final Transform<A, SA, B, SB> f;
  private final Transform<B, SB, C, SC> g;

  private final Composition<C, SC, B, SB, A, SA> inverse;

  /**
   * Create a new composition that represents the net transform of `g(f(x))`. The output channel
   * count of `f` and the input channel count of `g` must be equal.
   *
   * @param f
   *     The first function in the composition
   * @param g
   *     The second function in the composition
   * @throws NullPointerException
   *     if `f` or `g` are null
   * @throws IllegalArgumentException
   *     if the output channels of `f` don't equal the input channels of `g`
   */
  public Composition(Transform<A, SA, B, SB> f, Transform<B, SB, C, SC> g) {
    Arguments.notNull("f", f);
    Arguments.notNull("g", g);

    // Sanity check
    Arguments.equals("intermediate channel count", f.getOutputSpace().getChannelCount(),
        g.getInputSpace().getChannelCount());

    this.f = f;
    this.g = g;

    Optional<? extends Transform<B, SB, A, SA>> fInv = f.inverse();
    Optional<? extends Transform<C, SC, B, SB>> gInv = g.inverse();

    if (fInv.isPresent() && gInv.isPresent()) {
      inverse = new Composition<>(gInv.get(), fInv.get(), this);
    } else {
      inverse = null;
    }
  }

  private Composition(Transform<A, SA, B, SB> f, Transform<B, SB, C, SC> g, Composition<C, SC, B, SB, A, SA> inverse) {
    this.f = f;
    this.g = g;
    this.inverse = inverse;
  }

  @Override
  public Optional<Composition<C, SC, B, SB, A, SA>> inverse() {
    return Optional.ofNullable(inverse);
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
