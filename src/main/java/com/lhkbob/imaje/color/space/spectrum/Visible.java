package com.lhkbob.imaje.color.space.spectrum;

/**
 * Visible
 * =======
 *
 * A SpectrumSpace that is defined from `360nm` to `830nm`, with samples spaced linearly over that
 * range. This represents the commonly accepted range of visible spectrum for humans and is
 * equivalent to the defined domain of the CIE 1931 color matching functions.
 *
 * @author Michael Ludwig
 */
public class Visible extends SpectrumSpace<Visible> {
  /**
   * Predefined Visible space with 32 channels.
   */
  public static final Visible SPACE_32 = new Visible(32);
  /**
   * Predefined Visible space with 64 channels.
   */
  public static final Visible SPACE_64 = new Visible(64);
  /**
   * Predefined Visible space with 128 channels.
   */
  public static final Visible SPACE_128 = new Visible(128);

  /**
   * Create a Visible spectrum space with the given number of channels.
   *
   * @param channelCount
   *     The channel count or number of samples of the space
   */
  public Visible(int channelCount) {
    super(channelCount, 360.0, 830.0, false);
    initializeDefault();
  }
}
