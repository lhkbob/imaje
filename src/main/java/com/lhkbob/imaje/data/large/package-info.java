/**
 * Large DataBuffer's
 * ==================
 *
 * This package contains implementations for the primitive-specialized DataBuffer types that
 * concatenate multiple instances to provide DataBuffers with lengths longer than what is
 * representable by an `int`. The implementations provided in the {@link com.lhkbob.imaje.data.array
 * array} {@link com.lhkbob.imaje.data.nio nio} packages are limited by this type restriction. By
 * combining several instances of those implementations together, much larger buffers can be
 * represented. The implementation mapping is:
 *
 * + {@link com.lhkbob.imaje.data.ByteData} -> {@link com.lhkbob.imaje.data.large.LargeByteData}.
 * + {@link com.lhkbob.imaje.data.ShortData} -> {@link com.lhkbob.imaje.data.large.LargeShortData}.
 * + {@link com.lhkbob.imaje.data.IntData} -> {@link com.lhkbob.imaje.data.large.LargeIntData}.
 * + {@link com.lhkbob.imaje.data.LongData} -> {@link com.lhkbob.imaje.data.large.LargeLongData}.
 * + {@link com.lhkbob.imaje.data.FloatData} -> {@link com.lhkbob.imaje.data.large.LargeFloatData}.
 * + {@link com.lhkbob.imaje.data.DoubleData} -> {@link
 * com.lhkbob.imaje.data.large.LargeDoubleData}.
 *
 * These implementations are automatically used by both {@link
 * com.lhkbob.imaje.data.Data#arrayDataFactory()} and {@link
 * com.lhkbob.imaje.data.Data#nioDataFactory()} when lengths are greater than a specific threshold.
 *
 * @author Michael Ludwig
 */
package com.lhkbob.imaje.data.large;