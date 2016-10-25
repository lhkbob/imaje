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
package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.ColorSpace;
import com.lhkbob.imaje.color.icc.DeviceAttributes;
import com.lhkbob.imaje.color.icc.GenericColorValue;
import com.lhkbob.imaje.color.icc.PrimaryPlatform;
import com.lhkbob.imaje.color.icc.ProfileClass;
import com.lhkbob.imaje.color.icc.ProfileID;
import com.lhkbob.imaje.color.icc.RenderingIntent;
import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;
import java.time.ZonedDateTime;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextDateTimeNumber;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextSignature;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt32Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt64Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextXYZNumber;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.skip;

/**
 *
 */
public final class Header {
  public static final Signature PROFILE_SIGNATURE = Signature.fromName("acsp");

  private final ColorSpace aSide;
  private final DeviceAttributes attributes;
  private final ColorSpace bSide;
  private final ZonedDateTime creationTime;
  private final Signature creator;
  private final long flags;
  private final ProfileID id;
  private final GenericColorValue illuminant;
  private final Signature manufacturer;
  private final Signature model;
  private final Signature preferredCMMType;
  private final PrimaryPlatform primaryPlatform;
  private final ProfileClass profileClass;
  private final RenderingIntent renderingIntent;
  private final Signature signature;
  private final long size;
  private final long version;

  private Header(
      ColorSpace aSide, ColorSpace bSide, ZonedDateTime creationTime, Signature creator,
      Signature manufacturer, Signature model, DeviceAttributes attributes, long flags,
      ProfileID id, GenericColorValue illuminant, Signature preferredCMMType,
      PrimaryPlatform primaryPlatform, ProfileClass profileClass, RenderingIntent renderingIntent,
      Signature signature, long size, long version) {
    this.aSide = aSide;
    this.bSide = bSide;
    this.size = size;
    this.creationTime = creationTime;
    this.creator = creator;
    this.manufacturer = manufacturer;
    this.model = model;
    this.attributes = attributes;
    this.flags = flags;
    this.id = id;
    this.illuminant = illuminant;
    this.preferredCMMType = preferredCMMType;
    this.primaryPlatform = primaryPlatform;
    this.profileClass = profileClass;
    this.renderingIntent = renderingIntent;
    this.signature = signature;
    this.version = version;
  }

  public static Header fromBytes(ByteBuffer data) {
    if (data.remaining() != 128) {
      throw new IllegalStateException("Expected 128 bytes for header, not: " + data.remaining());
    }

    // Before parsing the header completely, spot check the signature and fail if it's not the expected.
    // This will help detect bad data before some index out of bounds or enum not found, etc. obfuscates
    // the source of the error.
    int dataStart = data.position();
    data.limit(dataStart + 40).position(dataStart + 36);
    Signature signature = nextSignature(data);
    if (!PROFILE_SIGNATURE.equals(signature)) {
      throw new IllegalArgumentException(
          "Header does not contain expected signature ('acsp') at required byte position: "
              + signature);
    }
    // Signature is valid, assume header is accurate and resume parsing
    data.limit(dataStart + 128).position(dataStart);

    long size = nextUInt32Number(data);
    Signature cmmType = nextSignature(data);
    long version = nextUInt32Number(data);
    ProfileClass profileClass = ProfileClass.fromSignature(nextSignature(data));
    ColorSpace aSide = ColorSpace.fromSignature(nextSignature(data));
    ColorSpace bSide = ColorSpace.fromSignature(nextSignature(data));
    ZonedDateTime creation = nextDateTimeNumber(data);
    skip(data, 4); // This is the signature field, but it's already been read
    PrimaryPlatform platform = PrimaryPlatform.fromSignature(nextSignature(data));
    long flags = nextUInt32Number(data);
    Signature manufacturer = nextSignature(data);
    Signature model = nextSignature(data);
    DeviceAttributes attrs = new DeviceAttributes(nextUInt64Number(data));
    RenderingIntent intent = RenderingIntent.values()[Math.toIntExact(nextUInt32Number(data))];
    GenericColorValue illuminant = nextXYZNumber(data, GenericColorValue.ColorType.PCSXYZ);
    Signature creator = nextSignature(data);

    byte[] idBits = new byte[16];
    data.get(idBits);
    ProfileID id = new ProfileID(idBits);

    skip(data, 28); // Skip empty header content
    return new Header(aSide, bSide, creation, creator, manufacturer, model, attrs, flags, id,
        illuminant, cmmType, platform, profileClass, intent, signature, size, version);
  }

  public ColorSpace getASideColorSpace() {
    return aSide;
  }

  public DeviceAttributes getAttributes() {
    return attributes;
  }

  public ColorSpace getBSideColorSpace() {
    return bSide;
  }

  public ZonedDateTime getCreationDate() {
    return creationTime;
  }

  public Signature getCreator() {
    return creator;
  }

  public long getFlags() {
    return flags;
  }

  public ProfileID getID() {
    return id;
  }

  public GenericColorValue getIlluminant() {
    return illuminant;
  }

  public Signature getManufacturer() {
    return manufacturer;
  }

  public Signature getModel() {
    return model;
  }

  public Signature getPreferredCMMType() {
    return preferredCMMType;
  }

  public PrimaryPlatform getPrimaryPlatform() {
    return primaryPlatform;
  }

  public ProfileClass getProfileClass() {
    return profileClass;
  }

  public long getProfileSize() {
    return size;
  }

  public RenderingIntent getRenderingIntent() {
    return renderingIntent;
  }

  public Signature getSignature() {
    return signature;
  }

  public long getVersion() {
    return version;
  }
}
