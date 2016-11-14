/**
 * Custom Numeric Bit Representations
 * ==================================
 *
 * In order to minimize storage space, many image file formats and hardware-accelerated image
 * rendering systems define custom binary representations used for pixel channel values. In C++ and
 * other languages that define operator overloads it is easy enough for those libraries to provide a
 * set of operators that make operating on the custom number types seamless with other native
 * primitive types.
 *
 * Java does not provide such functionality, so this package contains a {@link
 * com.lhkbob.imaje.data.types.BinaryRepresentation} interface to describe the conversion to
 * `double` values from a `long` bit field and vice versa. Many implementations are provided that,
 * at the time of writing, span the custom numeric types encountered in imaging systems so far (when
 * considering single-value representations only). These are:
 *
 * + {@link com.lhkbob.imaje.data.types.SignedInteger}: A 2's complement integer for configurable
 * bit size.
 * + {@link com.lhkbob.imaje.data.types.SignedNormalizedInteger}: A 2's complement integer that is
 * then normalized to `[-1, 1]`, also with configurable bit size.
 * + {@link com.lhkbob.imaje.data.types.UnsignedInteger}: A 1's complement unsigned integer with
 * configurable bit size.
 * + {@link com.lhkbob.imaje.data.types.UnsignedNormalizedInteger}: A 1's complement unsigned
 * integer that is then normalized to `[0, 1]`, also with configurable bit size.
 * + {@link com.lhkbob.imaje.data.types.SignedFloatingPointNumber}: A very flexible floating-point
 * implementation with customizable exponent and mantissa bit counts.
 * + {@link com.lhkbob.imaje.data.types.Signed64FloatingPointNumber}: Simple wrapper for Java's
 * `double`.
 * + {@link com.lhkbob.imaje.data.types.UnsignedFloatingPointNumber}: A flexible floating-point
 * implementation that only represents non-negative values.
 *
 * In addition to these single-value numeric representations, {@link
 * com.lhkbob.imaje.data.types.UnsignedSharedExponent} provides support for shared-exponent
 * representations that encode a vector of values. This format is often seen in HDR images that
 * still try and maintain reasonable file sizes.
 *
 * By wrapping BitData sources holding non-native numeric representations with a {@link
 * com.lhkbob.imaje.data.types.CustomBinaryData} and appropriate BinaryRepresentation then the
 * in-memory representation of an image or data source can remain efficient. This is an improvement
 * in other libraries that either had no support for more custom data types or would convert them
 * when reading into memory and store values converted to 32-bits or 64-bits, potentially
 * significantly increasing memory pressure.
 *
 * @author Michael Ludwig
 */
package com.lhkbob.imaje.data.types;