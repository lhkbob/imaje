/**
 * Space.XYZ
 * =========
 *
 * The `com.lhkbob.imaje.color.space.xyz` package defines the color spaces for representing {@link
 * com.lhkbob.imaje.color.XYZ XYZ} color and the derived {@link com.lhkbob.imaje.color.Yxy Yxy}
 * color. The XYZ color space family is a tristimulus-based system where each channel's value is
 * determined by a color matching function that is integrated with the spectral representation of
 * the color. The Y tristimulus is defined such that it is equivalent to photometric luminance.
 *
 * Specific XYZ color spaces are formed by the definition of these color matching functions for X,
 * Y, and Z. Singleton spaces are defined in this package for the {@link
 * com.lhkbob.imaje.color.space.xyz.CIE31 '31 CIE XYZ} and {@link
 * com.lhkbob.imaje.color.space.xyz.CIE64 '64 CIE XYZ} spaces. CIE31 is the generally recommended
 * space to assume when dealing with XYZ colors due to its prevalence.
 *
 * @author Michael Ludwig
 */
package com.lhkbob.imaje.color.space.xyz;