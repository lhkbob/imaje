package com.lhkbob.imaje.color.icc;

import com.lhkbob.imaje.color.icc.transforms.ColorMatrix;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public final class Profile {
  public static class Builder {
    private final ProfileClass profileClass;
    private final long version;
    private ColorSpace aSide;
    private ColorSpace bSide;
    private ZonedDateTime calibrationTime;
    private String charTarget;
    private ColorMatrix chromaticAdaptation;
    private Colorant chromaticity;
    private List<NamedColor> colorantInTable;
    private int[] colorantOrder;
    private List<NamedColor> colorantOutTable;
    private ColorimetricIntent colorimetricIntent;
    private LocalizedString copyright;
    private ZonedDateTime creationTime;
    private Signature creator;
    private ProfileDescription description;
    private long flags;
    private GenericColorValue illuminant;
    private GenericColorValue luminance;
    private Measurement measurement;
    private GenericColorValue mediaWhitePoint;
    private List<NamedColor> namedColors;
    private ResponseCurveSet outputResponse;
    private RenderingIntentGamut perceptualGamut;
    private Signature preferredCMMType;
    private PrimaryPlatform primaryPlatform;
    private List<ProfileDescription> profileSequence;
    private RenderingIntent renderingIntent;
    private RenderingIntentGamut saturationGamut;
    private ViewingCondition viewingCondition;

    private Builder(ProfileClass profileClass, long version) {
      this.profileClass = profileClass;
      this.version = version;
    }

    public Profile build() {
      completeDefaults();
      validate();
      return new Profile(aSide, bSide, calibrationTime, charTarget, chromaticAdaptation,
          chromaticity, Collections.unmodifiableList(new ArrayList<>(colorantInTable)),
          Arrays.copyOf(colorantOrder, colorantOrder.length),
          Collections.unmodifiableList(new ArrayList<>(colorantOutTable)), colorimetricIntent,
          copyright, creationTime, creator, description, flags, illuminant, luminance, measurement,
          mediaWhitePoint, Collections.unmodifiableList(new ArrayList<>(namedColors)),
          outputResponse, perceptualGamut, preferredCMMType, primaryPlatform, profileClass,
          Collections.unmodifiableList(new ArrayList<>(profileSequence)), renderingIntent,
          saturationGamut, version, viewingCondition);
    }

    public Builder setASideColorSpace(ColorSpace aSide) {
      this.aSide = aSide;
      return this;
    }

    public Builder setBSideColorSpace(ColorSpace bSide) {
      this.bSide = bSide;
      return this;
    }

    public Builder setCalibrationDate(ZonedDateTime time) {
      calibrationTime = time;
      return this;
    }

    public Builder setCharTarget(String target) {
      charTarget = target;
      return this;
    }

    public Builder setChromaticAdaptation(ColorMatrix matrix) {
      chromaticAdaptation = matrix;
      return this;
    }

    public Builder setColorantChromaticities(Colorant colorant) {
      chromaticity = colorant;
      return this;
    }

    public Builder setColorantInputTable(List<NamedColor> in) {
      colorantInTable = in;
      return this;
    }

    public Builder setColorantOrder(int[] colorantOrder) {
      this.colorantOrder = colorantOrder;
      return this;
    }

    public Builder setColorantOutputTable(List<NamedColor> out) {
      colorantOutTable = out;
      return this;
    }

    public Builder setColorimetricIntent(ColorimetricIntent intent) {
      colorimetricIntent = intent;
      return this;
    }

    public Builder setCopyright(LocalizedString copyright) {
      this.copyright = copyright;
      return this;
    }

    public Builder setCreationDate(ZonedDateTime time) {
      creationTime = time;
      return this;
    }

    public Builder setCreator(Signature creator) {
      this.creator = creator;
      return this;
    }

    public Builder setDescription(ProfileDescription desc) {
      description = desc;
      return this;
    }

    public Builder setFlags(long flags) {
      this.flags = flags;
      return this;
    }

    public Builder setIlluminant(GenericColorValue illuminant) {
      this.illuminant = illuminant;
      return this;
    }

    public Builder setLuminance(GenericColorValue luminance) {
      this.luminance = luminance;
      return this;
    }

    public Builder setMeasurement(Measurement measurement) {
      this.measurement = measurement;
      return this;
    }

    public Builder setMediaWhitePoint(GenericColorValue whitepoint) {
      mediaWhitePoint = whitepoint;
      return this;
    }

    public Builder setNamedColors(List<NamedColor> colors) {
      namedColors = colors;
      return this;
    }

    public Builder setOutputResponse(ResponseCurveSet output) {
      outputResponse = output;
      return this;
    }

    public Builder setPerceptualRenderingIntentGamut(RenderingIntentGamut gamut) {
      perceptualGamut = gamut;
      return this;
    }

    public Builder setPreferredCMMType(Signature signature) {
      preferredCMMType = signature;
      return this;
    }

    public Builder setPrimaryPlatform(PrimaryPlatform platform) {
      primaryPlatform = platform;
      return this;
    }

    public Builder setProfileSequenceDescriptions(List<ProfileDescription> sequence) {
      profileSequence = sequence;
      return this;
    }

    public Builder setRenderingIntent(RenderingIntent intent) {
      renderingIntent = intent;
      return this;
    }

    public Builder setSaturationRenderingIntentGamut(RenderingIntentGamut gamut) {
      saturationGamut = gamut;
      return this;
    }

    public Builder setViewingCondition(ViewingCondition view) {
      viewingCondition = view;
      return this;
    }

    private void completeDefaults() {
      if (aSide == null) {
        aSide = ColorSpace.RGB;
      }
      if (bSide == null) {
        bSide = ColorSpace.CIEXYZ;
      }
      // FIXME validate bSide depending on profile class

      if (creationTime == null) {
        creationTime = ZonedDateTime.now();
      }
      if (calibrationTime == null) {
        calibrationTime = creationTime;
      }
      if (charTarget == null) {
        charTarget = "";
      }
      if (chromaticAdaptation == null) {
        chromaticAdaptation = ColorMatrix.IDENTITY_3X3;
      }
      // FIXME can we build the colorant table from the chromaticity list if its present?
      if (colorantInTable == null) {
        colorantInTable = new ArrayList<>();
      }
      if (colorantOutTable == null) {
        colorantOutTable = new ArrayList<>();
      }
      if (colorantOrder == null) {
        colorantOrder = new int[colorantInTable.size()];
        for (int i = 0; i < colorantOrder.length; i++) {
          colorantOrder[i] = i;
        }
      }
      if (colorimetricIntent == null) {
        colorimetricIntent = ColorimetricIntent.PICTURE_REFERRED;
      }
      // Technically required by the ICC but it doesn't affect functionality if it's missing
      if (copyright == null) {
        copyright = new LocalizedString();
      }
      if (creator == null) {
        creator = Signature.NULL;
      }
      if (description == null) {
        description = new ProfileDescription();
      }
      // FIXME what are good defaults for illuminant and luminance? should they stay null?
      if (namedColors == null) {
        namedColors = new ArrayList<>();
      }
      if (perceptualGamut == null) {
        perceptualGamut = RenderingIntentGamut.UNDEFINED;
      }
      if (saturationGamut == null) {
        saturationGamut = RenderingIntentGamut.UNDEFINED;
      }
      if (preferredCMMType == null) {
        preferredCMMType = Signature.NULL;
      }
      if (profileSequence == null) {
        profileSequence = new ArrayList<>();
      }
      if (renderingIntent == null) {
        renderingIntent = RenderingIntent.PERCEPTUAL;
      }

      // FIXME add profile class specific validation rules
    }

    private void validate() {
      if (colorantInTable.size() != 0 && colorantInTable.size() != aSide.getChannelCount()) {
        throw new IllegalStateException(
            "Colorant input table does not provide A-side channel count colorants");
      }
      // FIXME does this have to match b side or a side?
      if (colorantOutTable.size() != 0 && colorantOutTable.size() != bSide.getChannelCount()) {
        throw new IllegalStateException(
            "Colorant output table does not provide B-side channel count colorants");
      }
      if (colorantOrder.length != colorantInTable.size()) {
        throw new IllegalStateException(
            "Colorant order length does not equal size of colorant input table");
      }
      if (chromaticAdaptation.getInputChannels() != 3
          && chromaticAdaptation.getOutputChannels() != 3) {
        throw new IllegalStateException("Chromatic adaptation matrix must be a 3x3 matrix");
      }
      if (chromaticity != null && chromaticity.getChannelCount() != aSide.getChannelCount()) {
        throw new IllegalStateException(
            "Colorant chromaticities are not provided for A-side channel count colorants");
      }
      if (mediaWhitePoint == null) {
        throw new IllegalStateException("Media white point must be provided");
      }
      // FIXME does this have to match b side or a side?
      if (outputResponse != null && outputResponse.getChannelCount() != bSide.getChannelCount()) {
        throw new IllegalStateException(
            "Output response must have channel count equal to B-sides channel count");
      }
      if (primaryPlatform == null) {
        throw new IllegalStateException("Primary platform cannot be null");
      }
      // FIXME do we need to evaluate the color type for the illuminant, media white point, and luminance?
    }
  }

  private final ColorSpace aSide;
  private final ColorSpace bSide;
  private final ZonedDateTime calibrationTime;
  private final String charTarget;
  private final ColorMatrix chromaticAdaptation;
  private final Colorant chromaticity;
  private final List<NamedColor> colorantInTable;
  private final int[] colorantOrder;
  private final List<NamedColor> colorantOutTable;
  private final ColorimetricIntent colorimetricIntent;
  private final LocalizedString copyright;
  private final ZonedDateTime creationTime;
  private final Signature creator;
  private final ProfileDescription description;
  private final long flags;
  //  private final ColorTransform gamut; // FIXME should this transform be handled separately from everything else?
  private final GenericColorValue illuminant;
  private final GenericColorValue luminance;
  private final Measurement measurement;
  private final GenericColorValue mediaWhitePoint;
  private final List<NamedColor> namedColors;
  private final ResponseCurveSet outputResponse;
  private final RenderingIntentGamut perceptualGamut;
  private final Signature preferredCMMType;
  private final PrimaryPlatform primaryPlatform;
  private final ProfileClass profileClass;
  private final List<ProfileDescription> profileSequence;
  private final RenderingIntent renderingIntent;
  private final RenderingIntentGamut saturationGamut;
  private final long version;
  private final ViewingCondition viewingCondition;

  private Profile(
      ColorSpace aSide, ColorSpace bSide, ZonedDateTime calibrationTime, String charTarget,
      ColorMatrix chromaticAdaptation, Colorant chromaticity, List<NamedColor> colorantInTable,
      int[] colorantOrder, List<NamedColor> colorantOutTable, ColorimetricIntent colorimetricIntent,
      LocalizedString copyright, ZonedDateTime creationTime, Signature creator,
      ProfileDescription description, long flags, GenericColorValue illuminant,
      GenericColorValue luminance, Measurement measurement, GenericColorValue mediaWhitePoint,
      List<NamedColor> namedColors, ResponseCurveSet outputResponse,
      RenderingIntentGamut perceptualGamut, Signature preferredCMMType,
      PrimaryPlatform primaryPlatform, ProfileClass profileClass,
      List<ProfileDescription> profileSequence, RenderingIntent renderingIntent,
      RenderingIntentGamut saturationGamut, long version, ViewingCondition viewingCondition) {
    this.aSide = aSide;
    this.bSide = bSide;
    this.calibrationTime = calibrationTime;
    this.charTarget = charTarget;
    this.chromaticAdaptation = chromaticAdaptation;
    this.chromaticity = chromaticity;
    this.colorantInTable = colorantInTable;
    this.colorantOrder = colorantOrder;
    this.colorantOutTable = colorantOutTable;
    this.colorimetricIntent = colorimetricIntent;
    this.copyright = copyright;
    this.creationTime = creationTime;
    this.creator = creator;
    this.description = description;
    this.flags = flags;
    this.illuminant = illuminant;
    this.luminance = luminance;
    this.measurement = measurement;
    this.mediaWhitePoint = mediaWhitePoint;
    this.namedColors = namedColors;
    this.outputResponse = outputResponse;
    this.perceptualGamut = perceptualGamut;
    this.preferredCMMType = preferredCMMType;
    this.primaryPlatform = primaryPlatform;
    this.profileClass = profileClass;
    this.profileSequence = profileSequence;
    this.renderingIntent = renderingIntent;
    this.saturationGamut = saturationGamut;
    this.version = version;
    this.viewingCondition = viewingCondition;
  }

  public static Builder newProfile(ProfileClass profileClass, long version) {
    return new Builder(profileClass, version);
  }

  public ZonedDateTime getCalibrationDate() {
    return calibrationTime;
  }

  public String getCharTarget() {
    return charTarget;
  }

  public ColorMatrix getChromaticAdaptation() {
    return chromaticAdaptation;
  }

  public Colorant getChromaticity() {
    return chromaticity;
  }

  public List<NamedColor> getColorantInTable() {
    return colorantInTable;
  }

  public int[] getColorantOrder() {
    return Arrays.copyOf(colorantOrder, colorantOrder.length);
  }

  public List<NamedColor> getColorantOutTable() {
    return colorantOutTable;
  }

  public ColorimetricIntent getColorimetricIntent() {
    return colorimetricIntent;
  }

  public LocalizedString getCopyright() {
    return copyright;
  }

  public GenericColorValue getLuminance() {
    return luminance;
  }

  public Measurement getMeasurement() {
    return measurement;
  }

  public GenericColorValue getMediaWhitePoint() {
    return mediaWhitePoint;
  }

  public List<NamedColor> getNamedColors() {
    return namedColors;
  }

  public ResponseCurveSet getOutputResponse() {
    return outputResponse;
  }

  public RenderingIntentGamut getPerceptualRenderingIntentGamut() {
    return perceptualGamut;
  }

  public List<ProfileDescription> getProfileSequence() {
    return profileSequence;
  }

  public RenderingIntentGamut getSaturationRenderingIntentGamut() {
    return saturationGamut;
  }

  public ViewingCondition getViewingCondition() {
    return viewingCondition;
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

  public GenericColorValue getIlluminant() {
    return illuminant;
  }

  public int getMajorVersion() {
    return (int) ((version >> 24) & 0xff);
  }

  public int getMinorVersion() {
    return (int) ((version >> 20) & 0xf);
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

  public RenderingIntent getRenderingIntent() {
    return renderingIntent;
  }

  public String getVersion() {
    return String.format("%d.%d.%d.0", getMajorVersion(), getMinorVersion(), getBugFixVersion());
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
