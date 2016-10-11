package com.lhkbob.imaje;

import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.layout.ArrayBackedPixel;
import com.lhkbob.imaje.layout.PixelArray;
import com.lhkbob.imaje.layout.SubImagePixelArray;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.IteratorChain;
import com.lhkbob.imaje.util.SpliteratorChain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;

/**
 *
 */
public class MipmapVolume<T extends Color> implements Image<T> {
  private final List<List<PixelArray>> mipmappedZData;
  private final Class<T> colorType;

  public MipmapVolume(List<Volume<T>> mipmaps) {
    Arguments.notEmpty("mipmaps", mipmaps);

    int[] widths = new int[mipmaps.size()];
    int[] heights = new int[widths.length];
    int[] depths = new int[widths.length];

    int i = 0;
    List<List<PixelArray>> lockedCopy = new ArrayList<>(mipmaps.size());
    for (Volume<T> volume : mipmaps) {
      widths[i] = volume.getWidth();
      heights[i] = volume.getHeight();
      depths[i] = volume.getDepth();
      i++;

      // The pixel arrays list returned by a volume is already immutable, no need to copy
      lockedCopy.add(volume.getPixelArrays());
    }

    checkVolumeMipmapDimensions(widths, heights, depths);

    colorType = mipmaps.get(0).getColorType();
    mipmappedZData = Collections.unmodifiableList(lockedCopy);
  }

  private static void checkVolumeMipmapDimensions(int[] widths, int[] heights, int[] depths) {
    // Validate that the dimension progression, which must be done here instead of using
    // ImageUtils because it won't take into account the 3rd dimension of the volume.
    int w = widths[0];
    int h = heights[0];
    int d = depths[0];
    int mipmapCount = Images.getMaxMipmaps(w, h, d);

    if (widths.length != mipmapCount) {
      throw new IllegalArgumentException(
          "Expected " + mipmapCount + " mipmaps based on dimensions of first volume, but provided "
              + widths.length);
    }

    for (int i = 0; i < mipmapCount; i++) {
      // Make sure width, height, and depth of each volume mipmap level are as expected
      if (Images.getMipmapDimension(w, i) != widths[i]
          || Images.getMipmapDimension(h, i) != heights[i]
          || Images.getMipmapDimension(d, i) != depths[i]) {
        throw new IllegalArgumentException(String.format(
            "Dimension mismatch for mipmap level %d, expected volume to be [%d, %d, %d] but was [%d, %d, %d]",
            i, Images.getMipmapDimension(w, i), Images.getMipmapDimension(h, i),
            Images.getMipmapDimension(d, i), widths[i], heights[i], depths[i]));
      }
    }
  }

  public MipmapVolume(Class<T> colorType, List<List<PixelArray>> mipmappedZData) {
    Arguments.notNull("colorType", colorType);
    Arguments.notEmpty("mipmappedZData", mipmappedZData);

    // Validate that all images are compatible with the color, and that for a given
    // mipmap level they have the same dimensions and format.
    int[] widths = new int[mipmappedZData.size()];
    int[] heights = new int[widths.length];
    int[] depths = new int[widths.length];

    int i = 0;
    List<List<PixelArray>> lockedCopy = new ArrayList<>(mipmappedZData.size());
    for (List<PixelArray> volume : mipmappedZData) {
      Arguments.notEmpty("zData", volume);
      Images.checkImageCompatibility(colorType, volume);
      Images.checkArrayCompleteness(volume);

      // Since the volume is array complete, any slice will have the same width and height
      widths[i] = volume.get(0).getLayout().getWidth();
      heights[i] = volume.get(0).getLayout().getHeight();
      depths[i] = volume.size();
      i++;

      // Make an immutable copy of the volume pixel array
      lockedCopy.add(Collections.unmodifiableList(new ArrayList<>(volume)));
    }

    checkVolumeMipmapDimensions(widths, heights, depths);

    this.colorType = colorType;
    this.mipmappedZData = Collections.unmodifiableList(lockedCopy);
  }

  public MipmapVolume<T> getSubImage(int x, int y, int z, int w, int h, int d) {
    return new MipmapVolume<>(
        colorType,
        SubImagePixelArray.createSubImagesForMipmapVolume(mipmappedZData, x, y, z, w, h, d));
  }

  public PixelArray getPixelArray(int mipmapLevel, int z) {
    return mipmappedZData.get(mipmapLevel).get(z);
  }

  public List<PixelArray> getPixelArraysForMipmap(int mipmapLevel) {
    return mipmappedZData.get(mipmapLevel);
  }

  public List<PixelArray> getPixelArraysForDepthSlice(int z) {
    List<PixelArray> zs = new ArrayList<>(mipmappedZData.size());
    for (List<PixelArray> mipmap : mipmappedZData) {
      // As mipmaps progress, the depth of each volume decreases, so terminate once z is exceeded
      if (z >= mipmap.size()) {
        break;
      }
      zs.add(mipmap.get(z));
    }
    return zs;
  }

  public List<List<PixelArray>> getPixelArrays() {
    return mipmappedZData;
  }

  public Raster<T> getDepthSliceAsRaster(int mipmapLevel, int z) {
    return new Raster<>(colorType, getPixelArray(mipmapLevel, z));
  }

  public Volume<T> getMipmapAsVolume(int mipmapLevel) {
    return new Volume<>(colorType, getPixelArraysForMipmap(mipmapLevel));
  }

  public RasterArray<T> getMipmapAsRasterArray(int mipmapLevel) {
    return new RasterArray<>(colorType, getPixelArraysForMipmap(mipmapLevel));
  }

  public Pixel<T> getPixel(int mipmapLevel, int x, int y, int z) {
    ArrayBackedPixel<T> p = new ArrayBackedPixel<>(
        colorType, getPixelArray(mipmapLevel, z), 0, mipmapLevel, z);
    p.refreshAt(x, y);
    return p;
  }

  public double get(int mipmapLevel, int x, int y, int z, T result) {
    return getPixelArray(mipmapLevel, z).get(x, y, result.getChannels());
  }

  public double getAlpha(int mipmapLevel, int x, int y, int z) {
    return getPixelArray(mipmapLevel, z).getAlpha(x, y);
  }

  public void set(int mipmapLevel, int x, int y, int z, T value) {
    getPixelArray(mipmapLevel, z)
        .set(x, y, value.getChannels(), getPixelArray(mipmapLevel, z).getAlpha(x, y));
  }

  public void set(int mipmapLevel, int x, int y, int z, T value, double alpha) {
    getPixelArray(mipmapLevel, z).set(x, y, value.getChannels(), alpha);
  }

  public void setAlpha(int mipmapLevel, int x, int y, int z, double alpha) {
    getPixelArray(mipmapLevel, z).setAlpha(x, y, alpha);
  }

  @Override
  public int getLayerCount() {
    return 1;
  }

  @Override
  public int getMipmapCount() {
    return mipmappedZData.size();
  }

  @Override
  public Pixel<T> getPixel(int layer, int mipmapLevel, int... coords) {
    Arguments.equals("layer", 0, layer);
    Arguments.equals("coords.length", 3, coords.length);
    return getPixel(mipmapLevel, coords[0], coords[1], coords[2]);
  }

  @Override
  public Class<T> getColorType() {
    return colorType;
  }

  @Override
  public boolean hasAlphaChannel() {
    return mipmappedZData.get(0).get(0).getFormat().hasAlphaChannel();
  }

  @Override
  public int getDimensionality() {
    return 3;
  }

  @Override
  public int getDimension(int dim) {
    if (dim == 0) {
      return getPixelArray(0, 0).getLayout().getWidth();
    } else if (dim == 1) {
      return getPixelArray(0, 0).getLayout().getHeight();
    } else if (dim == 2) {
      return getPixelArraysForMipmap(0).size();
    } else {
      return 1;
    }
  }

  @Override
  public Iterator<Pixel<T>> iterator() {
    // Each mipmap level is going to have its own depth value, so don't cache the inner loop size
    List<Iterator<Pixel<T>>> wrapped = new ArrayList<>();
    for (int i = 0; i < mipmappedZData.size(); i++) {
      List<PixelArray> mip = mipmappedZData.get(i);
      for (int z = 0; z < mip.size(); z++) {
        wrapped.add(ArrayBackedPixel.iterator(colorType, mip.get(z), 0, i, z));
      }
    }
    return new IteratorChain<>(wrapped);
  }

  @Override
  public Spliterator<Pixel<T>> spliterator() {
    // Each mipmap level is going to have its own depth value, so don't cache the inner loop size
    List<Spliterator<Pixel<T>>> wrapped = new ArrayList<>();
    for (int i = 0; i < mipmappedZData.size(); i++) {
      List<PixelArray> mip = mipmappedZData.get(i);
      for (int z = 0; z < mip.size(); z++) {
        wrapped.add(ArrayBackedPixel.spliterator(colorType, mip.get(z), 0, i, z));
      }
    }
    return new SpliteratorChain<>(wrapped);
  }
}
