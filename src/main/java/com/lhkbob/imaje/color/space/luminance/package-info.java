/**
 * Space.Luminance
 * ===============
 *
 * The `com.lhkbob.imaje.color.space.luminance` package defines color spaces for the single-channel
 * {@link com.lhkbob.imaje.color.Luminance Luminance} color type. Two spaces are currently provided,
 * both of which are defined with respect to a reference whitepoint measured in CIE '31 XYZ. The
 * first, {@link com.lhkbob.imaje.color.space.luminance.Linear Linear}, stores values as direct
 * photometric quantities. The second, {@link com.lhkbob.imaje.color.space.luminance.Gamma Gamma},
 * stores the photometric quantities with gamma encoding, which is suitable for if luminance values
 * are calculated directly from gamma-encoded RGB colors such as SRGB.
 *
 * @author Michael Ludwig
 */
package com.lhkbob.imaje.color.space.luminance;