package com.lhkbob.imaje.io.awt;

import com.lhkbob.imaje.data.Data;

/**
 *
 */
public class JPEGFormat extends ImageIOFormat {
  public static final String FILE_SUFFIX = "jpg";

  public JPEGFormat() {
    this(null);
  }

  public JPEGFormat(Data.Factory factory) {
    super(FILE_SUFFIX, factory);
  }}
