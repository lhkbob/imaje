package com.lhkbob.imaje.io;

import java.io.IOException;

/**
 *
 */
public class InvalidImageException extends IOException {
  public InvalidImageException() {

  }

  public InvalidImageException(Throwable cause) {
    super(cause);
  }

  public InvalidImageException(String message) {
    super(message);
  }

  public InvalidImageException(String message, Throwable cause) {
    super(message, cause);
  }
}
