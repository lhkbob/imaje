package com.lhkbob.imaje.color.space.spectrum;

/**
 * Infrared
 * ========
 *
 * A SpectrumSpace that is defined from `3000nm` to `50000nm`, with samples spaced logarithmically
 * over that range. This represents the spectrum range for [ISO 20473
 * Mid-Infrared](https://en.wikipedia.org/wiki/Infrared#ISO_20473_scheme).
 *
 * @author Michael Ludwig
 */
public class Infrared extends SpectrumSpace<Infrared> {
  /**
   * Predefined Infrared space with 32 channels.
   */
  public static final Infrared SPACE_32 = new Infrared(32);
  /**
   * Predefined Infrared space with 64 channels.
   */
  public static final Infrared SPACE_64 = new Infrared(64);
  /**
   * Predefined Infrared space with 128 channels.
   */
  public static final Infrared SPACE_128 = new Infrared(128);

  /**
   * Create an Infrared spectrum space with the given number of channels.
   *
   * @param channelCount
   *     The channel count or number of samples of the space
   */
  public Infrared(int channelCount) {
    super(channelCount, 3000.0, 50000.0, true);
    initializeDefault();
  }
}
