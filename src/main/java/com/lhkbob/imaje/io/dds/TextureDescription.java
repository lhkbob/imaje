package com.lhkbob.imaje.io.dds;

import com.lhkbob.imaje.Images;
import com.lhkbob.imaje.color.Alpha;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.Luminance;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.YUV;
import com.lhkbob.imaje.io.InvalidImageException;
import com.lhkbob.imaje.io.UnsupportedImageFormatException;
import com.lhkbob.imaje.layout.PackedPixelArray;
import com.lhkbob.imaje.layout.PixelFormat;
import com.lhkbob.imaje.layout.UnpackedPixelArray;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.lhkbob.imaje.io.dds.DDSHeader.makeFourCC;
import static com.lhkbob.imaje.io.dds.DDSHeader.unmakeFourCC;

/**
 *
 */
public class TextureDescription {
  // Supported common FourCC codes that map to DXGI formats
  private static final Map<Integer, DXGIFormat> FOURCC_FORMATS;

  static {
    Map<Integer, DXGIFormat> formats = new HashMap<>();
    formats.put(makeFourCC("DXT1"), DXGIFormat.BC1_UNORM);
    formats.put(makeFourCC("DXT3"), DXGIFormat.BC2_UNORM);
    formats.put(makeFourCC("DXT5"), DXGIFormat.BC3_UNORM);
    formats.put(makeFourCC("BC4U"), DXGIFormat.BC4_UNORM);
    formats.put(makeFourCC("BC4S"), DXGIFormat.BC4_SNORM);
    formats.put(makeFourCC("ATI2"), DXGIFormat.BC5_UNORM);
    formats.put(makeFourCC("BC5S"), DXGIFormat.BC5_SNORM);
    formats.put(makeFourCC("RGBG"), DXGIFormat.R8G8_B8G8_UNORM);
    formats.put(makeFourCC("GRGB"), DXGIFormat.G8R8_G8B8_UNORM);
    formats.put(36, DXGIFormat.R16G16B16A16_UNORM);
    formats.put(110, DXGIFormat.R16G16B16A16_SNORM);
    formats.put(111, DXGIFormat.R16_FLOAT);
    formats.put(112, DXGIFormat.R16G16_FLOAT);
    formats.put(113, DXGIFormat.R16G16B16A16_FLOAT);
    formats.put(114, DXGIFormat.R32_FLOAT);
    formats.put(115, DXGIFormat.R32G32_FLOAT);
    formats.put(116, DXGIFormat.R32G32B32A32_FLOAT);
    // No other common FourCC codes *that* map to a DXGI format

    FOURCC_FORMATS = Collections.unmodifiableMap(formats);
  }

  private int width;
  private int height;
  private int depth;
  private int imageCount; // Does not include mipmap images
  private boolean mipmapped;

  private TextureType target;
  private PixelFormat format;
  private Class<? extends Color> colorType;
  private boolean packed;

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getDepth() {
    return depth;
  }

  public void setDepth(int depth) {
    this.depth = depth;
  }

  public int getImageCount() {
    return imageCount;
  }

  public void setImageCount(int imageCount) {
    this.imageCount = imageCount;
  }

  public boolean isMipmapped() {
    return mipmapped;
  }

  public void setMipmapped(boolean mipmapped) {
    this.mipmapped = mipmapped;
  }

  public TextureType getTextureType() {
    return target;
  }

  public void setTextureType(TextureType target) {
    this.target = target;
  }

  public PixelFormat getPixelFormat() {
    return format;
  }

  public void setPixelFormat(PixelFormat format) {
    this.format = format;
  }

  public boolean isPackedFormat() {
    return packed;
  }

  public void setPackedFormat(boolean packed) {
    this.packed = packed;
  }

  public Class<? extends Color> getColorType() {
    return colorType;
  }

  public void setColorType(Class<? extends Color> colorType) {
    this.colorType = colorType;
  }

  public static TextureDescription createFromHeader(DDSHeader header) throws InvalidImageException,
      UnsupportedImageFormatException {
    TextureDescription desc = new TextureDescription();
    // Fill in colorType, packed, and format variables
    desc.identifyFormat(header);
    // Fill in width, height, depth, imageCount, mipmapped, and target
    desc.identifyTarget(header);
    return desc;
  }

  private void identifyTarget(DDSHeader header) throws InvalidImageException,
      UnsupportedImageFormatException {
    // Check 2D dimensions, must be present for any texture type
    if (header.isWidthValid()) {
      width = header.getWidth();
    } else {
      throw new InvalidImageException("DDS header is missing required flag DDSD_WIDTH");
    }

    if (header.isHeightValid()) {
      height = header.getHeight();
    } else {
      throw new InvalidImageException("DDS header is missing required flag DDSD_HEIGHT");
    }

    // We won't check for DDSCAPS_COMPLEX, since some files seem to ignore it when creating cube
    // maps or 3D textures

    // Set sensible defaults for imageCount and depth, to be overridden in deviating situations
    imageCount = 1;
    depth = 1;
    if (header.isVolume()) {
      // Validate 3D textures with DDS header and possibly DX10 header
      if (header.getDX10Header() != null) {
        if (header.getDX10Header().getResourceDimension()
            != DX10Header.D3D10ResourceDimension.TEXTURE3D) {
          throw new InvalidImageException("DX10 header and surface caps are inconsistent");
        }
        if (header.getDX10Header().getArraySize() > 1) {
          throw new UnsupportedImageFormatException("Texture3D arrays aren't supported");
        }
      }
      if (header.isDepthValid()) {
        depth = header.getDepth();
      } else {
        throw new InvalidImageException(
            "DDS header is missing required flag DDSD_DEPTH for a volume texture");
      }

      target = TextureType.TEXTURE_3D;
    } else if (header.isCubeMap()) {
      if (!header.hasAllCubeFaces()) {
        throw new UnsupportedImageFormatException("Cube map must have 6 faces present");
      }
      if (width != height) {
        throw new UnsupportedImageFormatException("Cube map must have square faces");
      }

      if (header.getDX10Header() != null) {
        if (!header.getDX10Header().isCubeMap()) {
          throw new InvalidImageException("DX10 header and surface caps are inconsistent");
        }

        if (header.getDX10Header().getResourceDimension()
            == DX10Header.D3D10ResourceDimension.TEXTURE2D) {
          if (header.getDX10Header().getArraySize() == 6
              || header.getDX10Header().getArraySize() == 1) {
            // nVidia sets the DX10 header to be a 2D tex, with arraySize = 6 for a single cubemap,
            // even though arraySize represents the number of total cubes
            target = TextureType.TEXTURE_CUBE;
            imageCount = 6;
          } else {
            // Assume it's a cubemap array, so total images is 6 * arraySize
            target = TextureType.TEXTURE_CUBE_ARRAY;
            imageCount = 6 * header.getDX10Header().getArraySize();
          }
        } else {
          throw new InvalidImageException("DX10 header and surface caps are inconsistent");
        }
      } else {
        // Simple single cube map
        target = TextureType.TEXTURE_CUBE;
        imageCount = 6;
      }
    } else {
      // No cubemap or volume caps bit set, means this is a basic 1D or 2D texture (determined by
      // dimension), unless a more specific resource type is specified in a DX10 header.
      if (header.getDX10Header() != null) {
        if (header.getDX10Header().getResourceDimension()
            == DX10Header.D3D10ResourceDimension.TEXTURE1D) {
          // TEXTURE_1D specified in DX10 header
          if (height != 1) {
            throw new InvalidImageException(
                "1D image must have a height of 1 pixel, not: " + height);
          }

          if (header.getDX10Header().getArraySize() > 1) {
            // Upgrade to TEXTURE_1D_ARRAY
            target = TextureType.TEXTURE_1D_ARRAY;
            imageCount = header.getDX10Header().getArraySize();
          } else {
            target = TextureType.TEXTURE_1D;
          }
        } else if (header.getDX10Header().getResourceDimension()
            == DX10Header.D3D10ResourceDimension.TEXTURE2D) {
          // TEXTURE_2D, although this could also be an array or cube map array if the misc bit is
          // set and the DDSHeader caps weren't flagged as cube map
          if (header.getDX10Header().isCubeMap()) {
            if (width != height) {
              throw new UnsupportedImageFormatException("Cube map images must be square");
            }

            if (header.getDX10Header().getArraySize() == 6
                || header.getDX10Header().getArraySize() == 1) {
              // nVidia sets the DX10 header to be a 2D tex, with arraySize = 6 for a single
              // cubemap, even though arraySize represents the number of total cubes
              target = TextureType.TEXTURE_CUBE;
              imageCount = 6;
            } else {
              // Assume it's a cubemap array, so total images is 6 * arraySize
              target = TextureType.TEXTURE_CUBE_ARRAY;
              imageCount = 6 * header.getDX10Header().getArraySize();
            }
          } else if (header.getDX10Header().getArraySize() > 1) {
            // A 2D array
            target = TextureType.TEXTURE_2D_ARRAY;
            imageCount = header.getDX10Header().getArraySize();
          } else {
            target = TextureType.TEXTURE_2D;
          }
        } else {
          throw new InvalidImageException("DX10 header and surface caps are inconsistent");
        }
      } else {
        // Assume simple 1D or 2D based on height dimension
        if (height == 1) {
          target = TextureType.TEXTURE_1D;
        } else {
          target = TextureType.TEXTURE_2D;
        }
      }
    }

    // Check for mipmap specification
    if (header.isMipmapped()) {
      // DDSCAPS_COMPLEX should be set in h.caps but that is not validated
      if (header.isMipmapCountValid()) {
        // Validate that provided count is complete mipmap set
        int expected = Images.getMaxMipmaps(width, height, depth);
        if (header.getMipmapCount() != expected) {
          throw new InvalidImageException(
              "Expected " + expected + " but got " + header.getMipmapCount() + " mipmaps instead");
        }
      }
      // else don't bother since MIPMAPCOUNT may not have been written, and assume a complete mipmap
      // set is provided
      mipmapped = true;
    } else {
      mipmapped = false;
    }
  }

  private void identifyFormat(DDSHeader header) throws InvalidImageException,
      UnsupportedImageFormatException {
    DXGIFormat dxgiFormat = null;
    if (header.getDX10Header() != null) {
      // The pixel format is stored in the dxgiFormat, which won't be null but set to UNKNOWN if not
      // in our defined enum
      dxgiFormat = header.getDX10Header().getDXGIFormat();
    } else if (header.getPixelFormat().isFourCCValid()) {
      // Look up the 4CC code and see if it maps to a known dxgiFormat
      dxgiFormat = FOURCC_FORMATS.get(header.getPixelFormat().getFourCC());
      if (dxgiFormat == null) {
        throw new UnsupportedImageFormatException(
            "Unsupported FOURCC code in header: " + unmakeFourCC(
                header.getPixelFormat().getFourCC()));
      }
    }

    if (dxgiFormat != null) {
      if (!dxgiFormat.isSupported()) {
        throw new UnsupportedImageFormatException("Unsupported DXGI pixel format: " + dxgiFormat);
      } else {
        colorType = dxgiFormat.getColorType();
        format = dxgiFormat.getFormat();
        packed = dxgiFormat.isPacked();
        return;
      }
    }

    // Otherwise reconstruct a PixelFormat from the masks; this assumes the header's flag for
    // a valid pixel format is true (even if it's false, could have been a poorly written file)
    long alphaMask = 0;
    // Put R, G, and B masks in directly, if they happen to be 0 then they will automatically get
    // filtered by the PixelFormat constructor.
    long[] colorMask = new long[] {
        (0xffffffffL & header.getPixelFormat().getRedBitMask()),
        (0xffffffffL & header.getPixelFormat().getGreenBitMask()),
        (0xffffffffL & header.getPixelFormat().getBlueBitMask()),
    };
    if (header.getPixelFormat().hasAlphaChannel()) {
      alphaMask = (0xffffffffL & header.getPixelFormat().getAlphaBitMask());
    }

    int channelCount;
    if (header.getPixelFormat().isLuminance()) {
      // The red mask is defined to hold luminance values as expected by PixelFormat, but 0 out
      colorType = Luminance.class;
      channelCount = 1;
    } else if (header.getPixelFormat().isRGB()) {
      // RGB data
      colorType = RGB.Linear.class;
      channelCount = 3;
    } else if (header.getPixelFormat().isYUV()) {
      // YUV data
      colorType = YUV.class;
      channelCount = 3;
    } else if (header.getPixelFormat().isOnlyAlpha()) {
      // Alpha only image, so move alpha mask into "red" mask and clear explicit alpha since
      // this will be modeled by the Alpha "color" class.
      colorMask[0] = alphaMask;
      alphaMask = 0;
      colorType = Alpha.class;
      channelCount = 1;
    } else {
      throw new InvalidImageException("Invalid pixel format header, does not specify color type");
    }

    format = PixelFormat
        .createFromMasks(colorMask, channelCount, alphaMask, PixelFormat.Type.UNORM);
    if (format.getTotalBitSize() != header.getPixelFormat().getRGBBitCount()) {
      throw new InvalidImageException(
          "Invalid pixel format header, reported bit count does not match channel bit masks");
    }

    // To determine if the format must be packed, use a simple heuristic of if PackedPixelArray
    // supports it and UnpackedPixelArray does not.
    packed = PackedPixelArray.isSupported(format) && !UnpackedPixelArray.isSupported(format);
  }
}
