package com.lhkbob.imaje.layout;

/**
 *
 */
public class RowMajorLayout implements PixelLayout {
  private final long rowWidth;
  private final long pixelWidth;

  public RowMajorLayout(int pixelsPerRow, int primitivesPerPixel) {
    this.rowWidth = pixelsPerRow * primitivesPerPixel;
    this.pixelWidth = primitivesPerPixel;
  }

  @Override
  public long getIndex(int x, int y) {
    return y * rowWidth + x * pixelWidth;
  }
}
