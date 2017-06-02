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

import java.util.Arrays;
import java.util.Optional;

/**
 * LookupTable
 * ===========
 *
 * LookupTable provides an explicit transformation between two multidimensional spaces. The each
 * input dimension can have its own sampling resolution, termed grid size. Elements within the table
 * are arranged in "row-major" order, where all values for the first sample of the first dimension
 * come before values for later samples of the first dimension. Within that first sample's row,
 * values are grouped by the first sample of the second dimension, etc. The final value for a given
 * input coordinate is a packed vector of primitives, with dimensionality matching the output space.
 *
 * Multidimensional linear interpolation is used to sample the table and estimate a smoothed output
 * value. If the table is two dimensional, this is equivalent to bilinear filtering. The layout and
 * estimation algorithm are compatible with the lookup table used in ICC color profiles.
 *
 * @author Michael Ludwig
 */
public class LookupTable<A extends Vector<A, SA>, SA extends VectorSpace<A, SA>, B extends Vector<B, SB>, SB extends VectorSpace<B, SB>> implements Transform<A, SA, B, SB> {
  private final int[] gridOffsets; // indexed by input channel
  private final int[] gridSizes;
  private final int[] hyperCubeOffsets; // indexed by hypercube index
  private final SA inSpace;
  private final SB outSpace;
  private final double[] values;

  /**
   * Create a lookup table used to transform between the two spaces. `gridSize` is the resolution of
   * each input channel in the table. `values` is a multi-valued hypercube with dimensions equal to
   * the channel count of `inSpace`. The layout of the `values` table is explained above.
   *
   * This is the same as the other constructor, where the `gridSizes` array is filled with the
   * constant `gridSize`.
   *
   * @param inSpace
   *     The input space
   * @param outSpace
   *     The output space
   * @param gridSize
   *     The resolution of each input dimension in the table
   * @param values
   *     The hypercube of values from input to output space
   * @throws IllegalArgumentException
   *     if `values` does not have the correct length given input, output space, and grid size
   */
  public LookupTable(SA inSpace, SB outSpace, int gridSize, double[] values) {
    this(inSpace, outSpace, createSimpleSizes(inSpace.getChannelCount(), gridSize), values);
  }

  /**
   * Create a lookup table used to transform between the two spaces. `gridSizes` specifies the
   * sampling resolution of each input dimension. Thus, its length must equal the channel count of
   * the input space. The layout and size of the `values` table is explained above.
   *
   * @param inSpace
   *     The input space
   * @param outSpace
   *     The output space
   * @param gridSizes
   *     The array of resolutions for each dimension of the input space
   * @param values
   *     The hypercube of values from input to output space
   * @throws IllegalArgumentException
   *     if `values` does not have the correct length given input, output space, and grid sizes
   */
  public LookupTable(SA inSpace, SB outSpace, int[] gridSizes, double[] values) {
    Arguments.notNull("gridSizes", gridSizes);
    Arguments.notNull("values", values);

    int inputChannels = inSpace.getChannelCount();
    int outputChannels = outSpace.getChannelCount();
    Arguments.equals("gridSizes.length", inputChannels, gridSizes.length);

    int expected = outputChannels;
    for (int i = 0; i < inputChannels; i++) {
      expected *= gridSizes[i];
    }
    Arguments.equals("values.length", expected, values.length);

    this.inSpace = inSpace;
    this.outSpace = outSpace;
    this.gridSizes = Arrays.copyOf(gridSizes, gridSizes.length);
    this.values = Arrays.copyOf(values, values.length);

    // Compute offsets for each input channel; ICC spec says first channel varies least rapidly
    gridOffsets = new int[inputChannels];
    gridOffsets[inputChannels - 1] = outputChannels;
    for (int i = inputChannels - 2; i >= 0; i--) {
      gridOffsets[i] = gridSizes[i + 1] * gridOffsets[i + 1];
    }
    // Compute offsets for each hypercube corner
    hyperCubeOffsets = new int[1 << inputChannels];
    hyperCubeOffsets[0] = 0; // base corner
    int power = 1;
    for (int i = 0; i < inputChannels; i++) {
      // Each dimension that is incorporated here basically copies the prior state of the array
      // and adds the grid offset for the current dimension
      for (int j = 0; j < power; j++) {
        hyperCubeOffsets[power + j] = hyperCubeOffsets[j] + gridOffsets[i];
      }
      power *= 2;
    }
  }

  @Override
  public boolean equals(Object o) {
    return o == this;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public Optional<LookupTable<B, SB, A, SA>> inverse() {
    return Optional.empty();
  }

  @Override
  public SA getInputSpace() {
    return inSpace;
  }

  @Override
  public SB getOutputSpace() {
    return outSpace;
  }

  @Override
  public String toString() {
    return String.format("CLUT (in: %d, out: %d,  grid: %s,)", inSpace.getChannelCount(),
        outSpace.getChannelCount(), Arrays.toString(gridSizes));
  }

  @Override
  public boolean applyUnchecked(double[] input, double[] output) {
    int inputChannels = inSpace.getChannelCount();
    int outputChannels = outSpace.getChannelCount();
    Arguments.equals("input.length", inputChannels, input.length);
    Arguments.equals("output.length", outputChannels, output.length);

    // working space for n alpha values from input channel to grid cell
    double[] axisAlphas = new double[inputChannels];
    // working space for 2^n hypercube corners during interpolation
    double[] weights = new double[1 << inputChannels];


    // Calculate offset into the grid for the lowest corner of hypercube, and calculate the
    // alpha value = input - axis for each dimension.
    int baseOffset = 0;
    for (int i = 0; i < inputChannels; i++) {
      int gridSizeM1 = gridSizes[i] - 1;
      double valueInGrid = input[i] * gridSizeM1;
      if (valueInGrid < 0.0) {
        valueInGrid = 0.0;
      } else if (valueInGrid > gridSizeM1) {
        valueInGrid = gridSizeM1;
      }

      int gridCell = (int) Math.floor(valueInGrid);
      if (gridCell >= gridSizeM1) {
        // Make sure that the base grid we access is length - 2 for a dimension, so that the
        // far corner of the hypercube will be at most length - 1 and we don't need to worry about
        // bounds checks later on in the algorithm.
        gridCell = gridSizeM1 - 1;
      }

      axisAlphas[i] = valueInGrid - gridCell; // Cells are already unit so no normalizing is needed
      baseOffset += gridCell * gridOffsets[i];
    }

    // Efficiently iterate over the 2^N hypercube corners and form every combination of
    // alpha_i and (1 - alpha_i) for all dimensions.
    int power = 1;
    weights[0] = 1.0;
    for (int i = 0; i < inputChannels; i++) {
      for (int j = 0; j < power; j++) {
        weights[power + j] = weights[j] * axisAlphas[i];
        weights[j] *= (1.0 - axisAlphas[i]);
      }
      power *= 2;
    }

    // Initialize output to the weighted base corner of the hypercube
    int d = baseOffset + hyperCubeOffsets[0];
    double w = weights[0];
    for (int o = 0; o < outputChannels; o++) {
      output[o] = w * values[d + o];
    }
    // Incorporate the remaining corners with their associated weights
    for (int i = 1; i < weights.length; i++) {
      d = baseOffset + hyperCubeOffsets[i];
      w = weights[i];
      for (int o = 0; o < outputChannels; o++) {
        output[o] += w * values[d + o];
      }
    }

    return true;
  }

  private static int[] createSimpleSizes(int inputChannels, int gridSize) {
    int[] sizes = new int[inputChannels];
    Arrays.fill(sizes, gridSize);
    return sizes;
  }
}
