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
package com.lhkbob.imaje.color.transform.curves;

import com.lhkbob.imaje.util.Arguments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * PiecewiseCurve
 * ==============
 *
 * A curve that is defined as a piecewise combination of functions defined on connected domain
 * ranges. This is represented as a list of curves that define the function for each section of the
 * net domain. The net domain is defined as the lower bound of the first curve's domain and the
 * upper bound of the last curve's domain. The upper bound of a curve segment's domain must equal
 * the lower bound of the subsequent curve's domain, ensuring that the piecewise function is defined
 * over the entire domain.
 *
 * @author Michael Ludwig
 */
public class PiecewiseCurve implements Curve {
  private final double[] segmentBreakpoints;
  private final List<Curve> segments;

  /**
   * Create a PiecewiseCurve based on the list of `segments`.
   *
   * @param segments
   *     The function definitions for each piece of the curve
   * @throws IllegalArgumentException
   *     if `segments` is empty or if the pieces' domains are not contiguous
   */
  public PiecewiseCurve(List<Curve> segments) {
    Arguments.notEmpty("segments", segments);

    // Confirm that segments are defined over a contiguous interval, even if
    // there are discontinuities (which is only worried about in inverted()).
    segmentBreakpoints = new double[segments.size()];
    segmentBreakpoints[0] = segments.get(0).getDomainMin();
    for (int i = 1; i < segments.size(); i++) {
      double left = segments.get(i - 1).getDomainMax();
      double right = segments.get(i).getDomainMin();
      if (Math.abs(left - right) >= EPS) {
        throw new IllegalArgumentException("Curve segments do not form contiguous interval");
      }
      segmentBreakpoints[i] = right;
    }
    this.segments = new ArrayList<>(segments);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof PiecewiseCurve)) {
      return false;
    }
    PiecewiseCurve c = (PiecewiseCurve) o;
    return Objects.equals(c.segments, segments);
  }

  @Override
  public double evaluate(double x) {
    int idx = Arrays.binarySearch(segmentBreakpoints, x);
    if (idx >= 0) {
      // Highly unlikely, but the input is exactly on the boundary of the curve
      return segments.get(idx).evaluate(x);
    } else {
      // idx = -insert - 1 -> insert = -(idx + 1)
      int insert = -(idx + 1);
      // insert represents the first breakpoint > x, but the breakpoints were set to the
      // corresponding segment's domain min. Thus, we really want to evaluate insert - 1.
      // If insert - 1 < 0, x is less than the leftmost minimum. If insert - 1 == length - 1
      // then we are in the last segment and must compare it to that segments domain maximum.
      if (insert <= 0) {
        // x is to the left of the leftmost minimum, so it's out of the domain interval
        return Double.NaN;
      } else if (insert >= segments.size()) {
        // x is right of the rightmost minimum, so compare it to the rightmost's max
        if (x > segments.get(segments.size() - 1).getDomainMax()) {
          return Double.NaN;
        }
      }

      // Otherwise, x belongs to the domain interval of segments[insert - 1]. But since we
      // allowed for EPS error between segment intervals, clamp it to the segments domain so that
      // there are not spurious NaNs within the domain.
      Curve s = segments.get(insert - 1);
      double clamped = Math.max(s.getDomainMin(), Math.min(x, s.getDomainMax()));
      return s.evaluate(clamped);
    }
  }

  @Override
  public double getDomainMax() {
    return segments.get(segments.size() - 1).getDomainMax();
  }

  @Override
  public double getDomainMin() {
    return segments.get(0).getDomainMin();
  }

  @Override
  public int hashCode() {
    return segments.hashCode();
  }

  @Override
  public Optional<Curve> inverted() {
    // the constructor verified that the domain is a continuous interval, but the function
    // segments themselves could have discontinuous values (if so then it can't be inverted)
    List<Curve> inverseSegments = new ArrayList<>(segments.size());

    Optional<Curve> invSeg = segments.get(0).inverted();
    if (invSeg.isPresent()) {
      inverseSegments.add(invSeg.get());
    } else {
      // segment can't be inverted so entire inverse is undefined
      return Optional.empty();
    } int monotonicity = 0;

    for (int i = 1; i < segments.size(); i++) {
      Curve left = segments.get(i - 1);
      Curve right = segments.get(i);

      // Using the curves reported min/max values avoids numerical imprecision issues that cause
      // unintended disconnects in the domain
      double ly = left.evaluate(left.getDomainMax());
      double ry = right.evaluate(right.getDomainMin());
      if (Math.abs(ly - ry) > EPS) {
        // discontinuous
        return Optional.empty();
      }

      // The left curve has already been inverted and added to inverseSegments, so try and invert
      // the right curve and make sure it doesn't overlap with the previous inverted segments
      invSeg = right.inverted();
      if (!invSeg.isPresent()) {
        return Optional.empty();
      }

      if (monotonicity == 0) {
        // haven't determined how to order the inverted segments
        if (Math.abs(inverseSegments.get(0).getDomainMax() - invSeg.get().getDomainMin()) < EPS) {
          // positively ordered, so new inverse segment goes to the right
          monotonicity = 1;
          inverseSegments.add(invSeg.get());
        } else if (Math.abs(inverseSegments.get(0).getDomainMin() - invSeg.get().getDomainMin())
            < EPS) {
          // negatively ordered, so new inverse segment goes to the left
          monotonicity = -1;
          inverseSegments.add(0, invSeg.get());
        } else {
          // Somehow doesn't line up like expected so can't invert
          return Optional.empty();
        }
      } else if (monotonicity > 0) {
        // expect the last segment's max to equal this new segment's min
        if (Math.abs(inverseSegments.get(inverseSegments.size() - 1).getDomainMax() - invSeg.get()
            .getDomainMin()) < EPS) {
          inverseSegments.add(invSeg.get());
        } else {
          return Optional.empty();
        }
      } else {
        // expect the first segment's min to equal this new segment's max
        if (Math.abs(inverseSegments.get(0).getDomainMin() - invSeg.get().getDomainMax()) < EPS) {
          inverseSegments.add(0, invSeg.get());
        } else {
          return Optional.empty();
        }
      }
    }

    return Optional.of(new PiecewiseCurve(inverseSegments));
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(String
        .format("x in [%.3f, %.3f], y(x) = piecewise curve:\n", getDomainMin(), getDomainMax()));
    for (int i = 0; i < segments.size(); i++) {
      // This assumes that each segment prints out their own domain, which is reasonable since they
      // are likely bounded curves, or unbounded and then restricted by a DomainWindow (which does
      // print out the domain).
      sb.append("  f").append(i + 1).append(": ").append(segments.get(i));
    }
    return sb.toString();
  }

  private static final double EPS = 1e-8;
}
