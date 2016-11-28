package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.util.Arguments;

import java.util.Arrays;

/**
 * PremultipliedAlphaArray
 * =======================
 *
 * A PixelArray wrapper that transforms a parent array storing color values that have been
 * premultiplied with the pixel's alpha value into the expected, non-premultiplied alpha state.
 * Thus, this implementation coerces premultiplied-alpha color data into the regular form that is
 * assumed by consumers of the PixelArray data.
 *
 * When a pixel has an alpha of 0, this array reports all 0s for color channel values as well.
 *
 * @author Michael Ludwig
 */
public class PremultipliedAlphaArray implements PixelArray {
  private final PixelArray parent;

  /**
   * Create a PremultipliedAlphaArray that wraps the given `parent`. It is assumed that the color
   * values reported by `parent`'s pixel accessors have had their values multiplied by the pixel's
   * alpha value. The alpha value is divided out and plain
   *
   * @param parent
   *     The parent to wrap
   */
  public PremultipliedAlphaArray(PixelArray parent) {
    Arguments.notNull("parent", parent);
    // While we could validate that the parent has an alpha channel, there's no real need since
    // an alpha of 1.0 is returned when the no alpha is present so this array becomes a no-op.
    this.parent = parent;
  }

  @Override
  public double get(int x, int y, double[] channelValues) {
    double alpha = parent.get(x, y, channelValues);
    toRegularChannels(channelValues, alpha);
    return alpha;
  }

  @Override
  public double get(int x, int y, double[] channelValues, long[] bandOffsets) {
    double alpha = parent.get(x, y, channelValues, bandOffsets);
    toRegularChannels(channelValues, alpha);
    return alpha;
  }

  @Override
  public double getAlpha(int x, int y) {
    return parent.getAlpha(x, y);
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a) {
    double[] premul = toPremultipliedChannels(channelValues, a);
    parent.set(x, y, premul, a);
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a, long[] bandOffsets) {
    double[] premul = toPremultipliedChannels(channelValues, a);
    parent.set(x, y, premul, a, bandOffsets);
  }

  @Override
  public void setAlpha(int x, int y, double alpha) {
    // Must update the entire color state as well since the alpha is baked into the color state
    double[] oldPremul = new double[getColorChannelCount()];
    double oldAlpha = parent.get(x, y, oldPremul);
    toRegularChannels(oldPremul, oldAlpha);
    double[] newPremul = toPremultipliedChannels(oldPremul, alpha);
    parent.set(x, y, newPremul, alpha);
  }

  private double[] toPremultipliedChannels(double[] regularChannels, double alpha) {
    // Create a new array since it's unsafe to modify regularChannels in place and then invert
    // after the data has been set (which is necessary to appear as if regularChannels was not
    // modified). Relies on escape analysis for performance.
    double[] pre = new double[regularChannels.length];
    for (int i = 0; i < regularChannels.length; i++) {
      pre[i] = regularChannels[i] * alpha;
    }
    return pre;
  }

  private void toRegularChannels(double[] premultipliedChannels, double alpha) {
    // Convert to regular (non-multiplied) channel values by dividing out alpha
    // Channel values are modified in place.
    if (Math.abs(alpha) < 1e-8) {
      // Do not divide by a very small alpha, and since the channel values are premultiplied,
      // then they ought to be black and have no well-defined color values to be set to even if
      // transparency is preserved.
      Arrays.fill(premultipliedChannels, 0.0);
    } else {
      for (int i = 0; i < premultipliedChannels.length; i++) {
        premultipliedChannels[i] /= alpha;
      }
    }
  }

  @Override
  public boolean isReadOnly() {
    return parent.isReadOnly();
  }

  @Override
  public PixelArray getParent() {
    return parent;
  }

  @Override
  public int getWidth() {
    return parent.getWidth();
  }

  @Override
  public int getHeight() {
    return parent.getHeight();
  }

  @Override
  public int getColorChannelCount() {
    return parent.getColorChannelCount();
  }

  @Override
  public boolean hasAlphaChannel() {
    return parent.hasAlphaChannel();
  }

  @Override
  public int getBandCount() {
    return parent.getBandCount();
  }

  @Override
  public void toParentCoordinate(ImageCoordinate coord) {
    // do nothing
  }

  @Override
  public void fromParentCoordinate(ImageCoordinate coord) {
    // do nothing
  }

  @Override
  public void toParentWindow(ImageWindow window) {
    // do nothing
  }

  @Override
  public void fromParentWindow(ImageWindow window) {
    // do nothing
  }
}
