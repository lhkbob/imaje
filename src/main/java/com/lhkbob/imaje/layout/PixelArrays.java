package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.Images;
import com.lhkbob.imaje.data.ByteData;
import com.lhkbob.imaje.data.DataBuffer;
import com.lhkbob.imaje.data.DoubleData;
import com.lhkbob.imaje.data.FloatData;
import com.lhkbob.imaje.data.IntData;
import com.lhkbob.imaje.data.LongData;
import com.lhkbob.imaje.data.ShortData;
import com.lhkbob.imaje.data.types.CustomBinaryData;
import com.lhkbob.imaje.data.types.SignedInteger;
import com.lhkbob.imaje.data.types.SignedNormalizedInteger;
import com.lhkbob.imaje.data.types.UnsignedInteger;
import com.lhkbob.imaje.data.types.UnsignedNormalizedInteger;
import com.lhkbob.imaje.util.Arguments;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;


/**
 *
 */
public final class PixelArrays {
  private PixelArrays() {}

  public static List<PixelArray> getHierarchy(PixelArray array) {
    List<PixelArray> hierarchy = new ArrayList<>();
    while (array != null) {
      hierarchy.add(array);
      array = array.getParent();
    }
    return hierarchy;
  }

  public static RootPixelArray getRoot(PixelArray array) {
    while (array.getParent() != null) {
      array = array.getParent();
    }
    return (RootPixelArray) array;
  }

  public static PixelArray createSubImageForRaster(PixelArray parent, int x, int y, int w, int h) {
    return new SubImagePixelArray(parent, x, y, w, h);
  }

  public static List<PixelArray> createSubImagesForMipmap(
      List<PixelArray> parents, int x, int y, int w, int h) {
    // Because w and h are necessary <= to the base width and height, then the max mipmaps of the
    // subimage pyramid must be <= to the size of parents.
    int mipmapCount = Images.getMaxMipmaps(w, h);

    List<PixelArray> subMips = new ArrayList<>(mipmapCount);
    for (int i = 0; i < mipmapCount; i++) {
      // The subimage size for the mipmap level is straightforwardly based on base subimage's size
      int mipWidth = Images.getMipmapDimension(w, i);
      int mipHeight = Images.getMipmapDimension(h, i);

      // The offset into the mipmap is offset / 2^i, flooring and clamping to 0, which accounts
      // for the effective pixel size doubling when moving from one level to a lower resolution one
      int mipX = Math.max(x >> i, 0);
      int mipY = Math.max(y >> i, 0);

      subMips.add(createSubImageForRaster(parents.get(i), mipX, mipY, mipWidth, mipHeight));
    }

    return subMips;
  }

  public static List<PixelArray> createSubImagesForArray(
      List<PixelArray> parents, int x, int y, int w, int h) {
    List<PixelArray> subImages = new ArrayList<>(parents.size());
    for (PixelArray parent : parents) {
      subImages.add(createSubImageForRaster(parent, x, y, w, h));
    }
    return subImages;
  }

  public static List<List<PixelArray>> createSubImagesForMipmapArray(
      List<List<PixelArray>> parents, int x, int y, int w, int h) {
    List<List<PixelArray>> subLayers = new ArrayList<>(parents.size());
    for (List<PixelArray> mipmaps : parents) {
      subLayers.add(createSubImagesForMipmap(mipmaps, x, y, w, h));
    }
    return subLayers;
  }

  public static List<PixelArray> createSubImagesForVolume(
      List<PixelArray> parents, int x, int y, int z, int w, int h, int d) {
    List<PixelArray> subImages = new ArrayList<>(d);
    for (int i = z; i < z + d; i++) {
      subImages.add(createSubImageForRaster(parents.get(i), x, y, w, h));
    }
    return subImages;
  }

  public static List<List<PixelArray>> createSubImagesForMipmapVolume(
      List<List<PixelArray>> parents, int x, int y, int z, int w, int h, int d) {
    // This process follows the same pattern as in createSubImagesForMipmap except that it handles
    // three dimensions, thus relying on createSubImagesForVolume for the inner subimage creation.
    int mipmapCount = Images.getMaxMipmaps(w, h, d);

    List<List<PixelArray>> subMips = new ArrayList<>(mipmapCount);
    for (int i = 0; i < mipmapCount; i++) {
      int mipWidth = Images.getMipmapDimension(w, i);
      int mipHeight = Images.getMipmapDimension(h, i);
      int mipDepth = Images.getMipmapDimension(d, i);

      int mipX = Math.max(x >> i, 0);
      int mipY = Math.max(y >> i, 0);
      int mipZ = Math.max(z >> i, 0);

      subMips.add(createSubImagesForVolume(parents.get(i), mipX, mipY, mipZ, mipWidth, mipHeight,
          mipDepth));
    }

    return subMips;
  }

  public static void checkBufferCompatible(
      DataBuffer data, PixelFormat.Type channelType, int channelBitSize) {
    // Make sure that elements of data can act as a data field with the given type semantics and
    // bits
    if (channelBitSize != data.getBitSize()) {
      throw new IllegalArgumentException(
          "Channel bit size is incompatible with data source bit size, expected " + channelBitSize
              + " but was " + data.getBitSize());
    }

    boolean badType = true;
    switch (channelType) {
    case UINT:
    case USCALED:
      // Must go through a known type with UINT semantics
      if (data instanceof CustomBinaryData && ((CustomBinaryData) data)
          .getBinaryRepresentation() instanceof UnsignedInteger) {
        badType = false;
      }
    case SINT:
    case SSCALED:
      // Native short, int, long, byte is preferred
      if (data instanceof ByteData || data instanceof ShortData || data instanceof IntData
          || data instanceof LongData) {
        badType = false;
      } else if (data instanceof CustomBinaryData && ((CustomBinaryData) data)
          .getBinaryRepresentation() instanceof SignedInteger) {
        badType = false;
      }
      break;
    case UNORM:
      // Must go through a known type with UNORM semantics
      if (data instanceof CustomBinaryData && ((CustomBinaryData) data)
          .getBinaryRepresentation() instanceof UnsignedNormalizedInteger) {
        badType = false;
      }
      break;
    case SNORM:
      // Must go through a known type with SNORM semantics
      if (data instanceof CustomBinaryData && ((CustomBinaryData) data)
          .getBinaryRepresentation() instanceof SignedNormalizedInteger) {
        badType = false;
      }
      break;
    case SFLOAT:
      // Native float or double is preferred
      if (data instanceof FloatData || data instanceof DoubleData) {
        badType = false;
      } else if (data instanceof CustomBinaryData) {
        // Only other valid source type is a BinaryNumericSource with one of the known SFLOAT types
        CustomBinaryData d = (CustomBinaryData) data;
        if (d.getBinaryRepresentation().isFloatingPoint() && !d.getBinaryRepresentation()
            .isUnsigned()) {
          badType = false;
        }
      }
      break;
    case UFLOAT:
      if (data instanceof CustomBinaryData) {
        CustomBinaryData d = (CustomBinaryData) data;
        if (d.getBinaryRepresentation().isFloatingPoint() && d.getBinaryRepresentation()
            .isUnsigned()) {
          badType = false;
        }
      }
      break;
    }

    if (badType) {
      throw new IllegalArgumentException(
          "Unsupported DataBuffer type (" + data + ") for pixel format type " + channelType);
    }
  }

  public static void copy(
      PixelArray src, int sx, int sy, PixelArray dst, int dx, int dy, int width, int height) {
    // Validate target dimensions
    Arguments.checkArrayRange("source window width", src.getWidth(), sx, width);
    Arguments.checkArrayRange("source window height", src.getHeight(), sy, height);
    Arguments.checkArrayRange("dest window width", dst.getWidth(), dx, width);
    Arguments.checkArrayRange("dest window height", dst.getHeight(), dy, height);

    // Make sure there are equal number of color channels
    if (src.getColorChannelCount() != dst.getColorChannelCount()) {
      throw new IllegalArgumentException(String
          .format("Color channel counts are not equal for src (%d) and dst (%d) arrays",
              src.getColorChannelCount(), dst.getColorChannelCount()));
    }

    // Check read-only on dst.
    if (dst.isReadOnly()) {
      throw new IllegalStateException("Cannot modify destination pixel array, it is read-only");
    }

    copyInternal(src, sx, sy, dst, dx, dy, width, height);
  }

  private static ImageWindow filterSubImageAndGetRootWindow(
      int x, int y, int width, int height, List<PixelArray> hierarchy) {
    ImageWindow window = new ImageWindow(x, y, width, height);
    Iterator<PixelArray> it = hierarchy.iterator();
    while (it.hasNext()) {
      PixelArray p = it.next();
      p.toParentWindow(window);
      if (p instanceof SubImagePixelArray) {
        it.remove();
      }
    }

    return window;
  }

  private static boolean areReorientedArraysCompatible(ReorientedArray s, ReorientedArray d) {
    // This logic is the same as checking their orientation option sets for equality but is
    // more efficient and avoids allocation.
    return s.isColumnMajor() == d.isColumnMajor() && s.isRightToLeft() == d.isColumnMajor()
        && s.isTopToBottom() == d.isTopToBottom();
  }

  private static void copyInternal(
      PixelArray src, int sx, int sy, PixelArray dst, int dx, int dy, int width, int height) {
    List<PixelArray> srcStack = getHierarchy(src);
    List<PixelArray> dstStack = getHierarchy(dst);

    // Check the root compatibility first since it can reject optimization in constant time
    RootPixelArray srcRoot = (RootPixelArray) srcStack.get(srcStack.size() - 1);
    RootPixelArray dstRoot = (RootPixelArray) dstStack.get(dstStack.size() - 1);
    if (srcRoot.getBandCount() != dstRoot.getBandCount() || !Objects
        .equals(srcRoot.getClass(), dstRoot.getClass()) || !Objects.equals(srcRoot.getFormat(), dstRoot.getFormat())) {
      copyPixelByPixel(src, sx, sy, dst, dx, dy, width, height);
      return;
    }

    // Strip out all sub-image pixel arrays and update the source and dest. offsets.
    // SubImageArrays are removed since they only affect the coordinate system and by removing
    // them, the remaining hierarchy represents additional transforms on the root data that
    // must be equivalent to do bulk copies.
    ImageWindow srcWindow = filterSubImageAndGetRootWindow(sx, sy, width, height, srcStack);
    ImageWindow dstWindow = filterSubImageAndGetRootWindow(dx, dy, width, height, dstStack);

    if (srcWindow.getWidth() != dstWindow.getWidth() || srcWindow.getHeight() != dstWindow
        .getHeight()) {
      copyPixelByPixel(src, sx, sy, dst, dx, dy, width, height);
      return;
    }

    // The remaining pixel arrays in src/dstStack represent potentially unknown transformations upon
    // the image data. This is a primitive heuristic to check if the two sets of transformations are
    // equivalent.
    boolean compatible = true;
    if (srcStack.size() == dstStack.size()) {
      // Check each paired PixelArray, but can skip the last PixelArray in each because those are
      // the two roots, which have already been checked for compatibility at the start.
      for (int i = 0; i < srcStack.size() - 1; i++) {
        PixelArray s = srcStack.get(i);
        PixelArray d = dstStack.get(i);

        if (Objects.equals(s.getClass(), d.getClass())) {
          if (s instanceof ReorientedArray) {
            if (!areReorientedArraysCompatible((ReorientedArray) s, (ReorientedArray) d)) {
              compatible = false;
              break;
            }
          } else {
            // Unknown implementation so it's not possible to check internal state for compatibility
            // TODO update this to support TransformedPixelArray
            compatible = false;
            break;
          }
        } else {
          // The two arrays are different implementations so hierarchy is definitely incompatible
          compatible = false;
          break;
        }
      }
    } else {
      compatible = false;
    }

    if (!compatible) {
      // Fall back to pixel-wise copy going through the entire pixel array stack
      copyPixelByPixel(src, sx, sy, dst, dx, dy, width, height);
      return;
    }

    // At this point, the requested copy windows have been converted into the root coordinate
    // system and the potential transformations on the raw data are equivalent. This means that
    // the copy can be optimized by moving raw primitives around in bulk based on the data layouts.
    copyOptimized(srcRoot, srcWindow, dstRoot, dstWindow);
  }

  private static void copyOptimized(
      RootPixelArray src, ImageWindow srcWindow, RootPixelArray dst, ImageWindow dstWindow) {
    // The copy is optimized by first iterating over the window with respect to the source layout,
    // which splits it into maximally spanning rows. Then the equivalent row within the dest
    // layout is iterated over, and within that visitor the data is copied over. If the strides
    // match then channel-block copying is done, otherwise it's done per pixel but at least skips
    // the numeric conversions that is required for copyPixelByPixel().
    boolean srcPixelPacked = isPixelPacked(src);
    boolean dstPixelPacked = isPixelPacked(dst);
    int bandCount = src.getBandCount();

    src.getLayout().iterateWindow(srcWindow, (srcX, srcY, srcStride, srcLength, srcOffsets) -> {
      // Invoked for a continuous row of data in the source image, so visit the corresponding
      // row in the destination image.
      int deltaSrcX = srcX - srcWindow.getX();
      int deltaSrcY = srcY - srcWindow.getY();
      dst.getLayout()
          .iterateRow(dstWindow.getX() + deltaSrcX, dstWindow.getY() + deltaSrcY, srcLength,
              (dstX, dstY, dstStride, dstLength, dstOffsets) -> {
                // Invoked in a continuous sub row of data in the dest image, essentially segmenting
                // the row that's being copied inton contiguous chunks.
                int deltaDstX = dstX - dstWindow.getX() - deltaSrcX;
                if (srcPixelPacked && dstPixelPacked) {
                  // Since both source and dest. are pixel-packed and earlier validation ensures
                  // band counts are equal, we can copy dstLength * stride elements in one go.
                  dst.getData(0)
                      .set(dstOffsets[0], src.getData(0), srcOffsets[0] + deltaDstX * bandCount,
                          dstLength * bandCount);
                } else if (srcStride == 1 && dstStride == 1) {
                  // Copy each band block separately using # band copies
                  for (int b = 0; b < bandCount; b++) {
                    dst.getData(b)
                        .set(dstOffsets[b], src.getData(b), srcOffsets[b] + deltaDstX, dstLength);
                  }
                } else {
                  // Copy each band of each pixel independently
                  for (int b = 0; b < bandCount; b++) {
                    for (int x = 0; x < dstLength; x++) {
                      dst.getData(b).set(dstOffsets[b] + x * dstStride, src.getData(b),
                          srcOffsets[b] + (x + deltaDstX) * srcStride, 1);
                    }
                  }
                }
              });
    });
  }

  private static boolean isPixelPacked(RootPixelArray array) {
    DataLayout layout = array.getLayout();
    if (layout instanceof TileInterleaveLayout) {
      TileInterleaveLayout l = (TileInterleaveLayout) layout;
      if (l.getInterleavingUnit() != TileInterleaveLayout.InterleavingUnit.PIXEL) {
        return false;
      }
    } else if (!(layout instanceof ScanlineLayout)) {
      return false;
    }

    // Even if the layout uses pixel based interleaving, must make sure that all bands access
    // the same data buffer.
    DataBuffer data = array.getData(0);
    for (int i = 1; i < array.getBandCount(); i++) {
      if (data != array.getData(i)) {
        return false;
      }
    }

    return true;
  }

  private static void copyPixelByPixel(
      PixelArray src, int sx, int sy, PixelArray dst, int dx, int dy, int width, int height) {
    double[] color = new double[src.getColorChannelCount()];
    long[] srcIndices = new long[src.getBandCount()];
    long[] dstIndices = new long[src.getBandCount()];

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        double alpha = src.get(sx + x, sy + y, color, srcIndices);
        dst.set(dx + x, dy + y, color, alpha, dstIndices);
      }
    }
  }
}
