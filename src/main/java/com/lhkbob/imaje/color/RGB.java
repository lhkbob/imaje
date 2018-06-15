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

/**
 *
 */
public class RGB<S extends ColorSpace<RGB<S>, S>> extends Color<RGB<S>, S> {
  public RGB(S space) {
    super(space, 3);
  }

  public RGB(S space, double r, double g, double b) {
    this(space);
    setRed(r);
    setGreen(g);
    setBlue(b);
  }

  public double b() {
    return getBlue();
  }

  public void b(double b) {
    setBlue(b);
  }

  @Override
  public RGB<S> clone() {
    return (RGB<S>) super.clone();
  }

  public double g() {
    return getGreen();
  }

  public void g(double g) {
    setGreen(g);
  }

  public double getBlue() {
    return get(2);
  }

  public double getGreen() {
    return get(1);
  }

  public double getRed() {
    return get(0);
  }

  public double r() {
    return getRed();
  }

  public void r(double r) {
    setRed(r);
  }

  public void setBlue(double b) {
    set(2, b);
  }

  public void setGreen(double g) {
    set(1, g);
  }

  public void setRed(double r) {
    set(0, r);
  }
}
