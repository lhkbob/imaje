package com.lhkbob.imaje.io;

import java.io.IOException;

/**
 *
 */
public class UnsupportedImageFormatException extends IOException {
  public UnsupportedImageFormatException() {

  }

  public UnsupportedImageFormatException(Throwable cause) {
    super(cause);
  }

  public UnsupportedImageFormatException(String message) {
    super(message);
  }

  public UnsupportedImageFormatException(String message, Throwable cause) {
    super(message, cause);
  }
}
