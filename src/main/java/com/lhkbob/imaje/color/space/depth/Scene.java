package com.lhkbob.imaje.color.space.depth;

import com.lhkbob.imaje.color.Depth;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.space.rgb.SRGB;
import com.lhkbob.imaje.color.transform.Identity;
import com.lhkbob.imaje.color.transform.Transform;

/**
 * Scene
 * =====
 *
 * The scene depth space represents depth values as the exact distance from the point in space to
 * the camera's plane, i.e. the `z` component of the point when transformed into the camera's
 * coordinate system. A value of 0 means the point is exactly on the camera's plane, positive values
 * extend in front of the camera. These values are unbounded.
 *
 * Because Scene is parameter-less, the Scene space is exposed as a singleton {@link #SPACE}.
 *
 * @author Michael Ludwig
 */
public final class Scene extends DepthSpace<Scene> {
  /**
   * The singleton the instance for the Scene depth space.
   */
  public static final Scene SPACE = new Scene();

  private final Identity<Depth<Scene>, Scene, Depth<Scene>, Scene> identity;
  private final Identity<Depth<Scene>, Scene, RGB<SRGB>, SRGB> toRGB;

  private Scene() {
    identity = new Identity<>(this, this);
    // There isn't a great default toRGB transform to use, since this is an unbounded depth value
    toRGB = new Identity<>(this, SRGB.SPACE);
  }

  @Override
  public Transform<Depth<Scene>, Scene, Depth<Scene>, Scene> getSceneTransform() {
    return identity;
  }

  @Override
  public Transform<Depth<Scene>, Scene, RGB<SRGB>, SRGB> getRGBTransform() {
    return toRGB;
  }

  @Override
  public int hashCode() {
    return Scene.class.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return o == this || o instanceof Scene;
  }

  @Override
  public String toString() {
    return "Scene Depth";
  }
}
