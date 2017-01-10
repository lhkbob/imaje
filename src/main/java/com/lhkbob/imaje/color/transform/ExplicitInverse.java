package com.lhkbob.imaje.color.transform;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.util.Arguments;

/**
 * ExplicitInverse
 * ===============
 *
 * A color transform that wraps another transform to override its reported inverse with another
 * explicit inverse. This is most useful when transform implementations cannot or do not provide an
 * automatic inverse (e.g. {@link com.lhkbob.imaje.color.space.spectrum.SpectrumToXYZ}) and
 * something else must be used. Often the paired functions passed to an ExplicitInverse are not true
 * inverses of one another but are acceptable because they represent the next-best scenario (i.e.
 * {@link com.lhkbob.imaje.color.space.spectrum.SmitsRGBToSpectrum} as the inverse to compute a
 * spectrum given a tristimulus).
 *
 * @author Michael Ludwig
 */
public class ExplicitInverse<SI extends ColorSpace<I, SI>, I extends Color<I, SI>, SO extends ColorSpace<O, SO>, O extends Color<O, SO>> implements ColorTransform<SI, I, SO, O> {
  private final ColorTransform<SI, I, SO, O> forward;
  private final ExplicitInverse<SO, O, SI, I> inverse;

  /**
   * Create a new ExplicitInverse transform that wraps `forward` but reports `inverse` when
   * {@link #inverse()} is called.
   *
   * @param forward
   *     The transform to wrap
   * @param inverse
   *     The explicit inverse overriding whatever `forward` would return
   * @throws NullPointerException
   *     if `forward` or `inverse` are null
   */
  public ExplicitInverse(
      ColorTransform<SI, I, SO, O> forward, ColorTransform<SO, O, SI, I> inverse) {
    Arguments.notNull("forward", forward);
    Arguments.notNull("inverse", inverse);

    this.forward = forward;
    this.inverse = new ExplicitInverse<>(this, inverse);
  }

  private ExplicitInverse(
      ExplicitInverse<SO, O, SI, I> inverse, ColorTransform<SI, I, SO, O> forward) {
    this.forward = forward;
    this.inverse = inverse;
  }

  @Override
  public ExplicitInverse<SO, O, SI, I> inverse() {
    return inverse;
  }

  @Override
  public SI getInputSpace() {
    return forward.getInputSpace();
  }

  @Override
  public SO getOutputSpace() {
    return forward.getOutputSpace();
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    return forward.applyUnchecked(input, output);
  }
}
