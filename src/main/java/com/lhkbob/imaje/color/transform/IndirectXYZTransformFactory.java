package com.lhkbob.imaje.color.transform;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.util.Arguments;

/**
 *
 */
public class IndirectXYZTransformFactory<I extends Color, O extends Color> implements TransformFactory<I, O> {
  private final TransformFactory<XYZ, O> fromXYZ;
  private final TransformFactory<I, XYZ> toXYZ;

  public IndirectXYZTransformFactory(
      TransformFactory<I, XYZ> toXYZ, TransformFactory<XYZ, O> fromXYZ) {
    Arguments.notNull("toXYZ", toXYZ);
    Arguments.notNull("fromXYZ", fromXYZ);

    this.toXYZ = toXYZ;
    this.fromXYZ = fromXYZ;
  }

  @Override
  public Class<I> getInputType() {
    return toXYZ.getInputType();
  }

  @Override
  public Class<O> getOutputType() {
    return fromXYZ.getOutputType();
  }

  @Override
  public ColorTransform<O, I> newInverseTransform() {
    return new IndirectXYZTransform<>(fromXYZ.newInverseTransform(), toXYZ.newInverseTransform());
  }

  @Override
  public ColorTransform<I, O> newTransform() {
    return new IndirectXYZTransform<>(toXYZ.newTransform(), fromXYZ.newTransform());
  }

  private static class IndirectXYZTransform<I extends Color, O extends Color> implements ColorTransform<I, O> {
    private final ColorTransform<XYZ, O> fromXYZ;
    private final XYZ temp;
    private final ColorTransform<I, XYZ> toXYZ;

    public IndirectXYZTransform(ColorTransform<I, XYZ> toXYZ, ColorTransform<XYZ, O> fromXYZ) {
      this.toXYZ = toXYZ;
      this.fromXYZ = fromXYZ;
      temp = new XYZ();
    }

    @Override
    public boolean apply(I input, O output) {
      boolean result = toXYZ.apply(input, temp);
      result &= fromXYZ.apply(temp, output);
      return result;
    }

    @Override
    public O apply(I input) {
      boolean valid = toXYZ.apply(input, temp);
      if (!valid)
        return null;

      return fromXYZ.apply(temp);
    }
  }
}
