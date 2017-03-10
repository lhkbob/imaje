/**
 * Space.RGB
 * =========
 *
 * The `com.lhkbob.imaje.color.space.rgb` package defines RGB color spaces for many of the current
 * color standards, along with flexible classes for easily defining new ones. All RGB spaces in this
 * package are defined similarly with respect to an XYZ color space, a reference whitepoint and
 * chromaticity coordinates for the red, green, and blue primaries. Optionally, they may also have a
 * decoding gamma function if the RGB values are non-linear.
 *
 * Common color standards are listed below with their matching classes. Each of these color spaces
 * are singletons so any RGB instance with one as its generic type parameter will have the same
 * space.
 *
 * + [Adobe's RGB 98 standard](https://en.wikipedia.org/wiki/Adobe_RGB_color_space): {@link
 * com.lhkbob.imaje.color.space.rgb.Adobe Adobe}
 * + [Apple's RGB standard](https://en.wikipedia.org/wiki/Apple_RGB): {@link
 * com.lhkbob.imaje.color.space.rgb.Apple Apple}
 * + [CIE's 1931 RGB
 * standard](https://en.wikipedia.org/wiki/CIE_1931_color_space#CIE_RGB_color_space): {@link
 * com.lhkbob.imaje.color.space.rgb.CIE CIE}
 * + [HDTV (ITU-R BT.709)standard](https://en.wikipedia.org/wiki/Rec._709): {@link
 * com.lhkbob.imaje.color.space.rgb.HDTV HDTV}
 * + [NTSC's original 1953 standard](https://en.wikipedia.org/wiki/NTSC#Colorimetry): {@link
 * com.lhkbob.imaje.color.space.rgb.NTSC NTSC}
 * + [PAL standard](https://en.wikipedia.org/wiki/PAL): {@link com.lhkbob.imaje.color.space.rgb.PAL
 * PAL}
 * + [ProPhoto RGB standard](https://en.wikipedia.org/wiki/ProPhoto_RGB_color_space): {@link
 * com.lhkbob.imaje.color.space.rgb.ProPhoto ProPhoto}
 * + [NTSC's SMPTE C standard](https://en.wikipedia.org/wiki/NTSC#SMPTE_C): {@link
 * com.lhkbob.imaje.color.space.rgb.SMPTEC SMPTEC}
 * + [sRGB standard](https://en.wikipedia.org/wiki/SRGB): {@link
 * com.lhkbob.imaje.color.space.rgb.SRGB SRGB}
 * + [UHDTV (ITU-R BT.2020) standard](https://en.wikipedia.org/wiki/Rec._2020): {@link
 * com.lhkbob.imaje.color.space.rgb.UHDTV UHDTV}
 * + [Adobe's Wide Gamut RGB
 * standard](https://en.wikipedia.org/wiki/Adobe_Wide_Gamut_RGB_color_space): {@link
 * com.lhkbob.imaje.color.space.rgb.WideGamut WideGamut}
 *
 * New RGB spaces can easily be defined by instantiating instances of {@link
 * com.lhkbob.imaje.color.space.rgb.CustomRGBSpace}. Linear versions of any RGB space can be made by
 * making instances of {@link com.lhkbob.imaje.color.space.luminance.Linear}. Unlike the standard
 * spaces that are singletons, instances of these space types do not necessarily describe the same
 * color space.
 *
 * @author Michael Ludwig
 */
package com.lhkbob.imaje.color.space.rgb;