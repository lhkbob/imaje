package com.lhkbob.imaje.color.icc;

import com.lhkbob.imaje.util.Arguments;

/**
 *
 */
public final class ViewingCondition {
  private final LocalizedString description;
  private final GenericColorValue illuminant;
  private final StandardIlluminant illuminantType;
  private final GenericColorValue surround;

  public ViewingCondition(
      GenericColorValue illuminant, StandardIlluminant illuminantType, GenericColorValue surround,
      LocalizedString description) {
    Arguments.notNull("description", description);
    Arguments.equals("illuminant.getType()", GenericColorValue.ColorType.CIEXYZ, illuminant.getType());
    Arguments.equals("surround.getType()", GenericColorValue.ColorType.CIEXYZ, surround.getType());

    this.description = description;
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
    return c.description.equals(description) && c.illuminantType.equals(illuminantType)
        && c.illuminant.equals(illuminant) && c.surround.equals(surround);
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
    result = 31 * result + description.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return String
        .format("ViewingCondition (desc: %s, illuminant: %s (%s), surround: %s)", description,
            illuminant, illuminantType, surround);
  }
}
