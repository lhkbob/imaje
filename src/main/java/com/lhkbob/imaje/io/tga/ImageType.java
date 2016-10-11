package com.lhkbob.imaje.io.tga;

/**
 *
 */
public enum ImageType {
  // No image data
  NO_IMAGE(0, false, false, false, false),
  // Uncompressed color mapped image
  COLORMAP(1, true, false, false, false),
  // Uncompressed true color image
  TRUECOLOR(2, false, false, false, false),
  // Uncompressed black and white image
  BLACKWHITE(3, false, false, false, true),
  // Compressed color mapped image via runlength encoding
  RL_COLORMAP(9, true, true, false, false),
  // Compressed true color image via runlength encoding
  RL_TRUECOLOR(10, false, true, false, false),
  // Compressed black and white image via runlength encoding
  RL_BLACKWHITE(11, false, true, false, true),
  // Compressed color mapped data using Huffman, Delta, and runlength encoding, 4-pass quadtree
  // processing.
  HDRL_COLORMAP_QUAD(32, true, true, true, false),
  // Compressed color mapped data with Huffman, Delta, and runlength encoding
  HDRL_COLORMAP(33, true, true, true, false);

  private final int type;
  private final boolean colorMap;
  private final boolean rlCompressed;
  private final boolean hdCompressed;
  private final boolean bw;

  ImageType(int type, boolean colorMap, boolean rlCompressed, boolean hdCompressed, boolean bw) {
    this.type = type;
    this.colorMap = colorMap;
    this.rlCompressed = rlCompressed;
    this.hdCompressed = hdCompressed;
    this.bw = bw;
  }

  public int getTypeID() {
    return type;
  }

  public boolean requiresColorMap() {
    return colorMap;
  }

  public boolean isRunLengthEncoded() {
    return rlCompressed;
  }

  public boolean isHuffmanDeltaCompressed() {
    return hdCompressed;
  }

  public boolean isBlackAndWhite() {
    return bw;
  }

  public static ImageType fromTypeID(int id) {
    for (ImageType t: values()) {
      if (t.getTypeID() == id) {
        return t;
      }
    }
    return null;
  }
}
