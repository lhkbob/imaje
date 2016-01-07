package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.icc.ColorSpace;
import com.lhkbob.imaje.color.icc.ColorimetricIntent;
import com.lhkbob.imaje.color.icc.DeviceTechnology;
import com.lhkbob.imaje.color.icc.GenericColorValue;
import com.lhkbob.imaje.color.icc.LocalizedString;
import com.lhkbob.imaje.color.icc.NamedColor;
import com.lhkbob.imaje.color.icc.Profile;
import com.lhkbob.imaje.color.icc.ProfileClass;
import com.lhkbob.imaje.color.icc.ProfileDescription;
import com.lhkbob.imaje.color.icc.ProfileID;
import com.lhkbob.imaje.color.icc.RenderingIntent;
import com.lhkbob.imaje.color.icc.RenderingIntentGamut;
import com.lhkbob.imaje.color.icc.Signature;
import com.lhkbob.imaje.color.icc.ViewingCondition;
import com.lhkbob.imaje.color.transform.curves.Curve;
import com.lhkbob.imaje.color.transform.general.Matrix;
import com.lhkbob.imaje.color.transform.general.Transform;
import com.lhkbob.imaje.color.transform.general.Curves;
import com.lhkbob.imaje.color.transform.general.LuminanceToXYZ;
import com.lhkbob.imaje.color.transform.general.Composition;
import com.lhkbob.imaje.color.transform.general.XYZToLab;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
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
  public static Profile readProfile(Path path) throws IOException {
    try (
        FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
      ByteBuffer data = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
      return parse(data);
    }
  }

  public static Profile readProfile(InputStream in) throws IOException {
    if (!(in instanceof BufferedInputStream)) {
      in = new BufferedInputStream(in);
    }

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
    byte[] data = new byte[count];
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
        .setMeasurement(tags.getTagValue(Tag.MEASUREMENT))
        .setNamedColors(tags.getTagValue(Tag.NAMED_COLOR2))
        .setOutputResponse(tags.getTagValue(Tag.OUTPUT_RESPONSE));

    // Relatively simple values from tags that need to be converted from signatures to enums or similar
    b.setColorimetricIntent(ColorimetricIntent
        .fromSignature(tags.getTagValue(Tag.COLORIMETRIC_INTENT_IMAGE_STATE, Signature.NULL)))
        .setPerceptualRenderingIntentGamut(RenderingIntentGamut
            .fromSignature(tags.getTagValue(Tag.PERCEPTUAL_RENDERING_INTENT_GAMUT, Signature.NULL)))
        .setSaturationRenderingIntentGamut(RenderingIntentGamut.fromSignature(
            tags.getTagValue(Tag.SATURATION_RENDERING_INTENT_GAMUT, Signature.NULL)));
    GenericColorValue luminance = getSingleColor(tags, Tag.LUMINANCE);
    if (luminance != null) {
      b.setLuminance(luminance.getChannel(1));
    }

    b.setMediaWhitePoint(getSingleColor(tags, Tag.MEDIA_WHITE_POINT));

    // Moderately complex tag values that should be consolidated before actually creating the profile

    // The chromatic adaptation tag has 9 values in an array that must be arranged as a matrix
    double[] adaptation = tags.getTagValue(Tag.CHROMATIC_ADAPTATION);
    if (adaptation != null) {
      if (adaptation.length != 9) {
        throw new IllegalStateException(
            "Chromatic adaptation tag must contain 9 values, not: " + adaptation.length);
      }
      b.setChromaticAdaptation(new Matrix(3, 3, adaptation));
    }

    // Reorder colorant tables by the colorantOrder tag if it's provided, otherwise order is
    // implicit. If the order tag is present, it affects the input colorant table for all
    // profile classes except the device link profile (in which case it affects the output table)
    List<NamedColor> colorantIn = tags.getTagValue(Tag.COLORANT_TABLE);
    List<NamedColor> colorantOut = tags.getTagValue(Tag.COLORANT_TABLE_OUT);
    int[] colorantOrder = tags.getTagValue(Tag.COLORANT_ORDER);
    if (colorantOrder != null) {
      if (header.getProfileClass() != ProfileClass.DEVICE_LINK_PROFILE) {
        // Reorder colorantIn if it's provided
        if (colorantIn != null) {
          colorantIn = reorderColorants(colorantIn, colorantOrder);
        }
      } else {
        // Reorder colorantOut if it's provided
        if (colorantOut != null) {
          colorantOut = reorderColorants(colorantOut, colorantOrder);
        }
      }
    }
    b.setColorantInputTable(colorantIn).setColorantOutputTable(colorantOut);

    // Combine sequence descriptions with ids and localized text descriptions
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

    // Combine viewing condition and viewing condition descriptions
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
    b.setDefaultTransform(constructMatrixTRCTransform(header, tags));

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
      b.setTransform(RenderingIntent.PERCEPTUAL, getTransform(tags, Tag.D_TO_B0, Tag.A_TO_B0));
      // Don't specify inverse matrix/TRC here so that the inverse defaulting can gracefully use
      // whichever perceptual forward transform was found instead of the matrix inverse
      b.setInverseTransform(RenderingIntent.PERCEPTUAL,
          getTransform(tags, Tag.B_TO_D0, Tag.B_TO_A0));

      // Media-relative intent is DToB1, AToB1, matrix/TRC in precedence order
      b.setTransform(RenderingIntent.MEDIA_RELATIVE_COLORIMETRIC,
          getTransform(tags, Tag.D_TO_B1, Tag.A_TO_B1));
      b.setInverseTransform(RenderingIntent.MEDIA_RELATIVE_COLORIMETRIC,
          getTransform(tags, Tag.B_TO_D1, Tag.B_TO_A1));

      // Saturation intent is DToB2, AToB2, matrix/TRC in precedence order
      b.setTransform(RenderingIntent.SATURATION, getTransform(tags, Tag.D_TO_B2, Tag.A_TO_B2));
      b.setInverseTransform(RenderingIntent.SATURATION,
          getTransform(tags, Tag.B_TO_D2, Tag.B_TO_A2));

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
          getTransform(tags, Tag.D_TO_B0, Tag.A_TO_B0));
      b.setInverseTransform(header.getRenderingIntent(),
          getTransform(tags, Tag.B_TO_D0, Tag.B_TO_A0));
      break;
    case NAMED_COLOR_PROFILE:
      // Named color profiles have no transformations available, the mapping is
      // explicitly handled through the NamedColor class.
      break;
    }

    return b.build();
  }

  private static List<NamedColor> reorderColorants(List<NamedColor> colorants, int[] colorantOrder) {
    if (colorants.size() != colorantOrder.length)
      throw new IllegalStateException(
          "Colorant order tag length different than colorant table tag length");

    List<NamedColor> reordered = new ArrayList<>();
    for (int i = 0; i < colorantOrder.length; i++) {
      if (colorantOrder[i] >= colorants.size()) {
        throw new IllegalStateException("Colorant order table references bad channel");
      }
      reordered.add(colorants.get(colorantOrder[i]));
    }

    return reordered;
  }

  @SafeVarargs
  private static Transform getTransform(
      TagTable tags, Tag.Definition<Transform>... precedence) {
    for (Tag.Definition<Transform> def : precedence) {
      Transform t = tags.getTagValue(def);
      if (t != null) {
        return t;
      }
    }

    return null;
  }

  private static Transform constructMatrixTRCTransform(Header header, TagTable tags) {
    GenericColorValue matrixRed = getSingleColor(tags, Tag.RED_MATRIX_COLUMN);
    GenericColorValue matrixGreen = getSingleColor(tags, Tag.GREEN_MATRIX_COLUMN);
    GenericColorValue matrixBlue = getSingleColor(tags, Tag.BLUE_MATRIX_COLUMN);

    Curve trcRed = tags.getTagValue(Tag.RED_TRC);
    Curve trcGreen = tags.getTagValue(Tag.GREEN_TRC);
    Curve trcBlue = tags.getTagValue(Tag.BLUE_TRC);

    // All 6 properties must be provided to have a valid trc/matrix transform
    if (matrixRed != null && matrixGreen != null && matrixBlue != null && trcRed != null
        && trcGreen != null && trcBlue != null) {
      Curves linearization = new Curves(Arrays.asList(trcRed, trcGreen, trcBlue));
      double[] matrix = new double[] {
          matrixRed.getChannel(0), matrixGreen.getChannel(0), matrixBlue.getChannel(0),
          matrixRed.getChannel(1), matrixGreen.getChannel(1), matrixBlue.getChannel(1),
          matrixRed.getChannel(2), matrixGreen.getChannel(2), matrixBlue.getChannel(2)
      };

      // Include channel normalizations
      return new Composition(Arrays
          .asList(header.getASideColorSpace().getNormalizingFunction(), linearization,
              new Matrix(3, 3, matrix),
              header.getBSideColorSpace().getNormalizingFunction().inverted()));
    }

    // See if a gray curve was provided
    Curve trcGray = tags.getTagValue(Tag.GRAY_TRC);
    if (trcGray != null) {
      List<Transform> stages = new ArrayList<>();
      stages.add(new Curves(Collections.singletonList(trcGray)));
      // Scale the gray curve into XYZ space
      GenericColorValue white = getSingleColor(tags, Tag.MEDIA_WHITE_POINT);
      if (white == null) {
        white = header.getIlluminant();
      }

      XYZ whiteXYZ = new XYZ(white.getChannel(0), white.getChannel(1), white.getChannel(2));
      stages.add(new LuminanceToXYZ(whiteXYZ));
      // Possibly convert from XYZ to LAB
      if (header.getBSideColorSpace() == ColorSpace.CIELAB) {
        stages.add(new XYZToLab(whiteXYZ));
      }
      return new Composition(stages);
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
