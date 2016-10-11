package com.lhkbob.imaje.io.awt;

import com.lhkbob.imaje.data.Data;

/**
 *
 */
public class PNGFormat extends ImageIOFormat {
  public static final String FILE_SUFFIX = "png";

  public PNGFormat() {
    this(null);
  }

  public PNGFormat(Data.Factory factory) {
    super(FILE_SUFFIX, factory);
  }}
