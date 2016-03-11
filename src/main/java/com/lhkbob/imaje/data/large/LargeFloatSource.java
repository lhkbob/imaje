package com.lhkbob.imaje.data.large;

import com.lhkbob.imaje.data.DataType;
import com.lhkbob.imaje.data.FloatSource;

/**
 *
 */
public class LargeFloatSource extends AbstractLargeDataSource<Float, FloatSource> implements FloatSource {
  public LargeFloatSource(FloatSource[] sources) {
    super(sources);
  }

  @Override
  public float get(long index) {
    return getSource(index).get(getIndexInSource(index));
  }

  @Override
  public DataType getDataType() {
    return DataType.FLOAT32;
  }

  @Override
  public void set(long index, float value) {
    getSource(index).set(getIndexInSource(index), value);
  }
}
