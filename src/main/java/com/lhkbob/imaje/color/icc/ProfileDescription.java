/*
 * BSD 3-Clause License - imaJe
 *
 * Copyright (c) 2016, Michael Ludwig
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.lhkbob.imaje.color.icc;

import com.lhkbob.imaje.util.Arguments;

import java.util.Objects;

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
    Arguments.notNull("id", id);
    Arguments.notNull("description", description);
    Arguments.notNull("manufacturer", manufacturer);
    Arguments.notNull("model", model);
    Arguments.notNull("attributes", attributes);
    Arguments.notNull("manufacturerDesc", manufacturerDesc);
    Arguments.notNull("modelDesc", modelDesc);

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
    return Objects.equals(p.id, id) && Objects.equals(p.description, description) && Objects
        .equals(p.deviceManufacturer, deviceManufacturer) && Objects.equals(p.deviceModel, deviceModel) && Objects
        .equals(p.attributes, attributes) && Objects.equals(p.technology, technology) && Objects
        .equals(p.manufacturerDesc, manufacturerDesc) && Objects.equals(p.modelDesc, modelDesc);
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
