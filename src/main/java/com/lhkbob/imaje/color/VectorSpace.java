package com.lhkbob.imaje.color;

/**
 * VectorSpace
 * ===========
 *
 * A description of a mathematical vector space of multiple real-valued dimensions. VectorSpace
 * subclasses and instances define the dimensionality of the space and provide factory methods for
 * creating vectors of the space. As a vector space, vectors can be added together and scaled by
 * real values. For simplicity, these operations are implemented unilaterally in {@link Vectors} as
 * static functions.
 *
 * Instances of a vector space of a given type do not necessarily represent the exact same space.
 * For example, a {@link com.lhkbob.imaje.color.space.GenericVectorSpace} provides a VectorSpace
 * implementation that comes with no semantics and has a variable dimensionality (per instance).
 * Thus even if two vectors have the same VectorSpace type parameter, they are not necessarily
 * compatible. On the other hand, some vector spaces have no parameters, such as {@link
 * com.lhkbob.imaje.color.space.rgb.SRGB}, and only expose a singleton instance. In this case, the
 * type declaration of the vector perfectly matches the vector space it belongs to.
 *
 * VectorSpace classes that represent singletons should declare themselves `final`, have a private
 * constructor, and expose a `public static final` singleton named `SPACE`. VectorSpaces that
 * require constructor arguments should also expose several common configurations of their vector
 * space as static instances.
 *
 * Subclasses should implement `equals()` and `hashCode()` appropriately so that VectorSpaces that
 * are equal allow Vector instances to interact in a mathematically valid manner. Subclasses should
 * be immutable and thread safe after construction.
 *
 * @author Michael Ludwig
 */
public interface VectorSpace<V extends Vector<V, S>, S extends VectorSpace<V, S>> {
  /**
   * Return the dimensionality of the vector space. All vectors associated with this space will have
   * the same channel count.
   *
   * @return The channel count of the vector space
   */
  int getChannelCount();

  /**
   * Construct a new vector instance of type `V` associated with this vector space. The returned
   * instance will return `this` space from {@link Vector#getVectorSpace()}. The state of the
   * returned instance must have all channel component values set to 0.
   *
   * @return A new vector of this space
   */
  V newValue();
}
