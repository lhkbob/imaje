package com.lhkbob.imaje.color.transform;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.annot.Gamma;
import com.lhkbob.imaje.color.HLS;
import com.lhkbob.imaje.color.HSV;
import com.lhkbob.imaje.color.annot.Illuminant;
import com.lhkbob.imaje.color.Lab;
import com.lhkbob.imaje.color.Luminance;
import com.lhkbob.imaje.color.Luv;
import com.lhkbob.imaje.color.annot.OpponentAxis;
import com.lhkbob.imaje.color.annot.Primaries;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.SRGB;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.YCbCr;
import com.lhkbob.imaje.color.YUV;
import com.lhkbob.imaje.color.Yxy;
import com.lhkbob.imaje.color.transform.curves.Curve;
import com.lhkbob.imaje.color.transform.curves.UnitGammaFunction;
import com.lhkbob.imaje.color.transform.general.CIELabToXYZ;
import com.lhkbob.imaje.color.transform.general.Curves;
import com.lhkbob.imaje.color.transform.general.HLSToRGB;
import com.lhkbob.imaje.color.transform.general.HSVToRGB;
import com.lhkbob.imaje.color.transform.general.HunterLabToXYZ;
import com.lhkbob.imaje.color.transform.general.Identity;
import com.lhkbob.imaje.color.transform.general.LuminanceToXYZ;
import com.lhkbob.imaje.color.transform.general.LuvToXYZ;
import com.lhkbob.imaje.color.transform.general.RGBToHLS;
import com.lhkbob.imaje.color.transform.general.RGBToHSV;
import com.lhkbob.imaje.color.transform.general.RGBToXYZ;
import com.lhkbob.imaje.color.transform.general.RGBToYCbCr;
import com.lhkbob.imaje.color.transform.general.XYZToYxy;
import com.lhkbob.imaje.color.transform.general.YxyToXYZ;
import com.lhkbob.imaje.util.Arguments;

import org.ejml.data.FixedMatrix3x3_64F;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public final class Transforms {
  private Transforms() {}

  public static <I extends Color, O extends Color> TransformFactory<I, O> getTransformFactory(
      Class<I> input, Class<O> output) {
    Arguments.notNull("input", input);
    Arguments.notNull("output", output);

    // Easy case if they are equal
    if (input.equals(output)) {
      return newIdentityFactory(input, output);
    }

    synchronized (TRANSFORM_LOCK) {
      // See if there's an exact match
      TransformFactory<I, O> transform = lookupTransform(input, output);
      if (transform != null) {
        return transform;
      }

      // Try finding an exact inverse of the requested configuration
      TransformFactory<O, I> inverted = lookupTransform(output, input);
      if (inverted != null) {
        return invert(inverted);
      }

      // Try making a bridge through the XYZ color space
      TransformFactory<I, XYZ> toXYZ = lookupTransform(input, XYZ.class);
      if (toXYZ == null) {
        // Try finding an the bridge from the other direction.
        TransformFactory<XYZ, I> inverseToXYZ = lookupTransform(XYZ.class, input);
        if (inverseToXYZ == null) {
          // Bridge cannot be connected.
          return null;
        }
        toXYZ = invert(inverseToXYZ);
      }

      TransformFactory<XYZ, O> fromXYZ = lookupTransform(XYZ.class, output);
      if (fromXYZ == null) {
        // Try finding an the bridge from the other direction.
        TransformFactory<O, XYZ> inverseFromXYZ = lookupTransform(output, XYZ.class);
        if (inverseFromXYZ == null) {
          // Bridge cannot be connected.
          return null;
        }
        fromXYZ = invert(inverseFromXYZ);
      }

      return throughXYZ(toXYZ, fromXYZ);
    }
  }

  public static <I extends Color, O extends Color> TransformFactory<O, I> invert(
      TransformFactory<I, O> factory) {
    return new InverseTransformFactory<>(factory);
  }

  public static TransformFactory<Lab.CIE, XYZ> newCIELabToXYZFactory(XYZ whitepoint) {
    CIELabToXYZ t = new CIELabToXYZ(whitepoint);
    return new GeneralTransformFactory<>(Lab.CIE.class, XYZ.class, t, t.inverted());
  }

  public static TransformFactory<Lab.Hunter, XYZ> newHunterLabToXYZFactory(XYZ whitepoint) {
    HunterLabToXYZ t = new HunterLabToXYZ(whitepoint);
    return new GeneralTransformFactory<>(Lab.Hunter.class, XYZ.class, t, t.inverted());
  }

  public static <I extends Color, O extends Color> TransformFactory<I, O> newIdentityFactory(Class<I> in, Class<O> out) {
    int inChannels = Color.getChannelCount(in);
    int outChannels = Color.getChannelCount(out);
    return new GeneralTransformFactory<>(in, out, new Identity(inChannels), new Identity(outChannels));
  }

  public static <R extends RGB> TransformFactory<R, RGB.Linear> newLinearRGBFactory(
      Class<R> rgbType) {
    Curve gamma = getGammaCorrectionCurve(rgbType);
    if (gamma == null) {
      // There is no gamma correction, so return an identity transform
      return newIdentityFactory(rgbType, RGB.Linear.class);
    }

    // R to Linear applies gamma
    Curves t = new Curves(Arrays.asList(gamma, gamma, gamma));
    return new GeneralTransformFactory<>(rgbType, RGB.Linear.class, t, t.inverted());
  }

  public static TransformFactory<RGB.Linear, XYZ> newLinearRGBToXYZFactory(
      Class<? extends RGB> originalRGBSpace) {
    FixedMatrix3x3_64F rgbToXYZ = getRGBToXYZMatrix(originalRGBSpace);
    RGBToXYZ t = new RGBToXYZ(rgbToXYZ, null);
    return new GeneralTransformFactory<>(RGB.Linear.class, XYZ.class, t, t.inverted());
  }

  public static TransformFactory<Luminance, XYZ> newLuminanceToXYZFactory(XYZ whitepoint) {
    LuminanceToXYZ t = new LuminanceToXYZ(whitepoint);
    return new GeneralTransformFactory<>(Luminance.class, XYZ.class, t, t.inverted());
  }

  public static TransformFactory<Luv, XYZ> newLuvToXYZFactory(XYZ whitepoint) {
    LuvToXYZ t = new LuvToXYZ(whitepoint);
    return new GeneralTransformFactory<>(Luv.class, XYZ.class, t, t.inverted());
  }

  public static <T extends RGB> TransformFactory<T, HLS> newRGBToHLSFactory(Class<T> rgb) {
    return new GeneralTransformFactory<>(rgb, HLS.class, new RGBToHLS(), new HLSToRGB());
  }

  public static <T extends RGB> TransformFactory<T, HSV> newRGBToHSVFactory(Class<T> rgb) {
    return new GeneralTransformFactory<>(rgb, HSV.class, new RGBToHSV(), new HSVToRGB());
  }

  public static <R extends RGB> TransformFactory<R, XYZ> newRGBToXYZFactory(Class<R> rgbType) {
    FixedMatrix3x3_64F rgbToXYZ = getRGBToXYZMatrix(rgbType);
    Curve gamma = getGammaCorrectionCurve(rgbType);
    RGBToXYZ t = new RGBToXYZ(rgbToXYZ, gamma);
    return new GeneralTransformFactory<>(rgbType, XYZ.class, t, t.inverted());
  }

  public static <R extends RGB, Y extends YCbCr> TransformFactory<R, Y> newRGBToYCbCrFactory(
      Class<R> rgbType, Class<Y> ycbcrType) {
    return newRGBToYCbCrFactory(rgbType, ycbcrType, 0.5, 0.5);
  }

  public static TransformFactory<RGB.UHDTV, YCbCr.REC2020> newRGBToYCbCrREC2020Factory() {
    return newRGBToYCbCrFactory(RGB.UHDTV.class, YCbCr.REC2020.class);
  }

  public static TransformFactory<RGB.SMPTEC, YCbCr.REC601> newRGBToYCbCrREC601Factory() {
    return newRGBToYCbCrFactory(RGB.SMPTEC.class, YCbCr.REC601.class);
  }

  public static TransformFactory<RGB.HDTV, YCbCr.REC709> newRGBToYCbCrREC709Factory() {
    return newRGBToYCbCrFactory(RGB.HDTV.class, YCbCr.REC709.class);
  }

  public static <R extends RGB, Y extends YUV> TransformFactory<R, Y> newRGBToYUVFactory(
      Class<R> rgbType, Class<Y> yuvType) {
    return newRGBToYCbCrFactory(rgbType, yuvType, 0.436, 0.615);
  }

  public static TransformFactory<RGB.UHDTV, YUV.REC2020> newRGBToYUVREC2020Factory() {
    return newRGBToYUVFactory(RGB.UHDTV.class, YUV.REC2020.class);
  }

  public static TransformFactory<RGB.SMPTEC, YUV.REC601> newRGBToYUVREC601Factory() {
    return newRGBToYUVFactory(RGB.SMPTEC.class, YUV.REC601.class);
  }

  public static TransformFactory<RGB.HDTV, YUV.REC709> newRGBToYUVREC709Factory() {
    return newRGBToYUVFactory(RGB.HDTV.class, YUV.REC709.class);
  }

  public static <I extends Color, O extends Color> ColorTransform<I, O> newTransform(
      Class<I> input, Class<O> output) {
    TransformFactory<I, O> factory = getTransformFactory(input, output);
    if (factory == null) {
      throw new UnsupportedOperationException(
          "No transform exists between " + input + " and " + output);
    }
    return factory.newTransform();
  }

  public static TransformFactory<XYZ, Yxy> newXYZToYyxFactory() {
    return new GeneralTransformFactory<>(XYZ.class, Yxy.class, new XYZToYxy(), new YxyToXYZ());
  }

  public static <I extends Color, O extends Color> void registerTransformFactory(
      TransformFactory<I, O> transform) {
    Arguments.notNull("transform", transform);
    synchronized (TRANSFORM_LOCK) {
      Map<Class<?>, TransformFactory<?, ?>> forInputType = transforms.get(transform.getInputType());
      if (forInputType == null) {
        forInputType = new HashMap<>();
        transforms.put(transform.getInputType(), forInputType);
      }

      forInputType.put(transform.getOutputType(), transform);
    }
  }

  public static <I extends Color, O extends Color> TransformFactory<I, O> throughXYZ(
      TransformFactory<I, XYZ> in, TransformFactory<XYZ, O> out) {
    return new IndirectXYZTransformFactory<>(in, out);
  }

  @SuppressWarnings("unchecked")
  public static <I extends Color, O extends Color> TransformFactory<I, O> unregisterTransformFactory(
      Class<I> input, Class<O> output) {
    synchronized (TRANSFORM_LOCK) {
      Map<Class<?>, TransformFactory<?, ?>> forInputType = transforms.get(input);
      if (forInputType != null) {
        return (TransformFactory<I, O>) forInputType.remove(output);
      } else {
        return null;
      }
    }
  }

  private static Curve getGammaCorrectionCurve(Class<? extends RGB> rgb) {
    Gamma g = rgb.getAnnotation(Gamma.class);
    if (g == null || (g.gamma() == 1.0 && g.a() == 1.0 && g.b() == 0.0 && g.c() == 0.0
        && g.d() == 0.0 && g.e() == 1.0 && g.f() == 0.0)) {
      // No gamma, or gamma is explicitly identity so return null since the RGBToXYZ accept a null curve
      // that is assumed to represent the identity transform
      return null;
    }

    return new UnitGammaFunction(g.gamma(), g.a(), g.b(), g.c(), g.e(), g.f(), g.d());
  }

  private static FixedMatrix3x3_64F getRGBToXYZMatrix(Class<? extends RGB> rgb) {
    Primaries p = rgb.getAnnotation(Primaries.class);
    Illuminant i = rgb.getAnnotation(Illuminant.class);

    double xr, xg, xb;
    double yr, yg, yb;
    if (p != null) {
      xr = p.red().x();
      yr = p.red().y();
      xg = p.green().x();
      yg = p.green().y();
      xb = p.blue().x();
      yb = p.blue().y();
    } else {
      // Default to REC BT 709 primaries
      xr = 0.64;
      yr = 0.33;
      xg = 0.3;
      yg = 0.6;
      xb = 0.15;
      yb = 0.06;
    }

    Yxy whitepoint;
    if (i != null) {
      whitepoint = Illuminants.fromIlluminant(i);
    } else {
      // Default to D65
      whitepoint = Illuminants.newD65(1.0);
    }
    XYZ whiteXYZ = new XYZ();
    newXYZToYyxFactory().newInverseTransform().apply(whitepoint, whiteXYZ);

    return RGBToXYZ.calculateLinearRGBToXYZ(xr, yr, xg, yg, xb, yb, whiteXYZ);
  }

  /*
   * Assumes that TRANSFORM_LOCK is already acquired.
   */
  @SuppressWarnings("unchecked")
  private static <I extends Color, O extends Color> TransformFactory<I, O> lookupTransform(
      Class<I> input, Class<O> output) {
    Map<Class<?>, TransformFactory<?, ?>> forInputType = transforms.get(input);
    if (forInputType != null) {
      return (TransformFactory<I, O>) forInputType.get(output);
    } else {
      return null;
    }
  }

  private static <R extends RGB, Y extends Color> TransformFactory<R, Y> newRGBToYCbCrFactory(
      Class<R> rgbType, Class<Y> ycbcrType, double umax, double vmax) {
    OpponentAxis axis = ycbcrType.getAnnotation(OpponentAxis.class);
    // Convert generic opponent axis to kb and kr parameters
    double kb, kr;
    if (axis != null) {
      kb = axis.aWeight();
      kr = axis.bWeight();
    } else {
      // default to rec 601
      kb = 0.114;
      kr = 0.299;
    }
    RGBToYCbCr t = new RGBToYCbCr(kb, kr, umax, vmax);
    return new GeneralTransformFactory<>(rgbType, ycbcrType, t, t.inverted());
  }
  private static final Object TRANSFORM_LOCK = new Object();
  private static final Map<Class<?>, Map<Class<?>, TransformFactory<?, ?>>> transforms = new HashMap<>();

  static {
    registerTransformFactory(newXYZToYyxFactory());

    registerTransformFactory(newRGBToHLSFactory(SRGB.class));
    registerTransformFactory(newRGBToHSVFactory(SRGB.class));

    registerTransformFactory(newRGBToYCbCrREC601Factory());
    registerTransformFactory(newRGBToYCbCrREC709Factory());
    registerTransformFactory(newRGBToYCbCrREC2020Factory());

    registerTransformFactory(newRGBToYUVREC601Factory());
    registerTransformFactory(newRGBToYUVREC709Factory());
    registerTransformFactory(newRGBToYUVREC2020Factory());

    registerTransformFactory(newRGBToXYZFactory(SRGB.class));
    registerTransformFactory(newRGBToXYZFactory(RGB.Adobe.class));
    registerTransformFactory(newRGBToXYZFactory(RGB.Apple.class));
    registerTransformFactory(newRGBToXYZFactory(RGB.CIE.class));
    registerTransformFactory(newRGBToXYZFactory(RGB.HDTV.class));
    registerTransformFactory(newRGBToXYZFactory(RGB.NTSC.class));
    registerTransformFactory(newRGBToXYZFactory(RGB.PAL.class));
    registerTransformFactory(newRGBToXYZFactory(RGB.ProPhoto.class));
    registerTransformFactory(newRGBToXYZFactory(RGB.SMPTEC.class));
    registerTransformFactory(newRGBToXYZFactory(RGB.UHDTV.class));
    // This is just a reasonable assumption about linear RGB, but there are endpoints to create
    // more accurate linear -> xyz transforms when necessary
    registerTransformFactory(newRGBToXYZFactory(RGB.Linear.class));

    registerTransformFactory(newLinearRGBFactory(SRGB.class));
    registerTransformFactory(newLinearRGBFactory(RGB.Adobe.class));
    registerTransformFactory(newLinearRGBFactory(RGB.Apple.class));
    registerTransformFactory(newLinearRGBFactory(RGB.CIE.class));
    registerTransformFactory(newLinearRGBFactory(RGB.HDTV.class));
    registerTransformFactory(newLinearRGBFactory(RGB.NTSC.class));
    registerTransformFactory(newLinearRGBFactory(RGB.PAL.class));
    registerTransformFactory(newLinearRGBFactory(RGB.ProPhoto.class));
    registerTransformFactory(newLinearRGBFactory(RGB.SMPTEC.class));
    registerTransformFactory(newLinearRGBFactory(RGB.UHDTV.class));

    XYZ defaultWhitePoint = new XYZ();
    newXYZToYyxFactory().newInverseTransform().apply(Illuminants.newD65(1.0), defaultWhitePoint);

    registerTransformFactory(newCIELabToXYZFactory(defaultWhitePoint));
    registerTransformFactory(newHunterLabToXYZFactory(defaultWhitePoint));
    registerTransformFactory(newLuvToXYZFactory(defaultWhitePoint));
    registerTransformFactory(newLuminanceToXYZFactory(defaultWhitePoint));
  }
}
