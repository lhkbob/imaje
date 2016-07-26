package com.lhkbob.imaje.data.large;

/**
 *
 */
public interface BulkOperation<S, D> {
  void run(S src, long srcOffset, D dst, int dstOffset, int length);
}
