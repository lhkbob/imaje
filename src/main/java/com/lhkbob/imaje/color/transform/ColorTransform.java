package com.lhkbob.imaje.color.transform;

import com.lhkbob.imaje.color.Color;

/**
 * Useful resources:
 * https://rip94550.wordpress.com/2009/10/26/color-from-spectrum-to-tristimulus/
 * http://www.color.org/specification/ICC1v43_2010-12.pdf
 * http://www.adobe.com/digitalimag/pdfs/AdobeRGB1998.pdf
 */
public interface ColorTransform<I extends Color, O extends Color> {
  boolean apply(I input, O output);
}
