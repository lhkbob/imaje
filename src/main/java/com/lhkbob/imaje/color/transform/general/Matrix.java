package com.lhkbob.imaje.color.transform.general;

import java.util.Arrays;

/**
 *
 */
public class Matrix implements Transform {
  public static final Matrix IDENTITY_3X3 = new Matrix(
      3, 3, new double[] { 1, 0, 0, 0, 1, 0, 0, 0, 1 });
  private final double[] matrix; // row major
  private final int numColumns;
  private final int numRows;
  private final double[] translation;

  public Matrix(int numRows, int numColumns, double[] matrix) {
    this(numRows, numColumns, matrix, new double[numRows]);
  }

  public Matrix(int numRows, int numColumns, double[] matrix, double[] translation) {
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

  private Matrix(int numRows, int numColumns) {
    this.numRows = numRows;
    this.numColumns = numColumns;
    this.matrix = new double[numRows * numColumns];
    this.translation = new double[numRows];
  }

  public double get(int i, int j) {
    if (j == numColumns) {
      return translation[i];
    } else {
      return matrix[i * numColumns + j];
    }
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
    if (!(o instanceof Matrix)) {
      return false;
    }
    Matrix c = (Matrix) o;
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
  public Transform inverted() {
    // To avoid importing a general matrix library for this single function, only a 3x3 matrix
    // inversion is implemented since it can be coded explicitly. Lastly, the vast majority of
    // color matrix use in an ICC profile is tied to the XYZ color space so should be a 3x3.
    //
    // If the matrix comes from one of the newer multiprocess element transforms, then it's likely
    // the profile builder provided an inverse transform tag as well.
    if (numColumns != 3 || numRows != 3) {
      return null;
    }
    // If the translation part of the transform is non-zero the matrix is technically not
    // 3x3 anymore and can't be inverted with this code
    for (int i = 0; i < numRows; i++) {
      if (Math.abs(translation[i]) > 1e-8) {
        return null;
      }
    }

    return invert3x3();
  }

  private double det3x3() {
    double t1 = get(1, 1) * get(2, 2) - get(1, 2) * get(2, 1);
    double t2 = get(1, 0) * get(2, 2) - get(1, 2) * get(2, 0);
    double t3 = get(1, 0) * get(2, 1) - get(1, 1) * get(2, 0);
    return get(0, 0) * t1 - get(0, 1) * t2 + get(0, 2) * t3;
  }

  private Matrix invert3x3() {
    double invDet = det3x3(); // not inverted yet
    if (Math.abs(invDet) < 1e-8) {
      return null; // singular matrix can't be inverted
    }
    invDet = 1 / invDet;

    // Fill in the matrix values in place, which is fine since the object hasn't been published yet
    Matrix m = new Matrix(3, 3);

    m.matrix[0] = invDet * (get(2, 2) * get(1, 1) - get(2, 1) * get(1, 2));
    m.matrix[1] = invDet * (get(2, 1) * get(0, 2) - get(2, 2) * get(0, 1));
    m.matrix[2] = invDet * (get(1, 2) * get(0, 1) - get(1, 1) * get(0, 2));

    m.matrix[3] = invDet * (get(2, 0) * get(1, 2) - get(2, 2) * get(1, 0));
    m.matrix[4] = invDet * (get(2, 2) * get(0, 0) - get(2, 0) * get(0, 2));
    m.matrix[5] = invDet * (get(1, 0) * get(0, 2) - get(1, 2) * get(0, 0));

    m.matrix[6] = invDet * (get(2, 1) * get(1, 0) - get(2, 0) * get(1, 1));
    m.matrix[7] = invDet * (get(2, 0) * get(0, 1) - get(2, 1) * get(0, 0));
    m.matrix[8] = invDet * (get(1, 1) * get(0, 0) - get(1, 0) * get(0, 1));

    return m;
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
