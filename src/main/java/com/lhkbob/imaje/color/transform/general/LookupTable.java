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
package com.lhkbob.imaje.color.transform.general;

import com.lhkbob.imaje.util.Arguments;

import java.util.Arrays;

/**
 *
 */
public class LookupTable implements Transform {
  private final double[] axisAlphas; // working space for n alpha values from input channel to grid cell
  private final int[] gridOffsets; // indexed by input channel
  private final int[] gridSizes;
  private final int[] hyperCubeOffsets; // indexed by hypercube index
  private final int inputChannels;
  private final int outputChannels;
  private final double[] values;
  private final double[] weights; // working space for 2^n hypercube corners during interpolation

  public LookupTable(int inputChannels, int outputChannels, int gridSize, double[] values) {
    this(inputChannels, outputChannels, createSimpleSizes(inputChannels, gridSize), values);
  }

  public LookupTable(int inputChannels, int outputChannels, int[] gridSizes, double[] values) {
    Arguments.notNull("gridSizes", gridSizes);
    Arguments.notNull("values", values);
    Arguments.isPositive("inputChannels", inputChannels);
    Arguments.isPositive("outputChannels", outputChannels);
    Arguments.equals("gridSizes.length", inputChannels, gridSizes.length);

    int expected = outputChannels;
    for (int i = 0; i < inputChannels; i++) {
      expected *= gridSizes[i];
    }
    Arguments.equals("values.length", expected, values.length);

    this.inputChannels = inputChannels;
    this.outputChannels = outputChannels;
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

    weights = new double[1 << inputChannels];
    axisAlphas = new double[inputChannels];
  }

  private LookupTable(LookupTable toClone) {
    // Read-only data that was calculated at construction time
    gridOffsets = toClone.gridOffsets;
    gridSizes = toClone.gridSizes;
    hyperCubeOffsets = toClone.hyperCubeOffsets;
    inputChannels = toClone.inputChannels;
    outputChannels = toClone.outputChannels;
    values = toClone.values;

    // Working storage during transform
    axisAlphas = new double[toClone.axisAlphas.length];
    weights = new double[toClone.weights.length];
  }

  @Override
  public boolean equals(Object o) {
    return o == this;
  }

  @Override
  public int getInputChannels() {
    return inputChannels;
  }

  @Override
  public Transform getLocallySafeInstance() {
    // The majority of a lookup-table's data is constant, except for two working arrays. Use this
    // private constructor to share data references where possible and allocate new safe member
    // instances for the working arrays.
    return new LookupTable(this);
  }

  @Override
  public int getOutputChannels() {
    return outputChannels;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public Transform inverted() {
    // FIXME is this even a reasonable thing to implement for a CLUT?
    return null;
  }

  @Override
  public String toString() {
    return String.format("CLUT (in: %d, out: %d,  grid: %s,)", inputChannels, outputChannels,
        Arrays.toString(gridSizes));
  }

  @Override
  public void transform(double[] input, double[] output) {
    Transform.validateDimensions(this, input, output);

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
  }

  private static int[] createSimpleSizes(int inputChannels, int gridSize) {
    int[] sizes = new int[inputChannels];
    Arrays.fill(sizes, gridSize);
    return sizes;
  }
}
