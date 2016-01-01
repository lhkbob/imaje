package com.lhkbob.imaje.color.icc;

import java.util.Arrays;

/**
 *
 */
public class ColorMatrix implements ColorTransform {
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
  public int getInputChannels() {
    return numColumns;
  }

  @Override
  public int getOutputChannels() {
    return numRows;
  }

  @Override
  public ColorTransform inverted() {
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
      for (int j = 0; j < numColumns; i++) {
        output[i] += matrix[offset + j] * input[j];
      }
    }
  }
}
