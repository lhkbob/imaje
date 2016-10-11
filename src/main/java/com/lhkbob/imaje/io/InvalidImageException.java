package com.lhkbob.imaje.io;

import java.io.IOException;

/**
 * Thrown if the file data is inconsistent with the expectations of the format.
 * Thrown when writing if the image cannot be saved under the restrictions of the format
 *   - FIXME Remove this last point, since we already coerce color types it should auto coerce
 *     image type to get down to a raster, etc.
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
