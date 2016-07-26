package com.lhkbob.imaje.util;

import com.lhkbob.imaje.Image;
import com.lhkbob.imaje.Raster;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.Depth;
import com.lhkbob.imaje.color.DepthStencil;
import com.lhkbob.imaje.color.SRGB;
import com.lhkbob.imaje.color.Stencil;
import com.lhkbob.imaje.layout.PixelFormat;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 */
public class GPUUtils {
  // FIXME we need a nice way of determining if an image is a Texture1D, Texture2D, Texture3D, Texture2DArray, Texture1DArray, TextureCubeMap, or TextureCubeMapArray
  // and if it has mipmaps, what its GPU format is, and if the data is accessible to the GPU
  // Could define a simple interface here for texture data transfer to/from the GPU and just not
  // implement it -> Both Ferox and Michael's library could then implement a version of it.
  //
  // The interface would avoid all the impl specific guts of pulling out buffers, etc. from sources
  // that this utility can do for it and then just call the appropriate simpler function.

  public static boolean isTexture1D(Image<?> image) {

  }

  public static boolean isTexture2D(Image<?> image) {

  }

  public static boolean isTexture3D(Image<?> image) {

  }

  public static boolean isTexture2DArray(Image<?> image) {

  }

  public static boolean isTexture1DArray(Image<?> image) {

  }

  public static boolean isTextureCubeMap(Image<?> image) {

  }

  public static boolean isTextureCubeMapArray(Image<?> image) {

  }

  public static GPUFormat getFormat(Image<?> image) {

  }

  private static GPUFormat getFormat(Raster<?> image) {
    Class<? extends Color> type = image.getColorType();
    PixelFormat pf = image.getPixelArray().getFormat();
    GPUFormat.Channel[] channels = new GPUFormat.Channel[pf.getDataChannelCount()];

    // Map integer based color channels to GPU semantics
    for (int i = 0; i < pf.getColorChannelCount(); i++) {
      GPUFormat.Channel semantic;

      if (i == 0) {
        // First color channel semantics are either R (for majority of colors), or D for depth and
        // depth-stencil types, and stencil for stencil only types.
        if (Depth.class.isAssignableFrom(type) || DepthStencil.class.isAssignableFrom(type)) {
          semantic = GPUFormat.Channel.D;
        } else if (Stencil.class.isAssignableFrom(type)) {
          semantic = GPUFormat.Channel.S;
        } else {
          semantic = GPUFormat.Channel.R;
        }
      } else if (i == 1) {
        if (DepthStencil.class.isAssignableFrom(type)) {
          semantic = GPUFormat.Channel.S;
        } else {
          semantic = GPUFormat.Channel.G;
        }
      } else if (i == 2) {
        semantic = GPUFormat.Channel.B;
      } else if (i == 3) {
        // 4th color channel maps to A so that CMYK and other 4 channel colors can still
        // be sent to the GPU even if it doesn't actually represent alpha values
        semantic = GPUFormat.Channel.A;
      } else {
        semantic = GPUFormat.Channel.X;
      }

      channels[pf.getColorChannelDataIndex(i)] = semantic;
    }

    // Include alpha semantic if necessary
    if (pf.hasAlphaChannel()) {
      channels[pf.getAlphaChannelDataIndex()] = GPUFormat.Channel.A;
    }

    // Fill in any skipped data channels with X
    for (int i = 0; i < channels.length; i++) {
      if (channels[i] == null) {
        channels[i] = GPUFormat.Channel.X;
      }
    }

    Stream<GPUFormat> formats = GPUFormat.streamAll().filter(GPUFormat.format(pf).and(GPUFormat.channelLayout(channels)));
    if (image.getPixelArray().getFormat().getDataChannelCount() > 1 && image.getPixelArray().getLayout().getChannelCount() == 1) {
      formats = formats.filter(GPUFormat::isPacked);
    } else {
      formats = formats.filter(GPUFormat::isUnpackedLayout);
    }

    // Reduce the set of formats depending on if the color type is sRGB or not
    Predicate<GPUFormat> isSRGB = GPUFormat::isSRGB;
    if (SRGB.class.isAssignableFrom(type)) {
      // Reduce to an SRGB format if possible, but take a plain format if no SRGB is available
      return formats.reduce(GPUFormat.UNDEFINED, (a, b) -> {
        if (b.isSRGB() && !a.isSRGB()) {
          // Upgrade the non-SRGB format to b
          return b;
        } else if (a == GPUFormat.UNDEFINED && b != GPUFormat.UNDEFINED) {
          // Upgrade to a real format
          return b;
        } else {
          // Stick with a
          return a;
        }
      });
    } else {
      // Filter out sRGB typed formats and then report first
      return formats.filter(isSRGB.negate()).findFirst().orElse(GPUFormat.UNDEFINED);
    }
  }
}
