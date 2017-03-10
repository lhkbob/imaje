package com.lhkbob.imaje.color.space.depth;

import com.lhkbob.imaje.color.Depth;
import com.lhkbob.imaje.color.DepthStencil;
import com.lhkbob.imaje.color.VectorSpace;
import com.lhkbob.imaje.util.Arguments;

import java.util.Objects;

/**
 * DepthStencilSpace
 * =================
 *
 * DepthStencilSpace is a composition space that attaches a stencil channel to a depth channel,
 * where those values are defined by the composed Depth vector space.
 *
 * @author Michael Ludwig
 */
public class DepthStencilSpace<S extends VectorSpace<Depth<S>, S>> implements VectorSpace<DepthStencil<S>, DepthStencilSpace<S>> {
  /**
   * Pre-defined DepthStencilSpace associated with the Scene Depth vector space.
   */
  public static final DepthStencilSpace<Scene> SPACE_SCENE = new DepthStencilSpace<>(Scene.SPACE);

  private final S depthSpace;

  /**
   * Create a new DepthStencilSpace that represents depth values equivalently to `depthSpace`.
   *
   * @param depthSpace
   *     The depth space that defines how depth values are represented in this composed space
   */
  public DepthStencilSpace(S depthSpace) {
    Arguments.notNull("depthSpace", depthSpace);
    this.depthSpace = depthSpace;
  }

  /**
   * @return The Depth pace this DepthStencil space is defined by.
   */
  public S getDepthSpace() {
    return depthSpace;
  }

  @Override
  public int getChannelCount() {
    return 2;
  }

  @Override
  public DepthStencil<S> newValue() {
    return new DepthStencil<>(this);
  }

  @Override
  public int hashCode() {
    return DepthStencilSpace.class.hashCode() ^ depthSpace.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof DepthStencilSpace)) {
      return false;
    }
    return Objects.equals(((DepthStencilSpace) o).depthSpace, depthSpace);
  }

  @Override
  public String toString() {
    return String.format("Depth-stencil (%s)", depthSpace);
  }
}
