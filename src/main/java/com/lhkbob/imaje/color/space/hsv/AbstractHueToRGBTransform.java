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

import java.util.Objects;

/**
 * AbstractHueToRGBTransform
 * =========================
 *
 * A helper parent class for hue-based color transformations to an RGB space. This is accomplished
 * by separating the logic of converting the hue space to an internal hue, chroma, and m space
 * which this parent class then processes into RGB. The hue, chroma, and m coordinates are described
 * (https://en.wikipedia.org/wiki/HSL_and_HSV#Hue_and_chroma)[here].
 *
 * @author Michael Ludwig
 */
public abstract class AbstractHueToRGBTransform<SI extends ColorSpace<I, SI>, I extends Color<I, SI>, SO extends ColorSpace<RGB<SO>, SO>> implements ColorTransform<SI, I, SO, RGB<SO>> {
  private final SI inputSpace;
  private final SO outputSpace;

  /**
   * Create a transformation from the `inputSpace` to the `outputSpace`.
   *
   * @param inputSpace
   *     The input hue space
   * @param outputSpace
   *     The output RGB space
   */
  public AbstractHueToRGBTransform(SI inputSpace, SO outputSpace) {
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

    toHueChromaM(input, output);
    hueChromaMToRGB(output);
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!getClass().isInstance(o)) {
      return false;
    }
    AbstractHueToRGBTransform s = (AbstractHueToRGBTransform) o;
    return Objects.equals(s.inputSpace, inputSpace) && Objects.equals(s.outputSpace, outputSpace);
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + getClass().hashCode();
    result = 31 * result + inputSpace.hashCode();
    result = 31 * result + outputSpace.hashCode();
    return result;
  }

  /**
   * Convert the `input` array of channel values, arranged as expected by the subclass, into the
   * internal hue, chroma, and M representation used for the final RGB process. These values should
   * be ordered hue, chroma, and M in the `hcm` array, which has length 3.
   *
   * @param input
   *     The input channel values
   * @param hcm
   *     The hue, chroma, M output
   */
  protected abstract void toHueChromaM(double[] input, double[] hcm);

  private void hueChromaMToRGB(double[] output) {
    // This assumes that output has been rewritten to hold hue, chroma, and m
    double chroma = output[1];
    double hp = output[0] / 60.0;
    double x = chroma * (1.0 - Math.abs(hp % 2.0 - 1.0));
    double m = output[2];

    if (hp < 1.0) {
      output[0] = chroma + m;
      output[1] = x + m;
      output[2] = m;
    } else if (hp < 2.0) {
      output[0] = x + m;
      output[1] = chroma + m;
      output[2] = m;
    } else if (hp < 3.0) {
      output[0] = m;
      output[1] = chroma + m;
      output[2] = x + m;
    } else if (hp < 4.0) {
      output[0] = m;
      output[1] = x + m;
      output[2] = chroma + m;
    } else if (hp < 5.0) {
      output[0] = x + m;
      output[1] = m;
      output[2] = chroma + m;
    } else {
      output[0] = chroma + m;
      output[1] = m;
      output[2] = x + m;
    }
  }
}
