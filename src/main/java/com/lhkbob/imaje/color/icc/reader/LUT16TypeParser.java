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


import com.lhkbob.imaje.color.icc.ColorSpace;
import com.lhkbob.imaje.color.icc.Signature;
import com.lhkbob.imaje.color.transform.curves.UniformlySampledCurve;
import com.lhkbob.imaje.color.transform.Composition;
import com.lhkbob.imaje.color.transform.CurveTransform;
import com.lhkbob.imaje.color.transform.LookupTable;
import com.lhkbob.imaje.color.transform.MatrixTransform;
import com.lhkbob.imaje.color.transform.general.Transform;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextS15Fixed16Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt16Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt8Number;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.skip;

/**
 *
 */
public final class LUT16TypeParser implements TagParser<Transform> {
  public static final Signature SIGNATURE = Signature.fromName("mft2");

  @Override
  public Signature getSignature() {
    return SIGNATURE;
  }

  @Override
  public Transform parse(Signature tag, Header header, ByteBuffer data) {
    List<Transform> transformStages = new ArrayList<>();

    int inputChannels = nextUInt8Number(data);
    int outputChannels = nextUInt8Number(data);
    int gridSize = nextUInt8Number(data);
    skip(data, 1); // padding, should be 0s

    // Even if the LUT won't use the matrix, the 9 matrix values are always specified
    double[] matrix = new double[9];
    for (int i = 0; i < matrix.length; i++) {
      matrix[i] = nextS15Fixed16Number(data);
    }

    Tag.Definition<?> def = Tag.fromSignature(tag);
    boolean forward = def == Tag.A_TO_B0 || def == Tag.A_TO_B1 || def == Tag.A_TO_B2;
    // Add a normalizing stage using either A or B's normalizing function
    transformStages.add(forward ? header.getASideColorSpace().getNormalizingFunction()
        : header.getBSideColorSpace().getNormalizingFunction());

    if ((forward && header.getASideColorSpace() == ColorSpace.CIEXYZ) || (!forward
        && header.getBSideColorSpace() == ColorSpace.CIEXYZ)) {
      // The matrix can be used
      if (inputChannels != 3) {
        throw new IllegalStateException(
            "Unexpected input channel count for XYZ color space: " + inputChannels);
      }
      // The matrix values were specified as row-major in the profile data so no
      // reordering is necessary
      transformStages.add(new MatrixTransform(3, 3, matrix));
    }

    int inputTableSize = nextUInt16Number(data);
    int outputTableSize = nextUInt16Number(data);

    // Input tables
    double[] inputTableEntries = new double[inputTableSize];
    List<UniformlySampledCurve> inputTable = new ArrayList<>(inputChannels);
    for (int i = 0; i < inputChannels; i++) {
      for (int j = 0; j < inputTableSize; j++) {
        inputTableEntries[j] = nextUInt16Number(data) / 65535.0;
      }
      // This constructor copies the data array so we can reuse our local variable
      inputTable.add(new UniformlySampledCurve(0.0, 1.0, inputTableEntries));
    }
    transformStages.add(new CurveTransform(inputTable));

    // CLUT
    double[] clutEntries = new double[(int) Math.pow(gridSize, inputChannels) * outputChannels];
    for (int i = 0; i < clutEntries.length; i++) {
      clutEntries[i] = nextUInt16Number(data) / 65535.0;
    }
    transformStages.add(new LookupTable(inputChannels, outputChannels, gridSize, clutEntries));

    // Output tables
    double[] outputTableEntries = new double[outputTableSize];
    List<UniformlySampledCurve> outputTable = new ArrayList<>(outputChannels);
    for (int i = 0; i < outputChannels; i++) {
      for (int j = 0; j < outputTableSize; j++) {
        outputTableEntries[j] = nextUInt16Number(data) / 65535.0;
      }
      // This constructor copies the data array so we can reuse our local variable
      outputTable.add(new UniformlySampledCurve(0.0, 1.0, outputTableEntries));
    }
    transformStages.add(new CurveTransform(outputTable));

    // Add a denormalizing stage
    transformStages.add(forward ? header.getBSideColorSpace().getNormalizingFunction().inverted()
        : header.getASideColorSpace().getNormalizingFunction().inverted());
    return new Composition(transformStages);
  }
}
