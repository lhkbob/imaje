package com.lhkbob.imaje.io.awt;

import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.util.Arguments;

/**
 *
 */
public class JPEGFormat extends ImageIOFormat {
  public static final String FILE_SUFFIX = "jpg";

  public JPEGFormat() {
    this(null);
  }

  public JPEGFormat(@Arguments.Nullable Data.Factory factory) {
    super(FILE_SUFFIX, factory);
  }}
