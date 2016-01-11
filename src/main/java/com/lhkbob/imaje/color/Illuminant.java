package com.lhkbob.imaje.color;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Illuminant {
  // https://en.wikipedia.org/wiki/Standard_illuminant
  enum Type {
    A, B, C, D50, D55, D65, D75, E, F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, TEMPERATURE,
    CHROMATICITY
  }

  Type type();

  Chromaticity chromaticity() default @Chromaticity(x = 0.3333, y = 0.3333);

  double temperature() default 5000;

  double luminance() default 1.0;
}
