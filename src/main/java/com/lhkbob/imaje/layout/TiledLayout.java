package com.lhkbob.imaje.layout;

/**
 *
 */
public class TiledLayout implements PixelLayout {
  // Number of primitives to advance to go to start of next tile.
  private final long regularTileWidth;
  // Number of primitives to advance to go to start of next tile along bottom row.
  private final long bottomTileWidth;
  // Number of primitives to advance to go to start of the next row of tiles.
  private final long tiledRowWidth;
  // Number of primitives to advance to start of next pixel row within a tile.
  private final long withinTileWidth;
  // Number of primitives to advance to start of next pixel row when in a right-edge tile.
  private final long withinRightTileWidth;

  // Number of primitives within each pixel.
  private final long pixelWidth;
  // Number of pixels per tile row.
  private final long tilePixelWidth;
  // Number of pixels per tile height.
  private final long tilePixelHeight;
  // Number of full tiles in a row.
  private final long fullTilesPerRow;
  // Number of full rows per image.
  private final long fullRowsPerImage;

  public TiledLayout(int pixelsPerRow, int numRows, int tiledPixelsWidth, int tiledPixelsHeight, int primitivesPerPixel) {
    // Easy measurements that are based on full rows or tiles, etc.
    pixelWidth = primitivesPerPixel;
    tilePixelWidth = tiledPixelsWidth;
    tilePixelHeight = tiledPixelsHeight;
    withinTileWidth = primitivesPerPixel * tiledPixelsWidth;
    regularTileWidth = withinTileWidth * tiledPixelsHeight;
    tiledRowWidth = primitivesPerPixel * pixelsPerRow * tiledPixelsHeight;

    // Hanging pixels along the right edge for partial tiles
    fullTilesPerRow = pixelsPerRow / tiledPixelsWidth;
    long rightHangingWidth = pixelsPerRow - fullTilesPerRow * tiledPixelsWidth;
    withinRightTileWidth = primitivesPerPixel * rightHangingWidth;

    // Hanging pixels along the bottom edge for partial tiles
    fullRowsPerImage = numRows / tiledPixelsHeight;
    long bottomHangingHeight = numRows - fullRowsPerImage * tiledPixelsHeight;
    bottomTileWidth = withinTileWidth * bottomHangingHeight;
  }

  @Override
  public long getIndex(int x, int y) {
    long tileX = x / tilePixelWidth;
    long tileY = y / tilePixelHeight;

    long tileOffset = tileY * tiledRowWidth;
    tileOffset += (tileY == fullRowsPerImage ? bottomTileWidth : regularTileWidth) * tileX;

    long subTileX = x % tilePixelWidth;
    long subTileY = y % tilePixelHeight;
    long inTileOffset = subTileX * pixelWidth;
    inTileOffset += subTileY * (tileX == fullTilesPerRow ? withinRightTileWidth : withinTileWidth);

    return tileOffset + inTileOffset;
  }
}
