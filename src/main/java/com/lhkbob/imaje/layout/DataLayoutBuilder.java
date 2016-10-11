package com.lhkbob.imaje.layout;

/**
 *
 */
public class DataLayoutBuilder implements Cloneable {
  private int width;
  private int height;
  private int tileWidth;
  private int tileHeight;
  private int channels;
  private GeneralLayout.InterleavingUnit interleavingUnit;
  private boolean flipY;
  private boolean flipX;

  public DataLayoutBuilder() {
    // Defaults
    width = 1;
    height = 1;
    tileWidth = -1;
    tileHeight = -1;
    channels = 1;
    interleavingUnit = GeneralLayout.InterleavingUnit.PIXEL;
    flipY = false;
    flipX = false;
  }

  @Override
  public DataLayoutBuilder clone() {
    try {
      // All fields are primitives or immutable so there is no further action required
      return (DataLayoutBuilder) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen", e);
    }
  }

  public DataLayoutBuilder compatibleWith(DataLayout layout) {
    // Set properties that don't depend on the nesting/wrapping of layouts
    width = layout.getWidth();
    height = layout.getHeight();
    channels = layout.getChannelCount();
    flipX = !layout.isDataLeftToRight();
    flipY = !layout.isDataBottomToTop();

    // Extract base layout to determine pixel interleaving and tiling properties
    while(layout instanceof InvertedLayout) {
        layout = ((InvertedLayout) layout).getOriginalLayout();
    }

    if (layout instanceof SimpleLayout) {
      tileWidth = -1;
      tileHeight = -1;
      interleavingUnit = GeneralLayout.InterleavingUnit.PIXEL;
    } else if (layout instanceof GeneralLayout) {
      GeneralLayout l = (GeneralLayout) layout;
      tileWidth = l.getTileWidth();
      tileHeight = l.getTileHeight();
      interleavingUnit = l.getInterleavingUnit();
    } else {
      throw new UnsupportedOperationException(
          "Unsupported pixel layout class: " + layout.getClass());
    }

    return this;
  }

  public DataLayoutBuilder topToBottom() {
    flipY = true;
    return this;
  }

  public DataLayoutBuilder bottomToTop() {
    flipY = false;
    return this;
  }

  public DataLayoutBuilder leftToRight() {
    flipX = false;
    return this;
  }

  public DataLayoutBuilder rightToLeft() {
    flipX = true;
    return this;
  }

  public DataLayoutBuilder width(int width) {
    this.width = width;
    return this;
  }

  public DataLayoutBuilder height(int height) {
    this.height = height;
    return this;
  }

  public DataLayoutBuilder tileWidth(int tileWidth) {
    this.tileWidth = tileWidth;
    return this;
  }

  public DataLayoutBuilder tileHeight(int tileHeight) {
    this.tileHeight = tileHeight;
    return this;
  }

  public DataLayoutBuilder channels(int channels) {
    this.channels = channels;
    return this;
  }

  public DataLayoutBuilder interleave(GeneralLayout.InterleavingUnit interleavingUnit) {
    this.interleavingUnit = interleavingUnit;
    return this;
  }

  private void validate() {
    if (width <= 0) {
      throw new UnsupportedOperationException("Image width must be at least 1, not: " + width);
    }
    if (height <= 0) {
      throw new UnsupportedOperationException("Image height must be at least 1, not: " + height);
    }
    if (channels < 0) {
      throw new UnsupportedOperationException("Channel count cannot be less than 0: " + channels);
    }

    if (tileWidth > 0 && tileHeight <= 0) {
      throw new UnsupportedOperationException(
          "Must provide tile height in addition to tile width (which is " + tileWidth + ")");
    } else if (tileHeight > 0 && tileWidth <= 0) {
      throw new UnsupportedOperationException(
          "Must provide tile width in addition to tile height (which is " + tileHeight + ")");
    }

    if (interleavingUnit == null) {
      throw new NullPointerException("Interleaving unit cannot be null");
    }
  }

  public DataLayout build() {
    validate();

    DataLayout layout;
    // Check if the desired layout cannot be represented by a simple RasterLayout
    if (tileWidth <= 0 && tileHeight <= 0
        && interleavingUnit == GeneralLayout.InterleavingUnit.PIXEL) {
      layout = new SimpleLayout(width, height, channels);
    } else {
      // Fall back to the general layout that can handle tiles and different interleaving units
      layout = new GeneralLayout(
          width, height, tileWidth, tileHeight, channels, interleavingUnit);
    }

    if (flipY || flipX) {
      return new InvertedLayout(layout, flipX, flipY);
    } else {
      return layout;
    }
  }
}
