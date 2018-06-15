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
public class Generic extends Vector<Generic, GenericVectorSpace<Generic>> {
  public static final GenericVectorSpace<Generic> VECTOR1_SPACE = newVectorSpace(1);
  public static final GenericVectorSpace<Generic> VECTOR2_SPACE = newVectorSpace(2);
  public static final GenericVectorSpace<Generic> VECTOR3_SPACE = newVectorSpace(3);
  public static final GenericVectorSpace<Generic> VECTOR4_SPACE = newVectorSpace(4);

  private Generic(GenericVectorSpace<Generic> space) {
    super(space, space.getChannelCount());
  }

  public static GenericVectorSpace<Generic> newVectorSpace(int dimensionality) {
    return new GenericVectorSpace<>(dimensionality, Generic::new);
  }

  public static Generic newVector1() {
    return VECTOR1_SPACE.newValue();
  }

  public static Generic newVector2() {
    return VECTOR2_SPACE.newValue();
  }

  public static Generic newVector3() {
    return VECTOR3_SPACE.newValue();
  }

  public static Generic newVector4() {
    return VECTOR4_SPACE.newValue();
  }
}
