/**
 * Space.Spectrum
 * ==============
 *
 * The `com.lhkbob.imaje.color.space.spectrum` package defines color spaces and transforms for full
 * spectral color representations. The provided {@link
 * com.lhkbob.imaje.color.space.spectrum.SpectrumSpace} samples a range of wavelengths. The sampling
 * strategy is either uniform or logarithmic. Subclasses are defined for common wavelength ranges of
 * interest:
 *
 * + Visible light (360nm to 830nm): {@link com.lhkbob.imaje.color.space.spectrum.Visible}.
 * + Mid-infrared light (3000nm to 5000nm): {@link com.lhkbob.imaje.color.space.spectrum.Infrared}.
 * + Ultraviolet (100nm to 400nm): {@link com.lhkbob.imaje.color.space.spectrum.Ultraviolet}.
 *
 * Each of these subclasses accept a resolution parameter that controls the number of channels used
 * to sample the spectral range of the space. While multiple instances of a spectral space such as
 * Visible may embody the same spectrum, they are not directly compatible unless they have the same
 * channel count or resolution.
 *
 * @author Michael Ludwig
 */
package com.lhkbob.imaje.color.space.spectrum;