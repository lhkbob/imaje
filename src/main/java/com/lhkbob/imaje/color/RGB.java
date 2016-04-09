package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({ "Red", "Green", "Blue" })
public class RGB extends SimpleColor {
  @Gamma(gamma = 2.19921875)
  @Illuminant(type = Illuminant.Type.D65)
  @Primaries(red = @Chromaticity(x = 0.64, y = 0.33),
      green = @Chromaticity(x = 0.21, y = 0.71),
      blue = @Chromaticity(x = 0.15, y = 0.06))
  public static class Adobe extends RGB {
    public Adobe() {

    }

    public Adobe(double r, double g, double b) {
      super(r, g, b);
    }

    @Override
    public Adobe clone() {
      return (Adobe) super.clone();
    }
  }

  @Gamma(gamma = 1.801)
  @Illuminant(type = Illuminant.Type.D65)
  @Primaries(red = @Chromaticity(x = 0.625, y = 0.34),
      green = @Chromaticity(x = 0.28, y = 0.595),
      blue = @Chromaticity(x = 0.155, y = 0.07))
  public static class Apple extends RGB {
    public Apple() {

    }

    public Apple(double r, double g, double b) {
      super(r, g, b);
    }

    @Override
    public Apple clone() {
      return (Apple) super.clone();
    }
  }

  @Gamma(gamma = 1.0)
  @Illuminant(type = Illuminant.Type.E)
  @Primaries(red = @Chromaticity(x = 0.7347, y = 0.2653),
      green = @Chromaticity(x = 0.2738, y = 0.7174),
      blue = @Chromaticity(x = 0.1666, y = 0.0089))
  public static class CIE extends RGB {
    public CIE() {

    }

    public CIE(double r, double g, double b) {
      super(r, g, b);
    }

    @Override
    public CIE clone() {
      return (CIE) super.clone();
    }
  }

  @Gamma(gamma = 1.0 / 0.45, a = 1.0 / 1.099, b = 0.099 / 1.099, c = 0.0, d = 0.081, e = 1.0
      / 4.5, f = 0.0)
  @Illuminant(type = Illuminant.Type.D65)
  @Primaries(red = @Chromaticity(x = 0.64, y = 0.33),
      green = @Chromaticity(x = 0.3, y = 0.6),
      blue = @Chromaticity(x = 0.15, y = 0.06))
  public static class HDTV extends RGB {
    public HDTV() {

    }

    public HDTV(double r, double g, double b) {
      super(r, g, b);
    }

    @Override
    public HDTV clone() {
      return (HDTV) super.clone();
    }
  }

  public static class Linear extends RGB {
    public Linear() {

    }

    public Linear(double r, double g, double b) {
      super(r, g, b);
    }

    @Override
    public Linear clone() {
      return (Linear) super.clone();
    }
  }

  @Gamma(gamma = 1.0 / 0.45, a = 1.0 / 1.099, b = 0.099 / 1.099, c = 0.0, d = 0.081, e = 1.0
      / 4.5, f = 0.0)
  @Illuminant(type = Illuminant.Type.C)
  @Primaries(red = @Chromaticity(x = 0.67, y = 0.33),
      green = @Chromaticity(x = 0.21, y = 0.71),
      blue = @Chromaticity(x = 0.14, y = 0.08))
  public static class NTSC extends RGB {
    public NTSC() {

    }

    public NTSC(double r, double g, double b) {
      super(r, g, b);
    }

    @Override
    public NTSC clone() {
      return (NTSC) super.clone();
    }
  }

  @Gamma(gamma = 1.0 / 0.45, a = 1.0 / 1.099, b = 0.099 / 1.099, c = 0.0, d = 0.081, e = 1.0
      / 4.5, f = 0.0)
  @Illuminant(type = Illuminant.Type.D65)
  @Primaries(red = @Chromaticity(x = 0.64, y = 0.33),
      green = @Chromaticity(x = 0.29, y = 0.6),
      blue = @Chromaticity(x = 0.15, y = 0.06))
  public static class PAL extends RGB {
    public PAL() {

    }

    public PAL(double r, double g, double b) {
      super(r, g, b);
    }

    @Override
    public PAL clone() {
      return (PAL) super.clone();
    }
  }

  @Gamma(gamma = 1.8, d = 0.002, e = 0.062)
  @Illuminant(type = Illuminant.Type.D50)
  @Primaries(red = @Chromaticity(x = 0.7347, y = 0.2653),
      green = @Chromaticity(x = 0.1596, y = 0.8404),
      blue = @Chromaticity(x = 0.0366, y = 0.0001))
  public static class ProPhoto extends RGB {
    public ProPhoto() {

    }

    public ProPhoto(double r, double g, double b) {
      super(r, g, b);
    }

    @Override
    public ProPhoto clone() {
      return (ProPhoto) super.clone();
    }
  }

  @Gamma(gamma = 1.0 / 0.45, a = 1.0 / 1.099, b = 0.099 / 1.099, c = 0.0, d = 0.081, e = 1.0
      / 4.5, f = 0.0)
  @Illuminant(type = Illuminant.Type.D65)
  @Primaries(red = @Chromaticity(x = 0.63, y = 0.34),
      green = @Chromaticity(x = 0.31, y = 0.595),
      blue = @Chromaticity(x = 0.155, y = 0.07))
  public static class SMPTEC extends RGB {
    public SMPTEC() {

    }

    public SMPTEC(double r, double g, double b) {
      super(r, g, b);
    }

    @Override
    public SMPTEC clone() {
      return (SMPTEC) super.clone();
    }
  }

  @Gamma(gamma = 2.222, a = 0.91, b = 0.09, c = 0.0, d = 0.018, e = 0.222, f = 0.0)
  @Illuminant(type = Illuminant.Type.D65)
  @Primaries(red = @Chromaticity(x = 0.708, y = 0.292),
      green = @Chromaticity(x = 0.170, y = 0.797),
      blue = @Chromaticity(x = 0.131, y = 0.046))
  public static class UHDTV extends RGB {
    public UHDTV() {

    }

    public UHDTV(double r, double g, double b) {
      super(r, g, b);
    }

    @Override
    public UHDTV clone() {
      return (UHDTV) super.clone();
    }
  }

  // https://en.wikipedia.org/wiki/RGB_color_space
  @Gamma(gamma = 2.19921875)
  @Illuminant(type = Illuminant.Type.D50)
  @Primaries(red = @Chromaticity(x = 0.7347, y = 0.2653),
      green = @Chromaticity(x = 0.1152, y = 0.8264),
      blue = @Chromaticity(x = 0.1566, y = 0.0177))
  public static class WideGamut extends RGB {
    public WideGamut() {

    }

    public WideGamut(double r, double g, double b) {
      super(r, g, b);
    }

    @Override
    public WideGamut clone() {
      return (WideGamut) super.clone();
    }
  }

  public RGB() {
    this(0, 0, 0);
  }

  public RGB(double r, double g, double b) {
    super(3);
    set(r, g, b);
  }

  public double b() {
    return getBlue();
  }

  public void b(double b) {
    setBlue(b);
  }

  @Override
  public RGB clone() {
    return (RGB) super.clone();
  }

  public double g() {
    return getGreen();
  }

  public void g(double g) {
    setGreen(g);
  }

  public double getBlue() {
    return channels[2];
  }

  public double getGreen() {
    return channels[1];
  }

  public double getRed() {
    return channels[0];
  }

  public double r() {
    return getRed();
  }

  public void r(double r) {
    setRed(r);
  }

  public void setBlue(double b) {
    channels[2] = b;
  }

  public void setGreen(double g) {
    channels[1] = g;
  }

  public void setRed(double r) {
    channels[0] = r;
  }
}
