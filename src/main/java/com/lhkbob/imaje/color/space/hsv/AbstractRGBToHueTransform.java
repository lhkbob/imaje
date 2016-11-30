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
package com.lhkbob.imaje.color.space.hsv;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.transform.ColorTransform;
import com.lhkbob.imaje.util.Arguments;

/**
 *
 */
public abstract class AbstractRGBToHueTransform<SI extends ColorSpace<RGB<SI>, SI>, SO extends ColorSpace<O, SO>, O extends Color<O, SO>> implements ColorTransform<SI, RGB<SI>, SO, O> {
  private final SI inputSpace;
  private final SO outputSpace;

  public AbstractRGBToHueTransform(SI inputSpace, SO outputSpace) {
    Arguments.notNull("inputSpace", inputSpace);
    Arguments.notNull("outputSpace", outputSpace);

    this.inputSpace = inputSpace;
    this.outputSpace = outputSpace;
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
    Arguments.equals("input.length", 3, input.length);
    Arguments.equals("output.length", 3, output.length);

    double hue, min, max;
    if (input[0] >= input[1] && input[0] >= input[2]) {
      // Red is the largest component
      max = input[0];
      min = Math.min(input[1], input[2]);
      hue = ((input[1] - input[2]) / (max - min)) % 6.0;
    } else if (input[1] >= input[2]) {
      // Green is the largest component
      max = input[1];
      min = Math.min(input[0], input[2]);
      hue = (input[2] - input[0]) / (max - min) + 2.0;
    } else {
      // Blue is the largest component
      max = input[2];
      min = Math.min(input[0], input[1]);
      hue = (input[0] - input[1]) / (max - min) + 4.0;
    }

    output[0] = hue;
    output[1] = min;
    output[2] = max;
    fromHueMinMax(output);
    return true;
  }

  protected abstract void fromHueMinMax(double[] output);
}
