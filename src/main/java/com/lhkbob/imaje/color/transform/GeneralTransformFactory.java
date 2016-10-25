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
import com.lhkbob.imaje.color.transform.general.Transform;
import com.lhkbob.imaje.util.Arguments;

/**
 *
 */
public class GeneralTransformFactory<I extends Color, O extends Color> implements TransformFactory<I, O> {
  private final Transform inToOut;
  private final Class<I> inType;
  private final Transform outToIn;
  private final Class<O> outType;

  public GeneralTransformFactory(Class<I> in, Class<O> out, Transform inToOut, Transform outToIn) {
    Arguments.notNull("in", in);
    Arguments.notNull("out", out);

    Arguments.equals("output channels", inToOut.getOutputChannels(),outToIn.getInputChannels());
    Arguments.equals("input channels", inToOut.getInputChannels(), outToIn.getOutputChannels());
    Arguments.equals("input compatibility", Color.getChannelCount(in), inToOut.getInputChannels());
    Arguments.equals("output compatibility", Color.getChannelCount(out), inToOut.getOutputChannels());

    inType = in;
    outType = out;
    this.inToOut = inToOut;
    this.outToIn = outToIn;
  }

  @Override
  public Class<I> getInputType() {
    return inType;
  }

  @Override
  public Class<O> getOutputType() {
    return outType;
  }

  @Override
  public ColorTransform<O, I> newInverseTransform() {
    return new ColorTransformAdapter<>(outToIn.getLocallySafeInstance(), Color.newInstance(inType));
  }

  @Override
  public ColorTransform<I, O> newTransform() {
    return new ColorTransformAdapter<>(inToOut.getLocallySafeInstance(), Color.newInstance(outType));
  }

  private static class ColorTransformAdapter<I extends Color, O extends Color> implements ColorTransform<I, O> {
    private final O outputCache;

    private final Transform transform;

    public ColorTransformAdapter(Transform transform, O outputCache) {
      this.transform = transform;
      this.outputCache = outputCache;
    }

    @Override
    public boolean apply(I input, O output) {
      if (output != input) {
        // We can write to the output's channel array directly
        transform.transform(input.getChannels(), output.getChannels());
      } else if (output != outputCache) {
        // We can use the cache's channel array to compute the value and then copy it to the
        // actual color instance
        transform.transform(input.getChannels(), outputCache.getChannels());
        output.set(outputCache.getChannels());
      } else {
        // The input and output instances are both outputCache, which is a very rare circumstance,
        // so just allocate a temporary array
        double[] temp = new double[transform.getOutputChannels()];
        transform.transform(input.getChannels(), temp);
        output.set(temp);
      }

      return true;
    }

    @Override
    public O apply(I input) {
      boolean result = apply(input, outputCache);
      return (result ? outputCache : null);
    }
  }
}
