package com.lhkbob.imaje.color.icc;

import java.time.ZonedDateTime;

/**
 *
 */
public final class Profile {
  private final ColorSpace aSide;
  private final ColorSpace bSide;
  private final ZonedDateTime creationTime;
  private final Signature creator;
  private final ProfileDescription description;
  private final long flags;
  private final ProfileID id;
  private final GenericColorValue illuminant;
  private final long preferredCMMType;
  private final PrimaryPlatform primaryPlatform;
  private final ProfileClass profileClass;
  private final RenderingIntent renderingIntent;
  private final Signature signature;
  private final long version;


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Profile)) {
      return false;
    }

    Profile profile = (Profile) o;
    return preferredCMMType == profile.preferredCMMType && version == profile.version
        && flags == profile.flags && profileClass == profile.profileClass && aSide == profile.aSide
        && bSide == profile.bSide && creationTime.equals(profile.creationTime) && signature
        .equals(profile.signature) && primaryPlatform == profile.primaryPlatform && description
        .equals(profile.description) && renderingIntent == profile.renderingIntent && illuminant
        .equals(profile.illuminant) && creator.equals(profile.creator) && id.equals(profile.id);
  }

  public ColorSpace getASideColorSpace() {
    return aSide;
  }

  public ColorSpace getBSideColorSpace() {
    return bSide;
  }

  public int getBugFixVersion() {
    return (int) ((version >> 16) & 0xf);
  }

  public ZonedDateTime getCreationDate() {
    return creationTime;
  }

  public Signature getCreator() {
    return creator;
  }

  public ProfileDescription getDescription() {
    return description;
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

  public RenderingIntent getRenderingIntent() {
    return renderingIntent;
  }

  public Signature getSignature() {
    return signature;
  }

  public String getVersion() {
    return String.format("%d.%d.%d.0", getMajorVersion(), getMinorVersion(), getBugFixVersion());
  }

  @Override
  public int hashCode() {
    int result = Long.hashCode(preferredCMMType);
    result = 31 * result + Long.hashCode(version);
    result = 31 * result + profileClass.hashCode();
    result = 31 * result + aSide.hashCode();
    result = 31 * result + bSide.hashCode();
    result = 31 * result + creationTime.hashCode();
    result = 31 * result + signature.hashCode();
    result = 31 * result + primaryPlatform.hashCode();
    result = 31 * result + Long.hashCode(flags);
    result = 31 * result + description.hashCode();
    result = 31 * result + renderingIntent.hashCode();
    result = 31 * result + illuminant.hashCode();
    result = 31 * result + creator.hashCode();
    result = 31 * result + id.hashCode();
    return result;
  }

  public boolean isEmbedded() {
    return (flags & EMBEDDED_FLAG_MASK) != 0;
  }

  public boolean isIndependentOfEmbedding() {
    return (flags & INDEPENDENT_FLAG_MASK) != 0;
  }

  private static final long EMBEDDED_FLAG_MASK = 1;
  private static final long INDEPENDENT_FLAG_MASK = 2;
}
