package com.lhkbob.imaje.color.icc.transforms;

import java.util.Arrays;

/**
 *
 */
public class ColorLookupTable implements ColorTransform {
  private final double[] axisAlphas; // working space for n alpha values from input channel to grid cell
  private final int[] gridOffsets; // indexed by input channel
  private final int[] gridSizes;
  private final int[] hyperCubeOffsets; // indexed by hypercube index
  private final int inputChannels;
  private final int outputChannels;
  private final double[] values;
  private final double[] weights; // working space for 2^n hypercube corners during interpolation

  public ColorLookupTable(int inputChannels, int outputChannels, int gridSize, double[] values) {
    this(inputChannels, outputChannels, createSimpleSizes(inputChannels, gridSize), values);
  }

  public ColorLookupTable(int inputChannels, int outputChannels, int[] gridSizes, double[] values) {
    if (gridSizes.length != inputChannels) {
      throw new IllegalArgumentException(
          "Grid size array length must equal number of input channels");
    }
    int expected = outputChannels;
    for (int i = 0; i < inputChannels; i++) {
      expected *= gridSizes[i];
    }
    if (expected != values.length) {
      throw new IllegalArgumentException(
          "Input channel, output channel and grid size do not match provided table length, expected: "
              + expected + ", actual: " + values.length);
    }
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

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public boolean equals(Object o) {
    return o == this;
  }

  @Override
  public String toString() {
    return String.format("CLUT (in: %d, out: %d,  grid: %s,)", inputChannels, outputChannels, Arrays.toString(gridSizes));
  }

  @Override
  public int getInputChannels() {
    return inputChannels;
  }

  @Override
  public int getOutputChannels() {
    return outputChannels;
  }

  @Override
  public ColorTransform inverted() {
    // FIXME is this even a reasonable thing to implement for a CLUT?
    return null;
  }

  @Override
  public void transform(double[] input, double[] output) {
    if (input.length != getInputChannels()) {
      throw new IllegalArgumentException(
          "Input vector must have " + getInputChannels() + " channels, but has " + input.length);
    }
    if (output.length != getOutputChannels()) {
      throw new IllegalArgumentException(
          "Output vector must have " + getOutputChannels() + " channels, but has " + output.length);
    }

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
