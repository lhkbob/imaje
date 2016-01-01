package com.lhkbob.imaje.color.icc;

/**
 *
 */
public final class NamedColor {
  private final GenericColorValue device;
  private final String name;
  private final GenericColorValue pcs;

  public NamedColor(String name, GenericColorValue pcs, GenericColorValue device) {
    if (pcs.getType() != GenericColorValue.ColorType.PCSLAB
        && pcs.getType() != GenericColorValue.ColorType.PCSXYZ) {
      throw new IllegalArgumentException("PCS color must be of type PCSLAB or PCSXYZ");
    }

    this.name = name;
    this.pcs = pcs;
    this.device = device;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NamedColor)) {
      return false;
    }

    NamedColor that = (NamedColor) o;
    return name.equals(that.name) && pcs.equals(that.pcs) && !(device != null ? !device
        .equals(that.device) : that.device != null);
  }

  public GenericColorValue getDeviceColor() {
    return device;
  }

  public String getName() {
    return name;
  }

  public GenericColorValue getPCSColor() {
    return pcs;
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + pcs.hashCode();
    result = 31 * result + (device != null ? device.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return String.format("Named color (name: %s, pcs: %s, device: %s)", name, pcs, device);
  }
}
