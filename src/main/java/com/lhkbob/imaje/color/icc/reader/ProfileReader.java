package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.ColorSpace;
import com.lhkbob.imaje.color.icc.ColorimetricIntent;
import com.lhkbob.imaje.color.icc.DeviceTechnology;
import com.lhkbob.imaje.color.icc.GenericColorValue;
import com.lhkbob.imaje.color.icc.LocalizedString;
import com.lhkbob.imaje.color.icc.Profile;
import com.lhkbob.imaje.color.icc.ProfileDescription;
import com.lhkbob.imaje.color.icc.ProfileID;
import com.lhkbob.imaje.color.icc.RenderingIntent;
import com.lhkbob.imaje.color.icc.RenderingIntentGamut;
import com.lhkbob.imaje.color.icc.Signature;
import com.lhkbob.imaje.color.icc.ViewingCondition;
import com.lhkbob.imaje.color.icc.curves.Curve;
import com.lhkbob.imaje.color.icc.transforms.ColorMatrix;
import com.lhkbob.imaje.color.icc.transforms.ColorTransform;
import com.lhkbob.imaje.color.icc.transforms.CurveTransform;
import com.lhkbob.imaje.color.icc.transforms.LuminanceToXYZTransform;
import com.lhkbob.imaje.color.icc.transforms.SequentialTransform;
import com.lhkbob.imaje.color.icc.transforms.XYZToLabTransform;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt32Number;

/**
 *
 */
public class ProfileReader {
  public static void main(String[] args) throws IOException {
//    Path path = Paths.get("/Users/mludwig/Desktop/C/ICCLib_V2.16/lab2lab.icm");
    Path path = Paths.get("/Users/mludwig/Desktop/color-profiles/sRGB Profile.icc");
    Profile p = readProfile(path);
    System.out.println(p.getDescription());
    System.out.println(p.getVersion());
    System.out.println(p.getProfileClass());
    System.out.println(p.getASideColorSpace() + " -> " + p.getBSideColorSpace());
    System.out.println(p.getCreator() + ": " + p.getCreationDate() + ", " + p.getCalibrationDate());
    System.out.println(p.getCopyright());
    System.out.println(p.getPrimaryPlatform());
    System.out.println(p.getPreferredCMMType());
    System.out.println(p.getRenderingIntent());
    System.out.println(p.getMediaWhitePoint());
  }

  public static Profile readProfile(File file) throws IOException {
    return readProfile(file.toPath());
  }

  public static Profile readProfile(Path path) throws IOException {
    try (
        FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
      ByteBuffer data = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
      return parse(data);
    }
  }

  public static Profile readProfile(InputStream in) throws IOException {
    if (!(in instanceof BufferedInputStream))
      in = new BufferedInputStream(in);

    int profileSize = getProfileSize(in);
    ByteBuffer profileData = readBytes(in, profileSize);
    return parse(profileData);
  }

  private static int getProfileSize(InputStream in) throws IOException {
    // Assumes stream is buffered
    in.mark(4);
    int profileSize = Math.toIntExact(nextUInt32Number(readBytes(in, 4)));
    in.reset();
    return profileSize;
  }

  private static ByteBuffer readBytes(InputStream in, int count) throws IOException {
    byte[] data= new byte[count];
    int nread = 0;
    int n;
    while ((n = in.read(data, nread, data.length - nread)) >= 0) {
      nread += n;
    }
    return ByteBuffer.wrap(data);
  }

  public static Profile parse(ByteBuffer data) {
    int dataStart = data.position();
    data.limit(dataStart + 128);
    Header header = Header.fromBytes(data);
    data.limit(dataStart + Math.toIntExact(header.getProfileSize()));
    TagTable table = TagTable.fromBytes(header, data);
    return combine(header, table);
  }

  private static Profile combine(Header header, TagTable tags) {
    Profile.Builder b = Profile.newProfile(header.getProfileClass(), header.getVersion());
    // Copy straight forward header state into the profile
    b.setASideColorSpace(header.getASideColorSpace())
        .setBSideColorSpace(header.getBSideColorSpace()).setCreationDate(header.getCreationDate())
        .setCreator(header.getCreator()).setFlags(header.getFlags())
        .setPreferredCMMType(header.getPreferredCMMType())
        .setPrimaryPlatform(header.getPrimaryPlatform())
        .setRenderingIntent(header.getRenderingIntent()).setIlluminant(header.getIlluminant());

    // Integrate description tags with the rest of the header to complete the profile description
    LocalizedString descr = tags.getTagValue(Tag.PROFILE_DESCRIPTION, new LocalizedString());
    LocalizedString manufDesc = tags.getTagValue(Tag.DEVICE_MFG_DESC, new LocalizedString());
    LocalizedString modelDesc = tags.getTagValue(Tag.DEVICE_MODEL_DESC, new LocalizedString());
    DeviceTechnology tech = DeviceTechnology
        .fromSignature(tags.getTagValue(Tag.TECHNOLOGY, Signature.NULL));

    b.setDescription(
        new ProfileDescription(header.getID(), descr, header.getManufacturer(), header.getModel(),
            header.getAttributes(), tech, manufDesc, modelDesc));

    // Simple values from tags
    b.setCalibrationDate(tags.getTagValue(Tag.CALIBRATION_DATE_TIME))
        .setCharTarget(tags.getTagValue(Tag.CHAR_TARGET))
        .setCopyright(tags.getTagValue(Tag.COPYRIGHT))
        .setColorantChromaticities(tags.getTagValue(Tag.CHROMATICITY))
        .setColorantInputTable(tags.getTagValue(Tag.COLORANT_TABLE))
        .setColorantOutputTable(tags.getTagValue(Tag.COLORANT_TABLE_OUT))
        .setColorantOrder(tags.getTagValue(Tag.COLORANT_ORDER))
        .setMeasurement(tags.getTagValue(Tag.MEASUREMENT))
        .setNamedColors(tags.getTagValue(Tag.NAMED_COLOR2))
        .setOutputResponse(tags.getTagValue(Tag.OUTPUT_RESPONSE));

    // Relatively simple values from tags that need to be converted from signatures to enums
    b.setColorimetricIntent(ColorimetricIntent
        .fromSignature(tags.getTagValue(Tag.COLORIMETRIC_INTENT_IMAGE_STATE, Signature.NULL)))
        .setPerceptualRenderingIntentGamut(RenderingIntentGamut
            .fromSignature(tags.getTagValue(Tag.PERCEPTUAL_RENDERING_INTENT_GAMUT, Signature.NULL)))
        .setSaturationRenderingIntentGamut(RenderingIntentGamut.fromSignature(
            tags.getTagValue(Tag.SATURATION_RENDERING_INTENT_GAMUT, Signature.NULL)));

    // Moderately complex tag values that should be consolidated before actually creating the profile
    b.setLuminance(getSingleColor(tags, Tag.LUMINANCE))
        .setMediaWhitePoint(getSingleColor(tags, Tag.MEDIA_WHITE_POINT));

    double[] adaptation = tags.getTagValue(Tag.CHROMATIC_ADAPTATION);
    if (adaptation != null) {
      if (adaptation.length != 9) {
        throw new IllegalStateException(
            "Chromatic adaptation tag must contain 9 values, not: " + adaptation.length);
      }
      b.setChromaticAdaptation(new ColorMatrix(3, 3, adaptation));
    }

    List<ProfileDescription> sequenceDescriptions = tags.getTagValue(Tag.PROFILE_SEQUENCE_DESC);
    if (sequenceDescriptions != null) {
      // See if text descriptions were also included, which are stored in a separate tag
      LinkedHashMap<ProfileID, LocalizedString> sequenceText = tags
          .getTagValue(Tag.PROFILE_SEQUENCE_IDENTIFIER);
      if (sequenceText != null) {
        if (sequenceText.size() != sequenceDescriptions.size()) {
          throw new IllegalStateException(
              "Profile sequence identifier tag must have the same number of profiles as the profile sequence description tag");
        }

        // Assume that the ids and text are in the same order as the profile descriptions
        List<ProfileDescription> updated = new ArrayList<>();
        Iterator<Map.Entry<ProfileID, LocalizedString>> idText = sequenceText.entrySet().iterator();
        for (ProfileDescription desc : sequenceDescriptions) {
          Map.Entry<ProfileID, LocalizedString> newData = idText.next();
          ProfileDescription newDesc = new ProfileDescription(newData.getKey(), newData.getValue(),
              desc.getDeviceManufacturer(), desc.getDeviceModel(), desc.getDeviceAttributes(),
              desc.getDeviceTechnology(), desc.getManufacturerDescription(),
              desc.getModelDescription());
          updated.add(newDesc);
        }

        sequenceDescriptions = updated;
      }
    }
    b.setProfileSequenceDescriptions(sequenceDescriptions);

    ViewingCondition viewCond = tags.getTagValue(Tag.VIEWING_CONDITION);
    if (viewCond != null) {
      LocalizedString desc = tags.getTagValue(Tag.VIEWING_COND_DESC);
      if (desc != null) {
        // Update the ViewingCondition data to include the auxiliary text description
        viewCond = new ViewingCondition(
            viewCond.getIlluminant(), viewCond.getIlluminantType(), viewCond.getSurround(), desc);
      }
    }
    b.setViewingCondition(viewCond);

    // Handle all rendering intent transformations that are defined for the profile class
    ColorTransform matrixTRC = constructMatrixTRCTransform(header, tags);
    switch (header.getProfileClass()) {
    // Input, output, display, and color space profiles have the same transform options, except
    // that output and color have limited matrixTRC support; output can use a gray trc and
    // color ought not to have any. However, I see no harm in allowing its use if the
    // tag was provided.
    case INPUT_DEVICE_PROFILE:
    case DISPLAY_DEVICE_PROFILE:
    case COLOR_SPACE_PROFILE:
    case OUTPUT_DEVICE_PROFILE: {
      // Perceptual intent is DToB0, AToB0, matrix/TRC in precedence order
      b.setTransform(RenderingIntent.PERCEPTUAL,
          getTransform(tags, matrixTRC, Tag.D_TO_B0, Tag.A_TO_B0));
      // Don't specify inverse matrix/TRC here so that the inverse defaulting can gracefully use
      // whichever perceptual forward transform was found instead of the matrix inverse
      b.setInverseTransform(RenderingIntent.PERCEPTUAL,
          getTransform(tags, null, Tag.B_TO_D0, Tag.B_TO_A0));

      // Media-relative intent is DToB1, AToB1, matrix/TRC in precedence order
      b.setTransform(RenderingIntent.MEDIA_RELATIVE_COLORIMETRIC,
          getTransform(tags, matrixTRC, Tag.D_TO_B1, Tag.A_TO_B1));
      b.setInverseTransform(RenderingIntent.MEDIA_RELATIVE_COLORIMETRIC,
          getTransform(tags, null, Tag.B_TO_D1, Tag.B_TO_A1));

      // Saturation intent is DToB2, AToB2, matrix/TRC in precedence order
      b.setTransform(RenderingIntent.SATURATION,
          getTransform(tags, matrixTRC, Tag.D_TO_B2, Tag.A_TO_B2));
      b.setInverseTransform(RenderingIntent.SATURATION,
          getTransform(tags, null, Tag.B_TO_D2, Tag.B_TO_A2));

      // ICC-absolute intent is DToB3, or computed implicitly from media-relative transformation
      b.setTransform(RenderingIntent.ICC_ABSOLUTE_COLORIMETRIC, tags.getTagValue(Tag.D_TO_B3));
      b.setInverseTransform(
          RenderingIntent.ICC_ABSOLUTE_COLORIMETRIC, tags.getTagValue(Tag.B_TO_D3));
      break;
    }

    // Device link and abstract profiles rely on the x_TO_B0 tags solely and never use the
    // matrix/TRC transform
    case DEVICE_LINK_PROFILE:
    case ABSTRACT_PROFILE:
      // The device link profile's intent is determined by the header's rendering intent;
      // Abstract profiles have no well-defined intent so just use the headers.
      b.setTransform(header.getRenderingIntent(),
          getTransform(tags, null, Tag.D_TO_B0, Tag.A_TO_B0));
      b.setInverseTransform(header.getRenderingIntent(),
          getTransform(tags, null, Tag.B_TO_D0, Tag.B_TO_A0));
      break;
    case NAMED_COLOR_PROFILE:
      // Named color profiles have no transformations available, the mapping is
      // explicitly handled through the NamedColor class.
      break;
    }

    return b.build();
  }

  @SafeVarargs
  private static ColorTransform getTransform(
      TagTable tags, ColorTransform matrixTRC, Tag.Definition<ColorTransform>... precedence) {
    ColorTransform t = null;
    for (Tag.Definition<ColorTransform> def : precedence) {
      t = tags.getTagValue(def);
      if (t != null) {
        break;
      }
    }

    return (t == null ? matrixTRC : t);
  }

  private static ColorTransform constructMatrixTRCTransform(Header header, TagTable tags) {
    GenericColorValue matrixRed = getSingleColor(tags, Tag.RED_MATRIX_COLUMN);
    GenericColorValue matrixGreen = getSingleColor(tags, Tag.GREEN_MATRIX_COLUMN);
    GenericColorValue matrixBlue = getSingleColor(tags, Tag.BLUE_MATRIX_COLUMN);

    Curve trcRed = tags.getTagValue(Tag.RED_TRC);
    Curve trcGreen = tags.getTagValue(Tag.GREEN_TRC);
    Curve trcBlue = tags.getTagValue(Tag.BLUE_TRC);

    // All 6 properties must be provided to have a valid trc/matrix transform
    if (matrixRed != null && matrixGreen != null && matrixBlue != null && trcRed != null
        && trcGreen != null && trcBlue != null) {
      CurveTransform linearization = new CurveTransform(Arrays.asList(trcRed, trcGreen, trcBlue));
      double[] matrix = new double[] {
          matrixRed.getChannel(0), matrixGreen.getChannel(0), matrixBlue.getChannel(0),
          matrixRed.getChannel(1), matrixGreen.getChannel(1), matrixBlue.getChannel(1),
          matrixRed.getChannel(2), matrixGreen.getChannel(2), matrixBlue.getChannel(2)
      };

      // Include channel normalizations
      return new SequentialTransform(Arrays
          .asList(header.getASideColorSpace().getNormalizingFunction(), linearization,
              new ColorMatrix(3, 3, matrix),
              header.getBSideColorSpace().getNormalizingFunction().inverted()));
    }

    // See if a gray curve was provided
    Curve trcGray = tags.getTagValue(Tag.GRAY_TRC);
    if (trcGray != null) {
      List<ColorTransform> stages = new ArrayList<>();
      stages.add(new CurveTransform(Collections.singletonList(trcGray)));
      // Scale the gray curve into XYZ space
      GenericColorValue white = getSingleColor(tags, Tag.MEDIA_WHITE_POINT);
      if (white == null)
        white = header.getIlluminant();

      stages.add(new LuminanceToXYZTransform(white));
      // Possibly convert from XYZ to LAB
      if (header.getBSideColorSpace() == ColorSpace.CIELAB) {
        stages.add(new XYZToLabTransform(white));
      }
      return new SequentialTransform(stages);
    }

    // This transform model is not present
    return null;
  }

  private static GenericColorValue getSingleColor(
      TagTable tags, Tag.Definition<List<GenericColorValue>> def) {
    List<GenericColorValue> colors = tags.getTagValue(def);
    if (colors != null && !colors.isEmpty()) {
      return colors.get(0);
    } else {
      return null;
    }
  }
}
