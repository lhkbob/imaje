package com.lhkbob.imaje.color.icc;

/**
 *
 */
public final class ProfileDescription {
  private final DeviceAttributes attributes;
  private final LocalizedString description;
  private final Signature deviceManufacturer;
  private final Signature deviceModel;
  private final ProfileID id;
  private final LocalizedString manufacturerDesc;
  private final LocalizedString modelDesc;
  private final DeviceTechnology technology;

  public ProfileDescription() {
    this(new ProfileID(new byte[16]), new LocalizedString(), Signature.fromName(""),
        Signature.fromName(""), new DeviceAttributes(0L), DeviceTechnology.CATHODE_RAY_TUBE_DISPLAY,
        new LocalizedString(), new LocalizedString());
  }

  public ProfileDescription(
      ProfileID id, LocalizedString description, Signature manufacturer, Signature model,
      DeviceAttributes attributes, DeviceTechnology technology, LocalizedString manufacturerDesc,
      LocalizedString modelDesc) {
    this.id = id;
    this.description = description;
    this.deviceManufacturer = manufacturer;
    this.deviceModel = model;
    this.attributes = attributes;
    this.technology = technology;
    this.manufacturerDesc = manufacturerDesc;
    this.modelDesc = modelDesc;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ProfileDescription)) {
      return false;
    }

    ProfileDescription p = (ProfileDescription) o;
    return p.id.equals(id) && p.description.equals(description) && p.deviceManufacturer
        .equals(deviceManufacturer) && p.deviceModel.equals(deviceModel) && p.attributes
        .equals(attributes) && p.technology.equals(technology) && p.manufacturerDesc
        .equals(manufacturerDesc) && p.modelDesc.equals(modelDesc);
  }

  public DeviceAttributes getDeviceAttributes() {
    return attributes;
  }

  public Signature getDeviceManufacturer() {
    return deviceManufacturer;
  }

  public Signature getDeviceModel() {
    return deviceModel;
  }

  public DeviceTechnology getDeviceTechnology() {
    return technology;
  }

  public LocalizedString getManufacturerDescription() {
    return manufacturerDesc;
  }

  public LocalizedString getModelDescription() {
    return modelDesc;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + id.hashCode();
    result = 31 * result + description.hashCode();
    result = 31 * result + deviceManufacturer.hashCode();
    result = 31 * result + deviceModel.hashCode();
    result = 31 * result + attributes.hashCode();
    result = 31 * result + technology.hashCode();
    result = 31 * result + manufacturerDesc.hashCode();
    result = 31 * result + modelDesc.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return String.format(
        "ProfileDescription (desc: %s, id: %s,  manufacturer: %s (%s), model: %s (%s), technology: %s, attributes: %s)",
        description, id, manufacturerDesc, deviceManufacturer, modelDesc, deviceModel, technology,
        attributes);
  }
}
