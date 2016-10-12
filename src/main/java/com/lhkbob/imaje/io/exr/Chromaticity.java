package com.lhkbob.imaje.io.exr;

import com.lhkbob.imaje.data.Bytes;
import com.lhkbob.imaje.io.IO;
import com.lhkbob.imaje.io.InvalidImageException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * Predefined attribute describing RGB color space, or set to special values for CIE XYZ stored
 * in r,g,b channels.
 */
public class Chromaticity {
  private final float redX, redY, greenX, greenY, blueX, blueY, whiteX, whiteY;

  public Chromaticity(
      float redX, float redY, float greenX, float greenY, float blueX, float blueY, float whiteX,
      float whiteY) {
    this.redX = redX;
    this.redY = redY;
    this.greenX = greenX;
    this.greenY = greenY;
    this.blueX = blueX;
    this.blueY = blueY;
    this.whiteX = whiteX;
    this.whiteY = whiteY;
  }

  public float getRedX() {
    return redX;
  }

  public float getRedY() {
    return redY;
  }

  public float getGreenX() {
    return greenX;
  }

  public float getGreenY() {
    return greenY;
  }

  public float getBlueX() {
    return blueX;
  }

  public float getBlueY() {
    return blueY;
  }

  public float getWhiteX() {
    return whiteX;
  }

  public float getWhiteY() {
    return whiteY;
  }

  public boolean isCIEXYZ() {
    return Math.abs(redX - 1.0) < 1e-5 && Math.abs(redY) < 1e-5 && Math.abs(greenX) < 1e-5
        && Math.abs(greenY - 1.0) < 1e-5 && Math.abs(blueX) < 1e-5 && Math.abs(blueY) < 1e-5
        && Math.abs(whiteX - 0.33333) < 1e-5 && Math.abs(whiteY - 0.33333) < 1e-5;
  }

  public static Chromaticity read(SeekableByteChannel in, ByteBuffer work) throws IOException {
    if (!IO.read(in, work, 32)) {
      throw new InvalidImageException("Unexpected EOF while reading Chromaticity type");
    }

    float redX = Bytes.bytesToFloatLE(work);
    float redY = Bytes.bytesToFloatLE(work);
    float greenX = Bytes.bytesToFloatLE(work);
    float greenY = Bytes.bytesToFloatLE(work);
    float blueX = Bytes.bytesToFloatLE(work);
    float blueY = Bytes.bytesToFloatLE(work);
    float whiteX = Bytes.bytesToFloatLE(work);
    float whiteY = Bytes.bytesToFloatLE(work);

    return new Chromaticity(redX, redY, greenX, greenY, blueX, blueY, whiteX, whiteY);
  }
}
