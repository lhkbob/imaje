package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.Images;
import com.lhkbob.imaje.data.DataBuffer;
import com.lhkbob.imaje.util.Arguments;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SubImagePixelArray implements PixelArray {
  private final PixelArray parent;

  private final int offsetX;
  private final int offsetY;
  private final int width;
  private final int height;

  public SubImagePixelArray(PixelArray parent, int x, int y, int w, int h) {
    Arguments.notNull("parent", parent);
    Arguments.checkArrayRange("width", parent.getWidth(), x, w);
    Arguments.checkArrayRange("height", parent.getHeight(), y, h);

    this.parent = parent;
    offsetX = x;
    offsetY = y;
    width = w;
    height = h;
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

  @Override
  public DataLayout getLayout() {
    return parent.getLayout();
  }

  @Override
  public DataBuffer getData() {
    return parent.getData();
  }

  @Override
  public PixelFormat getFormat() {
    return parent.getFormat();
  }

  @Override
  public int getColorChannelCount() {
    return parent.getColorChannelCount();
  }

  @Override
  public double get(int x, int y, double[] channelValues) {
    checkCoordinate(x, y);
    return parent.get(offsetX + x, offsetY + y, channelValues);
  }

  @Override
  public double get(int x, int y, double[] channelValues, long[] channels) {
    checkCoordinate(x, y);
    return parent.get(offsetX + x, offsetY + y, channelValues, channels);
  }

  @Override
  public double getAlpha(int x, int y) {
    checkCoordinate(x, y);
    return parent.getAlpha(offsetX + x, offsetY + y);
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a) {
    checkCoordinate(x, y);
    parent.set(offsetX + x, offsetY + y, channelValues, a);
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a, long[] channels) {
    checkCoordinate(x, y);
    parent.set(offsetX + x, offsetY + y, channelValues, a, channels);
  }

  @Override
  public void setAlpha(int x, int y, double alpha) {
    checkCoordinate(x, y);
    parent.setAlpha(offsetX + x, offsetY + y, alpha);
  }

  @Override
  public boolean isReadOnly() {
    return parent.isReadOnly();
  }

  @Override
  public PixelArray getParent() {
    return parent;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  private void checkCoordinate(int x, int y) {
    // Hypothetically, out-of-bounds x and y values for the sub image can access valid pixels of
    // the parent image, which is inconsistent with PixelArray's API and is not detected by the
    // parent so this array must validate coordinates explicitly
    Arguments.checkArrayRange("x", width, 0, x);
    Arguments.checkArrayRange("y", height, 0, y);
  }
}
