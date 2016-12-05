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
import com.lhkbob.imaje.color.transform.Composition;
import com.lhkbob.imaje.color.transform.CurveTransform;
import com.lhkbob.imaje.color.transform.LookupTable;
import com.lhkbob.imaje.color.transform.MatrixTransform;
import com.lhkbob.imaje.color.transform.general.Transform;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextS15Fixed16Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextSignature;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt16Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt32Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt8Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.skip;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.skipToBoundary;

/**
 *
 */
public final class LUTTypeParser implements TagParser<Transform> {
  public static final Signature A_TO_B_SIGNATURE = Signature.fromName("mAB");
  public static final Signature B_TO_A_SIGNATURE = Signature.fromName("mBA");
  private final boolean reverse;
  private final Signature signature;

  protected LUTTypeParser(Signature signature, boolean reverse) {
    this.signature = signature;
    this.reverse = reverse;
  }

  public static LUTTypeParser newLUTAtoBTypeParser() {
    return new LUTTypeParser(A_TO_B_SIGNATURE, false);
  }

  public static LUTTypeParser newLUTBtoATypeParser() {
    return new LUTTypeParser(B_TO_A_SIGNATURE, true);
  }

  @Override
  public Signature getSignature() {
    return signature;
  }

  @Override
  public Transform parse(Signature tag, Header header, ByteBuffer data) {
    int tagStart = data.position() - 8;
    List<Transform> transformStages = new ArrayList<>();

    // Push on a normalizing function for the start space, or if in reverse mode push on
    // the denormalizing function since the stages are reversed at the very end
    transformStages.add(reverse ? header.getASideColorSpace().getNormalizingFunction().inverted()
        : header.getASideColorSpace().getNormalizingFunction());

    int inputChannels = nextUInt8Number(data);
    int outputChannels = nextUInt8Number(data);
    if (reverse) {
      // Both LUTAtoB and LUTBtoA specify input and output channels with respect to how they
      // are used; however, if we swap in/out in the BtoA case then we can reuse the rest of the
      // code unchanged with an additional reverse of the transform elements at the end.
      int t = inputChannels;
      inputChannels = outputChannels;
      outputChannels = t;
    }

    // Offsets to data, if any value is 0 that data chunk is not present
    // Offsets are relative to tagStart
    int bCurveOffset = Math.toIntExact(nextUInt32Number(data));
    int matrixOffset = Math.toIntExact(nextUInt32Number(data));
    int mCurveOffset = Math.toIntExact(nextUInt32Number(data));
    int clutOffset = Math.toIntExact(nextUInt32Number(data));
    int aCurveOffset = Math.toIntExact(nextUInt32Number(data));
    int tagEnd = data.position();

    // Note that we can't use a PositionNumber as the key to the cache because the size
    // is not available until after the curve tag has been read fully, which defeats the
    // purpose. Thus, only the offset should be used.
    Map<Integer, Curve> curveCache = new HashMap<>();
    Map<Integer, Integer> curveSizes = new HashMap<>();

    // Read in inputChannels count "A" curves for the start of the transform, if they are present
    if (aCurveOffset != 0) {
      transformStages.add(
          readCurveBlock(tag, header, data, tagStart + aCurveOffset, inputChannels, curveCache,
              curveSizes));
      tagEnd = Math.max(tagEnd, data.position());
    }

    // Read in the CLUT if it's present
    if (clutOffset != 0) {
      transformStages
          .add(readCLUTBlock(data, tagStart + clutOffset, inputChannels, outputChannels));
      tagEnd = Math.max(tagEnd, data.position());
    }

    // Read in "M" curves if they are present
    if (mCurveOffset != 0) {
      transformStages.add(
          readCurveBlock(tag, header, data, tagStart + mCurveOffset, outputChannels, curveCache,
              curveSizes));
      tagEnd = Math.max(tagEnd, data.position());
    }

    // Read in the matrix if it's present
    if (matrixOffset != 0) {
      transformStages.add(readMatrixBlock(data, tagStart + matrixOffset));
      tagEnd = Math.max(tagEnd, data.position());
    }

    // Read in the "B" curves if they are present
    if (bCurveOffset != 0) {
      transformStages.add(
          readCurveBlock(tag, header, data, tagStart + bCurveOffset, outputChannels, curveCache,
              curveSizes));
      tagEnd = Math.max(tagEnd, data.position());
    }

    data.position(tagEnd);

    // Push on a denormalizing function, or if we're in reverse mode, push on a normalizing function
    transformStages.add(reverse ? header.getBSideColorSpace().getNormalizingFunction()
        : header.getBSideColorSpace().getNormalizingFunction().inverted());

    if (reverse) {
      // Reorder the AtoB order the code above creates to have the desired BtoA order
      Collections.reverse(transformStages);
    }
    return new Composition(transformStages);
  }

  private LookupTable readCLUTBlock(
      ByteBuffer data, int start, int inputChannels, int outputChannels) {
    data.position(start);

    // Read in the number of points along each input dimension for the CLUT and calculate the
    // total size of the table based on these dimension sizes.
    int clutSize = outputChannels;
    int[] gridSizes = new int[inputChannels];
    for (int i = 0; i < inputChannels; i++) {
      gridSizes[i] = nextUInt8Number(data);
      clutSize *= gridSizes[i];
    }
    skip(data, 16 - inputChannels); // CLUT element has a fixed 16 element array for these sizes

    // CLUT data is either normalized bytes or shorts
    int precision = nextUInt8Number(data);

    // Read all CLUT data
    double[] values = new double[clutSize];
    if (precision == 1) {
      for (int i = 0; i < values.length; i++) {
        values[i] = nextUInt8Number(data) / 255.0;
      }
    } else if (precision == 2) {
      for (int i = 0; i < values.length; i++) {
        values[i] = nextUInt16Number(data) / 65535.0;
      }
    } else {
      throw new IllegalStateException(
          "Unexpected byte precision: " + precision + " (only 1 or 2 is defined)");
    }

    skipToBoundary(data);
    return new LookupTable(inputChannels, outputChannels, gridSizes, values);
  }

  private CurveTransform readCurveBlock(
      Signature tag, Header header, ByteBuffer data, int start, int curveCount,
      Map<Integer, Curve> cache, Map<Integer, Integer> sizes) {
    // There are inputChannels count curves stored sequentially as a fully formed tag type
    // Each curve ends with 0-3 bytes to align it with a 4 byte boundary
    data.position(start);
    List<Curve> curves = new ArrayList<>();
    for (int i = 0; i < curveCount; i++) {
      int currentOffset = data.position();
      Curve curve;

      if (cache.containsKey(currentOffset)) {
        // Already loaded the curve before, look it up and advance data to the end of it
        // as if we had fully read it in again.
        curve = cache.get(currentOffset);
        int size = sizes.get(currentOffset);
        data.position(currentOffset + size);
      } else {
        Signature type = nextSignature(data);
        skip(data, 4); // reserved

        TagParser<Curve> curveParser;
        if (Objects.equals(type, CurveTypeParser.SIGNATURE)) {
          curveParser = new CurveTypeParser();
        } else if (Objects.equals(type, ParametricCurveTypeParser.SIGNATURE)) {
          curveParser = new ParametricCurveTypeParser();
        } else {
          throw new IllegalStateException("Unsupported signature for curve: " + type);
        }

        curve = curveParser.parse(tag, header, data);
        // Advance position to 4 byte boundary
        skipToBoundary(data);

        // Cache curve and size (including the byte boundary
        cache.put(currentOffset, curve);
        sizes.put(currentOffset, data.position() - currentOffset);
      }

      curves.add(curve);
    }

    return new CurveTransform(curves);
  }

  private MatrixTransform readMatrixBlock(ByteBuffer data, int start) {
    data.position(start);

    // The first 9 values are row-major for a 3x3 matrix
    double[] matrix = new double[9];
    for (int i = 0; i < matrix.length; i++) {
      matrix[i] = nextS15Fixed16Number(data);
    }
    // The next 3 values are a vector offset
    double[] translation = new double[3];
    for (int i = 0; i < translation.length; i++) {
      translation[i] = nextS15Fixed16Number(data);
    }
    skipToBoundary(data);

    return new MatrixTransform(3, 3, matrix, translation);
  }
}
