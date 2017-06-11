package com.lhkbob.imaje.color.space.depth;

import com.lhkbob.imaje.color.Depth;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.space.rgb.SRGB;
import com.lhkbob.imaje.color.transform.Identity;
import com.lhkbob.imaje.color.transform.ScaleChannels;
import com.lhkbob.imaje.color.transform.Transform;
import com.lhkbob.imaje.util.Arguments;

/**
 * Normalized
 * ==========
 *
 * Normalized is a non-standard, but convenient representation for encoding scene depth values.
 * Given a near and far clip plane, scene depths are normalized to the range [0, 1] where 0
 * corresponds to the near clipping plane and 1 corresponds to the far clipping plane.
 *
 * @author Michael Ludwig
 */
public class Normalized extends DepthSpace<Normalized> {
  private final double near;
  private final double far;

  private final Identity<Depth<Normalized>, Normalized, RGB<SRGB>, SRGB> toRGB;
  private final ScaleChannels<Depth<Normalized>, Normalized, Depth<Scene>, Scene> toScene;

  /**
   * Create a new Normalized that is defined by the scene depths for the `near` and `far` clipping
   * planes. `far` must be greater than `near`.
   *
   * @param near
   *     The near clipping plane depth
   * @param far
   *     The far clipping plane depth
   * @throws IllegalArgumentException
   *     if  `far` is less than or equal to `near`.
   */
  public Normalized(double near, double far) {
    Arguments.isGreaterThan("far", near, far);
    this.near = near;
    this.far = far;

    toRGB = new Identity<>(this, SRGB.SPACE);
    toScene = new ScaleChannels<>(
        this, Scene.SPACE, new double[] { 0.0 }, new double[] { 1.0 }, new double[] { near },
        new double[] { far });
  }

  @Override
  public Transform<Depth<Normalized>, Normalized, Depth<Scene>, Scene> getTransformToScene() {
    return toScene;
  }

  @Override
  public Transform<Depth<Normalized>, Normalized, RGB<SRGB>, SRGB> getTransformToRGB() {
    return toRGB;
  }

  /**
   * @return The scene depth of the near clipping plane
   */
  public double getNearPlane() {
    return near;
  }

  /**
   * @return The scene depth of the far clipping plane
   */
  public double getFarPlane() {
    return far;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + Double.hashCode(near);
    result = 31 * result + Double.hashCode(far);
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof Normalized)) {
      return false;
    }

    Normalized z = (Normalized) o;
    return Double.compare(z.near, near) == 0 && Double.compare(z.far, far) == 0;
  }

  @Override
  public String toString() {
    return "Normalized Scene";
  }
}
