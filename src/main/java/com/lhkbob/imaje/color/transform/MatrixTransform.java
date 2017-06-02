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

import com.lhkbob.imaje.color.Vector;
import com.lhkbob.imaje.color.VectorSpace;
import com.lhkbob.imaje.util.Arguments;

import org.ejml.data.DenseMatrix64F;
import org.ejml.ops.CommonOps;

import java.util.Arrays;
import java.util.Optional;

/**
 * MatrixTransform
 * ===============
 *
 * MatrixTransform is a general purpose transformation between vector spaces that is represented
 * as either:
 *
 * # `o = Mx`
 * # `o = Mx + c`
 *
 * where `o` is the output vector, `x` is the input vector, and `M` is a matrix and `c` is
 * an optional vector added to the result of the matrix multiplication.
 *
 * @author Michael Ludwig
 */
public class MatrixTransform<I extends Vector<I, SI>, SI extends VectorSpace<I, SI>, O extends Vector<O, SO>, SO extends VectorSpace<O, SO>> implements Transform<I, SI, O, SO> {
  private final SI inputSpace;
  private final SO outputSpace;

  private final boolean isAffine;
  private final DenseMatrix64F matrix;

  private final MatrixTransform<O, SO, I, SI> inverse;

  /**
   * Create a new MatrixTransform between the two spaces. The `matrix` is represented in row-major
   * order in the array, with a number of rows equal to the output space's channel count and a
   * number of columns equal to the input space's channel count. This matrix is a linear operation
   * between the input and output color spaces.
   *
   * The values within `matrix` are copied so future changes to that array will not affect the
   * transform's state.
   *
   * @param inputSpace
   *     The input vector space
   * @param outputSpace
   *     The output vector space
   * @param matrix
   *     The array containing a row-major matrix transform between input and output space
   * @throws IllegalArgumentException
   *     if the `matrix` array has incorrect length
   */
  public MatrixTransform(SI inputSpace, SO outputSpace, double[] matrix) {
    this(inputSpace, outputSpace,
        makeMatrix(outputSpace.getChannelCount(), inputSpace.getChannelCount(), matrix, null),
        false, true);
  }

  /**
   * Create a new MatrixTransform between the two spaces. The `matrix` is represented in row-major
   * order in the array, with a number of rows equal to the output space's channel count and a
   * number of columns equal to the input space's channel count. This matrix is a linear operation
   * between the input and output color spaces.
   *
   * If `translation` is not null, it represents an additional affine operation applied after the
   * linear matrix operation. It must have a length equal to the output space's channel count. If
   * `translation` is null, then no offset is applied to the result of the matrix multiplication.
   *
   * The values within `matrix` and `translation` are copied so future changes to these arrays will
   * not affect the transform's state.
   *
   * @param inputSpace
   *     The input vector space
   * @param outputSpace
   *     The output vector space
   * @param matrix
   *     The array containing a row-major matrix transform between input and output space
   * @param translation
   *     An optional vector to add to post-matrix multiplication
   * @throws IllegalArgumentException
   *     if the `matrix` or `translation` arrays have incorrect lengths
   */
  public MatrixTransform(
      SI inputSpace, SO outputSpace, double[] matrix, @Arguments.Nullable double[] translation) {
    this(inputSpace, outputSpace,
        makeMatrix(outputSpace.getChannelCount(), inputSpace.getChannelCount(), matrix,
            translation), true, true);
  }

  /**
   * Create a new MatrixTransform between the two spaces. The `matrix` is copied so that future
   * modifications to the provided instance will not impact the state of the transform. The required
   * dimensionality of the matrix is dependent on the channel counts of the two spaces and whether
   * or not the transform acts as an affine transform instead of a linear transform.
   *
   * If `isAffine` is false, then `matrix` is a linear transform that has the number of rows equal
   * to the channel count of `outputSpace`, and a number of columns equal to the channel count of
   * `inputSpace`.
   *
   * If `isAffine` is true, then `matrix` represents an affine transform. To achieve this it has an
   * additional row and column compared to the `isAffine = false` condition, and all input vectors
   * are considered to have an additional component equal to 1 (and the value of the corresponding
   * output vector's extra component is ignored).
   *
   * @param inputSpace
   *     The input vector space
   * @param outputSpace
   *     The output vector space
   * @param matrix
   *     The matrix transformation between the two spaces
   * @param isAffine
   *     True if the matrix has dimensions corresponding to an affine transform
   * @throws IllegalArgumentException
   *     if the dimensions of `matrix` are incorrect
   */
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

  private MatrixTransform(MatrixTransform<O, SO, I, SI> inverse) {
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
  public Optional<MatrixTransform<O, SO, I, SI>> inverse() {
    return Optional.of(inverse);
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

    // output will be length output matrix, or 1 less (and we ignore the additional homogeneous
    // coordinate)
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
