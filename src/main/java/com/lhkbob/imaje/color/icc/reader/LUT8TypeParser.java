package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.transform.general.LookupTable;
import com.lhkbob.imaje.color.transform.general.Matrix;
import com.lhkbob.imaje.color.icc.ColorSpace;
import com.lhkbob.imaje.color.transform.general.Transform;
import com.lhkbob.imaje.color.transform.general.Curves;
import com.lhkbob.imaje.color.transform.general.Composition;
import com.lhkbob.imaje.color.icc.Signature;
import com.lhkbob.imaje.color.transform.curves.UniformlySampledCurve;

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
public final class LUT8TypeParser implements TagParser<Transform> {
  public static final Signature SIGNATURE = Signature.fromName("mft1");

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
      transformStages.add(new Matrix(3, 3, matrix));
    }

    // Input tables
    double[] inputTableEntries = new double[256];
    List<UniformlySampledCurve> inputTable = new ArrayList<>(inputChannels);
    for (int i = 0; i < inputChannels; i++) {
      for (int j = 0; j < inputTableEntries.length; j++) {
        inputTableEntries[j] = nextUInt8Number(data) / 255.0;
      }
      // This constructor copies the data array so we can reuse our local variable
      inputTable.add(new UniformlySampledCurve(0.0, 1.0, inputTableEntries));
    }
    transformStages.add(new Curves(inputTable));

    // CLUT
    double[] clutEntries = new double[(int) Math.pow(gridSize, inputChannels) * outputChannels];
    for (int i = 0; i < clutEntries.length; i++) {
      clutEntries[i] = nextUInt8Number(data) / 255.0;
    }
    transformStages.add(new LookupTable(inputChannels, outputChannels, gridSize, clutEntries));

    // Output tables
    double[] outputTableEntries = new double[256];
    List<UniformlySampledCurve> outputTable = new ArrayList<>(outputChannels);
    for (int i = 0; i < outputChannels; i++) {
      for (int j = 0; j < outputTableEntries.length; j++) {
        outputTableEntries[j] = nextUInt16Number(data) / 255.0;
      }
      // This constructor copies the data array so we can reuse our local variable
      outputTable.add(new UniformlySampledCurve(0.0, 1.0, outputTableEntries));
    }
    transformStages.add(new Curves(outputTable));

    // Add a denormalizing stage
    transformStages.add(forward ? header.getBSideColorSpace().getNormalizingFunction().inverted()
        : header.getASideColorSpace().getNormalizingFunction().inverted());
    return new Composition(transformStages);
  }
}
