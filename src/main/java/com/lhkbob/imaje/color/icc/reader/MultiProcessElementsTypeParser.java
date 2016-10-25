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

import com.lhkbob.imaje.color.icc.Signature;
import com.lhkbob.imaje.color.transform.curves.Curve;
import com.lhkbob.imaje.color.transform.curves.DomainWindow;
import com.lhkbob.imaje.color.transform.curves.ExponentialFunction;
import com.lhkbob.imaje.color.transform.curves.GammaFunction;
import com.lhkbob.imaje.color.transform.curves.LogGammaFunction;
import com.lhkbob.imaje.color.transform.curves.PiecewiseCurve;
import com.lhkbob.imaje.color.transform.curves.UniformlySampledCurve;
import com.lhkbob.imaje.color.transform.general.Composition;
import com.lhkbob.imaje.color.transform.general.Curves;
import com.lhkbob.imaje.color.transform.general.LookupTable;
import com.lhkbob.imaje.color.transform.general.Matrix;
import com.lhkbob.imaje.color.transform.general.Transform;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextFloat32Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextPositionNumber;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextSignature;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt16Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt32Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt8Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.skip;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.skipToBoundary;

/**
 *
 */
public final class MultiProcessElementsTypeParser implements TagParser<Transform> {
  public static final Signature SIGNATURE = Signature.fromName("mpet");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public Transform parse(Signature tag, Header header, ByteBuffer data) {
    int tagStart = data.position() - 8;
    int tagLimit = data.limit();

    int inputChannels = nextUInt16Number(data);
    int outputChannels = nextUInt16Number(data);

    int processElementCount = Math.toIntExact(nextUInt32Number(data));
    if (processElementCount == 0) {
      throw new IllegalStateException("At least one process element must be provided in this tag");
    }

    // Read in the table
    ICCDataTypeUtil.PositionNumber[] table = new ICCDataTypeUtil.PositionNumber[processElementCount];
    for (int i = 0; i < processElementCount; i++) {
      table[i] = nextPositionNumber(data);
    }

    // The ICC spec says that elements may be shared
    Map<ICCDataTypeUtil.PositionNumber, Transform> elementCache = new HashMap<>();
    // Note that unlike the LUTx parsers, there is no need to add normalizing functions to the start/end
    // because the elements here except the full range of floats or perform clipping (e.g. CLUT),
    // which is handled already by the individual ColorTransform instances.
    List<Transform> elements = new ArrayList<>();

    // Read all elements based on the table
    int tagEnd = data.position();
    for (int i = 0; i < processElementCount; i++) {
      ICCDataTypeUtil.PositionNumber pos = table[i];
      Transform t = elementCache.get(pos);
      if (t == null) {
        // Must instead load it

        t = readProcessElement(header, data, tagStart, pos);
        if (t == null) {
          // There was an unsupported process element, so abort since we aren't allowed
          // to use this tag for transformation purposes
          return null;
        }
        tagEnd = Math.max(tagEnd, data.position());
        elementCache.put(pos, t);
      }

      // Expansion elements are known elements (so not unsupported) that are defined
      // to be an identity transform, so just skip including them in the list
      if (t != EXPANSION_ELEMENT) {
        elements.add(t);
      }
    }
    data.limit(tagLimit).position(tagEnd);

    // Sanity check for input and output dimensions
    if (elements.get(0).getInputChannels() != inputChannels) {
      throw new IllegalStateException(
          "First process element's input channel count does not match tag");
    }
    if (elements.get(elements.size() - 1).getOutputChannels() != outputChannels) {
      throw new IllegalStateException(
          "Last process element's output channel count does not match tag");
    }

    return new Composition(elements);
  }

  private LookupTable readCLUTElement(ByteBuffer data, int inputChannels, int outputChannels) {
    // Read in the number of points along each input dimension for the CLUT and calculate the
    // total size of the table based on these dimension sizes.
    int clutSize = outputChannels;
    int[] gridSizes = new int[inputChannels];
    for (int i = 0; i < inputChannels; i++) {
      gridSizes[i] = nextUInt8Number(data);
      clutSize *= gridSizes[i];
    }
    skip(data, 16 - inputChannels); // CLUT element has a fixed 16 element array for these sizes

    // Read all CLUT data
    double[] values = new double[clutSize];
    for (int i = 0; i < values.length; i++) {
      values[i] = nextFloat32Number(data);
    }

    skipToBoundary(data);

    return new LookupTable(inputChannels, outputChannels, gridSizes, values);
  }

  private Curve readCurveSegments(ByteBuffer data, int start, ICCDataTypeUtil.PositionNumber pos) {
    pos.configureBuffer(data, start);

    Signature sig = nextSignature(data);
    if (!CURVE_SEGMENT_SIGNATURE.equals(sig)) {
      throw new IllegalStateException("Expected data of type 'curf' but was: " + sig);
    }
    skip(data, 4); // reserved

    int segmentCount = nextUInt16Number(data);
    skip(data, 2); // reserved

    if (segmentCount == 1) {
      // There are no breakpoints to read and the following data must be a formula curve, and
      // the formula curves returned all have an infinite domain, which is what we want
      return readFormulaCurveSegment(data);
    } else {
      // Read breakpoints and store them in a padded array that includes -/+ inf
      double[] domains = new double[segmentCount + 1];
      domains[0] = Double.NEGATIVE_INFINITY;
      domains[segmentCount] = Double.POSITIVE_INFINITY;
      for (int i = 1; i < segmentCount - 1; i++) {
        domains[i] = nextFloat32Number(data);
        if (domains[i] <= domains[i - 1]) {
          throw new IllegalStateException("Segment breakpoints are not increasing");
        }
      }

      List<Curve> segments = new ArrayList<>(segmentCount);
      for (int i = 0; i < segmentCount; i++) {
        // Peek at signature
        int segmentStart = data.position();
        Signature type = nextSignature(data);
        data.position(segmentStart);

        if (type.equals(FORMULA_CURVE_SIGNATURE)) {
          Curve f = readFormulaCurveSegment(data);
          // Wrap in a domain window so it's only defined on the breakpoint interval
          segments.add(new DomainWindow(f, domains[i], domains[i + 1]));
        } else if (type.equals(SAMPLED_CURVE_SIGNATURE)) {
          if (i == 0 || i == (segmentCount - 1)) {
            throw new IllegalStateException(
                "Sampled curve segments are not allowed to be first or last segment");
          }

          // Compute preceeding value to use for interpolation end point
          double leftValue = segments.get(i - 1).evaluate(domains[i]);
          Curve s = readSampledCurveSegment(data, domains[i], leftValue, domains[i + 1]);
          // No need to wrap the sampled curve in a domain window since it has the domain
          // interval built into it
          segments.add(s);
        } else {
          throw new IllegalStateException(
              "Curve segment type must be sampled or formula, not: " + type);
        }
      }

      return new PiecewiseCurve(segments);
    }
  }

  private Curves readCurveSetElement(
      ByteBuffer data, int inputChannels, int outputChannels) {
    if (inputChannels != outputChannels) {
      throw new IllegalStateException(
          "Input and output channel counts must be equal for curve set processing element");
    }

    int curveStart = data.position() - 12; // sig(4), reserved(4), in(2), out(2)

    ICCDataTypeUtil.PositionNumber[] curvePos = new ICCDataTypeUtil.PositionNumber[inputChannels];
    for (int i = 0; i < inputChannels; i++) {
      curvePos[i] = nextPositionNumber(data);
    }

    // ICC spec allows for curves to be shared across channels so this map allows us to
    // avoid re-reading the same byte region in the data.
    Map<ICCDataTypeUtil.PositionNumber, Curve> curveCache = new HashMap<>();
    List<Curve> curves = new ArrayList<>(inputChannels);
    for (int i = 0; i < inputChannels; i++) {
      ICCDataTypeUtil.PositionNumber pos = curvePos[i];
      Curve c = curveCache.get(pos);
      if (c == null) {
        // Must read curve
        c = readCurveSegments(data, curveStart, pos);
        curveCache.put(pos, c);
      }

      curves.add(c);
    }

    skipToBoundary(data);
    return new Curves(curves);
  }

  private Transform readExpansionElement(ByteBuffer data) {
    Signature sig = nextSignature(data);
    skipToBoundary(data);
    return EXPANSION_ELEMENT;
  }

  private Curve readFormulaCurveSegment(ByteBuffer data) {
    Signature sig = nextSignature(data);
    if (!FORMULA_CURVE_SIGNATURE.equals(sig)) {
      throw new IllegalStateException("Expected data of type 'parf' but was: " + sig);
    }
    skip(data, 4); // reserved

    int functionType = nextUInt16Number(data);
    skip(data, 2); // reserved

    Curve c;
    switch (functionType) {
    case 0: {
      double[] p = readFormulaParameters(data, 4);
      c = new GammaFunction(p[0], p[1], p[2], p[3]);
      break;
    }
    case 1: {
      double[] p = readFormulaParameters(data, 5);
      c = new LogGammaFunction(p[0], p[2], p[3], p[1], p[4]);
      break;
    }
    case 2: {
      double[] p = readFormulaParameters(data, 5);
      c = new ExponentialFunction(p[1], p[2], p[3], p[0], p[4]);
      break;
    }
    default:
      throw new IllegalStateException("Unknown function type for formula: " + functionType);
    }

    return c;
  }

  private double[] readFormulaParameters(ByteBuffer data, int paramCount) {
    double[] values = new double[paramCount];
    for (int i = 0; i < values.length; i++) {
      values[i] = nextFloat32Number(data);
    }
    return values;
  }

  private Matrix readMatrixElement(ByteBuffer data, int inputChannels, int outputChannels) {
    double[] matrix = new double[inputChannels * outputChannels];
    double[] translation = new double[outputChannels];

    // Matrix is listed as row major
    for (int i = 0; i < matrix.length; i++) {
      matrix[i] = nextFloat32Number(data);
    }
    // Followed by the translation
    for (int i = 0; i < translation.length; i++) {
      translation[i] = nextFloat32Number(data);
    }
    skipToBoundary(data);

    return new Matrix(outputChannels, inputChannels, matrix, translation);
  }

  private Transform readProcessElement(
      Header header, ByteBuffer data, int start, ICCDataTypeUtil.PositionNumber element) {
    element.configureBuffer(data, start);
    Signature signature = nextSignature(data);
    skip(data, 4); // reserved data

    int inputChannels = nextUInt16Number(data);
    int outputChannels = nextUInt16Number(data);

    if (MATRIX_SIGNATURE.equals(signature)) {
      return readMatrixElement(data, inputChannels, outputChannels);
    } else if (CLUT_SIGNATURE.equals(signature)) {
      return readCLUTElement(data, inputChannels, outputChannels);
    } else if (EACS_SIGNATURE.equals(signature)) {
      readExpansionElement(data);
      return null;
    } else if (BACS_SIGNATURE.equals(signature)) {
      return readExpansionElement(data);
    } else if (CURVE_SIGNATURE.equals(signature)) {
      return readCurveSetElement(data, inputChannels, outputChannels);
    } else {
      // Unknown process element
      return null;
    }
  }

  private Curve readSampledCurveSegment(
      ByteBuffer data, double firstBreakpoint, double firstValue, double secondBreakpoint) {
    Signature sig = nextSignature(data);
    if (!SAMPLED_CURVE_SIGNATURE.equals(sig)) {
      throw new IllegalStateException("Expected data of type 'samf' but was: " + sig);
    }
    skip(data, 4); // reserved

    int count = Math.toIntExact(nextUInt32Number(data));
    double[] values = new double[count + 1];
    // The preceeding breakpoint, and its evaluation are not included in the table in the profile,
    // but are used for interpolating. To work with SampledCurve function, this adds it manually
    values[0] = firstValue;
    for (int i = 1; i < values.length; i++) {
      values[i] = nextFloat32Number(data);
    }

    return new UniformlySampledCurve(firstBreakpoint, secondBreakpoint, values);
  }

  private static final Signature BACS_SIGNATURE = Signature.fromName("bACS");
  private static final Signature CLUT_SIGNATURE = Signature.fromName("clut");
  private static final Signature CURVE_SEGMENT_SIGNATURE = Signature.fromName("curf");
  private static final Signature CURVE_SIGNATURE = Signature.fromName("cvst");
  private static final Signature EACS_SIGNATURE = Signature.fromName("eACS");
  // Dummy object returned by readExpansionElement
  private static final Transform EXPANSION_ELEMENT = new Transform() {
    @Override
    public int getInputChannels() {
      return 0;
    }

    @Override
    public Transform getLocallySafeInstance() {
      return this;
    }

    @Override
    public int getOutputChannels() {
      return 0;
    }

    @Override
    public Transform inverted() {
      return null;
    }

    @Override
    public void transform(double[] input, double[] output) {

    }
  };
  private static final Signature FORMULA_CURVE_SIGNATURE = Signature.fromName("parf");
  private static final Signature MATRIX_SIGNATURE = Signature.fromName("matf");
  private static final Signature SAMPLED_CURVE_SIGNATURE = Signature.fromName("samf");
}
