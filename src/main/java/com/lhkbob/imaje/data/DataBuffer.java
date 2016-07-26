package com.lhkbob.imaje.data;

/**
 * How to add bulk operations and updates? What types of updates are necessary:
 *
 * 1. Copy into the source from an array, buffer, stream, channel that are assumed to exactly match
 *    the expected bit pattern of the source
 * 2. Copy into the source from an array, buffer, stream, channel that are numeric and must be
 *    re-encoded into the source's format --> Unnecessary, just use the DataSource copy then
 * 3. Copy back and forth between two sources, either optimized if bit compatible or by decoding
 *    and recoding as necessary
 * 4. All of these copies should have source and dest offsets and length, for streams it should have length
 *    but no dest offset, and length can be implicit (e.g. read as much as possible, either until source
 *    is full or stream is empty)
 *
 *    Other things to think about:
 *    The specific types that these things support require using the concrete type, or at least the
 *    more concrete interface like ByteSource. But this doesn't apply when you want to send values
 *    to
 */
public interface DataBuffer {
  long getLength();

  boolean isBigEndian();

  boolean isGPUAccessible();

  int getBitSize();
}
