package com.lhkbob.imaje.color.space;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.ColorSpace;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.color.space.rgb.SRGB;
import com.lhkbob.imaje.color.space.xyz.CIE31;
import com.lhkbob.imaje.color.transform.Transform;

/**
 *
 */
public class ChromaticAdaptedSpace<I extends Color<I, SI>, SI extends ColorSpace<I, SI> & WhitePoint<II>, II extends Illuminant, IO extends Illuminant> implements ColorSpace<I, SI>, WhitePoint<IO> {
  @Override
  public int getChannelCount() {
    return 0;
  }

  @Override
  public I newColor() {
    return null;
  }

  @Override
  public Transform<I, SI, XYZ<CIE31>, CIE31> getTransformToXYZ() {
    return null;
  }

  @Override
  public String getChannelName(int channel) {
    return null;
  }

  public static void main() {
    ChromaticAdaptedSpace<RGB<SRGB>, SRGB, Illuminant.D50, Illuminant.D65> adjust;
  }
}
