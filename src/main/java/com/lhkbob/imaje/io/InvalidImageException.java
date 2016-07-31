package com.lhkbob.imaje.io;

/**
 *
 */
public class InvalidImageException extends RuntimeException {
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
