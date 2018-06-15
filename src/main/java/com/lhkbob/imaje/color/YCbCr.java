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

import com.lhkbob.imaje.color.space.yuv.YCbCrSpace;

/**
 *
 */
public class YCbCr<S extends ColorSpace<RGB<S>, S>> extends Color<YCbCr<S>, YCbCrSpace<S>> {
  public YCbCr(YCbCrSpace<S> space) {
    super(space, 3);
  }

  public YCbCr(YCbCrSpace<S> space, double y, double cb, double cr) {
    this(space);
    setY(y);
    setCb(cb);
    setCr(cr);
  }

  @Override
  public YCbCr<S> clone() {
    return (YCbCr<S>) super.clone();
  }

  public double cb() {
    return getCb();
  }

  public void cb(double cb) {
    setCb(cb);
  }

  public double cr() {
    return getCr();
  }

  public void cr(double cr) {
    setCr(cr);
  }

  public double getCb() {
    return get(1);
  }

  public double getCr() {
    return get(2);
  }

  public double getY() {
    return get(0);
  }

  public void setCb(double cb) {
    set(1, cb);
  }

  public void setCr(double cr) {
    set(2, cr);
  }

  public void setY(double y) {
    set(0, y);
  }

  public double y() {
    return getY();
  }

  public void y(double y) {
    setY(y);
  }
}
