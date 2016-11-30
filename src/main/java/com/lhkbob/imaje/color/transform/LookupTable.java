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

import java.util.Arrays;

/**
 *
 */
public class LookupTable<SA extends ColorSpace<A, SA>, A extends Color<A, SA>, SB extends ColorSpace<B, SB>, B extends Color<B, SB>> implements ColorTransform<SA, A, SB, B> {
  private final int[] gridOffsets; // indexed by input channel
  private final int[] gridSizes;
  private final int[] hyperCubeOffsets; // indexed by hypercube index
  private final SA inSpace;
  private final SB outSpace;
  private final double[] values;

  public LookupTable(SA inSpace, SB outSpace, int gridSize, double[] values) {
    this(inSpace, outSpace, createSimpleSizes(inSpace.getChannelCount(), gridSize), values);
  }

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
  public LookupTable<SB, B, SA, A> inverse() {
    // FIXME implement this somehow, brute force reconstruction of another table? creation of
    // an explicit inverse (e.g. also provided in ICC profile)
    throw new UnsupportedOperationException("Inverse is unavailable for look up tables");
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
    return String.format("CLUT (in: %d, out: %d,  grid: %s,)", inSpace.getChannelCount(), outSpace.getChannelCount(),
        Arrays.toString(gridSizes));
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
