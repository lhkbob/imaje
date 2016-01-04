package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.ColorimetricIntent;
import com.lhkbob.imaje.color.icc.DeviceTechnology;
import com.lhkbob.imaje.color.icc.GenericColorValue;
import com.lhkbob.imaje.color.icc.LocalizedString;
import com.lhkbob.imaje.color.icc.Profile;
import com.lhkbob.imaje.color.icc.ProfileDescription;
import com.lhkbob.imaje.color.icc.ProfileID;
import com.lhkbob.imaje.color.icc.RenderingIntentGamut;
import com.lhkbob.imaje.color.icc.Signature;
import com.lhkbob.imaje.color.icc.ViewingCondition;
import com.lhkbob.imaje.color.icc.transforms.ColorMatrix;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    // FIXME handle all the color transform options
    return b.build();
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
