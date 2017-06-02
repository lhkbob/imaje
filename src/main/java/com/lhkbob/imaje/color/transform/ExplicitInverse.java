package com.lhkbob.imaje.color.transform;

import com.lhkbob.imaje.color.Vector;
import com.lhkbob.imaje.color.VectorSpace;
import com.lhkbob.imaje.util.Arguments;

import java.util.Objects;
import java.util.Optional;

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
public class ExplicitInverse<I extends Vector<I, SI>, SI extends VectorSpace<I, SI>, O extends Vector<O, SO>, SO extends VectorSpace<O, SO>> implements Transform<I, SI, O, SO> {
  private final Transform<I, SI, O, SO> forward;
  private final ExplicitInverse<O, SO, I, SI> inverse;

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
      Transform<I, SI, O, SO> forward, Transform<O, SO, I, SI> inverse) {
    Arguments.notNull("forward", forward);
    Arguments.notNull("inverse", inverse);

    this.forward = forward;
    this.inverse = new ExplicitInverse<>(this, inverse);
  }

  private ExplicitInverse(
      ExplicitInverse<O, SO, I, SI> inverse, Transform<I, SI, O, SO> forward) {
    this.forward = forward;
    this.inverse = inverse;
  }

  @Override
  public Optional<ExplicitInverse<O, SO, I, SI>> inverse() {
    return Optional.of(inverse);
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

  @Override
  public int hashCode() {
    int result = ExplicitInverse.class.hashCode();
    result = 31 * result + forward.hashCode();
    result = 31 * result + inverse.forward.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this)
      return true;
    if (!(o instanceof ExplicitInverse))
      return false;

    ExplicitInverse e = (ExplicitInverse) o;
    return Objects.equals(e.forward, forward) && Objects.equals(e.inverse.forward, inverse.forward);
  }

  @Override
  public String toString() {
    return String.format("%s with inverse (%s)", forward, inverse.forward);
  }
}
