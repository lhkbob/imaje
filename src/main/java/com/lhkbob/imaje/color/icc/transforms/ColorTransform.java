package com.lhkbob.imaje.color.icc.transforms;

/**
 *
 */
public interface ColorTransform {
  int getInputChannels();

  int getOutputChannels();

  ColorTransform inverted();

  void transform(double[] input, double[] output);

  // FIXME do we need to track the range and domain of the elements?
  // e.g. if subsequent stages have different intervals, do we clamp or scale?
  // FIXME what about the multiProcessElement curves that have an infinite range, compared to
  // the functions using the LUT-based tags that assume 0-1?

  // It looks like there's either a 0-1 range/domain scheme, OR infinite range/domain
}
