package com.lhkbob.imaje.color.icc.transforms;

import java.util.Arrays;

/**
 *
 */
public class ColorMatrix implements ColorTransform {
  public static final ColorMatrix IDENTITY_3X3 = new ColorMatrix(
      3, 3, new double[] { 1, 0, 0, 0, 1, 0, 0, 0, 1 });
  private final double[] matrix; // row major
  private final int numColumns;
  private final int numRows;
  private final double[] translation;

  public ColorMatrix(int numRows, int numColumns, double[] matrix) {
    this(numRows, numColumns, matrix, new double[numRows]);
  }

  public ColorMatrix(int numRows, int numColumns, double[] matrix, double[] translation) {
    if (numRows < 1) {
      throw new IllegalArgumentException("Number of rows must be at least 1, not: " + numRows);
    }
    if (numColumns < 1) {
      throw new IllegalArgumentException(
          "Number of columns must be at least 1, not: " + numColumns);
    }
    if (matrix.length != numRows * numColumns) {
      throw new IllegalArgumentException(
          "Matrix array must be of length " + (numRows * numColumns) + ", but was "
              + matrix.length);
    }
    if (translation.length != numRows) {
      throw new IllegalArgumentException(
          "Translation vector must be of length " + numRows + ", but was " + translation.length);
    }

    this.numColumns = numColumns;
    this.numRows = numRows;
    this.matrix = Arrays.copyOf(matrix, matrix.length);
    this.translation = Arrays.copyOf(translation, translation.length);
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + Arrays.hashCode(matrix);
    result = 31 * result + Arrays.hashCode(translation);
    result = 31 * result + numRows;
    result = 31 * result + numColumns;
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ColorMatrix)) {
      return false;
    }
    ColorMatrix c = (ColorMatrix) o;
    return c.numColumns == numColumns && c.numRows == numRows && Arrays
        .equals(c.translation, translation) && Arrays.equals(c.matrix, matrix);
  }

  @Override
  public String toString() {
    boolean hasTranslation = false;
    for (int i = 0; i < translation.length; i++) {
      if (translation[i] != 0.0) {
        hasTranslation = true;
      }
    }

    StringBuilder sb = new StringBuilder("Color Matrix (").append(numRows).append(" x ")
        .append(numColumns).append("):\n");
    sb.append("  matrix: [");
    for (int i = 0; i < numRows; i++) {
      if (i > 0) {
        sb.append("\n           "); // padding for alignment
      }
      for (int j = 0; j < numColumns; j++) {
        if (j > 0) {
          sb.append(", ");
        }
        sb.append(String.format("%.3f", matrix[i * numColumns + j]));
      }
    }
    sb.append("]");

    if (hasTranslation) {
      sb.append("\n  with offset: [");
      for (int i = 0; i < numColumns; i++) {
        if (i > 0) {
          sb.append(", ");
        }
        sb.append(String.format("%.3f", translation[i]));
      }
      sb.append("]");
    }

    return sb.toString();
  }

  @Override
  public int getInputChannels() {
    return numColumns;
  }

  @Override
  public int getOutputChannels() {
    return numRows;
  }

  @Override
  public ColorTransform inverted() {
    // FIXME implement explicit 3x3 inverse
    return null;
  }

  @Override
  public void transform(double[] input, double[] output) {
    if (input.length != numColumns) {
      throw new IllegalArgumentException(
          "Input vector must have dimension " + numColumns + ", not " + input.length);
    }
    if (output.length != numRows) {
      throw new IllegalArgumentException(
          "Output vector must have dimension " + numRows + ", not " + output.length);
    }

    for (int i = 0; i < numRows; i++) {
      int offset = i * numColumns;
      output[i] = translation[i];
      for (int j = 0; j < numColumns; j++) {
        output[i] += matrix[offset + j] * input[j];
      }
    }
  }
}
