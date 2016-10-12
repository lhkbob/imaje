package com.lhkbob.imaje.io.exr;

/**
 *
 */
public interface OffsetTable {
  int getTotalOffsets();

  long getOffset(int... chunkCoords);
}
