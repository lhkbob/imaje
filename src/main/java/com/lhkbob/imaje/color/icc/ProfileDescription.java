package com.lhkbob.imaje.color.icc;

/**
 *
 */
public final class ProfileDescription {
  private final DeviceAttributes attributes;
  private final Signature deviceManufacturer;
  private final Signature deviceModel;
  private final LocalizedString manufacturerDesc;
  private final LocalizedString modelDesc;
  private final DeviceTechnology technology;

  public ProfileDescription(
      Signature manufacturer, Signature model, DeviceAttributes attributes,
      DeviceTechnology technology, LocalizedString manufacturerDesc, LocalizedString modelDesc) {
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
    return p.deviceManufacturer.equals(deviceManufacturer) && p.deviceModel.equals(deviceModel)
        && p.attributes.equals(attributes) && p.technology.equals(technology) && p.manufacturerDesc
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
        "ProfileDescription (manufacturer: %s (%s), model: %s (%s), technology: %s, attributes: %s)",
        manufacturerDesc, deviceManufacturer, modelDesc, deviceModel, technology, attributes);
  }
}
