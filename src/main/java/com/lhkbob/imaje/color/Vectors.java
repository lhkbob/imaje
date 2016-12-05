/*
 * BSD 3-Clause License - imaJe
 *
 * Copyright (c) 2016, Michael Ludwig
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.lhkbob.imaje.color;

import com.lhkbob.imaje.util.Arguments;

/**
 * Vectors
 * =======
 *
 * Defines mathematical operations on generically-typed {@link Vector Vectors} as static functions.
 * The last argument of each function takes a `result` parameter that is modified to contain the
 * answer. The input arguments are not modified. All functions can use the `result` vector as one of
 * the inputs without affecting the validity of the result, but in this case the inputs are
 * technically modified.
 *
 * @author Michael Ludwig
 */
public final class Vectors {
  private Vectors() {

  }

  /**
   * Set all channel values of the vector to 0.
   *
   * @param result
   *     The vector to modify
   * @param <V>
   *     The Vector type
   * @param <S>
   *     The VectorSpace type
   */
  public static <V extends Vector<V, S>, S extends VectorSpace<V, S>> void zero(V result) {
    double[] c = result.getChannels();
    for (int i = 0; i < c.length; i++) {
      c[i] = 0.0;
    }
  }

  /**
   * Scale `vector` by `c`. Set the channel values of `result` to `vector * c`, the channel values
   * of the input vector scaled by the scalar.
   *
   * @param vector
   *     The input vector that is scaled
   * @param c
   *     The scalar factor
   * @param result
   *     The vector to modify
   * @param <V>
   *     The Vector type
   * @param <S>
   *     The VectorSpace type
   */
  public static <V extends Vector<V, S>, S extends VectorSpace<V, S>> void scale(
      V vector, double c, V result) {
    Arguments.equals("vector space", vector.getVectorSpace(), result.getVectorSpace());

    double[] in = vector.getChannels();
    double[] out = result.getChannels();
    for (int i = 0; i < in.length; i++) {
      out[i] = in[i] * c;
    }
  }

  /**
   * Add `a` and `b` together, summing each corresponding component. Set the channel values of
   * `result` to the sum of the corresponding channels in `a` and `b`.
   *
   * @param a
   *     The first term
   * @param b
   *     The second term
   * @param result
   *     The vector to modify
   * @param <V>
   *     The Vector type
   * @param <S>
   *     The VectorSpace type
   */
  public static <V extends Vector<V, S>, S extends VectorSpace<V, S>> void add(V a, V b, V result) {
    Arguments.equals("vector space", a.getVectorSpace(), result.getVectorSpace());
    Arguments.equals("vector space", b.getVectorSpace(), result.getVectorSpace());

    double[] inA = a.getChannels();
    double[] inB = b.getChannels();
    double[] out = result.getChannels();
    for (int i = 0; i < inA.length; i++) {
      out[i] = inA[i] + inB[i];
    }
  }

  /**
   * Subtract `b` from `a`. Set the channel values of `result` to the subtraction of the
   * corresponding channels, `a - b`.
   *
   * @param a
   *     The first term
   * @param b
   *     The second term
   * @param result
   *     The vector to modify
   * @param <V>
   *     The Vector type
   * @param <S>
   *     The VectorSpace type
   */
  public static <V extends Vector<V, S>, S extends VectorSpace<V, S>> void sub(V a, V b, V result) {
    Arguments.equals("vector space", a.getVectorSpace(), result.getVectorSpace());
    Arguments.equals("vector space", b.getVectorSpace(), result.getVectorSpace());

    double[] inA = a.getChannels();
    double[] inB = b.getChannels();
    double[] out = result.getChannels();
    for (int i = 0; i < inA.length; i++) {
      out[i] = inA[i] - inB[i];
    }
  }

  /**
   * Multiply component-wise `a` and `b`. Set the channel values of `result` to the product of the
   * corresponding components of `a` and `b`.
   *
   * @param a
   *     The first vector in the product
   * @param b
   *     The second vector in the product
   * @param result
   *     The vector to modify
   * @param <V>
   *     The Vector type
   * @param <S>
   *     The VectorSpace type
   */
  public static <V extends Vector<V, S>, S extends VectorSpace<V, S>> void mul(V a, V b, V result) {
    Arguments.equals("vector space", a.getVectorSpace(), result.getVectorSpace());
    Arguments.equals("vector space", b.getVectorSpace(), result.getVectorSpace());

    double[] inA = a.getChannels();
    double[] inB = b.getChannels();
    double[] out = result.getChannels();
    for (int i = 0; i < inA.length; i++) {
      out[i] = inA[i] * inB[i];
    }
  }

  /**
   * Add a constant `c` to the `vector`. Set the channel values of
   * `result` to the value of each channel in `a` summed with `c`.
   *
   * @param vector
   *     The vector
   * @param c
   *     The scalar added to `vector`
   * @param result
   *     The vector to modify
   * @param <V>
   *     The Vector type
   * @param <S>
   *     The VectorSpace type
   */
  public static <V extends Vector<V, S>, S extends VectorSpace<V, S>> void add(
      V vector, double c, V result) {
    Arguments.equals("vector space", vector.getVectorSpace(), result.getVectorSpace());

    double[] in = vector.getChannels();
    double[] out = result.getChannels();
    for (int i = 0; i < in.length; i++) {
      out[i] = in[i] + c;
    }
  }

  /**
   * Subtract a constant `c` from the `vector`. Set the channel values of
   * `result` to the value of each channel in `a` minus with `c`.
   *
   * @param vector
   *     The vector
   * @param c
   *     The scalar added to `vector`
   * @param result
   *     The vector to modify
   * @param <V>
   *     The Vector type
   * @param <S>
   *     The VectorSpace type
   */
  public static <V extends Vector<V, S>, S extends VectorSpace<V, S>> void sub(
      V vector, double c, V result) {
    Arguments.equals("vector space", vector.getVectorSpace(), result.getVectorSpace());

    double[] in = vector.getChannels();
    double[] out = result.getChannels();
    for (int i = 0; i < in.length; i++) {
      out[i] = in[i] - c;
    }
  }

  /**
   * Add a scaled `b` to `a`, specifically computing `a + scaledB * b`. This is a common operation
   * that is combined into a single action instead of requiring a scale and separate add call. Sets
   * the channel values of `result` to the sum of `a` and `scaledB * b`.
   *
   * @param a
   *     The first term
   * @param b
   *     The second term added, after being scaled by `scaleB`
   * @param scaleB
   *     The scalar implicitly multiplied with `b` before adding
   * @param result
   *     The vector to modify
   * @param <V>
   *     The Vector type
   * @param <S>
   *     The VectorSpace type
   */
  public static <V extends Vector<V, S>, S extends VectorSpace<V, S>> void addScaled(
      V a, V b, double scaleB, V result) {
    Arguments.equals("vector space", a.getVectorSpace(), result.getVectorSpace());
    Arguments.equals("vector space", b.getVectorSpace(), result.getVectorSpace());

    double[] inA = a.getChannels();
    double[] inB = b.getChannels();
    double[] out = result.getChannels();
    for (int i = 0; i < inA.length; i++) {
      out[i] = inA[i] + scaleB * inB[i];
    }
  }
}
