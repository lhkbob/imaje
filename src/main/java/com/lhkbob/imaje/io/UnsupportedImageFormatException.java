package com.lhkbob.imaje.io;

/**
 *
 */
public class UnsupportedImageFormatException extends RuntimeException {
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
