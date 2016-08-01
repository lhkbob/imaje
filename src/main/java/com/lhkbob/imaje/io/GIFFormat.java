package com.lhkbob.imaje.io;

import com.lhkbob.imaje.data.Data;

/**
 *
 */
public class GIFFormat extends ImageIOFormat {
  public static final String FILE_SUFFIX = "gif";

  public GIFFormat() {
    this(null);
  }

  public GIFFormat(Data.Factory factory) {
    super(FILE_SUFFIX, factory);
  }
}
