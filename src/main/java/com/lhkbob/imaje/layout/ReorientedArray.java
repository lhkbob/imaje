package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.util.Arguments;

import java.util.EnumSet;

/**
 * ReorientedArray
 * ===============
 *
 * ReorientedArray is a PixelArray wrapper that changes how the dimensions and coordinates of its
 * parent's array are interpreted with respect to the underlying data layout. Specifically, it
 * supports changing the directionality of a given axis and swapping the X and Y axis. This is
 * achieved by mapping the input coordinates to its parent's coordinate system in different ways,
 * depending on the {@link OrientationOption options} configured. See the description of each option
 * for how it influences the array.
 *
 * To reduce confusion, ReorientedArray can only wrap a RootPixelArray, preventing the situation
 * where multiple ReorientedArray's are nested together. This is not functionally limiting because
 * any combination of nested ReorientedArrays can be created by a single ReorientedArray with the
 * appropriate options.
 *
 * The coordinate system exposed by the ReorientedArray is in line with the default coordinate
 * system described in {@link PixelArray}. It reorients the non-default coordinate system of its
 * parent to match imaJe's conventions.
 *
 * @author Michael Ludwig
 */
public class ReorientedArray implements PixelArray {
  /**
   * OrientationOption
   * =================
   *
   * Set of configurable options to describe how to map a parent's coordinate system to the default
   * system. Options fall into three categories for controlling the mapping: the X axis direction of
   * data (left to right or right to left), the Y axis direction of data (bottom to top or top to
   * bottom), and which dimension is dominant (Y or X). Options come in pairs that are mutually
   * exclusive. They define opposite behavior, where one option is the default behavior that is
   * assumed when no controlling option in that category is specified.
   *
   * @author Michael Ludwig
   */
  public enum OrientationOption {
    /**
     * Pixels at the top of the image come earlier in the data and proceed to the bottom of the
     * image. This effectively maps `y = 0` of the ReorientedArray to `y' = height - 1` in the
     * parent, and `y = height - 1` to `y' = 0` in the parent. Here `height` refers to the dimension
     * reported by the ReorientedArray and not its parent. Thus, when combined with COLUMN_MAJOR,
     * column entries are provided top to bottom. When combined with ROW_MAJOR, rows are provided
     * top to bottom. The top to bottom behavior is independent of the left to right or right to
     * left behavior of the X axis.
     */
    TOP_TO_BOTTOM,
    /**
     * Pixels at the bottom of the image come earlier in the data and proceed to the top of the
     * image. This is the default Y axis orientation and does not transform the up-down ordering of
     * the parent array. Since it is the default Y axis behavior, it is mostly provided so it can be
     * included for clarity if desired. When combined with COLUMN_MAJOR, column entries are provided
     * bottom to top. When combined with ROW_MAJOR, rows are provided bottom to top. The bottom to
     * top behavior is independent of the left to right or right to left behavior of the X axis.
     */
    BOTTOM_TO_TOP,
    /**
     * Pixels at the right of the image come earlier in the data and proceed to the left of the
     * image. This effectively maps `x = 0` of the ReorientedArray to `x' = width - 1` in the
     * parent, and `x = width - 1` to `x' = 0` in the parent. Here `width` refers to the dimension
     * reported by the ReorientedArray and not its parent. Thus, when combined with COLUMN_MAJOR,
     * columns are provided right to left. When combined with ROW_MAJOR, row entries are provided
     * right to left. The right to left behavior is independent of the bottom to top or top to
     * bottom behavior of the Y axis.
     */
    RIGHT_TO_LEFT,
    /**
     * Pixels at the left of the image come earlier in the data and proceed to the right of the
     * image. This is the default X axis orientation and does not transform the left-right ordering
     * of the parent array. Since it is the default X axis behavior, it is mostly provided so it can
     * be included for clarity if desired. Thus, when combined with COLUMN_MAJOR, columns are
     * provided left to right. When combined with ROW_MAJOR, row entries are provided left to right.
     * The left to right behavior is independent of the bottom to top or top to bottom behavior of
     * the Y axis.
     */
    LEFT_TO_RIGHT,
    /**
     * The two dimensional mapping of XY locations to one dimensional buffer locations proceeds in a
     * column-major fashion. This means that a vertical column of pixels is a continuous block of
     * coordinates, so it is more cache-friendly to iterate over the X axis first and the Y axis
     * within the inner loop. Because {@link DataLayout} assumes a row-major ordering for its
     * coordinate system, this functions by swapping the X and Y coordinates of a pixel. This also
     * means that the ReorientedArray's width is equal to its parent's height and its height is
     * equal to its parent's width. This swap also extends to any tiling dimensions the layout may
     * define: while the layout may refer to a tile width and height, that tile's width logically
     * refers to the height of a tile with respect to the ReorientedArray. {@link PixelArrayBuilder}
     * cleanly takes care of this when constructing compatible arrays.
     */
    COLUMN_MAJOR,
    /**
     * The two dimensional mapping of XY locations to one dimensional buffer locations proceeds in a
     * row-major fashion. This means that a horizontal row of pixels is a continuous block of
     * coordinates, so it is more cache-friendly to iterate over the Y axis first and the X axis
     * within the inner loop. This is the default axis convention, but is provided so it can be
     * included for clarity.
     */
    ROW_MAJOR
  }

  private final RootPixelArray parent;
  private final boolean topToBottom;
  private final boolean rightToLeft;

  private final boolean columnMajor;

  /**
   * Create a ReorientedArray that reorients the coordinate system and data of `parent` based on the
   * set of `options`. Duplicates and null values within `options` are ignored. An exception is
   * thrown if both `TOP_TO_BOTTOM` and `BOTTOM_TO_TOP`, or both `RIGHT_TO_LEFT` and
   * `LEFT_TO_RIGHT`, or both `COLUMN_MAJOR` and `ROW_MAJOR` are included. These pairs of options
   * are mutually exclusive. All other combinations of options (in any order) are acceptable.
   *
   * Providing an empty array in `options` creates a ReorientedArray that is a direct pass-through
   * to its parent array.
   *
   * @param parent
   *     The parent to wrap
   * @param options
   *     The reorientation configuration
   * @throws NullPointerException
   *     if `parent` is null
   * @throws IllegalArgumentException
   *     if the `options` array fails the criteria outlined above
   */
  public ReorientedArray(RootPixelArray parent, OrientationOption... options) {
    Arguments.notNull("parent", parent);
    this.parent = parent;

    if (hasOption(options, OrientationOption.TOP_TO_BOTTOM)) {
      topToBottom = true;
      if (hasOption(options, OrientationOption.BOTTOM_TO_TOP)) {
        throw new IllegalArgumentException(
            "Cannot provide TOP_TO_BOTTOM and BOTTOM_TO_TOP at the same time");
      }
    } else {
      topToBottom = false;
    }

    if (hasOption(options, OrientationOption.RIGHT_TO_LEFT)) {
      rightToLeft = true;
      if (hasOption(options, OrientationOption.LEFT_TO_RIGHT)) {
        throw new IllegalArgumentException(
            "Cannot provide RIGHT_TO_LEFT and LEFT_TO_RIGHT at the same time");
      }
    } else {
      rightToLeft = false;
    }

    if (hasOption(options, OrientationOption.COLUMN_MAJOR)) {
      columnMajor = true;
      if (hasOption(options, OrientationOption.ROW_MAJOR)) {
        throw new IllegalArgumentException(
            "Cannot provide COLUMN_MAJOR and ROW_MAJOR at the same time");
      }
    } else {
      columnMajor = false;
    }
  }

  /**
   * @return True if data is arranged from top to bottom; if false then data is bottom to top.
   */
  public boolean isTopToBottom() {
    return topToBottom;
  }

  /**
   * @return True if data is arranged from right to left; if false then data is left to right.
   */
  public boolean isRightToLeft() {
    return rightToLeft;
  }

  /**
   * @return True if data is arranged column-major; if false then data is row-major.
   */
  public boolean isColumnMajor() {
    return columnMajor;
  }

  /**
   * Get the set of OrientationOptions that control the behavior of this array. If `isTopToBottom()`
   * returns true then the set will contain {@link OrientationOption#TOP_TO_BOTTOM} otherwise it
   * will contain {@link OrientationOption#BOTTOM_TO_TOP}. If `isRightToLeft()` returns true then
   * the set will contain {@link OrientationOption#RIGHT_TO_LEFT}, otherwise it will contain {@link
   * OrientationOption#LEFT_TO_RIGHT}. If `isColumnMajor()` returns true then the set will contain
   * {@link OrientationOption#COLUMN_MAJOR} otherwise it will contain {@link
   * OrientationOption#ROW_MAJOR}.
   *
   * @return The set of options defining this array's transform of data
   */
  public EnumSet<OrientationOption> getOrientationOptions() {
    OrientationOption vertical =
        topToBottom ? OrientationOption.TOP_TO_BOTTOM : OrientationOption.BOTTOM_TO_TOP;
    OrientationOption horizontal =
        rightToLeft ? OrientationOption.RIGHT_TO_LEFT : OrientationOption.LEFT_TO_RIGHT;
    OrientationOption major =
        columnMajor ? OrientationOption.COLUMN_MAJOR : OrientationOption.ROW_MAJOR;
    return EnumSet.of(major, vertical, horizontal);
  }

  private int getParentY(int x, int y) {
    if (columnMajor) {
      return getOrientedX(x);
    } else {
      return getOrientedY(y);
    }
  }

  private int getParentX(int x, int y) {
    if (columnMajor) {
      return getOrientedY(y);
    } else {
      return getOrientedX(x);
    }
  }

  private int getOrientedY(int y) {
    if (topToBottom) {
      return getHeight() - y - 1;
    } else {
      return y;
    }
  }

  private int getOrientedX(int x) {
    if (rightToLeft) {
      return getWidth() - x - 1;
    } else {
      return x;
    }
  }

  private static boolean hasOption(OrientationOption[] options, OrientationOption target) {
    if (options == null) {
      return false;
    }
    for (OrientationOption option : options) {
      if (option == target) {
        return true;
      }
    }
    return false;
  }

  @Override
  public double get(int x, int y, double[] channelValues) {
    return parent.get(getParentX(x, y), getParentY(x, y), channelValues);
  }

  @Override
  public double get(int x, int y, double[] channelValues, long[] bandOffsets) {
    return parent.get(getParentX(x, y), getParentY(x, y), channelValues, bandOffsets);
  }

  @Override
  public double getAlpha(int x, int y) {
    return parent.getAlpha(getParentX(x, y), getParentY(x, y));
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a) {
    parent.set(getParentX(x, y), getParentY(x, y), channelValues, a);
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a, long[] bandOffsets) {
    parent.set(getParentX(x, y), getParentY(x, y), channelValues, a, bandOffsets);
  }

  @Override
  public void setAlpha(int x, int y, double alpha) {
    parent.setAlpha(getParentX(x, y), getParentY(x, y), alpha);
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
    return columnMajor ? parent.getHeight() : parent.getWidth();
  }

  @Override
  public int getHeight() {
    return columnMajor ? parent.getWidth() : parent.getHeight();
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
    int x = coord.getX();
    int y = coord.getY();
    coord.setX(getParentX(x, y));
    coord.setY(getParentY(x, y));
  }

  @Override
  public void fromParentCoordinate(ImageCoordinate coord) {
    // It just so happens that the to-parent conversion math is its own inverse to go from parent
    // to child coordinate space.
    toParentCoordinate(coord);
  }

  @Override
  public void toParentWindow(ImageWindow window) {
    int x = window.getX();
    int y = window.getY();
    int w = window.getWidth();
    int h = window.getHeight();
    window.setX(getParentX(x, y));
    window.setY(getParentY(x, y));
    window.setWidth(columnMajor ? h : w);
    window.setHeight(columnMajor ? w : h);
  }

  @Override
  public void fromParentWindow(ImageWindow window) {
    toParentWindow(window);
  }
}
