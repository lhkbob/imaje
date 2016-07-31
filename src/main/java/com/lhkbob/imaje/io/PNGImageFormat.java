package com.lhkbob.imaje.io;

import com.lhkbob.imaje.data.Data;

/**
 *
 */
public class PNGImageFormat extends ImageIOFormat {
  public static final String FILE_SUFFIX = "png";

  public PNGImageFormat() {
    this(null);
  }

  public PNGImageFormat(Data.Factory factory) {
    super(FILE_SUFFIX, factory);
  }}
