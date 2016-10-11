package com.lhkbob.imaje.io.awt;

import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.util.Arguments;

/**
 *
 */
public class PNGFormat extends ImageIOFormat {
  public static final String FILE_SUFFIX = "png";

  public PNGFormat() {
    this(null);
  }

  public PNGFormat(@Arguments.Nullable Data.Factory factory) {
    super(FILE_SUFFIX, factory);
  }}
