/*
 * BSD 3-Clause License - imaJe
 *
 * Copyright (c) 2016, Michael Ludwig
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.lhkbob.imaje.color.transform;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.util.Arguments;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import java.util.Arrays;

/**
 */
public class MatrixTransform<SI extends ColorSpace<I, SI>, I extends Color<I, SI>, SO extends ColorSpace<O, SO>, O extends Color<O, SO>> implements ColorTransform<SI, I, SO, O> {
  private final SI inputSpace;
  private final SO outputSpace;

  private final boolean isAffine;
  private final DenseMatrix64F matrix;

  private final MatrixTransform<SO, O, SI, I> inverse;

  public MatrixTransform(SI inputSpace, SO outputSpace, int numRows, int numCols, double[] matrix) {
    this(inputSpace, outputSpace, makeMatrix(numRows, numCols, matrix, null), false, true);
  }

  public MatrixTransform(
      SI inputSpace, SO outputSpace, int numRows, int numCols, double[] matrix,
      @Arguments.Nullable double[] translation) {
    this(inputSpace, outputSpace, makeMatrix(numRows, numCols, matrix, translation), true, true);
  }

  public MatrixTransform(SI inputSpace, SO outputSpace, DenseMatrix64F matrix, boolean isAffine) {
    this(inputSpace, outputSpace, matrix, isAffine, false);
  }

  private MatrixTransform(
      SI inputSpace, SO outputSpace, DenseMatrix64F matrix, boolean isAffine, boolean ownMatrix) {
    Arguments.equals("inputSpace.getChannelCount()", isAffine ? matrix.numCols - 1 : matrix.numCols,
        inputSpace.getChannelCount());
    Arguments
        .equals("outputSpace.getChannelCount()", isAffine ? matrix.numRows - 1 : matrix.numRows,
            outputSpace.getChannelCount());

    this.matrix = (ownMatrix ? matrix : matrix.copy());
    this.isAffine = isAffine;
    this.inputSpace = inputSpace;
    this.outputSpace = outputSpace;

    inverse = new MatrixTransform<>(this);
  }

  private MatrixTransform(MatrixTransform<SO, O, SI, I> inverse) {
    matrix = new DenseMatrix64F(inverse.matrix.numCols, inverse.matrix.numRows);

    if (matrix.numRows != matrix.numCols || !CommonOps.invert(inverse.matrix, matrix)) {
      // Calculate a pseudo-inverse instead of failing completely
      CommonOps.pinv(inverse.matrix, matrix);
    }

    isAffine = inverse.isAffine;
    inputSpace = inverse.getOutputSpace();
    outputSpace = inverse.getInputSpace();
    this.inverse = inverse;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof MatrixTransform)) {
      return false;
    }
    MatrixTransform c = (MatrixTransform) o;
    return c.matrix.numCols == matrix.numCols && c.matrix.numRows == matrix.numRows
        && c.isAffine == isAffine && Arrays.equals(c.matrix.data, matrix.data);
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
  public MatrixTransform<SO, O, SI, I> inverse() {
    return inverse;
  }

  @Override
  public SI getInputSpace() {
    return inputSpace;
  }

  @Override
  public SO getOutputSpace() {
    return outputSpace;
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    Arguments.equals("input.length", isAffine ? matrix.numCols - 1 : matrix.numCols, input.length);
    Arguments
        .equals("output.length", isAffine ? matrix.numRows - 1 : matrix.numRows, output.length);

    DenseMatrix64F in = new DenseMatrix64F(matrix.getNumCols(), 1);
    DenseMatrix64F out = new DenseMatrix64F(1, matrix.getNumRows());

    // input will be length of input matrix, or 1 less
    System.arraycopy(input, 0, in.data, 0, input.length);
    if (isAffine) {
      // Make sure last value in input matrix is 1.0 (when affine, input.length ==
      // this.input.numCols - 1)
      in.set(input.length, 1.0);
    }
    CommonOps.mult(matrix, in, out);

    // output will be length output matrix, or 1 less (and we ignore the additional homogenous
    // coord)
    System.arraycopy(out.data, 0, output, 0, output.length);
    return true;
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
