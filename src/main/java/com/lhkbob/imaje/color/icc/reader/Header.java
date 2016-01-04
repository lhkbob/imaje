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
  private final long preferredCMMType;
  private final PrimaryPlatform primaryPlatform;
  private final ProfileClass profileClass;
  private final RenderingIntent renderingIntent;
  private final Signature signature;
  private final long size;
  private final long version;

  private Header(
      ColorSpace aSide, ColorSpace bSide, ZonedDateTime creationTime, Signature creator,
      Signature manufacturer, Signature model, DeviceAttributes attributes, long flags,
      ProfileID id, GenericColorValue illuminant, long preferredCMMType,
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

    long size = nextUInt32Number(data);
    long cmmType = nextUInt32Number(data);
    long version = nextUInt32Number(data);
    ProfileClass profileClass = ProfileClass.fromSignature(nextSignature(data));
    ColorSpace aSide = ColorSpace.fromSignature(nextSignature(data));
    ColorSpace bSide = ColorSpace.fromSignature(nextSignature(data));
    ZonedDateTime creation = nextDateTimeNumber(data);
    Signature signature = nextSignature(data);
    PrimaryPlatform platform = PrimaryPlatform.fromSignature(nextSignature(data));
    long flags = nextUInt32Number(data);
    Signature manufacturer = nextSignature(data);
    Signature model = nextSignature(data);
    DeviceAttributes attrs = new DeviceAttributes(nextUInt64Number(data));
    RenderingIntent intent = RenderingIntent.values()[Math.toIntExact(nextUInt32Number(data))];
    GenericColorValue illuminant = nextXYZNumber(
        data, GenericColorValue.ColorType.NORMALIZED_CIEXYZ);
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

  public int getMajorVersion() {
    return (int) ((version >> 24) & 0xff);
  }

  public int getMinorVersion() {
    return (int) ((version >> 20) & 0xf);
  }

  public long getPreferredCMMType() {
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

  public long getRawVersion() {
    return version;
  }

  public int getBugFixVersion() {
    return (int) ((version >> 16) & 0xf);
  }


  public String getVersion() {
    return String.format("%d.%d.%d.0", getMajorVersion(), getMinorVersion(), getBugFixVersion());
  }
}
