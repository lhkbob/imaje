package com.lhkbob.imaje.color.space.depth;

import com.lhkbob.imaje.color.Depth;
import com.lhkbob.imaje.color.transform.Transform;
import com.lhkbob.imaje.util.Arguments;

import java.util.Objects;

/**
 * PerspectiveTransform
 * ====================
 *
 * This transforms {@link Scene} depth values to {@link ZBuffer} values in the range 0 to 1. This
 * uses the ZBuffer's specified near and far plane to calculate the perspective projected clip
 * coordinates for `z` and `w`, does perspective division to get normalized device coordinates, and
 * then shifts these from the range [-1, 1] to [0, 1].
 *
 * @author Michael Ludwig
 */
public class PerspectiveTransform implements Transform<Depth<Scene>, Scene, Depth<ZBuffer>, ZBuffer> {
  private final ZBuffer space;
  private final InversePerspectiveTransform inverse;

  /**
   * Create a PerspectiveTransform defined for the given ZBuffer space.
   *
   * @param space
   *     The ZBuffer space of this transform
   */
  public PerspectiveTransform(ZBuffer space) {
    this.space = space;
    inverse = new InversePerspectiveTransform(this);
  }

  PerspectiveTransform(InversePerspectiveTransform inverse) {
    this.space = inverse.getInputSpace();
    this.inverse = inverse;
  }

  @Override
  public InversePerspectiveTransform inverse() {
    return inverse;
  }

  @Override
  public Scene getInputSpace() {
    return Scene.SPACE;
  }

  @Override
  public ZBuffer getOutputSpace() {
    return space;
  }

  @Override
  public int hashCode() {
    return PerspectiveTransform.class.hashCode() ^ space.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof PerspectiveTransform)) {
      return false;
    }
    return Objects.equals(((PerspectiveTransform) o).space, space);
  }

  @Override
  public String toString() {
    return "Scene depth -> Z-buffer";
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    Arguments.equals("input.length", 1, input.length);
    Arguments.equals("output.length", 1, output.length);

    // See https://www.opengl.org/archives/resources/faq/technical/depthbuffer.htm
    // - but assumes that w_e (the eye-space homogenous coordinate is 1)
    double f = space.getFarPlane();
    double n = space.getNearPlane();
    double zNDC = (f + n) / (f - n) + 2 * (1.0 / input[0]) * f * n / (f - n);
    output[0] = 0.5 * zNDC + 0.5;
    return true;
  }
}
