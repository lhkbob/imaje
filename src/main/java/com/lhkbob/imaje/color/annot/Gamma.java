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
public @interface Gamma {
  double a() default 1.0;

  double b() default 0.0;

  double c() default 0.0;

  double d() default 0.0;

  double e() default 1.0;

  double f() default 0.0;

  double gamma();
}
