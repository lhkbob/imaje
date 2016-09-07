package com.lhkbob.imaje.io;

import com.lhkbob.imaje.Image;
import com.lhkbob.imaje.ImageBuilder;
import com.lhkbob.imaje.color.Alpha;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.Depth;
import com.lhkbob.imaje.color.DepthStencil;
import com.lhkbob.imaje.color.Generic;
import com.lhkbob.imaje.color.Luminance;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.SRGB;
import com.lhkbob.imaje.color.YCbCr;
import com.lhkbob.imaje.color.YUV;
import com.lhkbob.imaje.data.ByteData;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.DataBuffer;
import com.lhkbob.imaje.data.DoubleData;
import com.lhkbob.imaje.data.FloatData;
import com.lhkbob.imaje.data.IntData;
import com.lhkbob.imaje.data.LongData;
import com.lhkbob.imaje.data.ShortData;
import com.lhkbob.imaje.layout.PackedPixelArray;
import com.lhkbob.imaje.layout.PixelFormat;
import com.lhkbob.imaje.layout.UnpackedPixelArray;
import com.lhkbob.imaje.util.ByteOrderUtils;
import com.lhkbob.imaje.util.GPUFormat;
import com.lhkbob.imaje.util.IOUtils;
import com.lhkbob.imaje.util.ImageUtils;
import com.lhkbob.imaje.util.PixelFormatBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 *
 */
public class DDSReader implements ImageFileReader {
  private final Data.Factory dataFactory;

  public DDSReader() {
    this(null);
  }

  public DDSReader(Data.Factory factory) {
    if (factory == null) {
      factory = Data.getDefaultDataFactory();
    }
    dataFactory = factory;
  }

  @Override
  public Image<?> read(SeekableByteChannel in) throws IOException {
    // Validate and interpret the header
    DDSHeader header = readHeader(in);
    validateHeader(header);
    TextureDescription desc = identifyTextureDescription(header);

    DataBuffer data = readImageData(in, desc);

    ImageBuilder<?, ?, ?> b;
    switch (desc.target) {
    case TEXTURE_1D:
    case TEXTURE_2D:
      // These texture types map to Raster or Mipmap
      if (desc.mipmapped) {
        b = Image.newMipmap(desc.colorType);
      } else {
        b = Image.newRaster(desc.colorType);
      }
      break;
    case TEXTURE_1D_ARRAY:
    case TEXTURE_2D_ARRAY:
    case TEXTURE_CUBE:
    case TEXTURE_CUBE_ARRAY:
      // These texture types map to RasterArray or MipmapArray
      if (desc.mipmapped) {
        b = Image.newMipmapArray(desc.colorType).layers(desc.imageCount);
      } else {
        b = Image.newRasterArray(desc.colorType).layers(desc.imageCount);
      }
      break;
    case TEXTURE_3D:
      if (desc.mipmapped) {
        b = Image.newMipmapVolume(desc.colorType).depth(desc.depth);
      } else {
        b = Image.newVolume(desc.colorType).depth(desc.depth);
      }
      break;
    default:
      throw new InvalidImageException("Unknown texture target: " + desc.target);
    }

    // Common configuration for all builders
    b.width(desc.width).height(desc.height).format(desc.format, desc.packed).backedBy(data);

    return b.build();
  }

  private enum TextureType {
    TEXTURE_1D, TEXTURE_2D, TEXTURE_3D, TEXTURE_CUBE, TEXTURE_1D_ARRAY, TEXTURE_2D_ARRAY,
    TEXTURE_CUBE_ARRAY
  }

  private static class TextureDescription {
    int width;
    int height;
    int depth;
    int imageCount; // Does not include mipmap images
    boolean mipmapped;

    TextureType target;
    PixelFormat format;
    Class<? extends Color> colorType;
    boolean packed;
  }

  // Stores a DDS header (equivalent for DX9 and DX10, DX10 may have non-null DX10Header, too)
  private static class DDSHeader {
    // DDS_HEADER or DDSURFACEDESC2
    int magic;
    int size;
    int flags;
    int height;
    int width;
    int linearSize;
    int depth;
    int mipmapCount;
    final int[] reserved1 = new int[11];

    final DDSPixelFormat pixelFormat = new DDSPixelFormat();

    // DDS_CAPS2 (embedded in DDS_HEADER, not DDSURFACEDESC2)
    int caps;
    int caps2;
    int caps3;
    int caps4;

    int reserved2;

    // Not really part of the header, but it follows immediately. Not null if this is a DX10 dds
    // texture, i.e. pixelFormat.fourCC == 'DX10'.
    DX10Header headerDX10;
  }

  // Stores the pixel format information for the dds texture If the fourCC is valid and set to
  // 'DX10', then the pixel format is stored in a DXGIPixelFormat enum instead of the DX10 header.
  private static class DDSPixelFormat {
    int size;
    int flags;
    int fourCC;
    int rgbBitCount;
    int rBitMask;
    int gBitMask;
    int bBitMask;
    int aBitMask;
  }

  // Only present if header.pixelFormat.fourCC == 'DX10'
  private static class DX10Header {
    DXGIFormat dxgiFormat;
    int resourceDimension;

    int miscFlag;
    int arraySize;
    int reserved;
  }

  /*
   * DXGI_FORMAT enumeration: https://msdn.microsoft.com/en-us/library/bb173059(v=vs.85).aspx Enum
   * values are ordered such that ordinal() corresponds to defined value from above spec. This maps
   * the DDS format to the Vulkan-based GPUFormat where possible, or to the more flexible
   * PixelFormat class and an associated Color type.
   *
   * Note that for non-byte aligned formats, DXGI lists least-significant bit components first,
   * which is why the component order is reversed from corresponding GPUFormat names.
   */
  private enum DXGIFormat {
    UNKNOWN(null), R32G32B32A32_TYPELESS(RGB.Linear.class),
    R32G32B32A32_FLOAT(RGB.Linear.class, GPUFormat.R32G32B32A32_SFLOAT),
    R32G32B32A32_UINT(RGB.Linear.class, GPUFormat.R32G32B32A32_UINT),
    R32G32B32A32_SINT(RGB.Linear.class, GPUFormat.R32G32B32A32_SINT),
    R32G32B32_TYPELESS(RGB.Linear.class),
    R32G32B32_FLOAT(RGB.Linear.class, GPUFormat.R32G32B32_SFLOAT),
    R32G32B32_UINT(RGB.Linear.class, GPUFormat.R32G32B32_UINT),
    R32G32B32_SINT(RGB.Linear.class, GPUFormat.R32G32B32_SINT),
    R16G16B16A16_TYPELESS(RGB.Linear.class),
    R16G16B16A16_FLOAT(RGB.Linear.class, GPUFormat.R16G16B16A16_SFLOAT),
    R16G16B16A16_UNORM(RGB.Linear.class, GPUFormat.R16G16B16A16_UNORM),
    R16G16B16A16_UINT(RGB.Linear.class, GPUFormat.R16G16B16A16_UINT),
    R16G16B16A16_SNORM(RGB.Linear.class, GPUFormat.R16G16B16A16_SNORM),
    R16G16B16A16_SINT(RGB.Linear.class, GPUFormat.R16G16B16A16_SINT), R32G32_TYPELESS(null),
    R32G32_FLOAT(Generic.C2.class, GPUFormat.R32G32_SFLOAT),
    R32G32_UINT(Generic.C2.class, GPUFormat.R32G32_UINT),
    R32G32_SINT(Generic.C2.class, GPUFormat.R32G32_SINT), R32G8X24_TYPELESS(null),
    D32_FLOAT_S8X24_UINT(DepthStencil.class, GPUFormat.D32_SFLOAT_S8_UINT),
    R32_FLOAT_X8X24_TYPELESS(null), X32_TYPELESS_G8X24_UINT(null),
    R10G10B10A2_TYPELESS(RGB.Linear.class),
    R10G10B10A2_UNORM(RGB.Linear.class, GPUFormat.A2B10G10R10_UNORM_PACK32),
    R10G10B10A2_UINT(RGB.Linear.class, GPUFormat.A2B10G10R10_UINT_PACK32),
    R11G11B10_FLOAT(RGB.Linear.class, GPUFormat.B10G11R11_UFLOAT_PACK32),
    R8G8B8A8_UNORM(RGB.Linear.class, GPUFormat.R8G8B8A8_UNORM),
    R8G8B8A8_UNORM_SRGB(SRGB.class, GPUFormat.R8G8B8A8_SRGB),
    R8G8B8A8_UINT(RGB.Linear.class, GPUFormat.R8G8B8A8_UINT),
    R8G8B8A8_SNORM(RGB.Linear.class, GPUFormat.R8G8B8A8_SNORM),
    R8G8B8A8_SINT(RGB.Linear.class, GPUFormat.R8G8B8A8_SINT), R16G16_TYPELESS(null),
    R16G16_FLOAT(Generic.C2.class, GPUFormat.R16G16_SFLOAT),
    R16G16_UNORM(Generic.C2.class, GPUFormat.R16G16_UNORM),
    R16G16_UINT(Generic.C2.class, GPUFormat.R16G16_UINT),
    R16G16_SNORM(Generic.C2.class, GPUFormat.R16G16_SNORM),
    R16G16_SINT(Generic.C2.class, GPUFormat.R16G16_SINT), R32_TYPELESS(Luminance.class),
    D32_FLOAT(Depth.class, GPUFormat.D32_SFLOAT), R32_FLOAT(Luminance.class, GPUFormat.R32_SFLOAT),
    R32_UINT(Luminance.class, GPUFormat.R32_UINT), R32_SINT(Luminance.class, GPUFormat.R32_SINT),
    R24G8_TYPELESS(null), D24_UNORM_S8_UINT(DepthStencil.class, GPUFormat.D24_UNORM_S8_UINT),
    R24_UNORM_X8_TYPELESS(null), X24_TYPELESS_G8_UINT(null), R8G8_TYPELESS(null),
    R8G8_UNORM(Generic.C2.class, GPUFormat.R8G8_UNORM),
    R8G8_UINT(Generic.C2.class, GPUFormat.R8G8_UINT),
    R8G8_SNORM(Generic.C2.class, GPUFormat.R8G8_SNORM),
    R8G8_SINT(Generic.C2.class, GPUFormat.R8G8_SINT), R16_TYPELESS(Luminance.class),
    R16_FLOAT(Luminance.class, GPUFormat.R16_SFLOAT), D16_UNORM(Depth.class, GPUFormat.D16_UNORM),
    R16_UNORM(Luminance.class, GPUFormat.R16_UNORM), R16_UINT(Luminance.class, GPUFormat.R16_UINT),
    R16_SNORM(Luminance.class, GPUFormat.R16_SNORM), R16_SINT(Luminance.class, GPUFormat.R16_SINT),
    R8_TYPELESS(null), R8_UNORM(Luminance.class, GPUFormat.R8_UNORM),
    R8_UINT(Luminance.class, GPUFormat.R8_UINT), R8_SNORM(Luminance.class, GPUFormat.R8_SINT),
    R8_SINT(Luminance.class, GPUFormat.R8_SINT), A8_UNORM(Alpha.class, GPUFormat.R8_UNORM),
    R1_UNORM(null), R9G9B9E5_SHAREDEXP(RGB.Linear.class, GPUFormat.E5B9G9R9_UFLOAT_PACK32),
    R8G8_B8G8_UNORM(null), G8R8_G8B8_UNORM(null), BC1_TYPELESS(null),
    BC1_UNORM(RGB.Linear.class, GPUFormat.BC1_RGB_UNORM_BLOCK),
    BC1_UNORM_SRGB(SRGB.class, GPUFormat.BC1_RGB_SRGB_BLOCK), BC2_TYPELESS(null),
    BC2_UNORM(RGB.Linear.class, GPUFormat.BC2_UNORM_BLOCK),
    BC2_UNORM_SRGB(SRGB.class, GPUFormat.BC2_SRGB_BLOCK), BC3_TYPELESS(null),
    BC3_UNORM(RGB.Linear.class, GPUFormat.BC3_UNORM_BLOCK),
    BC3_UNORM_SRGB(SRGB.class, GPUFormat.BC3_SRGB_BLOCK), BC4_TYPELESS(null),
    BC4_UNORM(Luminance.class, GPUFormat.BC4_UNORM_BLOCK), BC4_SNORM(null), BC5_TYPELESS(null),
    BC5_UNORM(Generic.C2.class, GPUFormat.BC5_UNORM_BLOCK), BC5_SNORM(null),
    B5G6R5_UNORM(RGB.Linear.class, GPUFormat.B5G6R5_UNORM_PACK16),
    B5G5R5A1_UNORM(RGB.Linear.class, GPUFormat.B5G5R5A1_UNORM_PACK16),
    B8G8R8A8_UNORM(RGB.Linear.class, GPUFormat.B8G8R8A8_UNORM), B8G8R8X8_UNORM(
        RGB.Linear.class,
        new PixelFormatBuilder().channels(2, 1, 0, PixelFormat.SKIP_CHANNEL).bits(8)
            .types(PixelFormat.Type.UNORM).build(), false),
    R10G10B10_XR_BIAS_A2_UNORM(RGB.Linear.class, null), B8G8R8A8_TYPELESS(RGB.Linear.class, null),
    B8G8R8A8_UNORM_SRGB(SRGB.class, GPUFormat.B8G8R8A8_SRGB),
    B8G8R8X8_TYPELESS(RGB.Linear.class, null), B8G8R8X8_UNORM_SRGB(
        SRGB.class, new PixelFormatBuilder().channels(2, 1, 0, PixelFormat.SKIP_CHANNEL).bits(8)
        .types(PixelFormat.Type.UNORM).build(), false), BC6H_TYPELESS(null),
    BC6H_UF16(null, GPUFormat.BC6H_UFLOAT_BLOCK), BC6H_SF16(null, GPUFormat.BC6H_SFLOAT_BLOCK),
    BC7_TYPELESS(null), BC7_UNORM(null, GPUFormat.BC7_UNORM_BLOCK),
    BC7_UNORM_SRGB(null, GPUFormat.BC7_SRGB_BLOCK),
    // FIXME See https://msdn.microsoft.com/en-us/library/dd206750(v=vs.85).aspx
    // Will require implementing different chroma vs intensity resolution sample rates
    AYUV(YUV.class, null), Y410(YUV.class, null), Y416(YUV.class, null), NV12(YUV.class, null),
    P010(YUV.class, null), P016(YUV.class, null), _420_OPAQUE(YUV.class, null),
    YUY2(YUV.class, null), Y216(YUV.class, null), NV11(YUV.class, null), AI44(YUV.class, null),
    IA44(YUV.class, null), P8(YUV.class, null), A8P8(YUV.class, null),
    B4G4R4A4_UNORM(RGB.Linear.class, GPUFormat.B4G4R4A4_UNORM_PACK16), P208(YUV.class, null),
    V208(YCbCr.class, null), V408(YCbCr.class, null);

    final PixelFormat format;
    final boolean packed;
    final Class<? extends Color> color;

    DXGIFormat(Class<? extends Color> color) {
      this(color, null, false);
    }

    DXGIFormat(Class<? extends Color> color, GPUFormat format) {
      this(color, format.getPixelFormat(), format.isPacked());
    }

    DXGIFormat(Class<? extends Color> color, PixelFormat format, boolean packed) {
      this.color = color;
      this.format = format;
      this.packed = packed;
    }

    public boolean isSupported() {
      return color != null && format != null;
    }
  }

  // Selected bits in DDSHeader flags
  private static final int DDSD_CAPS = 0x00000001; // Capabilities are valid
  private static final int DDSD_HEIGHT = 0x00000002; // Height is valid
  private static final int DDSD_WIDTH = 0x00000004; // Width is valid
  private static final int DDSD_PITCH = 0x00000008; // Pitch is valid
  private static final int DDSD_PIXELFORMAT = 0x00001000; // ddpfPixelFormat is valid
  private static final int DDSD_MIPMAPCOUNT = 0x00020000; // Mipmap count is valid
  private static final int DDSD_LINEARSIZE = 0x00080000; // dwLinearSize is valid
  private static final int DDSD_DEPTH = 0x00800000; // dwDepth is valid

  // Selected bits in DDSPixelFormat flags
  private static final int DDPF_ALPHAPIXELS = 0x00000001; // Alpha channel is present
  private static final int DDPF_ALPHA = 0x00000002; // Only contains alpha information
  private static final int DDPF_LUMINANCE = 0x00020000; // luminance data
  private static final int DDPF_FOURCC = 0x00000004; // FourCC code is valid
  private static final int DDPF_RGB = 0x00000040; // RGB data is present
  private static final int DDPF_YUV = 0x00000200; // YUV data is present in RGB channels

  // Selected bits in DDS capabilities flags
  private static final int DDSCAPS_TEXTURE = 0x00001000; // Can be used as a texture
  private static final int DDSCAPS_MIPMAP = 0x00400000; // Is one level of a mip-map
  private static final int DDSCAPS_COMPLEX = 0x00000008; // Complex surface structure, such as a cube map

  // Selected bits in DDS capabilities 2 flags
  private static final int DDSCAPS2_CUBEMAP = 0x00000200;
  private static final int DDSCAPS2_CUBEMAP_POSITIVEX = 0x00000400;
  private static final int DDSCAPS2_CUBEMAP_NEGATIVEX = 0x00000800;
  private static final int DDSCAPS2_CUBEMAP_POSITIVEY = 0x00001000;
  private static final int DDSCAPS2_CUBEMAP_NEGATIVEY = 0x00002000;
  private static final int DDSCAPS2_CUBEMAP_POSITIVEZ = 0x00004000;
  private static final int DDSCAPS2_CUBEMAP_NEGATIVEZ = 0x00008000;
  private static final int DDSCAPS2_CUBEMAP_ALL_FACES = 0x0000fc00;
  private static final int DDSCAPS2_VOLUME = 0x00200000;

  // Selected bits in DDSHeader_DX10 misc flags
  private static final int D3D10_MISC_RESOURCE_GENERATE_MIPS = 0x1;
  private static final int D3D10_MISC_RESOURCE_SHARED = 0x2;
  private static final int D3D10_MISC_RESOURCE_TEXTURECUBE = 0x4;

  // D3D10 Resource Dimension enum
  private static final int D3D10_RESOURCE_DIMENSION_UNKNOWN = 0;
  private static final int D3D10_RESOURCE_DIMENSION_BUFFER = 1;
  private static final int D3D10_RESOURCE_DIMENSION_TEXTURE1D = 2;
  private static final int D3D10_RESOURCE_DIMENSION_TEXTURE2D = 3;
  private static final int D3D10_RESOURCE_DIMENSION_TEXTURE3D = 4;

  // Magic number for the file
  private static final int FOURCC_DDS = makeFourCC("DDS ");
  // Special FOURCC code that designates a DX10 header is after the regular header
  private static final int FOURCC_DX10 = makeFourCC("DX10");

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

  private static void validateHeader(DDSHeader h) throws IOException {
    // Must have the magic number 'DDS '
    // Size must be 124, although devIL reports that some files have 'DDS '
    // in the size var as well, so we'll support that.
    if (h.magic != FOURCC_DDS || (h.size != 124 && h.size != FOURCC_DDS)) {
      throw new InvalidImageException("DDS header is invalid");
    }
    if (h.pixelFormat.size != 32) {
      throw new InvalidImageException("DDS pixel format header is invalid");
    }
    // DDSD_CAPS, DDSD_PIXELFORMAT should be set in h.flags, but that is not validated
    // DDSCAPS_TEXTURE should be set in h.caps but that is not validated either

    // Give me some valid assumptions
    if (h.size == FOURCC_DDS) {
      h.size = 124;
    }
    if (h.mipmapCount == 0) {
      h.mipmapCount = 1;
    }
    if (h.depth == 0) {
      h.depth = 1;
    }

    // Header flags will be further validated as the header is interpreted in identifyX() methods
  }

  private TextureDescription identifyTextureDescription(DDSHeader header) throws IOException {
    TextureDescription desc = new TextureDescription();
    // Fill color, format, and packed state of the description
    identifyTextureFormat(header, desc);

    // Check 2D dimensions, must be present for any texture type
    if (isFlagSet(header.flags, DDSD_WIDTH)) {
      desc.width = header.width;
    } else {
      throw new InvalidImageException("DDS header is missing required flag DDSD_WIDTH");
    }

    if (isFlagSet(header.flags, DDSD_HEIGHT)) {
      desc.height = header.height;
    } else {
      throw new InvalidImageException("DDS header is missing required flag DDSD_HEIGHT");
    }

    // We won't check for DDSCAPS_COMPLEX, since some files seem to ignore it when creating cube
    // maps or 3D textures

    // Set sensible defaults for imageCount and depth, to be overridden in deviating situations
    desc.imageCount = 1;
    desc.depth = 1;
    if (isFlagSet(header.caps2, DDSCAPS2_VOLUME)) {
      // Validate 3D textures with DDS header and possibly DX10 header
      if (header.headerDX10 != null) {
        if (header.headerDX10.resourceDimension != D3D10_RESOURCE_DIMENSION_TEXTURE3D) {
          throw new InvalidImageException("DX10 header and surface caps are inconsistent");
        }
        if (header.headerDX10.arraySize > 1) {
          throw new UnsupportedImageFormatException("Texture3D arrays aren't supported");
        }
      }
      if (!isFlagSet(header.flags, DDSD_DEPTH)) {
        desc.depth = header.depth;
      } else {
        throw new InvalidImageException(
            "DDSD header is missing required flag DDSD_DEPTH for a volume texture");
      }

      desc.target = TextureType.TEXTURE_3D;
    } else if (isFlagSet(header.caps2, DDSCAPS2_CUBEMAP)) {
      if (!isFlagSet(header.caps2, DDSCAPS2_CUBEMAP_ALL_FACES)) {
        throw new UnsupportedImageFormatException("Cube map must have 6 faces present");
      }
      if (desc.width != desc.height) {
        throw new UnsupportedImageFormatException("Cube map must have square faces");
      }

      if (header.headerDX10 != null) {
        if (!isFlagSet(header.headerDX10.miscFlag, D3D10_MISC_RESOURCE_TEXTURECUBE)) {
          throw new InvalidImageException("DX10 header and surface caps are inconsistent");
        }

        if (header.headerDX10.resourceDimension == D3D10_RESOURCE_DIMENSION_TEXTURE2D) {
          if (header.headerDX10.arraySize == 6 || header.headerDX10.arraySize == 1) {
            // nVidia sets the DX10 header to be a 2D tex, with arraySize = 6 for a single cubemap,
            // even though arraySize represents the number of total cubes
            desc.target = TextureType.TEXTURE_CUBE;
            desc.imageCount = 6;
          } else {
            // Assume it's a cubemap array, so total images is 6 * arraySize
            desc.target = TextureType.TEXTURE_CUBE_ARRAY;
            desc.imageCount = 6 * header.headerDX10.arraySize;
          }
        } else {
          throw new InvalidImageException("DX10 header and surface caps are inconsistent");
        }
      } else {
        // Simple single cube map
        desc.target = TextureType.TEXTURE_CUBE;
        desc.imageCount = 6;
      }
    } else {
      // No cubemap or volume caps bit set, means this is a basic 1D or 2D texture (determined by
      // dimension), unless a more specific resource type is specified in a DX10 header.
      if (header.headerDX10 != null) {
        if (header.headerDX10.resourceDimension == D3D10_RESOURCE_DIMENSION_TEXTURE1D) {
          // TEXTURE_1D specified in DX10 header
          if (desc.height != 1) {
            throw new InvalidImageException(
                "1D image must have a height of 1 pixel, not: " + desc.height);
          }

          if (header.headerDX10.arraySize > 1) {
            // Upgrade to TEXTURE_1D_ARRAY
            desc.target = TextureType.TEXTURE_1D_ARRAY;
            desc.imageCount = header.headerDX10.arraySize;
          } else {
            desc.target = TextureType.TEXTURE_1D;
          }
        } else if (header.headerDX10.resourceDimension == D3D10_RESOURCE_DIMENSION_TEXTURE2D) {
          // TEXTURE_2D, although this could also be an array or cube map array if the misc bit is set
          if (isFlagSet(header.headerDX10.miscFlag, D3D10_MISC_RESOURCE_TEXTURECUBE)) {
            if (desc.width != desc.height) {
              throw new UnsupportedImageFormatException("Cube map images must be square");
            }

            if (header.headerDX10.arraySize == 6 || header.headerDX10.arraySize == 1) {
              // nVidia sets the DX10 header to be a 2D tex, with arraySize = 6 for a single cubemap,
              // even though arraySize represents the number of total cubes
              desc.target = TextureType.TEXTURE_CUBE;
              desc.imageCount = 6;
            } else {
              // Assume it's a cubemap array, so total images is 6 * arraySize
              desc.target = TextureType.TEXTURE_CUBE_ARRAY;
              desc.imageCount = 6 * header.headerDX10.arraySize;
            }
          } else if (header.headerDX10.arraySize > 1) {
            // A 2D array
            desc.target = TextureType.TEXTURE_2D_ARRAY;
            desc.imageCount = header.headerDX10.arraySize;
          } else {
            desc.target = TextureType.TEXTURE_2D;
          }
        } else {
          throw new InvalidImageException("DX10 header and surface caps are inconsistent");
        }
      } else {
        // Assume 1D or 2D based on height dimension
        if (desc.height == 1) {
          desc.target = TextureType.TEXTURE_1D;
        } else {
          desc.target = TextureType.TEXTURE_2D;
        }
      }
    }

    // Check for mipmap specification
    if (isFlagSet(header.caps, DDSCAPS_MIPMAP)) {
      // DDSCAPS_COMPLEX should be set in h.caps but that is not validated
      if (!isFlagSet(header.flags, DDSD_MIPMAPCOUNT)) {
        // Validate that provided count is complete mipmap set
        int expected = ImageUtils
            .getMaxMipmaps(Math.max(desc.width, Math.max(desc.height, desc.depth)));
        if (header.mipmapCount != expected) {
          throw new InvalidImageException(
              "Expected " + expected + " but got " + header.mipmapCount + " mipmaps instead");
        }
      } // else don't bother since MIPMAPCOUNT may not have been written, and assume a complete mipmap set
      desc.mipmapped = true;
    } else {
      desc.mipmapped = false;
    }

    return desc;
  }

  private void identifyTextureFormat(DDSHeader header, TextureDescription desc) throws IOException {
    DXGIFormat dxgiFormat = null;
    if (header.headerDX10 != null) {
      // The pixel format is stored in the dxgiFormat, which won't be null but set to UNKNOWN if not in our defined enum
      dxgiFormat = header.headerDX10.dxgiFormat;
    } else if (isFlagSet(header.pixelFormat.flags, DDPF_FOURCC)) {
      // Look up the 4CC code and see if it maps to a known dxgiFormat
      dxgiFormat = FOURCC_FORMATS.get(header.pixelFormat.fourCC);
      if (dxgiFormat == null) {
        throw new UnsupportedImageFormatException(
            "Unsupported FOURCC code in header: " + unmakeFourCC(header.pixelFormat.fourCC));
      }
    }

    if (dxgiFormat != null) {
      if (!dxgiFormat.isSupported()) {
        throw new UnsupportedImageFormatException(
            "Unsupported DXGI pixel format: " + header.headerDX10.dxgiFormat);
      } else {
        desc.colorType = header.headerDX10.dxgiFormat.color;
        desc.format = header.headerDX10.dxgiFormat.format;
        desc.packed = header.headerDX10.dxgiFormat.packed;
        return;
      }
    }

    // Otherwise reconstruct a PixelFormat from the masks
    long alphaMask = 0;
    // Put R, G, and B masks in directly, if they happen to be 0 then they will automatically get
    // filtered by the PixelFormat constructor.
    long[] colorMask = new long[] {
        (0xffffffffL & header.pixelFormat.rBitMask), (0xffffffffL & header.pixelFormat.gBitMask),
        (0xffffffffL & header.pixelFormat.bBitMask),
    };
    if (isFlagSet(header.pixelFormat.flags, DDPF_ALPHAPIXELS)) {
      alphaMask = (0xffffffffL & header.pixelFormat.aBitMask);
    }

    int channelCount;
    if (isFlagSet(header.pixelFormat.flags, DDPF_LUMINANCE)) {
      // The red mask is defined to hold luminance values as expected by PixelFormat, but 0 out
      desc.colorType = Luminance.class;
      channelCount = 1;
    } else if (isFlagSet(header.pixelFormat.flags, DDPF_RGB)) {
      // RGB data
      desc.colorType = RGB.Linear.class;
      channelCount = 3;
    } else if (isFlagSet(header.pixelFormat.flags, DDPF_YUV)) {
      // YUV data
      desc.colorType = YUV.class;
      channelCount = 3;
    } else if (isFlagSet(header.pixelFormat.flags, DDPF_ALPHA)) {
      // Alpha only image, so move alpha mask into "red" mask and clear explicit alpha since
      // this will be modeled by the Alpha "color" class.
      colorMask[0] = alphaMask;
      alphaMask = 0;
      desc.colorType = Alpha.class;
      channelCount = 1;
    } else {
      throw new InvalidImageException("Invalid pixel format header, does not specify color type");
    }

    desc.format = PixelFormat
        .createFromMasks(colorMask, channelCount, alphaMask, PixelFormat.Type.UNORM);
    if (desc.format.getTotalBitSize() != header.pixelFormat.rgbBitCount) {
      throw new InvalidImageException(
          "Invalid pixel format header, reported bit count does not match channel bit masks");
    }

    // To determine if the format must be packed, use a simple heuristic of if PackedPixelArray supports
    // it and UnpackedPixelArray does not.
    desc.packed =
        PackedPixelArray.isSupported(desc.format) && !UnpackedPixelArray.isSupported(desc.format);
  }

  private DDSHeader readHeader(SeekableByteChannel in) throws IOException {
    // Magic number is 4 bytes, header is 124 bytes, and DX10 header is 20 bytes = 148 maximum
    // just to read the header. However, the rest of the data will be mapped into memory and
    // copied directly so there is no need to use a conventional work buffer that is quite large
    ByteBuffer work = Data.getBufferFactory().newByteBuffer(148);

    DDSHeader h = new DDSHeader();

    // Read first 128 bytes, which all valid DDS files must have
    IOUtils.read(in, work, 128);
    h.magic = ByteOrderUtils.bytesToIntLE(work);
    h.size = ByteOrderUtils.bytesToIntLE(work);
    h.flags = ByteOrderUtils.bytesToIntLE(work);
    h.height = ByteOrderUtils.bytesToIntLE(work);
    h.width = ByteOrderUtils.bytesToIntLE(work);
    h.linearSize = ByteOrderUtils.bytesToIntLE(work);
    h.depth = ByteOrderUtils.bytesToIntLE(work);
    h.mipmapCount = ByteOrderUtils.bytesToIntLE(work);
    for (int i = 0; i < h.reserved1.length; i++) {
      h.reserved1[i] = ByteOrderUtils.bytesToIntLE(work);
    }

    h.pixelFormat.size = ByteOrderUtils.bytesToIntLE(work);
    h.pixelFormat.flags = ByteOrderUtils.bytesToIntLE(work);
    h.pixelFormat.fourCC = ByteOrderUtils.bytesToIntLE(work);
    h.pixelFormat.rgbBitCount = ByteOrderUtils.bytesToIntLE(work);
    h.pixelFormat.rBitMask = ByteOrderUtils.bytesToIntLE(work);
    h.pixelFormat.gBitMask = ByteOrderUtils.bytesToIntLE(work);
    h.pixelFormat.bBitMask = ByteOrderUtils.bytesToIntLE(work);
    h.pixelFormat.aBitMask = ByteOrderUtils.bytesToIntLE(work);

    h.caps = ByteOrderUtils.bytesToIntLE(work);
    h.caps2 = ByteOrderUtils.bytesToIntLE(work);
    h.caps3 = ByteOrderUtils.bytesToIntLE(work);
    h.caps4 = ByteOrderUtils.bytesToIntLE(work);

    h.reserved2 = ByteOrderUtils.bytesToIntLE(work);
    // This ends the 128 bytes that were previously read

    if (h.pixelFormat.fourCC == FOURCC_DX10) {
      // Read additional bytes
      IOUtils.read(in, work, 20);

      h.headerDX10 = new DX10Header();
      int dxgi = ByteOrderUtils.bytesToIntLE(work);
      if (dxgi < 0 || dxgi >= DXGIFormat.values().length) {
        h.headerDX10.dxgiFormat = DXGIFormat.values()[dxgi];
      } else {
        h.headerDX10.dxgiFormat = DXGIFormat.UNKNOWN;
      }

      h.headerDX10.resourceDimension = ByteOrderUtils.bytesToIntLE(work);
      h.headerDX10.miscFlag = ByteOrderUtils.bytesToIntLE(work);
      h.headerDX10.arraySize = ByteOrderUtils.bytesToIntLE(work);
      h.headerDX10.reserved = ByteOrderUtils.bytesToIntLE(work);
    } else {
      h.headerDX10 = null;
    }

    return h;
  }

  private DataBuffer readImageData(SeekableByteChannel in, TextureDescription desc) throws
      IOException {
    // The descriptions redundant dimensions (possible depth or height) will be set to 1 prior to
    // this, so there is no need to create a 1, 2, or 3 element array; the math is the same
    int[] dimensions = new int[] { desc.width, desc.height, desc.depth };
    long pixels = desc.imageCount * ImageUtils.getUncompressedImageSize(dimensions, desc.mipmapped);
    // Regardless of whether or not the format is packed, the total bit size is the same and correct
    long bytes = pixels * (desc.format.getTotalBitSize() / Byte.SIZE);

    if (in.size() - in.position() < bytes) {
      throw new InvalidImageException(
          "Not enough bytes remaining in file to fully specify image data, expected " + bytes
              + " but only has " + (in.size() - in.position()));
    }

    // Map the file for efficient copying into the final data buffer
    MappedByteBuffer mappedData = ((FileChannel) in)
        .map(FileChannel.MapMode.READ_ONLY, in.position(), bytes);
    // Mark it as little endian since DDS files are all LE, which will then automatically swap
    // the bytes around as necessary
    mappedData.order(ByteOrder.LITTLE_ENDIAN);

    int bitSize;
    if (desc.packed) {
      bitSize = desc.format.getTotalBitSize();
    } else {
      // Assumes every color channel has the same number of bits
      bitSize = desc.format.getColorChannelBitSize(0);
      if (desc.format.getColorChannelType(0) == PixelFormat.Type.SFLOAT) {
        // Possibly use native float or double support
        if (bitSize == 32) {
          // FloatData and mapped as a FloatBuffer
          FloatData data = dataFactory.newFloatData(bytes / Float.BYTES);
          data.setValues(0, mappedData.asFloatBuffer());
          return data;
        } else if (bitSize == 64) {
          // DoubleData and mapped as a DoubleBuffer
          DoubleData data = dataFactory.newDoubleData(bytes / Double.BYTES);
          data.setValues(0, mappedData.asDoubleBuffer());
          return data;
        }
        // Otherwise fall through and just use the bit representation
      }
    }

    switch (bitSize) {
    case 64: {
      // LongData and mapped as a LongBuffer
      LongData data = dataFactory.newLongData(bytes / Long.BYTES);
      data.set(0, mappedData.asLongBuffer());
      return data;
    }
    case 32: {
      // IntData and mapped as a IntBuffer
      IntData data = dataFactory.newIntData(bytes / Integer.BYTES);
      data.set(0, mappedData.asIntBuffer());
      return data;
    }
    case 16: {
      // ShortData and mapped as a ShortBuffer
      ShortData data = dataFactory.newShortData(bytes / Short.BYTES);
      data.set(0, mappedData.asShortBuffer());
      return data;
    }
    case 8: {
      // ByteData and mapped as a ByteBuffer
      ByteData data = dataFactory.newByteData(bytes);
      data.set(0, mappedData);
      return data;
    }
    default:
      throw new UnsupportedImageFormatException("Unsupported bit size for data: " + bitSize);
    }
  }

  // check whether or not flag is set in the flags bit field
  private static boolean isFlagSet(int flags, int flag) {
    return (flags & flag) == flag;
  }

  // Create a 4cc code from the given string. The string must have length = 4
  private static int makeFourCC(String c) {
    if (c.length() != 4) {
      throw new IllegalArgumentException("Input string for a 4CC must have size of 4");
    }
    char[] cc = c.toCharArray();
    return ((cc[3] & 0xff) << 24) | ((cc[2] & 0xff) << 16) | ((cc[1] & 0xff) << 8) | ((cc[0]
        & 0xff));
  }

  // Convert a 4cc code back into string form
  private static String unmakeFourCC(int fourcc) {
    char[] cc = new char[4];
    cc[3] = (char) ((fourcc & 0xff000000) >> 24);
    cc[2] = (char) ((fourcc & 0xff0000) >> 16);
    cc[1] = (char) ((fourcc & 0xff00) >> 8);
    cc[0] = (char) ((fourcc & 0xff));
    return new String(cc);
  }
}
