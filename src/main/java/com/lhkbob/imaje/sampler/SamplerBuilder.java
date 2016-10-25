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

import com.lhkbob.imaje.Image;
import com.lhkbob.imaje.color.Color;

/**
 *
 */
public abstract class SamplerBuilder<T extends Color, S extends Sampler<T>> {
  protected boolean filterLinear;
  protected BoundaryStrategy<T> uStrategy;
  protected BoundaryStrategy<T> vStrategy;
  protected BoundaryStrategy<T> wStrategy;

  protected int layer;
  protected int mipmapBase;
  protected int mipmapMax;

  public SamplerBuilder() {
    // Initialize layer to be 0 (e.g. first image)
    // and the mipmap base to 0 (e.g. highest resolution)
    // but set the max mipmap to negative so that it is calculated dynamically based on image
    layer = 0;
    mipmapBase = 0;
    mipmapMax = -1;

    // Nearest filtering with no assigned boundary strategy
    filterLinear = false;
    uStrategy = null;
    vStrategy = null;
    wStrategy = null;
  }

  public SamplerBuilder<T, S> filterNearest() {
    filterLinear = false;
    return this;
  }

  public SamplerBuilder<T, S> filterLinear() {
    filterLinear = true;
    return this;
  }

  public SamplerBuilder<T, S> clampToBorder(T color, double alpha) {
    return boundary(Samplers.clampToBorder(color, alpha));
  }

  public SamplerBuilder<T, S> clampToEdge() {
    return boundary(Samplers.clampToEdge());
  }

  public SamplerBuilder<T, S> mirror() {
    return boundary(Samplers.mirror());
  }

  public SamplerBuilder<T, S> repeat() {
    return boundary(Samplers.repeat());
  }

  public SamplerBuilder<T, S> boundary(BoundaryStrategy<T> strategy) {
    return boundaryU(strategy).boundaryV(strategy).boundaryW(strategy);
  }

  public SamplerBuilder<T, S> clampToBorderU(T color, double alpha) {
    return boundaryU(Samplers.clampToBorder(color, alpha));
  }

  public SamplerBuilder<T, S> clampToBorderV(T color, double alpha) {
    return boundaryV(Samplers.clampToBorder(color, alpha));
  }

  public SamplerBuilder<T, S> clampToBorderW(T color, double alpha) {
    return boundaryW(Samplers.clampToBorder(color, alpha));
  }

  public SamplerBuilder<T, S> clampToEdgeU() {
    return boundaryU(Samplers.clampToEdge());
  }

  public SamplerBuilder<T, S> clampToEdgeV() {
    return boundaryV(Samplers.clampToEdge());
  }

  public SamplerBuilder<T, S> clampToEdgeW() {
    return boundaryW(Samplers.clampToEdge());
  }

  public SamplerBuilder<T, S> mirrorU() {
    return boundaryU(Samplers.mirror());
  }

  public SamplerBuilder<T, S> mirrorV() {
    return boundaryV(Samplers.mirror());
  }

  public SamplerBuilder<T, S> mirrorW() {
    return boundaryW(Samplers.mirror());
  }

  public SamplerBuilder<T, S> repeatU() {
    return boundaryU(Samplers.repeat());
  }

  public SamplerBuilder<T, S> repeatV() {
    return boundaryV(Samplers.repeat());
  }

  public SamplerBuilder<T, S> repeatW() {
    return boundaryW(Samplers.repeat());
  }

  public SamplerBuilder<T, S> boundaryU(BoundaryStrategy<T> strategy) {
    uStrategy = strategy;
    return this;
  }

  public SamplerBuilder<T, S> boundaryV(BoundaryStrategy<T> strategy) {
    vStrategy = strategy;
    return this;
  }

  public SamplerBuilder<T, S> boundaryW(BoundaryStrategy<T> strategy) {
    wStrategy = strategy;
    return this;
  }

  public SamplerBuilder<T, S> layer(int index) {
    layer = index;
    return this;
  }

  public SamplerBuilder<T, S> mipmapBase(int mipmap) {
    mipmapBase = mipmap;
    return this;
  }

  public SamplerBuilder<T, S> mipmapMax(int mipmap) {
    mipmapMax = mipmap;
    return this;
  }

  public abstract S build(Image<T> image);
}
