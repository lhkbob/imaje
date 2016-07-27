package com.lhkbob.imaje.color.transform.general;

import com.lhkbob.imaje.util.Arguments;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import java.util.Arrays;

/**
 */
public class Matrix implements Transform {
  public static final Matrix IDENTITY_3X3 = new Matrix(
      new DenseMatrix64F(3, 3, true, 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0), false);
  private final DenseMatrix64F input;
  private final boolean isAffine;
  private final DenseMatrix64F matrix;
  private final DenseMatrix64F output;

  public Matrix(int numRows, int numCols, double[] matrix) {
    this(makeMatrix(numRows, numCols, matrix, null), false, true);
  }

  public Matrix(int numRows, int numCols, double[] matrix, @Arguments.Nullable double[] translation) {
    this(makeMatrix(numRows, numCols, matrix, translation), true, true);
  }

  public Matrix(DenseMatrix64F matrix, boolean isAffine) {
    this(matrix, isAffine, false);
  }

  private Matrix(DenseMatrix64F matrix, boolean isAffine, boolean ownMatrix) {
    this.matrix = (ownMatrix ? matrix : matrix.copy());
    this.isAffine = isAffine;
    input = new DenseMatrix64F(matrix.getNumCols(), 1);
    output = new DenseMatrix64F(1, matrix.getNumRows());
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
    return c.matrix.numCols == matrix.numCols && c.matrix.numRows == matrix.numRows
        && c.isAffine == isAffine && Arrays.equals(c.matrix.data, matrix.data);
  }

  @Override
  public int getInputChannels() {
    return isAffine ? matrix.numCols - 1 : matrix.numCols;
  }

  @Override
  public Matrix getLocallySafeInstance() {
    // Input/output matrices cannot be shared by threads
    return new Matrix(matrix, isAffine, true);
  }

  @Override
  public int getOutputChannels() {
    return isAffine ? matrix.numRows - 1 : matrix.numRows;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + Arrays.hashCode(matrix.data);
    result = 31 * result + matrix.numCols;
    result = 31 * result + matrix.numRows;
    result = 31 * result + Boolean.hashCode(isAffine);
    return result;
  }

  @Override
  public Transform inverted() {
    DenseMatrix64F inv = new DenseMatrix64F(matrix.numCols, matrix.numRows);

    if (matrix.numRows != matrix.numCols || !CommonOps.invert(matrix, inv)) {
      // Calculate a pseudo-inverse instead of failing completely
      CommonOps.pinv(matrix, inv);
    }
    return new Matrix(inv, isAffine, true);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Color Matrix (").append(matrix.numRows).append(" x ")
        .append(matrix.numCols).append("):\n");
    sb.append("  matrix: [");

    int matRows = (isAffine ? matrix.numRows - 1 : matrix.numRows);
    int matCols = (isAffine ? matrix.numCols - 1 : matrix.numCols);

    for (int i = 0; i < matRows; i++) {
      if (i > 0) {
        sb.append(",\n           "); // padding for alignment
      }
      for (int j = 0; j < matCols; j++) {
        if (j > 0) {
          sb.append(", ");
        }
        sb.append(String.format("%.3f", matrix.get(i, j)));
      }
    }
    sb.append("]");

    if (isAffine) {
      sb.append("\n  with offset: [");
      for (int i = 0; i < matCols; i++) {
        if (i > 0) {
          sb.append(", ");
        }
        sb.append(String.format("%.3f", matrix.get(matRows, i)));
      }
      sb.append("]");
    }

    return sb.toString();
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

    // input will be length of input matrix, or 1 less
    System.arraycopy(input, 0, this.input.data, 0, input.length);
    if (isAffine) {
      // Make sure last value in input matrix is 1.0 (when affine, input.length == this.input.numCols - 1)
      this.input.set(input.length, 1.0);
    }
    CommonOps.mult(matrix, this.input, this.output);

    // output will be length output matrix, or 1 less (and we ignore the additional homogenous coord)
    System.arraycopy(this.output.data, 0, output, 0, output.length);
  }

  private static DenseMatrix64F makeMatrix(
      int numRows, int numCols, double[] matrix, @Arguments.Nullable double[] translation) {
    Arguments.isPositive("numRows", numRows);
    Arguments.isPositive("numCols", numCols);
    Arguments.notNull("matrix", matrix);

    if (translation == null) {
      // Assume that the data is a plain row-major matrix
      return new DenseMatrix64F(numRows, numCols, true, matrix);
    } else {
      Arguments.equals("matrix.length", numRows * numCols, matrix.length);
      Arguments.equals("translation.length", numRows, translation.length);

      // Assume that the matrix value is numRows x numCols but then add a row and column to
      // make it affine by adding [translation, 1.0] as a column and all 0s in the new last row (besides the 1)
      DenseMatrix64F m = new DenseMatrix64F(numRows + 1, numCols + 1);
      m.set(numRows, numCols, 1.0);
      for (int i = 0; i < numRows; i++) {
        m.set(i, numCols, translation[i]);
        for (int j = 0; j < numCols; j++) {
          m.set(i, j, matrix[i * numCols + j]);
        }
      }

      return m;
    }
  }
}
