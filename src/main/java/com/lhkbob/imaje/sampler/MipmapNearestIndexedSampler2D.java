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

import com.lhkbob.imaje.MipmapArray;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.Images;

/**
 *
 */
public class MipmapNearestIndexedSampler2D<T extends Color> implements IndexedSampler2D<T> {
  private final MipmapArray<T> image;
  private final BoundaryStrategy<T> uStrategy;
  private final BoundaryStrategy<T> vStrategy;

  private final int levelBase;
  private final int levelMax;

  public MipmapNearestIndexedSampler2D(
      MipmapArray<T> image, BoundaryStrategy<T> uStrategy, BoundaryStrategy<T> vStrategy,
      int levelBase, int levelMax) {
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
  }

  @Override
  public MipmapArray<T> getImage() {
    return image;
  }

  @Override
  public double sample(double u, double v, int index, double lod, T result) {
    Arguments.checkIndex("index", image.getLayerCount(), index);
    int mipmap = Samplers.lodToMipmap(lod, levelBase, levelMax);

    // Get dimensions for the selected mipmap
    int w = Images.getMipmapDimension(image.getWidth(), mipmap);
    int h = Images.getMipmapDimension(image.getHeight(), mipmap);

    int i = Samplers.coordToTexel(u, w);
    int j = Samplers.coordToTexel(v, h);

    if (uStrategy.useBorder(i, w)) {
      result.set(uStrategy.getBorderColor().getChannels());
      return uStrategy.getBorderAlpha();
    } else if (vStrategy.useBorder(j, h)) {
      result.set(vStrategy.getBorderColor().getChannels());
      return vStrategy.getBorderAlpha();
    } else {
      // Ensure u and v are within the image bounds
      int x = uStrategy.wrap(i, w);
      int y = vStrategy.wrap(j, h);

      return image.get(index, mipmap, x, y, result);
    }
  }
}
