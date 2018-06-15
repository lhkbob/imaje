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

import com.lhkbob.imaje.Raster;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.Vectors;
import com.lhkbob.imaje.util.Arguments;

/**
 *
 */
public class BilinearSampler2D<T extends Color> implements Sampler2D<T> {
  private final Raster<T> image;
  private final BoundaryStrategy<T> uStrategy;
  private final BoundaryStrategy<T> vStrategy;

  private final T sample;

  public BilinearSampler2D(
      Raster<T> image, BoundaryStrategy<T> uStrategy, BoundaryStrategy<T> vStrategy) {
    Arguments.notNull("image", image);
    Arguments.notNull("uStrategy", uStrategy);
    Arguments.notNull("vStrategy", vStrategy);

    this.image = image;
    this.uStrategy = uStrategy;
    this.vStrategy = vStrategy;

    sample = Color.newInstance(image.getColorType());
  }

  @Override
  public Raster<T> getImage() {
    return image;
  }

  private double sample(int i, int j) {
    if (uStrategy.useBorder(i, image.getWidth())) {
      sample.set(uStrategy.getBorderColor().getChannels());
      return uStrategy.getBorderAlpha();
    } else if (vStrategy.useBorder(j, image.getHeight())) {
      sample.set(vStrategy.getBorderColor().getChannels());
      return vStrategy.getBorderAlpha();
    } else {
      int x = uStrategy.wrap(i, image.getWidth());
      int y = vStrategy.wrap(j, image.getHeight());
      return image.get(x, y, sample);
    }
  }

  @Override
  public double sample(double u, double v, double lod, T result) {
    // Ignore LOD value
    int i0 = Samplers.sampleToTexel(u, image.getWidth());
    int i1 = i0 + 1;
    int j0 = Samplers.sampleToTexel(v, image.getHeight());
    int j1 = j0 + 1;

    double alpha = Samplers.sampleWeight(u, image.getWidth());
    double beta = Samplers.sampleWeight(v, image.getHeight());

    // Collect the four corners (u0, v0), (u1, v0), (u0, v1), (u1, v1) and weighted
    // add them into result.
    double weight00 = (1.0 - alpha) * (1.0 - beta);
    double blendedAlpha = weight00 * sample(i0, j0);
    Vectors.scale(sample, weight00, result);

    double weight10 = alpha * (1.0 - beta);
    blendedAlpha += weight10 * sample(i1, j0);
    Vectors.addScaled(result, sample, weight10, result);

    double weight01 = (1.0 - alpha) * beta;
    blendedAlpha += weight01 * sample(i0, j1);
    Vectors.addScaled(result, sample, weight01, result);

    double weight11 = alpha * beta;
    blendedAlpha += weight11 * sample(i1, j1);
    Vectors.addScaled(result, sample, weight11, result);

    return blendedAlpha;
  }
}
