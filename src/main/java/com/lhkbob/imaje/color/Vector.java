package com.lhkbob.imaje.color;

import com.lhkbob.imaje.util.Arguments;

import java.util.Arrays;
import java.util.Objects;

/**
 * Vector
 * ======
 *
 * A Vector represents a value within a real-valued multidimensional vector space. Every vector is
 * associated with a particular vector space. Vectors belonging to the same vector space (as
 * determined by {@link Object#equals(Object)} can be added together to form new vector values
 * within the same space. Vectors of the same class do not necessarily have the same vector space,
 * both because some Vector subclasses are parameterized by their VectorSpace type, and because some
 * VectorSpace implementations allow for multiple instances.
 *
 * @author Michael Ludwig
 */
public abstract class Vector<V extends Vector<V, S>, S extends VectorSpace<V, S>> {
  private final S space;
  private double[] channels;

  /**
   * Create a new Vector that is associated with the given `space` and required dimensionality. An
   * exception is thrown if the space's channel count does not equal `requiredDimensions`. The
   * `requiredDimensions` parameter should not be exposed by subclasses in a public constructor. It
   * is used to enforce valid vector spaces are used (e.g. RGB requires 3 dimensions regardless of
   * the vector space instance). If the vector subclass does not have a hardcoded requirement, it
   * can just pass in `space.getChannelCount()`.
   *
   * @param space
   *     The vector space this belongs to
   * @param requiredDimensions
   *     The required dimensions the vector space must have
   * @throws IllegalArgumentException
   *     if the space does not have the required dimensions
   */
  protected Vector(S space, int requiredDimensions) {
    Arguments.notNull("space", space);
    Arguments.equals("space.getChannelCount()", requiredDimensions, space.getChannelCount());

    this.space = space;
    channels = new double[space.getChannelCount()];
  }

  /**
   * @return Get the vector space that this vector belongs to
   */
  public final S getVectorSpace() {
    return space;
  }

  @Override
  @SuppressWarnings("unchecked")
  public Vector<V, S> clone() {
    try {
      Vector<V, S> c = (Vector<V, S>) super.clone();
      // Make a deep clone of the channels array
      c.channels = Arrays.copyOf(channels, channels.length);
      return c;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen");
    }
  }

  /**
   * Get the current state of the particular coordinate, `channel`. The channel is zero-indexed, the
   * same as if `channel` was used to access the array returned by {@link #getChannels()}.
   *
   * @param channel
   *     The channel to access
   * @return The channel's value
   *
   * @throws IndexOutOfBoundsException
   *     if `channel` is less than 0 or greater than or equal to the channel count
   */
  public final double get(int channel) {
    return channels[channel];
  }

  /**
   * Set the state of the particular coordinate, `channel`, to `value`. The channel is zero-indexed,
   * the same as if `channel` was used to modify the array returned by {@link
   * #getChannels()}.
   *
   * @param channel
   *     The channel to modify
   * @param value
   *     The new value
   * @throws IndexOutOfBoundsException
   *     if `channel` is less than 0 or greater than or equal to the channel count
   */
  public final void set(int channel, double value) {
    channels[channel] = value;
  }

  /**
   * Get the number of channels in this vector, which will be equal to the channel count of its
   * corresponding {@link #getVectorSpace() vector space}.
   *
   * @return The dimensionality of this vector
   */
  public final int getChannelCount() {
    return channels.length;
  }

  /**
   * Copy the `values` into this Vector. The array must have a length equal to {@link
   * #getChannelCount()}. This bypasses any defined per-channel setters that a subclass might
   * provide.
   *
   * @param values
   *     The new set of channel values to store in this vector
   * @throws IllegalArgumentException
   *     if `values.length` is not equal to the channel count
   */
  public final void set(double... values) {
    Arguments.equals("channel count", channels.length, values.length);

    System.arraycopy(values, 0, channels, 0, channels.length);
  }

  /**
   * Get the raw array that stores the channel values for this Vector instance. The length of the
   * array is equal to {@link #getChannelCount()}. Modifications to the returned array will be
   * directly reflected by the Vector. These modifications bypass any defined setters per channel
   * that a subclass might provide.
   *
   * @return The raw values array
   */
  public final double[] getChannels() {
    return channels;
  }

  @Override
  public final int hashCode() {
    int result = 17;
    result = 31 * result + getClass().hashCode();
    result = 31 * result + space.hashCode();
    result = 31 * result + Arrays.hashCode(channels);
    return result;
  }

  @Override
  public final boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!getClass().isInstance(o)) {
      return false;
    }
    Vector c = (Vector) o;
    return Objects.equals(space, c.space) && Arrays.equals(c.channels, channels);
  }

  @Override
  public String toString() {
    return String.format("%s %s", getClass().getSimpleName(), Arrays.toString(channels));
  }
}
