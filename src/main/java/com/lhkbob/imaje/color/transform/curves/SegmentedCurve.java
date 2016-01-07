package com.lhkbob.imaje.color.transform.curves;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class SegmentedCurve implements Curve {
  private final double[] segmentBreakpoints;
  private final List<Curve> segments;

  public SegmentedCurve(List<Curve> segments) {
    if (segments.isEmpty()) {
      throw new IllegalArgumentException("Must provide at least one curve segment");
    }

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
    if (!(o instanceof SegmentedCurve)) {
      return false;
    }
    SegmentedCurve c = (SegmentedCurve) o;
    return c.segments.equals(segments);
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
  public Curve inverted() {
    // the constructor verified that the domain is a continuous interval, but the function
    // segments themselves could have discontinuous values (if so then it can't be inverted)
    List<Curve> inverseSegments = new ArrayList<>();

    Curve invSeg = segments.get(0).inverted();
    if (invSeg == null) {
      // segment can't be inverted so entire inverse is undefined
      return null;
    } else {
      inverseSegments.add(invSeg);
    }
    int monotonicity = 0;

    for (int i = 1; i < segments.size(); i++) {
      Curve left = segments.get(i - 1);
      Curve right = segments.get(i);

      // Using the curves reported min/max values avoids numerical imprecision issues that cause
      // unintended disconnects in the domain
      double ly = left.evaluate(left.getDomainMax());
      double ry = right.evaluate(right.getDomainMin());
      if (Math.abs(ly - ry) > EPS) {
        // discontinuous
        return null;
      }

      // The left curve has already been inverted and added to inverseSegments, so try and invert
      // the right curve and make sure it doesn't overlap with the previous inverted segments
      invSeg = right.inverted();
      if (invSeg == null) {
        return null;
      }

      if (monotonicity == 0) {
        // haven't determined how to order the inverted segments
        if (Math.abs(inverseSegments.get(0).getDomainMax() - invSeg.getDomainMin()) < EPS) {
          // positively ordered, so new inverse segment goes to the right
          monotonicity = 1;
          inverseSegments.add(invSeg);
        } else if (Math.abs(inverseSegments.get(0).getDomainMin() - invSeg.getDomainMin()) < EPS) {
          // negatively ordered, so new inverse segment goes to the left
          monotonicity = -1;
          inverseSegments.add(0, invSeg);
        } else {
          // Somehow doesn't line up like expected so can't invert
          return null;
        }
      } else if (monotonicity > 0) {
        // expect the last segment's max to equal this new segment's min
        if (Math.abs(
            inverseSegments.get(inverseSegments.size() - 1).getDomainMax() - invSeg.getDomainMin())
            < EPS) {
          inverseSegments.add(invSeg);
        } else {
          return null;
        }
      } else {
        // expect the first segment's min to equal this new segment's max
        if (Math.abs(inverseSegments.get(0).getDomainMin() - invSeg.getDomainMax()) < EPS) {
          inverseSegments.add(0, invSeg);
        } else {
          return null;
        }
      }
    }

    return new SegmentedCurve(inverseSegments);
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
