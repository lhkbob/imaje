package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.util.Arguments;

import java.util.Iterator;
import java.util.Spliterator;

/**
 * ImageWindow
 * ===========
 *
 * ImageWindow is a data type that stores the lower left corner of the window and the width and
 * height that extend to the upper right corner. A window is not necessarily constrained to the data
 * range of an image, it can represent negative coordinates or exceed the image dimensions. It is
 * intended as a way to describe a virtual two-dimensional block.
 *
 * @author Michael Ludwig
 */
public final class ImageWindow implements Cloneable, Iterable<ImageCoordinate> {
  private int x;
  private int y;
  private int width;
  private int height;

  /**
   * Create a new blank window at the origin with zero dimensions.
   */
  public ImageWindow() {
    this(0, 0, 0, 0);
  }

  /**
   * Create a new window with its origin at `(x, y)` and dimensions of `width` and `height`.
   *
   * @param x
   *     The x coordinate of the origin
   * @param y
   *     The y coordinate of the origin
   * @param width
   *     The width of the window
   * @param height
   *     The height of the window
   * @throws IllegalArgumentException
   *     if `width` or `height` are less than 0
   */
  public ImageWindow(int x, int y, int width, int height) {
    setX(x);
    setY(y);
    setWidth(width);
    setHeight(height);
  }

  @Override
  public ImageWindow clone() {
    try {
      return (ImageWindow) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen", e);
    }
  }

  /**
   * Return whether or not this window contains the given coordinate, `c`.
   *
   * @param c
   *     The image coordinate potentially within the window
   * @return True if `c` is within this window's boundaries
   */
  public boolean contains(ImageCoordinate c) {
    return c.getX() >= x && c.getX() < x + width && c.getY() >= y && c.getY() < y + height;
  }

  /**
   * Return whether or not this window contains the entirety of the window, `c`.
   *
   * @param c
   *     The image window potentially within the window
   * @return True if `c`'s bounds are within this window's boundaries
   */
  public boolean contains(ImageWindow c) {
    return c.getX() >= x && c.getX() + c.getWidth() < x + width && c.getY() >= y
        && c.getY() + c.getHeight() < y + height;
  }

  /**
   * Return whether or not this window is contained inside an image of the given dimensions, `width
   * X height`. This returns true if the x and y coordinates of this window are greater than or
   * equal to 0, and if the right+top boundaries of the window do not exceed `width` and `height`.
   *
   * @param width
   *     The width of the image
   * @param height
   *     The height of the image
   * @return True if this window is contained with an image of the given bounds
   */
  public boolean isContainedInImage(int width, int height) {
    return x >= 0 && y >= 0 && x + this.width < width && y + this.height < height;
  }

  /**
   * @return The x coordinate of the bottom-left corner of the window
   */
  public int getX() {
    return x;
  }

  /**
   * @return The y coordinate of the bottom-left corner of the window
   */
  public int getY() {
    return y;
  }

  /**
   * @return The width of the window, always at least 0
   */
  public int getWidth() {
    return width;
  }

  /**
   * @return The height of the window, always at least 0
   */
  public int getHeight() {
    return height;
  }

  @Override
  public Iterator<ImageCoordinate> iterator() {
    return new ImageCoordinate.FastIterator(this);
  }

  @Override
  public Spliterator<ImageCoordinate> spliterator() {
    return new ImageCoordinate.FastSpliterator(this);
  }

  /**
   * Update the x coordinate of the bottom-left corner of the window. This may be negative, in which
   * case the window definitely represents pixels outside of the standard image domain.
   *
   * @param x
   *     The new x coordinate
   */
  public void setX(int x) {
    this.x = x;
  }

  /**
   * Update the y coordinate of the bottom-left corner of the window. This may be negative, in which
   * case the window definitely represents pixels outside of the standard image domain.
   *
   * @param y
   *     The new y coordinate
   */
  public void setY(int y) {
    this.y = y;
  }

  /**
   * Update the width of the window. This must be at least 0.
   *
   * @param width
   *     The new width of the window
   */
  public void setWidth(int width) {
    Arguments.isGreaterThanOrEqualToZero("width", width);
    this.width = width;
  }

  /**
   * Update the height of the window. This must be at least 0.
   *
   * @param height
   *     The new height of the window
   */
  public void setHeight(int height) {
    Arguments.isGreaterThanOrEqualToZero("height", height);
    this.height = height;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + Integer.hashCode(x);
    result = 31 * result + Integer.hashCode(y);
    result = 31 * result + Integer.hashCode(width);
    result = 31 * result + Integer.hashCode(height);
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ImageWindow)) {
      return false;
    }
    ImageWindow w = (ImageWindow) o;
    return w.x == x && w.y == y && w.width == width && w.height == height;
  }

  @Override
  public String toString() {
    return String.format("+(%d, %d) %d x %d", x, y, width, height);
  }
}
