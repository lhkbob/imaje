package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({ RGB.RED, RGB.GREEN, RGB.BLUE })
public class RGB implements Color {
  public static final String BLUE = "Blue";
  public static final String GREEN = "Green";
  public static final String RED = "Red";

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
  private double b;
  private double g;
  private double r;

  public RGB() {
    this(0, 0, 0);
  }

  public RGB(double r, double g, double b) {
    this.r = r;
    this.g = g;
    this.b = b;
  }

  public double b() {
    return b;
  }

  public void b(double b) {
    this.b = b;
  }

  @Override
  public RGB clone() {
    try {
      return (RGB) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o == null || o.getClass() != getClass()) {
      return false;
    }
    RGB c = (RGB) o;
    return Double.compare(c.r, r) == 0 && Double.compare(c.g, g) == 0
        && Double.compare(c.b, b) == 0;
  }

  @Override
  public void fromArray(double[] array) {
    r = array[0];
    g = array[1];
    b = array[2];
  }

  public double g() {
    return g;
  }

  public void g(double g) {
    this.g = g;
  }

  @Override
  public double get(int channel) {
    switch (channel) {
    case 0:
      return r;
    case 1:
      return g;
    case 2:
      return b;
    default:
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }

  public double getBlue() {
    return b;
  }

  @Override
  public int getChannelCount() {
    return 3;
  }

  public double getGreen() {
    return g;
  }

  public double getRed() {
    return r;
  }

  @Override
  public int hashCode() {
    int result = Double.hashCode(r);
    result = 31 * result + Double.hashCode(g);
    result = 31 * result + Double.hashCode(b);
    return result;
  }

  public double r() {
    return r;
  }

  public void r(double r) {
    this.r = r;
  }

  public void setBlue(double b) {
    this.b = b;
  }

  public void setGreen(double g) {
    this.g = g;
  }

  public void setRed(double r) {
    this.r = r;
  }

  @Override
  public void toArray(double[] array) {
    array[0] = r;
    array[1] = g;
    array[2] = b;
  }

  @Override
  public String toString() {
    return String.format("%s(%.3f, %.3f, %.3f)", getClass().getSimpleName(), b, g, b);
  }
}
