/**
 * Array-backed DataBuffer's
 * =========================
 *
 * This package contains implementations for the primitive-specialized DataBuffer types that
 * operate by wrapping a primitive array. The implementation mapping is:
 *
 * + {@link com.lhkbob.imaje.data.ByteData} -> {@link com.lhkbob.imaje.data.array.ByteArrayData}.
 * + {@link com.lhkbob.imaje.data.ShortData} -> {@link com.lhkbob.imaje.data.array.ShortArrayData}.
 * + {@link com.lhkbob.imaje.data.IntData} -> {@link com.lhkbob.imaje.data.array.IntArrayData}.
 * + {@link com.lhkbob.imaje.data.LongData} -> {@link com.lhkbob.imaje.data.array.LongArrayData}.
 * + {@link com.lhkbob.imaje.data.FloatData} -> {@link com.lhkbob.imaje.data.array.FloatArrayData}.
 * + {@link com.lhkbob.imaje.data.DoubleData} -> {@link
 * com.lhkbob.imaje.data.array.DoubleArrayData}.
 *
 * These implementations are automatically used when {@link
 * com.lhkbob.imaje.data.Data#arrayDataFactory()} is used.
 *
 * @author Michael Ludwig
 */
package com.lhkbob.imaje.data.array;