package com.lhkbob.imaje.color.space.depth;

import com.lhkbob.imaje.color.Depth;
import com.lhkbob.imaje.color.transform.Transform;
import com.lhkbob.imaje.util.Arguments;

import java.util.Objects;
import java.util.Optional;

/**
 * InversePerspectiveTransform
 * ===========================
 *
 * The inverse transform of {@link PerspectiveTransform}, that goes from {@link ZBuffer} to {@link
 * Scene} depth values.
 *
 * @author Michael Ludwig
 */
public class InversePerspectiveTransform implements Transform<Depth<ZBuffer>, ZBuffer, Depth<Scene>, Scene> {
  private final ZBuffer space;
  private final PerspectiveTransform inverse;

  /**
   * Create an InversePerspectiveTransform defined for the given ZBuffer space.
   *
   * @param space
   *     The ZBuffer space of this transform
   */
  public InversePerspectiveTransform(ZBuffer space) {
    this.space = space;
    inverse = new PerspectiveTransform(this);
  }

  InversePerspectiveTransform(PerspectiveTransform inverse) {
    this.space = inverse.getOutputSpace();
    this.inverse = inverse;
  }

  @Override
  public Optional<PerspectiveTransform> inverse() {
    return Optional.of(inverse);
  }

  @Override
  public ZBuffer getInputSpace() {
    return space;
  }

  @Override
  public Scene getOutputSpace() {
    return Scene.SPACE;
  }

  @Override
  public int hashCode() {
    return InversePerspectiveTransform.class.hashCode() ^ space.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof InversePerspectiveTransform)) {
      return false;
    }
    return Objects.equals(((InversePerspectiveTransform) o).space, space);
  }

  @Override
  public String toString() {
    return "Z-buffer -> Scene depth";
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    Arguments.equals("input.length", 1, input.length);
    Arguments.equals("output.length", 1, output.length);

    // See https://www.opengl.org/archives/resources/faq/technical/depthbuffer.htm
    // - but assumes that w_e (the eye-space homogenous coordinate is 1)
    double zNDC = 2.0 * (input[0] - 0.5);
    double f = space.getFarPlane();
    double n = space.getNearPlane();
    double zInv = 0.5 * (f - n) * (zNDC - (f + n) / (f - n)) / (f * n);
    output[0] = 1.0 / zInv;
    return true;
  }
}
