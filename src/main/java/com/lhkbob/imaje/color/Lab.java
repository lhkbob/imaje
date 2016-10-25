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

import com.lhkbob.imaje.color.annot.Channels;

/**
 *
 */
@Channels({ "L", "a", "b" })
public abstract class Lab extends Color {
  public static class CIE extends Lab {
    public CIE() {

    }

    public CIE(double l, double a, double b) {
      set(l, a, b);
    }

    @Override
    public CIE clone() {
      return (CIE) super.clone();
    }
  }

  public static class Hunter extends Lab {
    public Hunter() {

    }

    public Hunter(double l, double a, double b) {
      set(l, a, b);
    }

    @Override
    public Hunter clone() {
      return (Hunter) super.clone();
    }
  }

  public double a() {
    return getA();
  }

  public void a(double a) {
    setA(a);
  }

  public double b() {
    return getB();
  }

  public void b(double b) {
    setB(b);
  }

  @Override
  public Lab clone() {
    return (Lab) super.clone();
  }

  public double getA() {
    return get(1);
  }

  public double getB() {
    return get(2);
  }

  public double getL() {
    return get(0);
  }

  public double l() {
    return getL();
  }

  public void l(double l) {
    setL(l);
  }

  public void setA(double a) {
    set(1, a);
  }

  public void setB(double b) {
    set(2, b);
  }

  public void setL(double l) {
    set(0, l);
  }
}
