package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.data.DataBuffer;

/**
 * RootPixelArray
 * ==============
 *
 * RootPixelArray is an abstract implementation of PixelArray that all root-level pixel arrays
 * (those that do not have a parent pixel array) must extend. It implements much of the PixelArray
 * interface in terms of several components that specialize in part of the pixel and image
 * specification:
 *
 * + {@link PixelFormat}, which determines the color channel count and alpha channel of the array.
 * + {@link DataLayout}, which determines the dimensions of the image and the band count.
 * + {@link DataBuffer}, which hold the pixel data as arranged by the layout
 *
 * RootPixelArray implementations define the constraints and relationships between these
 * components.
 *
 * @author Michael Ludwig
 */
public abstract class RootPixelArray implements PixelArray {
  /**
   * Get the DataLayout that describes how the two dimensional pixels of this array are mapped to
   * the single dimensionality of the data buffers storing actual color data.
   *
   * @return The data layout
   */
  public abstract DataLayout getLayout();

  /**
   * Get the DataBuffer that stores data for the given band, where the band is defined with respect
   * to the layout returned by {@link #getLayout()}. The offsets computed by the layout for the same
   * `band` can be used as an index for accessing or modifying the returned buffer. It may be the
   * case, depending on implementation, that the same DataBuffer is returned for all bands or that
   * each band has its own.
   *
   * All data buffers returned from this method must have the same length although they may not be
   * of the same type or implementation if the pixel format of the array mixes data types across
   * channels. The length must be equal to or exceed the required elements of the array's layout.
   *
   * @param band
   *     The band to lookup
   * @return The DataBuffer storing data for the band
   */
  public abstract DataBuffer getData(int band);

  /**
   * @return The PixelFormat that describes how logical color and alpha channels map to data fields
   */
  public abstract PixelFormat getFormat();

  @Override
  public final PixelArray getParent() { return null; }

  @Override
  public final int getWidth() {
    return getLayout().getWidth();
  }

  @Override
  public final int getHeight() {
    return getLayout().getHeight();
  }

  @Override
  public final int getColorChannelCount() {
    return getFormat().getColorChannelCount();
  }

  @Override
  public final boolean hasAlphaChannel() {
    return getFormat().hasAlphaChannel();
  }

  @Override
  public final int getBandCount() {
    return getLayout().getBandCount();
  }

  @Override
  public final void toParentCoordinate(ImageCoordinate coord) {
    // do nothing
  }

  @Override
  public final void fromParentCoordinate(ImageCoordinate coord) {
    // do nothing
  }

  @Override
  public final void toParentWindow(ImageWindow window) {
    // do nothing
  }

  @Override
  public final void fromParentWindow(ImageWindow window) {
    // do nothing
  }
}
