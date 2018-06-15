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

import com.lhkbob.imaje.color.space.GenericVectorSpace;

/**
 *
 */
public class Normal extends Vector<Normal, GenericVectorSpace<Normal>> {
  public static final GenericVectorSpace<Normal> VECTOR_SPACE = new GenericVectorSpace<>(3, Normal::new);

  public Normal() {
    this(0, 0, 0);
  }

  public Normal(double x, double y, double z) {
    this(VECTOR_SPACE);
    set(x, y, z);
  }

  private Normal(GenericVectorSpace<Normal> space) {
    super(space, 3);
  }

  @Override
  public Normal clone() {
    return (Normal) super.clone();
  }

  public double getX() {
    return get(0);
  }

  public double getY() {
    return get(1);
  }

  public double getZ() {
    return get(2);
  }

  public void setX(double x) {
    set(0, x);
  }

  public void setY(double y) {
    set(1, y);
  }

  public void setZ(double z) {
    set(2, z);
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

  public void z(double z) {
    setZ(z);
  }
}
