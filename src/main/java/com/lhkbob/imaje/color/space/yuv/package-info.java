/**
 * Space.YUV
 * =========
 *
 * The `com.lhkbob.imaje.color.space.yuv` package provides color space definitions for the more
 * common difference-chroma color representations, namely {@link com.lhkbob.imaje.color.YUV YUV}
 * and {@link com.lhkbob.imaje.color.YCbCr YCbCr}. These spaces are derived spaces that are
 * defined with respect to a particular RGB space and two constants describing how to calculate
 * chroma from the RGB primaries.
 *
 * The YUV and YCbCr spaces are very similar and differ mostly in choice of internal constants and
 * the real-world domains that used them. However, unless dealing with one of those domains
 * directly, it is recommended to use {@link com.lhkbob.imaje.color.space.lab.CIE CIELAB} instead
 * for a perceptually-oriented space.
 *
 * @author Michael Ludwig
 */
package com.lhkbob.imaje.color.space.yuv;