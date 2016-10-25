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
package com.lhkbob.imaje.sampler;

import com.lhkbob.imaje.Image;
import com.lhkbob.imaje.Mipmap;
import com.lhkbob.imaje.MipmapArray;
import com.lhkbob.imaje.MipmapVolume;
import com.lhkbob.imaje.Raster;
import com.lhkbob.imaje.RasterArray;
import com.lhkbob.imaje.Volume;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.layout.PixelArray;
import com.lhkbob.imaje.util.Arguments;
import com.lhkbob.imaje.util.Functions;
import com.lhkbob.imaje.Images;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 */
public final class Samplers {
  private Samplers() {}

  public static int sampleToTexel(double coord, int dimension) {
    return Functions.floorInt(coord * dimension - 0.5);
  }

  public static double sampleWeight(double coord, int dimension) {
    return Functions.frac(coord * dimension - 0.5);
  }

  public static int coordToTexel(double coord, int dimension) {
    return Functions.floorInt(coord * dimension);
  }

  public static double getLOD(
      double dUdX, double dVdX, double dWdX, double dUdY, double dVdY, double dWdY, Image<?> image,
      int levelBase, int levelMax) {
    Arguments.checkArrayRange("levels", image.getMipmapCount(), levelBase, levelMax - levelBase);

    if (levelBase == levelMax) {
      return 0.0;
    }

    // Convert normalized coordinate deltas into texel derivatives
    dUdX *= image.getWidth();
    dVdX *= image.getHeight();
    dWdX *= image.getDepth();

    dUdY *= image.getWidth();
    dVdY *= image.getHeight();
    dWdY *= image.getDepth();

    // Calculate scale factor as the maximum norm of partial derivatives
    double rho = Math.max(Math.sqrt(dUdX * dUdX + dVdX * dVdX + dWdX * dWdX),
        Math.sqrt(dUdY * dUdY + dVdY * dVdY + dWdY * dWdY));
    // Lambda represents the mipmap level in the range 0 to image.getMipmapCount().
    double lambda = Functions.log2(rho);

    // Clamp to base and max
    lambda = Functions.clamp(lambda, levelBase, levelMax);

    // Normalize to 0 to 1
    return (lambda - levelBase) / (levelMax - levelBase);
  }

  public static int lodToMipmap(double lod, int levelBase, int levelMax) {
    lod = Functions.clamp(lod, 0.0, 1.0);
    return Functions.roundToInt(lod * (levelMax - levelBase)) + levelBase;
  }

  public static int lodToMipmapLow(double lod, int levelBase, int levelMax) {
    // Note that this floors instead of rounds (unlike lodToMipmap()).
    lod = Functions.clamp(lod, 0.0, 1.0);
    return Functions.floorInt(lod * (levelMax - levelBase)) + levelBase;
  }

  public static int lodToMipmapHigh(double lod, int levelBase, int levelMax) {
    return Math.min(lodToMipmapLow(lod, levelBase, levelMax) + 1, levelMax);
  }

  public static double lodWeight(double lod, int levelBase, int levelMax) {
    lod = Functions.clamp(lod, 0.0, 1.0);
    return Functions.frac(lod * (levelMax - levelBase));
  }

  @SuppressWarnings("unchecked")
  public static <T extends Color> BoundaryStrategy<T> repeat() {
    return WRAP;
  }

  @SuppressWarnings("unchecked")
  public static <T extends Color> BoundaryStrategy<T> mirror() {
    return MIRROR;
  }

  public static <T extends Color> BoundaryStrategy<T> clampToBorder(
      T borderColor, double borderAlpha) {
    return new ClampToBorder<>(borderColor, borderAlpha);
  }

  @SuppressWarnings("unchecked")
  public static <T extends Color> BoundaryStrategy<T> clampToEdge() {
    return CLAMP_TO_EDGE;
  }

  public static <T extends Color> SamplerBuilder<T, Sampler2D<T>> newSampler2D() {
    return new Sampler2DBuilder<>();
  }

  public static <T extends Color> SamplerBuilder<T, IndexedSampler2D<T>> newIndexedSampler2D() {
    return new IndexedSampler2DBuilder<>();
  }

  public static <T extends Color> SamplerBuilder<T, Sampler3D<T>> newSampler3D() {
    return new Sampler3DBuilder<>();
  }

  private static final BoundaryStrategy CLAMP_TO_EDGE = new BoundaryStrategy() {
    @Override
    public int wrap(int coordinate, int dimension) {
      return Functions.clamp(coordinate, 0, dimension - 1);
    }

    @Override
    public boolean useBorder(int coordinate, int dimension) {
      return false;
    }

    @Override
    public Color getBorderColor() {
      // Never uses a border so returns null
      return null;
    }

    @Override
    public double getBorderAlpha() {
      // Never uses a border so always return 0
      return 0.0;
    }
  };

  private static final BoundaryStrategy WRAP = new BoundaryStrategy() {
    @Override
    public int wrap(int coordinate, int dimension) {
      // OpenGL REPEAT is defined as fmod(coord, size)
      return Math.floorMod(coordinate, dimension);
    }

    @Override
    public boolean useBorder(int coordinate, int dimension) {
      return false;
    }

    @Override
    public Color getBorderColor() {
      // Never uses a border so returns null
      return null;
    }

    @Override
    public double getBorderAlpha() {
      // Never uses a border so always return 0
      return 0.0;
    }
  };

  private static final BoundaryStrategy MIRROR = new BoundaryStrategy() {
    private int mirror(int a) {
      if (a >= 0) {
        return a;
      } else {
        return -(1 + a);
      }
    }

    @Override
    public int wrap(int coordinate, int dimension) {
      // OpenGL MIRRORED_REPEAT is defined as (size-1) - mirror(fmod(coord, 2*size) - size)
      return (dimension - 1) - mirror(Math.floorMod(coordinate, 2 * dimension) - dimension);
    }

    @Override
    public boolean useBorder(int coordinate, int dimension) {
      return false;
    }

    @Override
    public Color getBorderColor() {
      // Never uses a border so returns null
      return null;
    }

    @Override
    public double getBorderAlpha() {
      // Never uses a border so always return 0
      return 0.0;
    }
  };

  private static class ClampToBorder<T extends Color> implements BoundaryStrategy<T> {
    private final T color;
    private final double alpha;

    public ClampToBorder(T color, double alpha) {
      Arguments.notNull("color", color);
      this.color = color;
      this.alpha = alpha;
    }

    @Override
    public int wrap(int texel, int dimension) {
      // Given that useBorder returns true when texel < 0 and >= dimension, this is effectively
      // the same as clamping everything to -1, dimension and then using the border color for
      // texels exactly at -1 and dimension.
      return Functions.clamp(texel, 0, dimension - 1);
    }

    @Override
    public boolean useBorder(int texel, int dimension) {
      return texel < 0 || texel >= dimension;
    }

    @Override
    public T getBorderColor() {
      return color;
    }

    @Override
    public double getBorderAlpha() {
      return alpha;
    }
  }

  private static class Sampler2DBuilder<T extends Color> extends SamplerBuilder<T, Sampler2D<T>> {
    @Override
    public Sampler2D<T> build(Image<T> image) {
      BoundaryStrategy<T> u = uStrategy;
      if (u == null) {
        u = Samplers.clampToEdge();
      }
      BoundaryStrategy<T> v = vStrategy;
      if (v == null) {
        v = Samplers.clampToEdge();
      }

      if (image instanceof Raster) {
        // Nearest/linear filtering non-mipmapped 2D sampler
        Raster<T> img = (Raster<T>) image;
        if (filterLinear) {
          return new BilinearSampler2D<>(img, u, v);
        } else {
          return new NearestSampler2D<>(img, u, v);
        }
      } else if (image instanceof Mipmap) {
        // Nearest/linear filtering mipmapped 2D sampler
        Mipmap<T> img = (Mipmap<T>) image;
        int mmax = mipmapMax;
        if (mipmapMax < 0) {
          mmax = Images.getMaxMipmaps(img.getWidth(), img.getHeight());
        }
        if (filterLinear) {
          return new MipmapBilinearSampler2D<>(img, u, v, mipmapBase, mmax);
        } else {
          return new MipmapNearestSampler2D<>(img, u, v, mipmapBase, mmax);
        }
      } else if (image instanceof RasterArray) {
        // Select non-mipmapped layer from array
        Raster<T> img = ((RasterArray<T>) image).getLayerAsRaster(layer);
        return build(img);
      } else if (image instanceof MipmapArray) {
        // Select mipmapped layer from array
        Mipmap<T> img = ((MipmapArray<T>) image).getLayerAsMipmap(layer);
        return build(img);
      } else if (image instanceof Volume) {
        // Select non-mipmapped depth slice from volume
        Raster<T> img = ((Volume<T>) image).getDepthSliceAsRaster(layer);
        return build(img);
      } else if (image instanceof MipmapVolume) {
        // Select depth slice from volume, but because mipmaps are over 3D, can't really form
        // a conventional 2D stack of mipmaps so no mipmapping
        Raster<T> img = ((MipmapVolume<T>) image).getDepthSliceAsRaster(mipmapBase, layer);
        return build(img);
      } else {
        throw new UnsupportedOperationException("Unsupported image implementation: " + image);
      }
    }
  }

  private static class Sampler3DBuilder<T extends Color> extends SamplerBuilder<T, Sampler3D<T>> {
    @Override
    public Sampler3D<T> build(Image<T> image) {
      BoundaryStrategy<T> u = uStrategy;
      if (u == null) {
        u = Samplers.clampToEdge();
      }
      BoundaryStrategy<T> v = vStrategy;
      if (v == null) {
        v = Samplers.clampToEdge();
      }
      BoundaryStrategy<T> w = wStrategy;
      if (w == null) {
        w = Samplers.clampToEdge();
      }

      if (image instanceof Raster) {
        // Convert Raster into a volume with one layer
        return build(new Volume<>(Collections.singletonList((Raster<T>) image)));
      } else if (image instanceof Mipmap) {
        // Convert Mipmap into an mipmap volume with one layer
        Mipmap<T> mipmap = (Mipmap<T>) image;
        List<List<PixelArray>> mipmappedZData = new ArrayList<>(mipmap.getMipmapCount());
        // Re-arrange mipmap layers into "volumes" of depth 1
        for (PixelArray m : mipmap.getPixelArrays()) {
          mipmappedZData.add(Collections.singletonList(m));
        }
        return build(new MipmapVolume<>(mipmap.getColorType(), mipmappedZData));
      } else if (image instanceof RasterArray) {
        // Convert array into a volume
        Volume<T> img = ((RasterArray<T>) image).getAsVolume();
        return build(img);
      } else if (image instanceof MipmapArray) {
        // Convert base mipmap level of array into a volume
        Volume<T> img = ((MipmapArray<T>) image).getMipmapAsVolume(mipmapBase);
        return build(img);
      } else if (image instanceof Volume) {
        // Nearest/linear filtering of non-mipmapped volume
        Volume<T> img = (Volume<T>) image;
        if (filterLinear) {
          return new BilinearSampler3D<>(img, u, v, w);
        } else {
          return new NearestSampler3D<>(img, u, v, w);
        }
      } else if (image instanceof MipmapVolume) {
        // Nearest/linear filtering of mipmapped volume
        MipmapVolume<T> img = (MipmapVolume<T>) image;
        int mmax = mipmapMax;
        if (mipmapMax < 0) {
          mmax = Images.getMaxMipmaps(img.getWidth(), img.getHeight(), img.getDepth());
        }
        if (filterLinear) {
          return new MipmapBilinearSampler3D<>(img, u, v, w, mipmapBase, mmax);
        } else {
          return new MipmapNearestSampler3D<>(img, u, v, w, mipmapBase, mmax);
        }
      } else {
        throw new UnsupportedOperationException("Unsupported image implementation: " + image);
      }
    }
  }

  private static class IndexedSampler2DBuilder<T extends Color> extends SamplerBuilder<T, IndexedSampler2D<T>> {
    @Override
    public IndexedSampler2D<T> build(Image<T> image) {
      BoundaryStrategy<T> u = uStrategy;
      if (u == null) {
        u = Samplers.clampToEdge();
      }
      BoundaryStrategy<T> v = vStrategy;
      if (v == null) {
        v = Samplers.clampToEdge();
      }

      if (image instanceof Raster) {
        // Convert Raster into an array with one layer
        return build(new RasterArray<>(Collections.singletonList((Raster<T>) image)));
      } else if (image instanceof Mipmap) {
        // Convert Mipmap into an array with one layer
        return build(new MipmapArray<>(Collections.singletonList((Mipmap<T>) image)));
      } else if (image instanceof RasterArray) {
        // Nearest/linear filtering non-mipmapped array sampler
        RasterArray<T> img = (RasterArray<T>) image;
        if (filterLinear) {
          return new BilinearIndexedSampler2D<>(img, u, v);
        } else {
          return new NearestIndexedSampler2D<>(img, u, v);
        }
      } else if (image instanceof MipmapArray) {
        // Nearest/linear filtering mipmapped array sampler
        MipmapArray<T> img = (MipmapArray<T>) image;
        int mmax = mipmapMax;
        if (mipmapMax < 0) {
          mmax = Images.getMaxMipmaps(img.getWidth(), img.getHeight());
        }
        if (filterLinear) {
          return new MipmapBilinearIndexedSampler2D<>(img, u, v, mipmapBase, mmax);
        } else {
          return new MipmapNearestIndexedSampler2D<>(img, u, v, mipmapBase, mmax);
        }
      } else if (image instanceof Volume) {
        // Convert the depth stack of the volume into an non-mipmapped array
        RasterArray<T> img = ((Volume<T>) image).getAsRasterArray();
        return build(img);
      } else if (image instanceof MipmapVolume) {
        // Convert mipmapBase level depth stack of the volume into a non-mipmapped array
        RasterArray<T> img = ((MipmapVolume<T>) image).getMipmapAsRasterArray(mipmapBase);
        return build(img);
      } else {
        throw new UnsupportedOperationException("Unsupported image implementation: " + image);
      }
    }
  }
}
