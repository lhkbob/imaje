package com.lhkbob.imaje.color.annot;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SpectrumRange {
  // FIXME use Mitsuba's
  double DEFAULT_LOW_WAVELENGTH = 360.0;
  double DEFAULT_HIGH_WAVELENGTH = 830.0;

  double lowWavelength() default DEFAULT_LOW_WAVELENGTH;
  double highWavelength() default DEFAULT_HIGH_WAVELENGTH;
}
