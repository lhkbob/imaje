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

import com.lhkbob.imaje.color.icc.Colorant;
import com.lhkbob.imaje.color.icc.GenericColorValue;
import com.lhkbob.imaje.color.icc.LocalizedString;
import com.lhkbob.imaje.color.icc.Measurement;
import com.lhkbob.imaje.color.icc.NamedColor;
import com.lhkbob.imaje.color.icc.ProfileDescription;
import com.lhkbob.imaje.color.icc.ProfileID;
import com.lhkbob.imaje.color.icc.ResponseCurveSet;
import com.lhkbob.imaje.color.icc.Signature;
import com.lhkbob.imaje.color.icc.ViewingCondition;
import com.lhkbob.imaje.color.transform.curves.Curve;
import com.lhkbob.imaje.color.transform.general.Transform;

import java.nio.ByteBuffer;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextSignature;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.skip;

/**
 *
 */
public final class Tag<T> {
  public static final Definition<Transform> A_TO_B0 = new Definition<>(Signature.fromName("A2B0"),
      new LUT8TypeParser(), new LUT16TypeParser(), LUTTypeParser.newLUTAtoBTypeParser());
  public static final Definition<Transform> A_TO_B1 = new Definition<>(Signature.fromName("A2B1"),
      new LUT8TypeParser(), new LUT16TypeParser(), LUTTypeParser.newLUTAtoBTypeParser());
  public static final Definition<Transform> A_TO_B2 = new Definition<>(Signature.fromName("A2B2"),
      new LUT8TypeParser(), new LUT16TypeParser(), LUTTypeParser.newLUTAtoBTypeParser());
  public static final Definition<List<GenericColorValue>> BLUE_MATRIX_COLUMN = new Definition<>(
      Signature.fromName("bXYZ"), new XYZTypeParser());
  public static final Definition<Curve> BLUE_TRC = new Definition<>(
      Signature.fromName("bTRC"), new CurveTypeParser(), new ParametricCurveTypeParser());
  public static final Definition<Transform> B_TO_A0 = new Definition<>(Signature.fromName("B2A0"),
      new LUT8TypeParser(), new LUT16TypeParser(), LUTTypeParser.newLUTBtoATypeParser());
  public static final Definition<Transform> B_TO_A1 = new Definition<>(Signature.fromName("B2A1"),
      new LUT8TypeParser(), new LUT16TypeParser(), LUTTypeParser.newLUTBtoATypeParser());
  public static final Definition<Transform> B_TO_A2 = new Definition<>(Signature.fromName("B2A2"),
      new LUT8TypeParser(), new LUT16TypeParser(), LUTTypeParser.newLUTBtoATypeParser());
  public static final Definition<Transform> B_TO_D0 = new Definition<>(
      Signature.fromName("B2D0"), new MultiProcessElementsTypeParser());
  public static final Definition<Transform> B_TO_D1 = new Definition<>(
      Signature.fromName("B2D1"), new MultiProcessElementsTypeParser());
  public static final Definition<Transform> B_TO_D2 = new Definition<>(
      Signature.fromName("B2D2"), new MultiProcessElementsTypeParser());
  public static final Definition<Transform> B_TO_D3 = new Definition<>(
      Signature.fromName("B2D3"), new MultiProcessElementsTypeParser());
  public static final Definition<ZonedDateTime> CALIBRATION_DATE_TIME = new Definition<>(
      Signature.fromName("calt"), new DateTimeTypeParser());
  public static final Definition<String> CHAR_TARGET = new Definition<>(
      Signature.fromName("targ"), new TextTypeParser());
  public static final Definition<Colorant> CHROMATICITY = new Definition<>(
      Signature.fromName("chrm"), new ChromaticityTypeParser());
  // FIXME turn this into a matrix adapter somehow?
  public static final Definition<double[]> CHROMATIC_ADAPTATION = new Definition<>(
      Signature.fromName("chad"), new S15Fixed16ArrayTypeParser());
  public static final Definition<int[]> COLORANT_ORDER = new Definition<>(
      Signature.fromName("clro"), new ColorantOrderTypeParser());
  public static final Definition<List<NamedColor>> COLORANT_TABLE = new Definition<>(
      Signature.fromName("clrt"), new ColorantTableTypeParser());
  public static final Definition<List<NamedColor>> COLORANT_TABLE_OUT = new Definition<>(
      Signature.fromName("clot"), new ColorantTableTypeParser());
  public static final Definition<Signature> COLORIMETRIC_INTENT_IMAGE_STATE = new Definition<>(
      Signature.fromName("ciis"), new SignatureTypeParser());
  public static final Definition<LocalizedString> COPYRIGHT = new Definition<>(
      Signature.fromName("cprt"), new StringToLocalizedStringAdapter(new TextTypeParser()),
      new MultiLocalizedUnicodeTypeParser());
  public static final Definition<LocalizedString> DEVICE_MFG_DESC = new Definition<>(
      Signature.fromName("dmnd"), new MultiLocalizedUnicodeTypeParser(),
      new TextDescriptionTagParser());
  public static final Definition<LocalizedString> DEVICE_MODEL_DESC = new Definition<>(
      Signature.fromName("dmdd"), new MultiLocalizedUnicodeTypeParser(),
      new TextDescriptionTagParser());
  public static final Definition<Transform> D_TO_B0 = new Definition<>(
      Signature.fromName("B2D0"), new MultiProcessElementsTypeParser());
  public static final Definition<Transform> D_TO_B1 = new Definition<>(
      Signature.fromName("B2D1"), new MultiProcessElementsTypeParser());
  public static final Definition<Transform> D_TO_B2 = new Definition<>(
      Signature.fromName("B2D2"), new MultiProcessElementsTypeParser());
  public static final Definition<Transform> D_TO_B3 = new Definition<>(
      Signature.fromName("B2D3"), new MultiProcessElementsTypeParser());
  public static final Definition<Transform> GAMUT = new Definition<>(Signature.fromName("gamt"),
      new LUT8TypeParser(), new LUT16TypeParser(), LUTTypeParser.newLUTBtoATypeParser());
  public static final Definition<Curve> GRAY_TRC = new Definition<>(
      Signature.fromName("kTRC"), new CurveTypeParser(), new ParametricCurveTypeParser());
  public static final Definition<List<GenericColorValue>> GREEN_MATRIX_COLUMN = new Definition<>(
      Signature.fromName("gXYZ"), new XYZTypeParser());
  public static final Definition<Curve> GREEN_TRC = new Definition<>(
      Signature.fromName("gTRC"), new CurveTypeParser(), new ParametricCurveTypeParser());
  public static final Definition<List<GenericColorValue>> LUMINANCE = new Definition<>(
      Signature.fromName("lumi"), new XYZTypeParser());
  public static final Definition<Measurement> MEASUREMENT = new Definition<>(
      Signature.fromName("meas"), new MeasurementTypeParser());
  public static final Definition<List<GenericColorValue>> MEDIA_WHITE_POINT = new Definition<>(
      Signature.fromName("wtpt"), new XYZTypeParser());
  public static final Definition<List<NamedColor>> NAMED_COLOR2 = new Definition<>(
      Signature.fromName("ncl2"), new NamedColorTypeParser());
  public static final Definition<ResponseCurveSet> OUTPUT_RESPONSE = new Definition<>(
      Signature.fromName("resp"), new ResponseCurveSetTagParser());
  public static final Definition<Signature> PERCEPTUAL_RENDERING_INTENT_GAMUT = new Definition<>(
      Signature.fromName("rig0"), new SignatureTypeParser());
  public static final Definition<Transform> PREVIEW0 = new Definition<>(Signature.fromName("pre0"),
      new LUT8TypeParser(), new LUT16TypeParser(), LUTTypeParser.newLUTAtoBTypeParser());
  public static final Definition<Transform> PREVIEW1 = new Definition<>(Signature.fromName("pre1"),
      new LUT8TypeParser(), new LUT16TypeParser(), LUTTypeParser.newLUTAtoBTypeParser());
  public static final Definition<Transform> PREVIEW2 = new Definition<>(Signature.fromName("pre2"),
      new LUT8TypeParser(), new LUT16TypeParser(), LUTTypeParser.newLUTAtoBTypeParser());
  public static final Definition<LocalizedString> PROFILE_DESCRIPTION = new Definition<>(
      Signature.fromName("desc"), new MultiLocalizedUnicodeTypeParser(),
      new TextDescriptionTagParser());
  public static final Definition<List<ProfileDescription>> PROFILE_SEQUENCE_DESC = new Definition<>(
      Signature.fromName("pseq"), new ProfileSequenceDescriptionTypeParser());
  public static final Definition<LinkedHashMap<ProfileID, LocalizedString>> PROFILE_SEQUENCE_IDENTIFIER = new Definition<>(
      Signature.fromName("psid"), new ProfileSequenceIdentifierTypeParser());
  public static final Definition<List<GenericColorValue>> RED_MATRIX_COLUMN = new Definition<>(
      Signature.fromName("rXYZ"), new XYZTypeParser());
  public static final Definition<Curve> RED_TRC = new Definition<>(
      Signature.fromName("rTRC"), new CurveTypeParser(), new ParametricCurveTypeParser());
  public static final Definition<Signature> SATURATION_RENDERING_INTENT_GAMUT = new Definition<>(
      Signature.fromName("rig2"), new SignatureTypeParser());
  public static final Definition<Signature> TECHNOLOGY = new Definition<>(
      Signature.fromName("tech"), new SignatureTypeParser());
  public static final Definition<ViewingCondition> VIEWING_CONDITION = new Definition<>(
      Signature.fromName("view"), new ViewingConditionTypeParser());
  public static final Definition<LocalizedString> VIEWING_COND_DESC = new Definition<>(
      Signature.fromName("vued"), new MultiLocalizedUnicodeTypeParser(),
      new TextDescriptionTagParser());

  public static class Definition<T> {
    private final Map<Signature, TagParser<? extends T>> parsers;
    private final Signature tagSignature;

    @SafeVarargs
    public Definition(Signature tagSignature, TagParser<? extends T>... parsers) {
      this.tagSignature = tagSignature;
      this.parsers = new HashMap<>();

      for (TagParser<? extends T> p : parsers) {
        this.parsers.put(p.getSignature(), p);
      }
    }

    public Signature getSignature() {
      return tagSignature;
    }

    public TagParser<? extends T> getTagParser(Signature dataType) {
      return parsers.get(dataType);
    }

    public Tag<T> parseTag(Header header, ByteBuffer data) {
      Signature type = nextSignature(data);
      TagParser<? extends T> parser = getTagParser(type);

      if (parser == null) {
        // Unsupported tag type for the tag, pretend like it doesn't exist
        System.out.println("Unknown tag data type: " + type + " for tag: " + tagSignature);
        return null;
      }
      skip(data, 4);
      T value = parser.parse(tagSignature, header, data);
      if (value == null) {
        // Unsupported tag data configuration (really only will happen for undefined multiprocess
        // elements, which mandates skipping the multiprocesselement tag)
        System.out.println("Unknown multiprocess element");
        return null;
      }

      return new Tag<>(tagSignature, type, value);
    }
  }
  private final T data;
  private final Signature signature;
  private final Signature typeSignature;
  public Tag(Signature signature, Signature typeSignature, T data) {
    this.signature = signature;
    this.typeSignature = typeSignature;
    this.data = data;
  }

  public static Definition<?> fromSignature(Signature sig) {
    return KNOWN_SIGNATURES.get(sig);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Tag)) {
      return false;
    }
    Tag t = (Tag) o;
    return Objects.equals(t.signature, signature) && Objects.equals(t.typeSignature, typeSignature) && Objects
        .equals(t.data, data);
  }

  public T getData() {
    return data;
  }

  public Signature getSignature() {
    return signature;
  }

  public Signature getTagType() {
    return typeSignature;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + signature.hashCode();
    result = 31 * result + typeSignature.hashCode();
    result = 31 * result + data.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return String.format("Tag (sig: %s, data: %s)", signature, data);
  }

  private static void register(Definition<?> tag, Map<Signature, Definition<?>> map) {
    map.put(tag.getSignature(), tag);
  }

  private static final Map<Signature, Definition<?>> KNOWN_SIGNATURES;

  static {
    Map<Signature, Definition<?>> allSigs = new HashMap<>();
    register(A_TO_B0, allSigs);
    register(A_TO_B1, allSigs);
    register(A_TO_B2, allSigs);
    register(BLUE_MATRIX_COLUMN, allSigs);
    register(BLUE_TRC, allSigs);
    register(B_TO_A0, allSigs);
    register(B_TO_A1, allSigs);
    register(B_TO_A2, allSigs);
    register(B_TO_D0, allSigs);
    register(B_TO_D1, allSigs);
    register(B_TO_D2, allSigs);
    register(B_TO_D3, allSigs);
    register(CALIBRATION_DATE_TIME, allSigs);
    register(CHAR_TARGET, allSigs);
    register(CHROMATIC_ADAPTATION, allSigs);
    register(CHROMATICITY, allSigs);
    register(COLORANT_ORDER, allSigs);
    register(COLORANT_TABLE, allSigs);
    register(COLORANT_TABLE_OUT, allSigs);
    register(COLORIMETRIC_INTENT_IMAGE_STATE, allSigs);
    register(COPYRIGHT, allSigs);
    register(DEVICE_MFG_DESC, allSigs);
    register(DEVICE_MODEL_DESC, allSigs);
    register(D_TO_B0, allSigs);
    register(D_TO_B1, allSigs);
    register(D_TO_B2, allSigs);
    register(D_TO_B3, allSigs);
    register(GAMUT, allSigs);
    register(GRAY_TRC, allSigs);
    register(GREEN_MATRIX_COLUMN, allSigs);
    register(GREEN_TRC, allSigs);
    register(LUMINANCE, allSigs);
    register(MEASUREMENT, allSigs);
    register(MEDIA_WHITE_POINT, allSigs);
    register(NAMED_COLOR2, allSigs);
    register(OUTPUT_RESPONSE, allSigs);
    register(PERCEPTUAL_RENDERING_INTENT_GAMUT, allSigs);
    register(PREVIEW0, allSigs);
    register(PREVIEW1, allSigs);
    register(PREVIEW2, allSigs);
    register(PROFILE_DESCRIPTION, allSigs);
    register(PROFILE_SEQUENCE_DESC, allSigs);
    register(PROFILE_SEQUENCE_IDENTIFIER, allSigs);
    register(RED_MATRIX_COLUMN, allSigs);
    register(RED_TRC, allSigs);
    register(SATURATION_RENDERING_INTENT_GAMUT, allSigs);
    register(TECHNOLOGY, allSigs);
    register(VIEWING_COND_DESC, allSigs);
    register(VIEWING_CONDITION, allSigs);

    KNOWN_SIGNATURES = Collections.unmodifiableMap(allSigs);
  }
}
