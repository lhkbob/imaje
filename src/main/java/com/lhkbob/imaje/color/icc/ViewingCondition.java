package com.lhkbob.imaje.color.icc;

/**
 *
 */
public final class ViewingCondition {
  private final GenericColorValue illuminant;
  private final StandardIlluminant illuminantType;
  private final GenericColorValue surround;

  public ViewingCondition(
      GenericColorValue illuminant, StandardIlluminant illuminantType, GenericColorValue surround) {
    if (illuminant.getType() != GenericColorValue.ColorType.CIEXYZ) {
      throw new IllegalArgumentException(
          "Illuminant color values  must be specified as un-normalized CIEXYZ");
    }
    if (surround.getType() != GenericColorValue.ColorType.CIEXYZ) {
      throw new IllegalArgumentException(
          "Surround color values must be specified as un-normalized CIEXYZ");
    }

    this.illuminant = illuminant;
    this.illuminantType = illuminantType;
    this.surround = surround;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ViewingCondition)) {
      return false;
    }
    ViewingCondition c = (ViewingCondition) o;
    return c.illuminantType.equals(illuminantType) && c.illuminant.equals(illuminant) && c.surround
        .equals(surround);
  }

  public GenericColorValue getIlluminant() {
    return illuminant;
  }

  public StandardIlluminant getIlluminantType() {
    return illuminantType;
  }

  public GenericColorValue getSurround() {
    return surround;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + illuminantType.hashCode();
    result = 31 * result + illuminant.hashCode();
    result = 31 * result + surround.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return String
        .format("ViewingCondition (illuminant: %s (%s), surround: %s)", illuminant, illuminantType,
            surround);
  }
}
