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
package com.lhkbob.imaje.color.icc;

/**
 *
 */
public enum RenderingIntent {
  /**
   * This transformation corresponds to color reproduction on a hypothetical
   * reference reflective medium. Thus, the PCS values represent the appearance
   * of the reproduction as viewed in the reference viewing environment by an
   * observer adapted to that environment.
   *
   * This ensures reasonable consistency across devices but exact colorimetry
   * is not required.
   */
  PERCEPTUAL,
  /**
   * Transformations with this intent will re-scale in-gamut tristimulus values so that
   * the white point of the actual medium maps to the PCS adopted white point. The
   * {@link com.lhkbob.imaje.color.icc.reader.Tag#CHROMATIC_ADAPTATION} defines a
   * chromatic adaptation matrix specifying how to adapt tristimulus values to the
   * adopted white.
   */
  MEDIA_RELATIVE_COLORIMETRIC,
  /**
   * Vendor specific transformations that compromise accuracy to preserve hue or
   * enhance vividness of colors.
   */
  SATURATION,
  /**
   * This transformation does not modify normalized tristimulus values. The
   * class of D_TO_Bx and B_TO_Dx tags define absolute transformations that operate
   * on absolute colorimetric data. If these are not present, the absolute colorimetric
   * values can be calculated from a media relative colorimetric transformation and
   * ratio of media white points.
   */
  ICC_ABSOLUTE_COLORIMETRIC
}
