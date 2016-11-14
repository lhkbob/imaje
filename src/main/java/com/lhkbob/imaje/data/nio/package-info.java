/**
 * NIO Buffer-backed DataBuffer's
 * ==============================
 *
 * This package contains implementations for the primitive-specialized DataBuffer types that
 * operate by wrapping an NIO {@link java.nio.Buffer buffer}. The implementation mapping is:
 *
 * + {@link com.lhkbob.imaje.data.ByteData} -> {@link com.lhkbob.imaje.data.nio.ByteBufferData}.
 * + {@link com.lhkbob.imaje.data.ShortData} -> {@link com.lhkbob.imaje.data.nio.ShortBufferData}.
 * + {@link com.lhkbob.imaje.data.IntData} -> {@link com.lhkbob.imaje.data.nio.IntBufferData}.
 * + {@link com.lhkbob.imaje.data.LongData} -> {@link com.lhkbob.imaje.data.nio.LongBufferData}.
 * + {@link com.lhkbob.imaje.data.FloatData} -> {@link com.lhkbob.imaje.data.nio.FloatBufferData}.
 * + {@link com.lhkbob.imaje.data.DoubleData} -> {@link
 * com.lhkbob.imaje.data.nio.DoubleBufferData}.
 *
 * These implementations are automatically used when {@link
 * com.lhkbob.imaje.data.Data#nioDataFactory()} is used.
 *
 * @author Michael Ludwig
 */
package com.lhkbob.imaje.data.nio;