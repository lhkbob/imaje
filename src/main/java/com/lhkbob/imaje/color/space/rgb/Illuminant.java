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

import com.lhkbob.imaje.color.transform.Illuminants;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Illuminant
 * ==========
 *
 * A type annotation used with subclasses of {@link AnnotationRGBSpace} to specify the illuminant
 * and whitepoint of the RGB color space. This annotation can use a predefined [standard
 * illuminant](https://en.wikipedia.org/wiki/Standard_illuminant), color temperature, or explicit
 * chromaticity coordinates. These methods determine the chromaticity coordinates of the illuminant,
 * which is them combined with {@link #luminance()} to determine the final whitepoint.
 *
 * @author Michael Ludwig
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Illuminant {
  /**
   * Type
   * ====
   *
   * The Illuminant type, which is either a standard illuminant, or the special values `TEMPERATURE`
   * or `CHROMATICITY`. See each entry for specifics.
   *
   * @author Michael Ludwig
   */
  enum Type {
    /**
     * Corresponds to {@link Illuminants#newA(double)}, where {@link #luminance()} is the input
     * value provided.
     */
    A,
    /**
     * Corresponds to {@link Illuminants#newB(double)}, where {@link #luminance()} is the input
     * value provided.
     */
    B,
    /**
     * Corresponds to {@link Illuminants#newC(double)}, where {@link #luminance()} is the input
     * value provided.
     */
    C,
    /**
     * Corresponds to {@link Illuminants#newD50(double)}, where {@link #luminance()} is the input
     * value provided.
     */
    D50,
    /**
     * Corresponds to {@link Illuminants#newD55(double)}, where {@link #luminance()} is the input
     * value provided.
     */
    D55,
    /**
     * Corresponds to {@link Illuminants#newD65(double)}, where {@link #luminance()} is the input
     * value provided.
     */
    D65,
    /**
     * Corresponds to {@link Illuminants#newD75(double)}, where {@link #luminance()} is the input
     * value provided.
     */
    D75,
    /**
     * Corresponds to {@link Illuminants#newE(double)}, where {@link #luminance()} is the input
     * value provided.
     */
    E,
    /**
     * Corresponds to {@link Illuminants#newF1(double)}, where {@link #luminance()} is the input
     * value provided.
     */
    F1,
    /**
     * Corresponds to {@link Illuminants#newF2(double)}, where {@link #luminance()} is the input
     * value provided.
     */
    F2,
    /**
     * Corresponds to {@link Illuminants#newF3(double)}, where {@link #luminance()} is the input
     * value provided.
     */
    F3,
    /**
     * Corresponds to {@link Illuminants#newF4(double)}, where {@link #luminance()} is the input
     * value provided.
     */
    F4,
    /**
     * Corresponds to {@link Illuminants#newF5(double)}, where {@link #luminance()} is the input
     * value provided.
     */
    F5,
    /**
     * Corresponds to {@link Illuminants#newF6(double)}, where {@link #luminance()} is the input
     * value provided.
     */
    F6,
    /**
     * Corresponds to {@link Illuminants#newF7(double)}, where {@link #luminance()} is the input
     * value provided.
     */
    F7,
    /**
     * Corresponds to {@link Illuminants#newF8(double)}, where {@link #luminance()} is the input
     * value provided.
     */
    F8,
    /**
     * Corresponds to {@link Illuminants#newF9(double)}, where {@link #luminance()} is the input
     * value provided.
     */
    F9,
    /**
     * Corresponds to {@link Illuminants#newF10(double)}, where {@link #luminance()} is the input
     * value provided.
     */
    F10,
    /**
     * Corresponds to {@link Illuminants#newF11(double)}, where {@link #luminance()} is the input
     * value provided.
     */
    F11,
    /**
     * Corresponds to {@link Illuminants#newF12(double)}, where {@link #luminance()} is the input
     * value provided.
     */
    F12,
    /**
     * Corresponds to {@link Illuminants#newCorrelatedColorTemperature(double, double)} where
     * {@link #temperature()} specifies the CCT value and {@link #luminance()} is the luminance.
     */
    TEMPERATURE,
    /**
     * Uses the `x` and `y` chromaticity coordinates of {@link #chromaticity()} and the
     * {@link #luminance()} to explicity create a whitepoint color.
     */
    CHROMATICITY
  }

  /**
   * @return The explicit chromaticity coordinates, only used if type is `CHROMATICITY`.
   */
  Chromaticity chromaticity() default @Chromaticity(x = 0.3333, y = 0.3333);

  /**
   * @return The luminance of the white point, defaults to 1.0
   */
  double luminance() default 1.0;

  /**
   * @return The correlated color temperature, defaults to 5000, only used if type is `TEMPERATURE`.
   */
  double temperature() default 5000;

  /**
   * @return The type of the illuminant
   */
  Type type();
}
