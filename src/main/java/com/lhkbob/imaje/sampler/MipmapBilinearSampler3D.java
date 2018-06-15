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

import com.lhkbob.imaje.MipmapVolume;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.Vectors;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.Images;

/**
 *
 */
public class MipmapBilinearSampler3D<T extends Color> implements Sampler3D<T> {
  private final MipmapVolume<T> image;
  private final BoundaryStrategy<T> uStrategy;
  private final BoundaryStrategy<T> vStrategy;
  private final BoundaryStrategy<T> wStrategy;

  private final int levelBase;
  private final int levelMax;

  private final T sample;

  public MipmapBilinearSampler3D(
      MipmapVolume<T> image, BoundaryStrategy<T> uStrategy, BoundaryStrategy<T> vStrategy,
      BoundaryStrategy<T> wStrategy, int levelBase, int levelMax) {
    Arguments.notNull("image", image);
    Arguments.notNull("uStrategy", uStrategy);
    Arguments.notNull("vStrategy", vStrategy);
    Arguments.notNull("wStrategy", wStrategy);

    Arguments.checkArrayRange("mipmap levels", image.getMipmapCount(), levelBase, levelMax - levelBase);

    this.image = image;
    this.uStrategy = uStrategy;
    this.vStrategy = vStrategy;
    this.wStrategy = wStrategy;

    this.levelBase = levelBase;
    this.levelMax = levelMax;

    sample = Color.newInstance(image.getColorType());
  }

  @Override
  public MipmapVolume<T> getImage() {
    return image;
  }

  @Override
  public double sample(double u, double v, double w, double lod, T result) {
    // First zero out the result color since sampleLevel() accumulates into the result
    Vectors.zero(result);

    // Select the two mipmap levels that are to be sampled
    int d1 = Samplers.lodToMipmapLow(lod, levelBase, levelMax);
    int d2 = Samplers.lodToMipmapHigh(lod, levelBase, levelMax);

    // If sampling the same level, short cut and do one level of lookups
    if (d1 == d2) {
      return sampleLevel(u, v, w, d1, 1.0, result);
    }

    // Linearly blend the bilinearly filtered pixel values from d1 and d2 (8 pixel lookups)
    double weight = Samplers.lodWeight(lod, levelBase, levelMax);
    double blendedAlpha = sampleLevel(u, v, w, d1, 1.0 - weight, result);
    blendedAlpha += sampleLevel(u, v, w, d2, weight, result);
    return blendedAlpha;
  }

  private double sample(int i, int j, int k, int mipmap, int width, int height, int depth) {
    if (uStrategy.useBorder(i, width)) {
      sample.set(uStrategy.getBorderColor().getChannels());
      return uStrategy.getBorderAlpha();
    } else if (vStrategy.useBorder(j, height)) {
      sample.set(vStrategy.getBorderColor().getChannels());
      return vStrategy.getBorderAlpha();
    } else if (wStrategy.useBorder(k, depth)) {
      sample.set(wStrategy.getBorderColor().getChannels());
      return wStrategy.getBorderAlpha();
    } else {
      int x = uStrategy.wrap(i, width);
      int y = vStrategy.wrap(j, height);
      int z = wStrategy.wrap(k, depth);
      return image.get(mipmap, x, y, z, sample);
    }
  }

  private double sampleLevel(double u, double v, double w, int mipmap, double weight, T result) {
    // Get the dimensions for the current mipmap level
    int width = Images.getMipmapDimension(image.getWidth(), mipmap);
    int height = Images.getMipmapDimension(image.getHeight(), mipmap);
    int depth = Images.getMipmapDimension(image.getDepth(), mipmap);

    // Lookup coordinates of 8 corners that are to be sampled
    int i0 = Samplers.sampleToTexel(u, width);
    int i1 = i0 + 1;
    int j0 = Samplers.sampleToTexel(v, height);
    int j1 = j0 + 1;
    int k0 = Samplers.sampleToTexel(w, depth);
    int k1 = k0 + 1;

    double alpha = Samplers.sampleWeight(u, width);
    double beta = Samplers.sampleWeight(v, height);
    double gamma = Samplers.sampleWeight(w, depth);

    // Collect the eight corners (u0, v0, w0), (u1, v0, w0), (u0, v1, w0), (u1, v1, w0),
    // (u0, v0, w1), (u1, v0, w1), (u0, v1, w1), (u1, v1, w1) and weighte add them into result.
    double weight000 = weight * (1.0 - alpha) * (1.0 - beta) * (1.0 - gamma);
    double blendedAlpha = weight000 * sample(i0, j0, k0, mipmap, width, height, depth);
    Vectors.scale(sample, weight000, result);

    double weight100 = weight * alpha * (1.0 - beta) * (1.0 - gamma);
    blendedAlpha += weight100 * sample(i1, j0, k0, mipmap, width, height, depth);
    Vectors.addScaled(result, sample, weight100, result);

    double weight010 = weight * (1.0 - alpha) * beta * (1.0 - gamma);
    blendedAlpha += weight010 * sample(i0, j1, k0, mipmap, width, height, depth);
    Vectors.addScaled(result, sample, weight010, result);

    double weight110 = weight * alpha * beta * (1.0 - gamma);
    blendedAlpha += weight110 * sample(i1, j1, k0, mipmap, width, height, depth);
    Vectors.addScaled(result, sample, weight110, result);

    double weight001 = weight * (1.0 - alpha) * (1.0 - beta) * gamma;
    blendedAlpha += weight001 * sample(i0, j0, k1, mipmap, width, height, depth);
    Vectors.scale(sample, weight001, result);

    double weight101 = weight * alpha * (1.0 - beta) * gamma;
    blendedAlpha += weight101 * sample(i1, j0, k1, mipmap, width, height, depth);
    Vectors.addScaled(result, sample, weight101, result);

    double weight011 = weight * (1.0 - alpha) * beta * gamma;
    blendedAlpha += weight011 * sample(i0, j1, k1, mipmap, width, height, depth);
    Vectors.addScaled(result, sample, weight011, result);

    double weight111 = weight * alpha * beta * gamma;
    blendedAlpha += weight111 * sample(i1, j1, k1, mipmap, width, height, depth);
    Vectors.addScaled(result, sample, weight111, result);

    return blendedAlpha;
  }
}
