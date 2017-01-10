The txt files in this directory represent spectrum basis functions for reconstructing a spectrum
given an RGB tristimulus. They are used in a Smits-style reconstruction, as implemented in
SmitsRGBToSpectrum.java.

The approach is described here: http://www.cs.utah.edu/~bes/papers/color/
Updated colorGen and colorFun scripts (https://github.com/colour-science/smits1999) were used to
generate the values stored in the spectra files.

The spectra were generated to correspond to linear RGB using the same primary coordinates as sRGB.
The spectra are sampled 32 times with a wavelength range from 360 to 800nm.