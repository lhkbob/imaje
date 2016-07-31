package com.lhkbob.imaje.io;

import com.lhkbob.imaje.data.Data;

/**
 *
 */
public class GIFImageFormat extends ImageIOFormat {
  public static final String FILE_SUFFIX = "gif";

  public GIFImageFormat() {
    this(null);
  }

  public GIFImageFormat(Data.Factory factory) {
    super(FILE_SUFFIX, factory);
  }
}
