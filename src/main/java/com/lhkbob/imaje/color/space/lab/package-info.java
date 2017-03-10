/**
 * Space.LAB
 * =========
 *
 * The `com.lhkbob.image.color.space.lab` package defines color spaces for representing {@link
 * com.lhkbob.imaje.color.Lab Lab} colors and the the {@link com.lhkbob.imaje.color.CIELUV CIELUV}
 * color space. There are two Lab spaces defined currently, which is why the Lab color is
 * parameterized. The {@link com.lhkbob.imaje.color.space.lab.CIE} space represents the well-defined
 * CIELAB space that strives for perceptual color uniformity, and the {@link
 * com.lhkbob.imaje.color.space.lab.Hunter} space represents the Hunter Lab space that is another
 * attempt at color uniformity.
 *
 * There are no real variants of the CIELUV space, which is why that color is not parameterized and
 * its sole color space is defined by {@link com.lhkbob.imaje.color.space.lab.CIELUVSpace}.
 *
 * @author Michael Ludwig
 */
package com.lhkbob.imaje.color.space.lab;