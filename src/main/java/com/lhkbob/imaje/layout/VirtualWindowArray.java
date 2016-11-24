package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.util.Arguments;

import java.util.Arrays;

/**
 * VirtualWindowArray
 * ==================
 *
 * VirtualWindowArray is a wrapping PixelArray that defines a virtual coordinate system that
 * contains its parent image's data. This parent image can be positioned arbitrarily within the
 * virtual window, extending beyond the virtual window's dimensions (thus being cropped) or being
 * contained within, in which case the virtual window creates a border around the image data. When
 * the virtual window is contained completely within the image data, it is functionally equivalent
 * to {@link SubImagePixelArray} although that will be more efficient an implementation.
 *
 * @author Michael Ludwig
 */
public class VirtualWindowArray implements PixelArray {
  private final PixelArray parent;

  private final int width;
  private final int height;
  private final int parentOffsetX;
  private final int parentOffsetY;

  private final double[] outOfParentColor;
  private final double outOfParentAlpha;

  /**
   * Create a new VirtualWindowArray that wraps `parent` and creates a new coordinate space of the
   * given `width X height`. The parent is located within this coordinate space at `(parentX,
   * parentY)`. There are no restrictions on the location of the parent's image, and the coordinates
   * can be negative so that the virtual window only accesses part of the image state. Similarly,
   * the dimensions of the virtual window do not need to exceed that of the parents, it is permitted
   * for the virtual window to cut off parts of the parent data.
   *
   * The only restriction is that the virtual window dimensions much each be at least 1.
   *
   * This constructor assumes a background color of all 0s and a background alpha of 1.0.
   *
   * @param parent
   *     The parent to wrap
   * @param parentX
   *     The offset along X axis in the virtual window's coordinate frame to the parent's origin,
   *     may be negative
   * @param parentY
   *     The offset along Y axis in the virtual window's coordinate frame to the parent's origin,
   *     may be negative
   * @param width
   *     The width of the virtual window
   * @param height
   *     The height of the virtual window
   * @throws NullPointerException
   *     if `parent` is null
   * @throws IllegalArgumentException
   *     if `width` or `height` are less than 1
   */
  public VirtualWindowArray(PixelArray parent, int parentX, int parentY, int width, int height) {
    this(parent, parentX, parentY, width, height, new double[parent.getColorChannelCount()]);
  }

  /**
   * Create a new VirtualWindowArray that wraps `parent` and creates a new coordinate space of the
   * given `width X height`. The parent is located within this coordinate space at `(parentX,
   * parentY)`. There are no restrictions on the location of the parent's image, and the coordinates
   * can be negative so that the virtual window only accesses part of the image state. Similarly,
   * the dimensions of the virtual window do not need to exceed that of the parents, it is permitted
   * for the virtual window to cut off parts of the parent data.
   *
   * The only restriction is that the virtual window dimensions much each be at least 1.
   *
   * `backgroundColor` specifies the color channel values reported by pixel-accessors when the
   * requested pixel location is within the virtual window but not the parent's data. A defensive
   * copy is made so future modifications to `backgroundColor` do not affect this array.
   *
   * This constructor assumes a background alpha of 1.0.
   *
   * @param parent
   *     The parent to wrap
   * @param parentX
   *     The offset along X axis in the virtual window's coordinate frame to the parent's origin,
   *     may be negative
   * @param parentY
   *     The offset along Y axis in the virtual window's coordinate frame to the parent's origin,
   *     may be negative
   * @param width
   *     The width of the virtual window
   * @param height
   *     The height of the virtual window
   * @param backgroundColor
   *     The background color to use for out-of-parent pixels
   * @throws NullPointerException
   *     if `parent` or `backgroundColor` are null
   * @throws IllegalArgumentException
   *     if `width` or `height` are less than 1, or if the length of `backgroundColor` does not
   *     equal the color channel count of the parent array
   */
  public VirtualWindowArray(
      PixelArray parent, int parentX, int parentY, int width, int height,
      double[] backgroundColor) {
    this(parent, parentX, parentY, width, height, backgroundColor, 1.0);
  }

  /**
   * Create a new VirtualWindowArray that wraps `parent` and creates a new coordinate space of the
   * given `width X height`. The parent is located within this coordinate space at `(parentX,
   * parentY)`. There are no restrictions on the location of the parent's image, and the coordinates
   * can be negative so that the virtual window only accesses part of the image state. Similarly,
   * the dimensions of the virtual window do not need to exceed that of the parents, it is permitted
   * for the virtual window to cut off parts of the parent data.
   *
   * The only restriction is that the virtual window dimensions much each be at least 1.
   *
   * `backgroundColor` specifies the color channel values reported by pixel-accessors when the
   * requested pixel location is within the virtual window but not the parent's data. A defensive
   * copy is made so future modifications to `backgroundColor` do not affect this array. Similarly,
   * `backgroundAlpha` specifies the alpha reported for out-of-parent pixel locations. However, if
   * the parent has no alpha channel then this value is ignored and 1.0 is used instead.
   *
   * @param parent
   *     The parent to wrap
   * @param parentX
   *     The offset along X axis in the virtual window's coordinate frame to the parent's origin,
   *     may be negative
   * @param parentY
   *     The offset along Y axis in the virtual window's coordinate frame to the parent's origin,
   *     may be negative
   * @param width
   *     The width of the virtual window
   * @param height
   *     The height of the virtual window
   * @param backgroundColor
   *     The background color to use for out-of-parent pixels
   * @param backgroundAlpha
   *     The background alpha to use for out-of-parent pixels
   * @throws NullPointerException
   *     if `parent` or `backgroundColor` are null
   * @throws IllegalArgumentException
   *     if `width` or `height` are less than 1, or if the length of `backgroundColor` does not
   *     equal the color channel count of the parent array
   */
  public VirtualWindowArray(
      PixelArray parent, int parentX, int parentY, int width, int height, double[] backgroundColor,
      double backgroundAlpha) {
    Arguments.notNull("parent", parent);
    Arguments.isPositive("width", width);
    Arguments.isPositive("height", height);
    Arguments
        .equals("backgroundColor.length", parent.getColorChannelCount(), backgroundColor.length);

    this.parent = parent;
    this.width = width;
    this.height = height;
    parentOffsetX = parentX;
    parentOffsetY = parentY;
    outOfParentColor = Arrays.copyOf(backgroundColor, backgroundColor.length);
    if (parent.hasAlphaChannel()) {
      outOfParentAlpha = backgroundAlpha;
    } else {
      outOfParentAlpha = 1.0;
    }
  }

  /**
   * Get the X coordinate of the parent image's coordinate space with respect to the virtual
   * window's coordinate space. Thus, the returned X value is the location of the parent's image
   * origin inside the virtual window.
   *
   * @return The parent's X offset in this coordinate system
   */
  public int getParentOffsetX() {
    return parentOffsetX;
  }

  /**
   * Get the Y coordinate of the parent image's coordinate space with respect to the virtual
   * window's coordinate space. Thus, the returned Y value is the location of the parent's image
   * origin inside the virtual window.
   *
   * @return The parent's Y offset in this coordinate system
   */
  public int getParentOffsetY() {
    return parentOffsetY;
  }

  /**
   * Get the color channel values reported for pixel locations in the virtual window that do not
   * correspond to a valid location in the parent array. This returns a defensive copy and has
   * length equal to `getColorChannelCount()`.
   *
   * @return The background color
   */
  public double[] getBackgroundColor() {
    return Arrays.copyOf(outOfParentColor, outOfParentColor.length);
  }

  /**
   * Get the alpha value reported for pixel locations in the virtual window that do not correspond
   * to a valid location in the parent array. If the parent array does not have an alpha channel
   * this will be 1.0.
   *
   * @return The background alpha
   */
  public double getBackgroundAlpha() {
    return outOfParentAlpha;
  }

  @Override
  public double get(int x, int y, double[] channelValues) {
    checkCoordinate(x, y);
    if (isInParent(x, y)) {
      return parent.get(x - parentOffsetX, y - parentOffsetY, channelValues);
    } else {
      System.arraycopy(outOfParentColor, 0, channelValues, 0, outOfParentColor.length);
      return outOfParentAlpha;
    }
  }

  @Override
  public double get(int x, int y, double[] channelValues, long[] bandOffsets) {
    checkCoordinate(x, y);
    if (isInParent(x, y)) {
      return parent.get(x - parentOffsetX, y - parentOffsetY, channelValues, bandOffsets);
    } else {
      Arrays.fill(bandOffsets, -1);
      System.arraycopy(outOfParentColor, 0, channelValues, 0, outOfParentColor.length);
      return outOfParentAlpha;
    }
  }

  @Override
  public double getAlpha(int x, int y) {
    checkCoordinate(x, y);
    if (isInParent(x, y)) {
      return parent.getAlpha(x - parentOffsetX, y - parentOffsetY);
    } else {
      return outOfParentAlpha;
    }
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a) {
    checkCoordinate(x, y);
    if (isInParent(x, y)) {
      parent.set(x - parentOffsetX, y - parentOffsetY, channelValues, a);
    } // otherwise ignore
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a, long[] bandOffsets) {
    checkCoordinate(x, y);
    if (isInParent(x, y)) {
      parent.set(x - parentOffsetX, y - parentOffsetY, channelValues, a, bandOffsets);
    } // otherwise ignore
  }

  @Override
  public void setAlpha(int x, int y, double alpha) {
    checkCoordinate(x, y);
    if (isInParent(x, y)) {
      parent.setAlpha(x - parentOffsetX, y - parentOffsetY, alpha);
    }
  }

  private boolean isInParent(int x, int y) {
    // The (x, y) coordinates must be past the parent's offset but inside both the parent's
    // dimensions This doesn't need to worry about the case where the parent extends outside of the
    // virtual window because the it is only called when the virtual-window coordinate is valid,
    // which cannot access parent image data outside of the virtual window by definition.
    return x >= parentOffsetX && y >= parentOffsetY && x < parentOffsetX + parent.getWidth()
        && y < parentOffsetY + parent.getHeight();
  }

  private void checkCoordinate(int x, int y) {
    // Hypothetically, out-of-bounds x and y values for the sub image can access valid pixels of
    // the parent image, which is inconsistent with PixelArray's API and is not detected by the
    // parent so this array must validate coordinates explicitly.
    Arguments.checkArrayRange("x", width, 0, x);
    Arguments.checkArrayRange("y", height, 0, y);
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
    return width;
  }

  @Override
  public int getHeight() {
    return height;
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
    // Converting into the parent coordinate system subtracts the parent offsets from the coordinate
    // This is the opposite behavior from SubImagePixelArray.
    coord.setX(coord.getX() - parentOffsetX);
    coord.setY(coord.getY() - parentOffsetY);
  }

  @Override
  public void fromParentCoordinate(ImageCoordinate coord) {
    // (0,0) in the parent coordinate space maps to (parentOffsetX, parentOffsetY) so just add
    // the offsets (opposite behavior of SubImagePixelArray).
    coord.setX(coord.getX() + parentOffsetX);
    coord.setY(coord.getY() + parentOffsetY);
  }

  @Override
  public void toParentWindow(ImageWindow window) {
    // No need to update boundaries, so just update the window coordinate like toParentCoordinate()
    window.setX(window.getX() - parentOffsetX);
    window.setY(window.getY() - parentOffsetY);
  }

  @Override
  public void fromParentWindow(ImageWindow window) {
    // No need to update boundaries, so just update the window coordinate like
    // fromParentCoordinate()
    window.setX(window.getX() + parentOffsetX);
    window.setY(window.getY() + parentOffsetY);
  }
}
