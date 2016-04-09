package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.data.BitDataSource;

import java.util.Arrays;
import java.util.function.Predicate;

import static com.lhkbob.imaje.layout.GPUFormat.Type.SNORM;
import static com.lhkbob.imaje.layout.GPUFormat.Type.SRGB;
import static com.lhkbob.imaje.layout.GPUFormat.Type.SSCALED;
import static com.lhkbob.imaje.layout.GPUFormat.Type.UNORM;
import static com.lhkbob.imaje.layout.GPUFormat.Type.USCALED;
import static com.lhkbob.imaje.layout.GPUFormat.dataType;
import static com.lhkbob.imaje.layout.GPUFormat.packedLayout;

/**
 *
 */
public class PackedFixedPointPixelEncoder extends PixelEncoder {
  private final long[] signBitMasks;

  private final long[] masks;
  private final long[] shifts;
  private final double[] normScalar;
  private final double[] clampMin;
  private final double[] clampMax;

  private final int[] bitLayout; // Must save for getFormatFilter()

  private final BitDataSource data;

  public PackedFixedPointPixelEncoder(int[] bitLayout, boolean normalized, boolean signed,
      int[] dataToLogicalChannelMap, PixelLayout layout, BitDataSource data) {
    super(dataToLogicalChannelMap, layout);
    if (bitLayout.length != dataToLogicalChannelMap.length)
      throw new IllegalArgumentException(
          "Bit layout is incompatible with channel mapping, requires " + dataToLogicalChannelMap.length + " data channels, but layout provides " + bitLayout.length);
    if (layout.getChannelCount() != 1) {
      throw new IllegalArgumentException(
          "All color channel values must be packed into a single data channel");
    }
    if (data.getLength() < layout.getRequiredDataElements()) {
      throw new IllegalArgumentException(
          "Data source does not have sufficient elements for image layout");
    }

    // Convert sequential channel bit allocations into masks and shifts to extract those ranges
    masks = new long[bitLayout.length];
    shifts = new long[bitLayout.length];
    normScalar = (normalized ? new double[bitLayout.length] : null);
    signBitMasks = (signed ? new long[bitLayout.length] : null);
    clampMin = new double[bitLayout.length];
    clampMax = new double[bitLayout.length];

    // Validate the channel bits (count from the back so we can use the intermediate totalBits
    // value as the shift for that channel)
    int totalBits = 0;
    for (int i = bitLayout.length - 1; i >= 0; i--) {
      shifts[i] = totalBits;
      if (signed)
        signBitMasks[i] = 1 << (bitLayout[i] - 1);

      masks[i] = (1 << bitLayout[i]) - 1;

      if (normalized) {
        if (signed) {
          normScalar[i] = signBitMasks[i] - 1;
          clampMin[i] = -1.0;
          clampMax[i] = 1.0;
        } else {
          normScalar[i] = masks[i];
          clampMin[i] = 0.0;
          clampMax[i] = 1.0;
        }
      } else {
        clampMin[i] = Double.NEGATIVE_INFINITY;
        clampMax[i] = Double.POSITIVE_INFINITY;
      }

      totalBits += bitLayout[i];
    }

    if (totalBits != data.getBitSize()) {
      throw new IllegalArgumentException("Bit layout's total number of bits (" + totalBits + ") is not equal to bit size of data source (" + data.getBitSize() + ")");
    }
    this.data = data;
    this.bitLayout = Arrays.copyOf(bitLayout, bitLayout.length);
  }

  @Override
  public BitDataSource getData() {
    return data;
  }

  @Override
  public double get(int x, int y, double[] channelValues, int offset) {
    long index = layout.getChannelIndex(x, y, 0);
    return unpack(data.getBits(index), channelValues, offset);
  }

  @Override
  public double get(int x, int y, double[] channelValues, int offset, long[] channels) {
    layout.getChannelIndices(x, y, channels);
    return unpack(data.getBits(channels[0]), channelValues, offset);
  }

  @Override
  public double getAlpha(int x, int y) {
    if (hasAlphaChannel()) {
      long index = layout.getChannelIndex(x, y, 0);
      return bitFieldToDouble(data.getBits(index), alphaDataChannelIndex);
    } else {
      return 1.0;
    }
  }

  @Override
  public void set(int x, int y, double[] channelValues, int offset, double a) {
    long index = layout.getChannelIndex(x, y, 0);
    data.setBits(index, pack(channelValues, offset, a));
  }

  @Override
  public void set(int x, int y, double[] channelValues, int offset, double a, long[] channels) {
    layout.getChannelIndices(x, y, channels);
    data.setBits(channels[0], pack(channelValues, offset, a));
  }

  @Override
  public void setAlpha(int x, int y, double alpha) {
    if (hasAlphaChannel()) {
      long index = layout.getChannelIndex(x, y, 0);
      long bits = data.getBits(index);

      // Zero out original bit field
      long alphaMask = masks[alphaDataChannelIndex] << shifts[alphaDataChannelIndex];
      bits &= ~alphaMask;

      // Calculate new alpha bit field and or it into the remaining value, store into data source
      data.setBits(index, bits | doubleToBitField(alpha, alphaDataChannelIndex));
    } // else ignore set request
  }

  private long pack(double[] values, int offset, double a) {
    long bits = 0;
    for (int i = 0; i < logicalToDataChannelMap.length; i++) {
      int dataChannel = logicalToDataChannelMap[i];
      bits |= doubleToBitField(values[offset + i], dataChannel);
    }
    if (hasAlphaChannel()) {
      bits |= doubleToBitField(a, alphaDataChannelIndex);
    }

    return bits;
  }

  private double unpack(long bits, double[] values, int offset) {
    for (int i = 0; i < logicalToDataChannelMap.length; i++) {
      int dataChannel = logicalToDataChannelMap[i];
      values[offset + i] = bitFieldToDouble(bits, dataChannel);
    }

    if (hasAlphaChannel()) {
      return bitFieldToDouble(bits, alphaDataChannelIndex);
    } else {
      return 1.0;
    }
  }

  private double bitFieldToDouble(long bits, int field) {
    // First shift everything over so the LSB of the field is at bit 0
    bits >>= shifts[field];
    // Next mask off everything to the right of the MSB of the field
    bits &= masks[field];

    // Handle sign bit if necessary
    if (signBitMasks != null && (bits & signBitMasks[field]) != 0) {
      // First take off the sign bit
      bits &= (signBitMasks[field] - 1);
      // Then subtract the highest bit
      bits -= signBitMasks[field];
    }

    // Scale to double (where these values have been appropriately assigned
    // in the constructor to handle no normalization, signed and unsigned normalization)
    double normalized = bits / (normScalar == null ? 1.0 : normScalar[field]);
    // Then clamp
    if (normalized < clampMin[field])
      return clampMin[field];
    else if (normalized > clampMax[field])
      return clampMax[field];
    else
      return normalized;
  }

  private long doubleToBitField(double value, int field) {
    // First clamp everything to valid range
    if (value < clampMin[field])
      value = clampMin[field];
    else if (value > clampMax[field])
      value = clampMax[field];

    // Scale and round to the nearest integer
    long bits = Math.round(value * (normScalar == null ? 1.0 : normScalar[field]));

    // Handle sign bit if necessary
    if (signBitMasks != null && bits < 0) {
      // Set all but the lower, unsigned bits to 0
      bits &= (signBitMasks[field] - 1);
      // Force the 2's complement bit in this bit field to be 1
      bits |= signBitMasks[field];
    } else {
      // Set all but the field's bits to 0
      bits &= masks[field];
    }

    // Shift to the left so the bit field is aligned with the rest of the packed pixel
    return bits << shifts[field];
  }

  @Override
  public Predicate<GPUFormat> getFormatFilter() {
    Predicate<GPUFormat> typeFilter;
    if (signBitMasks != null) {
      // Dealing with signed data
      if (normScalar != null) {
        // Signed-normalized
        typeFilter = dataType(SNORM);
      } else {
        // Assume SSCALED and outside API can coerce to SINT if necessary
        typeFilter = dataType(SSCALED);
      }
    } else {
      // Unsigned
      if (normScalar != null) {
        typeFilter = dataType(UNORM).or(dataType(SRGB));
      } else {
        typeFilter = dataType(USCALED);
      }
    }

    return super.getFormatFilter().and(packedLayout(bitLayout)).and(typeFilter);
  }
}
