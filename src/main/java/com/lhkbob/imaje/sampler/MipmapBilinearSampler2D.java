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
package com.lhkbob.imaje.sampler;

import com.lhkbob.imaje.Mipmap;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.op.ColorOps;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.Images;

/**
 *
 */
public class MipmapBilinearSampler2D<T extends Color> implements Sampler2D<T> {
  private final Mipmap<T> image;
  private final BoundaryStrategy<T> uStrategy;
  private final BoundaryStrategy<T> vStrategy;

  private final int levelBase;
  private final int levelMax;

  private final T sample;

  public MipmapBilinearSampler2D(
      Mipmap<T> image, BoundaryStrategy<T> uStrategy, BoundaryStrategy<T> vStrategy, int levelBase,
      int levelMax) {
    Arguments.notNull("image", image);
    Arguments.notNull("uStrategy", uStrategy);
    Arguments.notNull("vStrategy", vStrategy);
    Arguments
        .checkArrayRange("mipmap levels", image.getMipmapCount(), levelBase, levelMax - levelBase);

    this.image = image;
    this.uStrategy = uStrategy;
    this.vStrategy = vStrategy;

    this.levelBase = levelBase;
    this.levelMax = levelMax;

    sample = Color.newInstance(image.getColorType());
  }

  @Override
  public Mipmap<T> getImage() {
    return image;
  }

  private double sample(int i, int j, int mipmap, int w, int h) {
    if (uStrategy.useBorder(i, w)) {
      sample.set(uStrategy.getBorderColor().getChannels());
      return uStrategy.getBorderAlpha();
    } else if (vStrategy.useBorder(j, h)) {
      sample.set(vStrategy.getBorderColor().getChannels());
      return vStrategy.getBorderAlpha();
    } else {
      int x = uStrategy.wrap(i, w);
      int y = vStrategy.wrap(j, h);
      return image.get(mipmap, x, y, sample);
    }
  }

  @Override
  public double sample(double u, double v, double lod, T result) {
    // First zero out the result color since sampleLevel() accumulates into the result
    ColorOps.zero(result);

    // Select the two mipmap levels that are to be sampled
    int d1 = Samplers.lodToMipmapLow(lod, levelBase, levelMax);
    int d2 = Samplers.lodToMipmapHigh(lod, levelBase, levelMax);

    // If sampling the same level, short cut and do one level of lookups
    if (d1 == d2) {
      return sampleLevel(u, v, d1, 1.0, result);
    }

    // Linearly blend the bilinearly filtered pixel values from d1 and d2 (8 pixel lookups)
    double weight = Samplers.lodWeight(lod, levelBase, levelMax);
    double blendedAlpha = sampleLevel(u, v, d1, 1.0 - weight, result);
    blendedAlpha += sampleLevel(u, v, d2, weight, result);
    return blendedAlpha;
  }

  private double sampleLevel(double u, double v, int mipmap, double weight, T result) {
    // Get dimensions for specific mipmap
    int w = Images.getMipmapDimension(image.getWidth(), mipmap);
    int h = Images.getMipmapDimension(image.getHeight(), mipmap);

    // Get sample coordinates for 4 corners being sampled
    int i0 = Samplers.sampleToTexel(u, w);
    int i1 = i0 + 1;
    int j0 = Samplers.sampleToTexel(v, h);
    int j1 = j0 + 1;

    double alpha = Samplers.sampleWeight(u, w);
    double beta = Samplers.sampleWeight(v, h);

    // Collect the four corners (u0, v0), (u1, v0), (u0, v1), (u1, v1) and weighted
    // add them into result.
    double weight00 = weight * (1.0 - alpha) * (1.0 - beta);
    double blendedAlpha = weight00 * sample(i0, j0, mipmap, w, h);
    ColorOps.addScaled(result, sample, weight00, result);

    double weight10 = weight * alpha * (1.0 - beta);
    blendedAlpha += weight10 * sample(i1, j0, mipmap, w, h);
    ColorOps.addScaled(result, sample, weight10, result);

    double weight01 = weight * (1.0 - alpha) * beta;
    blendedAlpha += weight01 * sample(i0, j1, mipmap, w, h);
    ColorOps.addScaled(result, sample, weight01, result);

    double weight11 = weight * alpha * beta;
    blendedAlpha += weight11 * sample(i1, j1, mipmap, w, h);
    ColorOps.addScaled(result, sample, weight11, result);

    return blendedAlpha;
  }
}
