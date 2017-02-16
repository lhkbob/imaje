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
package com.lhkbob.imaje.color.space.rgb;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Gamma
 * =====
 *
 * A type annotation that is processed by subclasses of {@link AnnotationRGBSpace} to define the
 * decoding gamma curve for the RGB space. The curve is modeled by a {@link
 * com.lhkbob.imaje.color.transform.curves.UnitGammaFunction}, which has seven parameters: `a`, `b`,
 * `c`, `d`, `e`, `f`, and `gamma`. This function is defined as:
 *
 *    x in [0, 1], y(x) =
 *       x >= d: (ax + b)^gamma + c
 *       x < d: ex + f
 *
 * @author Michael Ludwig
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Gamma {
  /**
   * @return The multiplier of `x` within the power function's base
   */
  double a() default 1.0;

  /**
   * @return The offset to `x` within the power function's base
   */
  double b() default 0.0;

  /**
   * @return The offset to the power function
   */
  double c() default 0.0;

  /**
   * @return The threshold between 0 and 1 to switch from a linear to a power function
   */
  double d() default 0.0;

  /**
   * @return The slope of the linear function
   */
  double e() default 1.0;

  /**
   * @return The offset of the linear function
   */
  double f() default 0.0;

  /**
   * @return The exponent of the power function
   */
  double gamma();
}
