package com.lhkbob.imaje.color.space.spectrum;

/**
 * Ultraviolet
 * ===========
 *
 * A SpectrumSpace that is defined from `100nm` to `400nm`, with samples spaced linearly over that
 * range. This represents the union of spectrum ranges for [UV A, B, and
 * C](https://en.wikipedia.org/wiki/Ultraviolet#Subtypes).
 *
 * @author Michael Ludwig
 */
public class Ultraviolet extends SpectrumSpace<Ultraviolet> {
  /**
   * Predefined Ultraviolet spectrum space with 32 channels.
   */
  public static final Ultraviolet SPACE_32 = new Ultraviolet(32);
  /**
   * Predefined Ultraviolet spectrum space with 64 channels.
   */
  public static final Ultraviolet SPACE_64 = new Ultraviolet(64);
  /**
   * Predefined Ultraviolet spectrum space with 128 channels.
   */
  public static final Ultraviolet SPACE_128 = new Ultraviolet(128);

  /**
   * Create an Ultraviolet spectrum space with the given number of channels.
   *
   * @param channelCount
   *     The channel count or number of samples of the space
   */
  public Ultraviolet(int channelCount) {
    super(channelCount, 100.0, 400.0, false);
    initializeDefault();
  }
}
