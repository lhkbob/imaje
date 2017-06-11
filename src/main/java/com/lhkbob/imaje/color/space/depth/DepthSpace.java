package com.lhkbob.imaje.color.space.depth;

import com.lhkbob.imaje.color.Depth;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.VectorSpace;
import com.lhkbob.imaje.color.space.rgb.SRGB;
import com.lhkbob.imaje.color.transform.Transform;

/**
 * DepthSpace
 * ==========
 *
 * An abstract vector space for storing depth values. Depth is defined with respect to some camera
 * and represents the distance from a point in space to the viewing plane of the camera (i.e. the
 * plane defined by the camera's direction). However, there are many encoding and normalization
 * schemes for depth values that transform this concrete definition into other spaces. Some
 * represent various stages in a graphics pipeline and others are defined merely for convenience
 * when encoding to an RGB image.
 *
 * All depth spaces must provide a transform that converts to the {@link Scene} depth space, which
 * represents the actual, un-encoded depth for a scene. In addition, DepthSpace requires a transform
 * to RGB space. This can be lossy or produce unrealistic color values and is intended as a
 * convenience for easily visualizing depth images.
 *
 * @author Michael Ludwig
 */
public abstract class DepthSpace<S extends DepthSpace<S>> implements VectorSpace<Depth<S>, S> {
  /**
   * @return The transformation from this depth space to the original Scene space.
   */
  public abstract Transform<Depth<S>, S, Depth<Scene>, Scene> getTransformToScene();

  /**
   * @return A visualization transformation to turn the depth image into a colored image.
   */
  public abstract Transform<Depth<S>, S, RGB<SRGB>, SRGB> getTransformToRGB();

  /**
   * @return The transformation from the original Scene space to this depth space
   */
  public Transform<Depth<Scene>, Scene, Depth<S>, S> getTransformFromScene() {
    return getTransformToScene().inverse().orElseThrow(UnsupportedOperationException::new);
  }

  /**
   * @return The transformation from a colored image to this depth space
   */
  public Transform<RGB<SRGB>, SRGB, Depth<S>, S> getTransformFromRGB() {
    return getTransformToRGB().inverse().orElseThrow(UnsupportedOperationException::new);
  }

  @Override
  public int getChannelCount() {
    return 1;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Depth<S> newValue() {
    return new Depth<>((S) this);
  }
}
