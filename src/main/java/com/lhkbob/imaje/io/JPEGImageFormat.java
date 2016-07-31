package com.lhkbob.imaje.io;

import com.lhkbob.imaje.data.Data;

/**
 *
 */
public class JPEGImageFormat extends ImageIOFormat {
  public static final String FILE_SUFFIX = "jpg";

  public JPEGImageFormat() {
    this(null);
  }

  public JPEGImageFormat(Data.Factory factory) {
    super(FILE_SUFFIX, factory);
  }}
