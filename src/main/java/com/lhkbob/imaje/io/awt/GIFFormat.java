package com.lhkbob.imaje.io.awt;

import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.util.Arguments;

/**
 *
 */
public class GIFFormat extends ImageIOFormat {
  public static final String FILE_SUFFIX = "gif";

  public GIFFormat() {
    this(null);
  }

  public GIFFormat(@Arguments.Nullable Data.Factory factory) {
    super(FILE_SUFFIX, factory);
  }
}
