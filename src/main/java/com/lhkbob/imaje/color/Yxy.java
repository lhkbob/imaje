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

import com.lhkbob.imaje.color.space.xyz.CIE31;
import com.lhkbob.imaje.color.space.xyz.YxySpace;

/**
 *
 */

public class Yxy<S extends ColorSpace<XYZ<S>, S>> extends Color<Yxy<S>, YxySpace<S>> {
  // FIXME defaults should be 0s to be consistent with other vector spaces, etc.
  public Yxy(YxySpace<S> space) {
    this(space, 0.33333, 0.33333);
  }

  public Yxy(YxySpace<S> space, double x, double y) {
    this(space, 1.0, x, y);
  }

  public Yxy(YxySpace<S> space, double luminance, double x, double y) {
    super(space, 3);
    setLuminance(luminance);
    setX(x);
    setY(y);
  }

  public static Yxy<CIE31> newCIE31() {
    return new Yxy<>(YxySpace.SPACE_CIE31);
  }

  public static Yxy<CIE31> newCIE31(double x, double y) {
    return new Yxy<>(YxySpace.SPACE_CIE31, x, y);
  }

  public static Yxy<CIE31> newCIE31(double luminance, double x, double y) {
    return new Yxy<>(YxySpace.SPACE_CIE31, luminance, x, y);
  }

  public static <S extends ColorSpace<XYZ<S>, S>> Yxy<S> newYxy(S xyzSpace) {
    return new Yxy<>(new YxySpace<>(xyzSpace));
  }

  public static <S extends ColorSpace<XYZ<S>, S>> Yxy<S> newYxy(S xyzSpace, double x, double y) {
    return new Yxy<>(new YxySpace<>(xyzSpace), x, y);
  }

  public static <S extends ColorSpace<XYZ<S>, S>> Yxy<S> newYxy(
      S xyzSpace, double luminance, double x, double y) {
    return new Yxy<>(new YxySpace<>(xyzSpace), luminance, x, y);
  }

  @Override
  public Yxy<S> clone() {
    return (Yxy<S>) super.clone();
  }

  public double getLuminance() {
    return get(0);
  }

  public double getX() {
    return get(1);
  }

  public double getY() {
    return get(2);
  }

  public double getZ() {
    return 1.0 - x() - y();
  }

  public double lum() {
    return getLuminance();
  }

  public void lum(double l) {
    setLuminance(l);
  }

  public void setLuminance(double luminance) {
    set(0, luminance);
  }

  public void setX(double x) {
    set(1, x);
  }

  public void setY(double y) {
    set(2, y);
  }

  public double x() {
    return getX();
  }

  public void x(double x) {
    setX(x);
  }

  public double y() {
    return getY();
  }

  public void y(double y) {
    setY(y);
  }

  public double z() {
    return getZ();
  }
}
