package com.lhkbob.imaje.color.space.depth;

import com.lhkbob.imaje.color.Depth;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.space.rgb.SRGB;
import com.lhkbob.imaje.color.transform.Identity;
import com.lhkbob.imaje.color.transform.Transform;
import com.lhkbob.imaje.util.Arguments;

/**
 * ZBuffer
 * =======
 *
 * ZBuffer represents depth values as they are stored by an OpenGL-like system.
 * The zbuffer values range from 0 to 1 and are domain-shifted normalized device coordinates (which
 * are originally defined between -1 and 1). A perspective division by the homogenous coordinate
 * creates normalized device coordinate values from projection of the scene depth.
 *
 * This space assumes the standard perspective projection matrix, in which case this perspective
 * transformation, only requires the scene depths of the near and far clipping planes for the
 * camera. Of course, this simplification of the perspective projection is only valid for the z
 * component, the x and y coordinates would require knowing the field of view.
 *
 * A particular ZBuffer depth space is defined by these near and far plane depth values, which
 * enables easy reconstruction of the linear scene depth.
 *
 * @author Michael Ludwig
 */
public class ZBuffer extends DepthSpace<ZBuffer> {
  private final double near;
  private final double far;

  private final Identity<Depth<ZBuffer>, ZBuffer, RGB<SRGB>, SRGB> toRGB;
  private final InversePerspectiveTransform toScene;

  /**
   * Create a new ZBuffer that is defined by the scene depths for the `near` and
   * `far` clipping planes. `near` must be greater than zero, and `far` must be
   * greater than `near`.
   *
   * @param near
   *     The near clipping plane depth
   * @param far
   *     The far clipping plane depth
   * @throws IllegalArgumentException
   *     if `near` is less than or equal to 0 or if `far` is less than
   *     or equal to `near`.
   */
  public ZBuffer(double near, double far) {
    Arguments.isPositive("near", near);
    Arguments.isGreaterThan("far", near, far);
    this.near = near;
    this.far = far;

    toRGB = new Identity<>(this, SRGB.SPACE);
    toScene = new InversePerspectiveTransform(this);
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
  public InversePerspectiveTransform getSceneTransform() {
    return toScene;
  }

  @Override
  public Transform<Depth<ZBuffer>, ZBuffer, RGB<SRGB>, SRGB> getRGBTransform() {
    return toRGB;
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
    } else if (!(o instanceof ZBuffer)) {
      return false;
    }

    ZBuffer z = (ZBuffer) o;
    return Double.compare(z.near, near) == 0 && Double.compare(z.far, far) == 0;
  }

  @Override
  public String toString() {
    return "Z-buffer";
  }
}
