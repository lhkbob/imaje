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
package com.lhkbob.imaje.color.transform;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.util.Arguments;

/**
 *
 */
public class IndirectXYZTransformFactory<I extends Color, O extends Color> implements TransformFactory<I, O> {
  private final TransformFactory<XYZ, O> fromXYZ;
  private final TransformFactory<I, XYZ> toXYZ;

  public IndirectXYZTransformFactory(
      TransformFactory<I, XYZ> toXYZ, TransformFactory<XYZ, O> fromXYZ) {
    Arguments.notNull("toXYZ", toXYZ);
    Arguments.notNull("fromXYZ", fromXYZ);

    this.toXYZ = toXYZ;
    this.fromXYZ = fromXYZ;
  }

  @Override
  public Class<I> getInputType() {
    return toXYZ.getInputType();
  }

  @Override
  public Class<O> getOutputType() {
    return fromXYZ.getOutputType();
  }

  @Override
  public ColorTransform<O, I> newInverseTransform() {
    return new IndirectXYZTransform<>(fromXYZ.newInverseTransform(), toXYZ.newInverseTransform());
  }

  @Override
  public ColorTransform<I, O> newTransform() {
    return new IndirectXYZTransform<>(toXYZ.newTransform(), fromXYZ.newTransform());
  }

  private static class IndirectXYZTransform<I extends Color, O extends Color> implements ColorTransform<I, O> {
    private final ColorTransform<XYZ, O> fromXYZ;
    private final XYZ temp;
    private final ColorTransform<I, XYZ> toXYZ;

    public IndirectXYZTransform(ColorTransform<I, XYZ> toXYZ, ColorTransform<XYZ, O> fromXYZ) {
      this.toXYZ = toXYZ;
      this.fromXYZ = fromXYZ;
      temp = new XYZ();
    }

    @Override
    public boolean apply(I input, O output) {
      boolean result = toXYZ.apply(input, temp);
      result &= fromXYZ.apply(temp, output);
      return result;
    }

    @Override
    public O apply(I input) {
      boolean valid = toXYZ.apply(input, temp);
      if (!valid)
        return null;

      return fromXYZ.apply(temp);
    }
  }
}
