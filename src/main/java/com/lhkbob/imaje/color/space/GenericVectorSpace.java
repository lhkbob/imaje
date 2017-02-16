package com.lhkbob.imaje.color.space;

import com.lhkbob.imaje.color.Vector;
import com.lhkbob.imaje.color.VectorSpace;
import com.lhkbob.imaje.util.Arguments;

import java.util.function.Function;

/**
 * GenericVectorSpace
 * ==================
 *
 * A generic vector space that can have an arbitrary positive number of channels or components. Each
 * instance of GenericVectorSpace represents a unique vector space, thus, multiple 3-channel vector
 * spaces represent unique semantically different systems even though their state appears identical.
 * This vector space can be used for vector definitions that do not need to have a complex selection
 * of spaces, or the color features exposed by a ColorSpace.
 *
 * @author Michael Ludwig
 */
public class GenericVectorSpace<V extends Vector<V, GenericVectorSpace<V>>> implements VectorSpace<V, GenericVectorSpace<V>> {
  private final int channelCount;
  private final Function<GenericVectorSpace<V>, V> ctor;

  /**
   * Create a new GenericVectorSpace that has the given `channelCount`. `ctor` is a constructor
   * function for vector instances of type `V` that takes a GenericVectorSpace as its only argument.
   * This will be invoked, using the newly created GenericVectorSpace, when {@link #newValue()} is
   * called.
   *
   * It is recommended that the particular function be private or protected so that the Vector class
   * can control its vector space definition. As an example, {@link com.lhkbob.imaje.color.Alpha}
   * creates a public static final GenericVectorSpace that has access to a private constructor of
   * Alpha that sets each instances vector space reference. In this way, all Alpha instances will
   * belong to the same public GenericVectorSpace.
   *
   * @param channelCount The channel count of the vector space
   * @param ctor The constructor function for making new vectors within this space
   */
  public GenericVectorSpace(int channelCount, Function<GenericVectorSpace<V>, V> ctor) {
    Arguments.isPositive("channelCount", channelCount);
    Arguments.notNull("ctor", ctor);
    this.channelCount = channelCount;
    this.ctor = ctor;
  }

  @Override
  public int getChannelCount() {
    return channelCount;
  }

  @Override
  public V newValue() {
    return ctor.apply(this);
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public boolean equals(Object o) {
    return this == o;
  }

  @Override
  public String toString() {
    return String.format("Generic(%d)", channelCount);
  }
}
