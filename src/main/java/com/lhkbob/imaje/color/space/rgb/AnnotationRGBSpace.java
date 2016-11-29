package com.lhkbob.imaje.color.space.rgb;

import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.Yxy;
import com.lhkbob.imaje.color.space.xyz.CIE31;
import com.lhkbob.imaje.color.transform.Illuminants;
import com.lhkbob.imaje.color.transform.curves.Curve;
import com.lhkbob.imaje.color.transform.curves.UnitGammaFunction;

/**
 * AnnotationRGBSpace
 * ==================
 *
 * An abstract subclass of {@link RGBSpace} that uses annotations to define the whitepoint,
 * primaries, and gamma curve required by RGBSpace to define the RGB to XYZ color transformation.
 * Concrete RGB coordinate spaces that are statically defined and standardized (i.e. not from an
 * embedded color space in an image file, or an ICC color profile) can extend this class and use
 * {@link Gamma}, {@link Primaries}, and {@link Illuminant} to specify the components of the color
 * transformation.
 *
 * If a subclass is not annotated with {@link Gamma}, no gamma curve is used (equivalent to a linear
 * coordinate system). If it is not annotated with {@link Primaries} then the REC BT 709 primaries
 * are used as a default. If it is not annotated with {@link Illuminant}, then the whitepoint is
 * defined as a D65 source with luminance of 1.0.
 *
 * @author Michael Ludwig
 */
public abstract class AnnotationRGBSpace<S extends AnnotationRGBSpace<S>> extends RGBSpace<S> {
  public AnnotationRGBSpace() {
    // Lookup primaries
    Primaries p = getClass().getAnnotation(Primaries.class);
    Yxy<CIE31> r, g, b;
    if (p != null) {
      r = Yxy.newCIE31(p.red().x(), p.red().y());
      g = Yxy.newCIE31(p.green().x(), p.green().y());
      b = Yxy.newCIE31(p.blue().x(), p.blue().y());
    } else {
      // Default to REC BT 709 primaries
      r = Yxy.newCIE31(0.64, 0.33);
      g = Yxy.newCIE31(0.3, 0.6);
      b = Yxy.newCIE31(0.15, 0.06);
    }

    // Whitepoint
    Illuminant i = getClass().getAnnotation(Illuminant.class);
    Yxy<CIE31> whitepoint;
    if (i != null) {
      whitepoint = fromIlluminant(i);
    } else {
      whitepoint = Illuminants.newD65(1.0);
    }

    XYZ<CIE31> whiteXYZ = whitepoint.getColorSpace().getXYZTransform().apply(whitepoint);

    // Gamma
    Gamma gamma = getClass().getAnnotation(Gamma.class);
    Curve gammaCurve;
    if (gamma == null || (gamma.gamma() == 1.0 && gamma.a() == 1.0 && gamma.b() == 0.0
        && gamma.c() == 0.0 && gamma.d() == 0.0 && gamma.e() == 1.0 && gamma.f() == 0.0)) {
      // No gamma, or gamma is explicitly identity so use null since the RGBToXYZ accept a null
      // curve that is assumed to represent the identity transform
      gammaCurve = null;
    } else {
      gammaCurve = new UnitGammaFunction(
          gamma.gamma(), gamma.a(), gamma.b(), gamma.c(), gamma.e(), gamma.f(), gamma.d());
    }


    initialize(whiteXYZ, r, g, b, gammaCurve);
  }

  private static Yxy<CIE31> fromIlluminant(Illuminant illum) {
    double l = illum.luminance();
    switch (illum.type()) {
    case A:
      return Illuminants.newA(l);
    case B:
      return Illuminants.newB(l);
    case C:
      return Illuminants.newC(l);
    case D50:
      return Illuminants.newD50(l);
    case D55:
      return Illuminants.newD55(l);
    case D65:
      return Illuminants.newD65(l);
    case D75:
      return Illuminants.newD75(l);
    case E:
      return Illuminants.newE(l);
    case F1:
      return Illuminants.newF1(l);
    case F2:
      return Illuminants.newF2(l);
    case F3:
      return Illuminants.newF3(l);
    case F4:
      return Illuminants.newF4(l);
    case F5:
      return Illuminants.newF5(l);
    case F6:
      return Illuminants.newF6(l);
    case F7:
      return Illuminants.newF7(l);
    case F8:
      return Illuminants.newF8(l);
    case F9:
      return Illuminants.newF9(l);
    case F10:
      return Illuminants.newF10(l);
    case F11:
      return Illuminants.newF11(l);
    case F12:
      return Illuminants.newF12(l);
    case TEMPERATURE:
      return Illuminants.newCorrelatedColorTemperature(illum.temperature(), l);
    case CHROMATICITY:
      return Yxy.newCIE31(l, illum.chromaticity().x(), illum.chromaticity().y());
    default:
      throw new IllegalArgumentException("Unsupported illuminant type: " + illum.type());
    }
  }
}
