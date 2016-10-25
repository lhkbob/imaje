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
@Channels(value = { "Cyan", "Magenta", "Yellow", "Key" }, shortNames = { "C", "M", "Y", "K"})
public class CMYK extends Color {

  public CMYK() {
    this(0.0, 0.0, 0.0, 1.0);
  }

  public CMYK(double c, double m, double y, double k) {
    set(c, m, y, k);
  }

  public double c() {
    return getCyan();
  }

  public void c(double c) {
    setCyan(c);
  }

  @Override
  public CMYK clone() {
    return (CMYK) super.clone();
  }

  public double getCyan() {
    return get(0);
  }

  public double getKey() {
    return get(3);
  }

  public double getMagenta() {
    return get(1);
  }

  public double getYellow() {
    return get(2);
  }

  public double k() {
    return getKey();
  }

  public void k(double k) {
    setKey(k);
  }

  public double m() {
    return getMagenta();
  }

  public void m(double m) {
    setMagenta(m);
  }

  public void setCyan(double cyan) {
    set(0, cyan);
  }

  public void setKey(double key) {
    set(3, key);
  }

  public void setMagenta(double magenta) {
    set(1, magenta);
  }

  public void setYellow(double yellow) {
    set(2, yellow);
  }

  public double y() {
    return getYellow();
  }

  public void y(double y) {
    setYellow(y);
  }
}
