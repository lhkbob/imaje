/**
 * Data
 * ====
 *
 * The data package contains a set of interfaces to represent one-dimensional data structures that
 * either store bit data or numeric data. Specialized interface are provided for operating on each
 * of Java's primitives. The top-level interface for this is {@link
 * com.lhkbob.imaje.data.DataBuffer} but necessarily cannot provide much behavior on its own. There
 * are two specializations for representing {@link com.lhkbob.imaje.data.BitData bit fields} and
 * {@link com.lhkbob.imaje.data.NumericData numeric data or real numbers}. Additional
 * specializations are defined for Java's primitive types (see below for the mapping).
 * Implementations are provided for arrays, NIO buffers, large arrays (greater than what an `int`
 * can index), and custom numeric binary representations. These implementations can largely be
 * avoided by relying on {@link com.lhkbob.imaje.data.Data.Factory}.
 *
 * Always use {@link com.lhkbob.imaje.data.Data#getBufferFactory()} to create new NIO buffers,
 * regardless of it is library code or application code.
 *
 * The primitive specializations are as follows:
 *
 * + `byte`
 * + + As a BitData: {@link com.lhkbob.imaje.data.ByteData}
 * + + As a NumericData: {@link com.lhkbob.imaje.data.ByteData.Numeric}
 * + `short`
 * + + As a BitData: {@link com.lhkbob.imaje.data.ShortData}
 * + + As a NumericData: {@link com.lhkbob.imaje.data.ShortData.Numeric}
 * + `int`
 * + + As a BitData: {@link com.lhkbob.imaje.data.IntData}
 * + + As a NumericData: {@link com.lhkbob.imaje.data.IntData.Numeric}
 * + `long`
 * + + As a BitData: {@link com.lhkbob.imaje.data.LongData}
 * + + As a NumericData: {@link com.lhkbob.imaje.data.LongData.Numeric}
 * + `float`: {@link com.lhkbob.imaje.data.FloatData}
 * + `double`: {@link com.lhkbob.imaje.data.DoubleData}
 *
 * The {@link com.lhkbob.imaje.data.array array} package has implementations that use primitive
 * arrays for data storage. The {@link com.lhkbob.imaje.data.nio nio} package has implementations
 * that use NIO buffers for data storage. The {@link com.lhkbob.imaje.data.large large} package
 * provides support for DataBuffers whose length exceeds that representable with an `int`. The
 * {@link com.lhkbob.imaje.data.types types} package contains support for custom numeric data types
 * that aren't normally supported by Java. These include unsigned integers, fixed-point normalized
 * integers (both signed and unsigned), and floating point formats that have custom bit sizes,
 * mantissas, and exponents.
 *
 * @author Michael Ludwig
 */
package com.lhkbob.imaje.data;