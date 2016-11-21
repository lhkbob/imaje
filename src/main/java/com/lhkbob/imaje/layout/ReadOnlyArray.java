package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.util.Arguments;

/**
 * ReadOnlyArray
 * =============
 *
 * A PixelArray wrapper that is {@link #isReadOnly() read-only}. It passes all pixel reads and other
 * state to its parent and does nothing for pixel writes.
 *
 * @author Michael Ludwig
 */
public class ReadOnlyArray implements PixelArray {
  private final PixelArray parent;

  /**
   * Create a ReadOnlyArray that wraps the given parent PixelArray.
   *
   * @param parent
   *     The array to wrap
   * @throws NullPointerException
   *     if `parent` is null
   */
  public ReadOnlyArray(PixelArray parent) {
    Arguments.notNull("parent", parent);
    this.parent = parent;
  }

  @Override
  public double get(int x, int y, double[] channelValues) {
    return parent.get(x, y, channelValues);
  }

  @Override
  public double get(int x, int y, double[] channelValues, long[] bandOffsets) {
    return parent.get(x, y, channelValues, bandOffsets);
  }

  @Override
  public double getAlpha(int x, int y) {
    return parent.getAlpha(x, y);
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a) {
    // Do nothing
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a, long[] bandOffsets) {
    // Do nothing
  }

  @Override
  public void setAlpha(int x, int y, double alpha) {
    // Do nothing
  }

  @Override
  public boolean isReadOnly() {
    return true;
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
    // Do nothing
  }

  @Override
  public void fromParentCoordinate(ImageCoordinate coord) {
    // Do nothing
  }

  @Override
  public void toParentWindow(ImageWindow window) {
    // Do nothing
  }

  @Override
  public void fromParentWindow(ImageWindow window) {
    // Do nothing
  }
}
