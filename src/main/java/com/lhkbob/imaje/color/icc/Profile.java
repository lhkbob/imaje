package com.lhkbob.imaje.color.icc;

import com.lhkbob.imaje.color.icc.reader.ProfileReader;
import com.lhkbob.imaje.color.transform.general.Matrix;
import com.lhkbob.imaje.color.transform.general.Transform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private Matrix chromaticAdaptation;
    private Colorant chromaticity;
    private List<NamedColor> colorantInTable;
    private List<NamedColor> colorantOutTable;
    private ColorimetricIntent colorimetricIntent;
    private LocalizedString copyright;
    private ZonedDateTime creationTime;
    private Signature creator;
    private ProfileDescription description;
    private long flags;
    private GenericColorValue illuminant;
    private Double luminance;
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

    private final Map<RenderingIntent, Transform> transforms;
    private final Map<RenderingIntent, Transform> inverseTransforms;
    private Transform defaultTransform;
    private Transform gamutTest;

    private Builder(ProfileClass profileClass, long version) {
      this.profileClass = profileClass;
      this.version = version;
      transforms = new HashMap<>();
      inverseTransforms = new HashMap<>();
    }

    public Profile build() {
      completeDefaults();
      validate();
      return new Profile(aSide, bSide, calibrationTime, charTarget, chromaticAdaptation,
          chromaticity, Collections.unmodifiableList(new ArrayList<>(colorantInTable)),
          Collections.unmodifiableList(new ArrayList<>(colorantOutTable)), colorimetricIntent,
          copyright, creationTime, creator, description, flags, gamutTest, illuminant, luminance,
          measurement, mediaWhitePoint, Collections.unmodifiableList(new ArrayList<>(namedColors)),
          outputResponse, perceptualGamut, preferredCMMType, primaryPlatform, profileClass,
          Collections.unmodifiableList(new ArrayList<>(profileSequence)), renderingIntent,
          saturationGamut, version, viewingCondition,
          Collections.unmodifiableMap(new HashMap<>(transforms)),
          Collections.unmodifiableMap(new HashMap<>(inverseTransforms)));
    }

    public Builder setDefaultTransform(Transform transform) {
      defaultTransform = transform;
      return this;
    }

    public Builder setGamutTest(Transform gamut) {
      gamutTest = gamut;
      return this;
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

    public Builder setChromaticAdaptation(Matrix matrix) {
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

    public Builder setLuminance(double luminance) {
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

    public Builder setTransform(RenderingIntent intent, Transform transform) {
      if (transform != null) {
        transforms.put(intent, transform);
      } else {
        transforms.remove(intent);
      }
      return this;
    }

    public Builder setInverseTransform(RenderingIntent intent, Transform transform) {
      if (transform != null) {
        inverseTransforms.put(intent, transform);
      } else {
        inverseTransforms.remove(intent);
      }
      return this;
    }

    private void completeDefaults() {
      if (aSide == null) {
        aSide = ColorSpace.RGB;
      }
      if (bSide == null) {
        bSide = ColorSpace.CIEXYZ;
      }

      if (creationTime == null) {
        creationTime = ZonedDateTime.now();
      }
      if (calibrationTime == null) {
        calibrationTime = creationTime;
      }
      if (chromaticAdaptation == null) {
        chromaticAdaptation = Matrix.IDENTITY_3X3;
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

      if (illuminant == null) {
        illuminant = GenericColorValue.pcsXYZ(0.9642, 1.0, 0.8249);
      }
      if (luminance == null) {
        if (viewingCondition != null) {
          luminance = viewingCondition.getIlluminant().getChannel(1);
        } else {
          luminance = 100.0;
        }
      }

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

      // Complete missing transforms that are related to one-another or have defaults

      // 1. First if an explicit inverse was given, but no normal mode was given, use that
      inverseTransforms.keySet().stream().filter(intent -> !transforms.containsKey(intent))
          .forEach(intent -> {
            Transform forward = inverseTransforms.get(intent).inverted();
            if (forward != null) {
              transforms.put(intent, forward);
            }
          });

      // 2. Calculate media relative colorimetric intent from ICC absolute.
      //  - Use this to get the media transform if an explicit absolute transform was provided
      //    before falling back to the default matrix/trc function
      if (transforms.containsKey(RenderingIntent.ICC_ABSOLUTE_COLORIMETRIC) && !transforms
          .containsKey(RenderingIntent.MEDIA_RELATIVE_COLORIMETRIC)) {
        // FIXME implement inverse
      }

      // 3. Fall back to the default tag for normal mode on anything that wasn't defined implicitly
      // in the two steps above; but not the ICC_ABSOLUTE intent (see below)
      if (defaultTransform != null) {
        if (!transforms.containsKey(RenderingIntent.PERCEPTUAL)) {
          transforms.put(RenderingIntent.PERCEPTUAL, defaultTransform);
        }
        if (!transforms.containsKey(RenderingIntent.SATURATION)) {
          transforms.put(RenderingIntent.SATURATION, defaultTransform);
        }
        if (!transforms.containsKey(RenderingIntent.MEDIA_RELATIVE_COLORIMETRIC)) {
          transforms.put(RenderingIntent.MEDIA_RELATIVE_COLORIMETRIC, defaultTransform);
        }
      }

      // 4. Calculate absolute colorimetric intent from media intent, which likely exists at this point
      // from being explicitly given, having a default, or from being inverted.
      if (transforms.containsKey(RenderingIntent.MEDIA_RELATIVE_COLORIMETRIC) && !transforms
          .containsKey(RenderingIntent.ICC_ABSOLUTE_COLORIMETRIC)) {
        // FIXME implement this
      }

      // 5. Calculate inverses for any provided transforms that were not also provided with explicit inverses
      // (In most cases this is the inverse of the TRC/matrix default)
      transforms.keySet().stream().filter(intent -> !inverseTransforms.containsKey(intent))
          .forEach(intent -> {
            Transform inverse = transforms.get(intent).inverted();
            if (inverse != null) {
              inverseTransforms.put(intent, inverse);
            }
          });

      // After transforms have been completed, build up the colorant tables if they have not already
      // been specified.
      if (colorantInTable == null) {
        // Calculate colorants based on the a-side space
        if (profileClass != ProfileClass.DEVICE_LINK_PROFILE) {
          // Use the media relative transformation if it exists, otherwise don't calculate pcs values
          Transform aToB = transforms.get(RenderingIntent.MEDIA_RELATIVE_COLORIMETRIC);
          colorantInTable = generateColorantTable(aToB, aSide, bSide);
        } else {
          // No transformation exists and pcs space must be LAB (additionally bSide does not
          // necessarily encode a legal pcs space)
          colorantInTable = generateColorantTable(null, aSide, ColorSpace.CIELAB);
        }
      }

      if (colorantOutTable == null) {
        // Calculate colorants based on the b-side space
        if (profileClass != ProfileClass.DEVICE_LINK_PROFILE) {
          // Generate a table that maps "device" (aka normalized pcs values) to the actual
          // channel ranges (hence the inverse of the normalizing function)
          colorantOutTable = generateColorantTable(
              bSide.getNormalizingFunction().inverted(), bSide, bSide);
        } else {
          // No transformation exists, and device link's must have pcs values in LAB
          colorantOutTable = generateColorantTable(null, bSide, ColorSpace.CIELAB);
        }
      }

      // FIXME is it possible to calculate the colorant chromaticities?
    }

    private List<NamedColor> generateColorantTable(
        Transform deviceToPCS, ColorSpace deviceSpace, ColorSpace pcsSpace) {
      // Skip table creation if the function is badly dimensioned (this is done so that
      // validate() can fail with a more useful error message about the particular transform)
      if (deviceToPCS != null && deviceToPCS.getInputChannels() != deviceSpace.getChannelCount()
          && deviceToPCS.getOutputChannels() != pcsSpace.getChannelCount()) {
        return Collections.emptyList();
      }

      List<NamedColor> table = new ArrayList<>();
      double[] pcs = new double[pcsSpace.getChannelCount()];
      for (int i = 0; i < deviceSpace.getChannelCount(); i++) {
        // Allocate this anew each time since the genericColor static function assumes the array is
        // safe to take ownership of
        double[] device = new double[aSide.getChannelCount()];
        device[i] = 1.0;

        if (deviceToPCS != null) {
          deviceToPCS.transform(device, pcs);
        }

        GenericColorValue pcsColor = new GenericColorValue(
            bSide == ColorSpace.CIEXYZ ? GenericColorValue.ColorType.PCSXYZ
                : GenericColorValue.ColorType.PCSLAB, pcs);

        String name = (deviceSpace.hasChannelNames() ? deviceSpace.getChannelName(i)
            : "Channel " + (i + 1));
        table.add(new NamedColor(name, pcsColor, GenericColorValue.genericColor(device)));
      }

      return table;
    }

    private void validate() {
      // b-side transformation must be XYZ or LAB for non-device link profiles
      if (profileClass != ProfileClass.DEVICE_LINK_PROFILE && bSide != ColorSpace.CIEXYZ
          && bSide != ColorSpace.CIELAB) {
        throw new IllegalStateException(
            "Non-device link profile must have XYZ or LAB b-side color space, not: " + bSide);
      }
      // abstract profiles need XYZ or LAB on a and b sides, but if we got here the b-side is already valid
      if (profileClass == ProfileClass.ABSTRACT_PROFILE && aSide != ColorSpace.CIEXYZ
          && aSide != ColorSpace.CIELAB) {
        throw new IllegalStateException(
            "Abstract profiles must have XYZ or LAB a-side color space, not: " + aSide);
      }
      // at least one transformation must be provided for non-named color profiles (named color profiles have no required transforms)
      if (profileClass != ProfileClass.NAMED_COLOR_PROFILE && transforms.isEmpty()) {
        throw new IllegalStateException("Must provide at least one transformation for profile");
      }
      // Must provide some named colors if it's a named color profile
      if (profileClass == ProfileClass.NAMED_COLOR_PROFILE && namedColors.isEmpty()) {
        throw new IllegalStateException("Named color profile defines no named colors");
      }

      // validate that all provided transformations match expected input/output channel counts for a and b side spaces
      transforms.values().stream().forEach(transform -> {
        if (transform.getInputChannels() != aSide.getChannelCount()
            || transform.getOutputChannels() != bSide.getChannelCount()) {
          throw new IllegalStateException(
              "Input/output channel counts for A to B transform do not match up with channels for a and b-side color spaces");
        }
      });
      inverseTransforms.values().stream().forEach(transform -> {
        if (transform.getInputChannels() != bSide.getChannelCount()
            || transform.getOutputChannels() != aSide.getChannelCount()) {
          throw new IllegalStateException(
              "Input/output channel counts for B to A transform do not match up with channels for a and b-side color spaces");
        }
      });

      // The colorant-in table must have a-side's channel count colorants
      if (colorantInTable.size() != aSide.getChannelCount()) {
        throw new IllegalStateException(
            "Colorants not specified for generic color space on a-side");
      }
      // The colorant output table must have b-side's channel count colorants
      if (colorantOutTable.size() != bSide.getChannelCount()) {
        throw new IllegalStateException(
            "Colorants not specified for generic color space on b-side");
      }

      // Validate various color tables
      validateNamedColors(colorantInTable, aSide);
      validateNamedColors(colorantOutTable, bSide);
      validateNamedColors(namedColors, aSide);

      if (chromaticity != null && chromaticity.getChannelCount() != aSide.getChannelCount()) {
        throw new IllegalStateException(
            "Colorant chromaticities are not provided for A-side channel count colorants");
      }

      if (outputResponse != null && outputResponse.getChannelCount() != aSide.getChannelCount()) {
        throw new IllegalStateException(
            "Output response must have channel count equal to A-sides channel count");
      }
      if (primaryPlatform == null) {
        throw new IllegalStateException("Primary platform cannot be null");
      }

      if (chromaticAdaptation.getInputChannels() != 3
          && chromaticAdaptation.getOutputChannels() != 3) {
        throw new IllegalStateException("Chromatic adaptation matrix must be a 3x3 matrix");
      }
      if (illuminant.getType() != GenericColorValue.ColorType.PCSXYZ) {
        throw new IllegalStateException("Illuminant must be specified as PCSXYZ");
      }
      if (mediaWhitePoint == null) {
        throw new IllegalStateException("Media white point must be provided");
      }
      if (mediaWhitePoint.getType() != GenericColorValue.ColorType.NORMALIZED_CIEXYZ) {
        throw new IllegalStateException("Media white point must be normalized CIEXYZ");
      }

      if (gamutTest != null) {
        // The gamut test goes from device space to a single value (presumably), which determines
        // the boolean (0 = in gamut, non-zero = out of gamut) response
        if (gamutTest.getInputChannels() != aSide.getChannelCount()) {
          throw new IllegalStateException(
              "Gamut test must have input channel count equal to a-side color space's channel count");
        }
        if (gamutTest.getOutputChannels() != 1) {
          throw new IllegalStateException("Gamut test must output a single value");
        }
      }
    }

    private void validateNamedColors(List<NamedColor> colors, ColorSpace deviceSpace) {
      // Make sure device colors in the colorant tables are the right dimension, and the pcs values
      // are in the b-side space (non-device profiles) or LAB (device profiles)
      GenericColorValue.ColorType pcsType;
      if (profileClass != ProfileClass.DEVICE_LINK_PROFILE) {
        pcsType = bSide == ColorSpace.CIEXYZ ? GenericColorValue.ColorType.PCSXYZ
            : GenericColorValue.ColorType.PCSLAB;
      } else {
        pcsType = GenericColorValue.ColorType.PCSLAB;
      }

      for (NamedColor c : colors) {
        if (c.getDeviceColor().getChannelCount() != deviceSpace.getChannelCount()) {
          throw new IllegalStateException(
              "Device color in a-side colorant table does not have correct dimensionality for color space");
        }
        if (c.getPCSColor().getType() != pcsType) {
          throw new IllegalStateException("Named color's PCS value not in expected color space");
        }
      }
    }
  }

  private final ColorSpace aSide;
  private final ColorSpace bSide;
  private final ZonedDateTime calibrationTime;
  private final String charTarget;
  private final Matrix chromaticAdaptation;
  private final Colorant chromaticity;
  private final List<NamedColor> colorantInTable;
  private final List<NamedColor> colorantOutTable;
  private final ColorimetricIntent colorimetricIntent;
  private final LocalizedString copyright;
  private final ZonedDateTime creationTime;
  private final Signature creator;
  private final ProfileDescription description;
  private final long flags;
  private final Transform gamutTest;
  private final GenericColorValue illuminant;
  private final double luminance;
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

  private final Map<RenderingIntent, Transform> aToBTransforms;
  private final Map<RenderingIntent, Transform> bToATransforms;

  private Profile(
      ColorSpace aSide, ColorSpace bSide, ZonedDateTime calibrationTime, String charTarget,
      Matrix chromaticAdaptation, Colorant chromaticity, List<NamedColor> colorantInTable,
      List<NamedColor> colorantOutTable, ColorimetricIntent colorimetricIntent,
      LocalizedString copyright, ZonedDateTime creationTime, Signature creator,
      ProfileDescription description, long flags, Transform gamutTest,
      GenericColorValue illuminant, double luminance, Measurement measurement,
      GenericColorValue mediaWhitePoint, List<NamedColor> namedColors,
      ResponseCurveSet outputResponse, RenderingIntentGamut perceptualGamut,
      Signature preferredCMMType, PrimaryPlatform primaryPlatform, ProfileClass profileClass,
      List<ProfileDescription> profileSequence, RenderingIntent renderingIntent,
      RenderingIntentGamut saturationGamut, long version, ViewingCondition viewingCondition,
      Map<RenderingIntent, Transform> aToBTransforms,
      Map<RenderingIntent, Transform> bToATransforms) {
    this.aSide = aSide;
    this.bSide = bSide;
    this.calibrationTime = calibrationTime;
    this.charTarget = charTarget;
    this.chromaticAdaptation = chromaticAdaptation;
    this.chromaticity = chromaticity;
    this.colorantInTable = colorantInTable;
    this.colorantOutTable = colorantOutTable;
    this.colorimetricIntent = colorimetricIntent;
    this.copyright = copyright;
    this.creationTime = creationTime;
    this.creator = creator;
    this.description = description;
    this.flags = flags;
    this.gamutTest = gamutTest;
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
    this.aToBTransforms = aToBTransforms;
    this.bToATransforms = bToATransforms;
  }

  public static Profile readProfile(File file) throws IOException {
    return readProfile(file.toPath());
  }

  public static Profile readProfile(Path path) throws IOException {
    return ProfileReader.readProfile(path);
  }

  public static Profile readProfile(String path) throws IOException {
   return ProfileReader.readProfile(Paths.get(path));
  }

  public static Profile readProfile(InputStream in) throws IOException {
    return ProfileReader.readProfile(in);
  }

  public static Profile fromBytes(ByteBuffer data) {
    return ProfileReader.parse(data);
  }

  public static Profile fromBytes(byte[] data) {
    return ProfileReader.parse(ByteBuffer.wrap(data));
  }

  public static Builder newProfile(ProfileClass profileClass, long version) {
    return new Builder(profileClass, version);
  }

  public Transform getAToBTransform(RenderingIntent intent) {
    return aToBTransforms.get(intent);
  }

  public Transform getBToATransform(RenderingIntent intent) {
    return bToATransforms.get(intent);
  }

  public boolean inGamut(double[] aColor) {
    if (aColor.length != aSide.getChannelCount()) {
      throw new IllegalArgumentException(
          "Color must have " + aSide.getChannelCount() + " channels");
    }
    if (gamutTest == null) {
      // FIXME should this be replaced with checking channel ranges or something? for at least a semblence of a gamut test?
      return true;
    }

    // gamut transform was verified to have an output of 1 channel in the profile builder
    double[] test = new double[1];
    gamutTest.transform(aColor, test);

    return Double.compare(test[0], 0.0) == 0;
  }

  public ZonedDateTime getCalibrationDate() {
    return calibrationTime;
  }

  public String getCharacterizationTarget() {
    return charTarget;
  }

  public Matrix getChromaticAdaptation() {
    return chromaticAdaptation;
  }

  public Colorant getChromaticity() {
    return chromaticity;
  }

  public List<NamedColor> getColorantInTable() {
    return colorantInTable;
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

  public double getLuminance() {
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
