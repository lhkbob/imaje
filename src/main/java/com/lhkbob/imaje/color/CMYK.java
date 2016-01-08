package com.lhkbob.imaje.color;

/**
 *
 */
@Channels({"Cyan", "Magenta", "Yellow", "Black"})
public class CMYK implements Color {
  private double cyan;
  private double magenta;
  private double yellow;
  private double black;

  public CMYK() {
    this(0.0, 0.0, 0.0, 1.0);
  }

  public CMYK(double c, double m, double y, double k) {
    cyan = c;
    magenta = m;
    yellow = y;
    black = k;
  }

  public double c() {
    return cyan;
  }

  public void c(double c) {
    cyan = c;
  }

  public double m() {
    return magenta;
  }

  public void m(double m) {
    magenta = m;
  }

  public double y() {
    return yellow;
  }

  public void y(double y) {
    yellow = y;
  }

  public double k() {
    return black;
  }

  public void k(double k) {
    black = k;
  }

  public double getCyan() {
    return cyan;
  }

  public void setCyan(double cyan) {
    this.cyan = cyan;
  }

  public double getBlack() {
    return black;
  }

  public void setBlack(double black) {
    this.black = black;
  }

  public double getYellow() {
    return yellow;
  }

  public void setYellow(double yellow) {
    this.yellow = yellow;
  }

  public double getMagenta() {
    return magenta;
  }

  public void setMagenta(double magenta) {
    this.magenta = magenta;
  }

  @Override
  public int getChannelCount() {
    return 4;
  }

  @Override
  public double get(int channel) {
    switch (channel) {
    case 0:
      return cyan;
    case 1:
      return magenta;
    case 2:
      return yellow;
    case 3:
      return black;
    default:
      throw new IndexOutOfBoundsException("Bad channel: " + channel);
    }
  }

  @Override
  public void toArray(double[] array) {
    array[0] = cyan;
    array[1] = magenta;
    array[2] = yellow;
    array[3] = black;
  }

  @Override
  public void fromArray(double[] array) {
    cyan = array[0];
    magenta = array[1];
    yellow = array[2];
    black = array[3];
  }

  @Override
  public CMYK clone() {
    try {
      return (CMYK) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen");
    }
  }

  @Override
  public int hashCode() {
    int result = Double.hashCode(cyan);
    result = 31 * result + Double.hashCode(magenta);
    result = 31 * result + Double.hashCode(yellow);
    result = 31 * result + Double.hashCode(black);
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o == null || o.getClass() != getClass()) {
      return false;
    }
    CMYK c = (CMYK) o;
    return Double.compare(c.cyan, cyan) == 0 && Double.compare(c.magenta, magenta) == 0 &&
        Double.compare(c.yellow, yellow) == 0 && Double.compare(c.black, black) == 0;
  }

  @Override
  public String toString() {
    return String.format("%s(%.3f, %.3f, %.3f, %.3f)", getClass().getSimpleName(), cyan, magenta, yellow, black);
  }
}
