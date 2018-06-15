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
package com.lhkbob.imaje.color;

import com.lhkbob.imaje.color.space.yuv.YUVSpace;

/**
 *
 */
public class YUV<S extends ColorSpace<RGB<S>, S>> extends Color<YUV<S>, YUVSpace<S>> {
  public YUV(YUVSpace<S> space) {
    super(space, 3);
  }

  public YUV(YUVSpace<S> space, double y, double u, double v) {
    this(space);
    setY(y);
    setU(u);
    setV(v);
  }

  @Override
  public YUV<S> clone() {
    return (YUV<S>) super.clone();
  }

  public double getU() {
    return get(1);
  }

  public double getV() {
    return get(2);
  }

  public double getY() {
    return get(0);
  }

  public void setU(double u) {
    set(1, u);
  }

  public void setV(double v) {
    set(2, v);
  }

  public void setY(double y) {
    set(0, y);
  }

  public double u() {
    return getU();
  }

  public void u(double u) {
    setU(u);
  }

  public double v() {
    return getV();
  }

  public void v(double v) {
    setV(v);
  }

  public double y() {
    return getY();
  }

  public void y(double y) {
    setY(y);
  }
}
