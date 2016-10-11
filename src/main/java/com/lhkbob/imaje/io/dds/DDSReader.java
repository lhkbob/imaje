package com.lhkbob.imaje.io.dds;

import com.lhkbob.imaje.Image;
import com.lhkbob.imaje.ImageBuilder;
import com.lhkbob.imaje.Images;
import com.lhkbob.imaje.data.ByteData;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.DataBuffer;
import com.lhkbob.imaje.data.DoubleData;
import com.lhkbob.imaje.data.FloatData;
import com.lhkbob.imaje.data.IntData;
import com.lhkbob.imaje.data.LongData;
import com.lhkbob.imaje.data.ShortData;
import com.lhkbob.imaje.io.ImageFileReader;
import com.lhkbob.imaje.io.InvalidImageException;
import com.lhkbob.imaje.io.UnsupportedImageFormatException;
import com.lhkbob.imaje.layout.PixelFormat;
import com.lhkbob.imaje.util.Arguments;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;


/**
 *
 */
public class DDSReader implements ImageFileReader {
  private final Data.Factory dataFactory;

  public DDSReader() {
    this(null);
  }

  public DDSReader(@Arguments.Nullable Data.Factory factory) {
    if (factory == null) {
      factory = Data.getDefaultDataFactory();
    }
    dataFactory = factory;
  }

  @Override
  public Image<?> read(SeekableByteChannel in) throws IOException {
    // Validate and interpret the header
    DDSHeader header = DDSHeader.readHeader(in);
    TextureDescription desc = TextureDescription.createFromHeader(header);

    // Configure an image builder based on the texture target
    ImageBuilder<?, ?, ?> b;
    switch (desc.getTextureType()) {
    case TEXTURE_1D:
    case TEXTURE_2D:
      // These texture types map to Raster or Mipmap
      if (desc.isMipmapped()) {
        b = Images.newMipmap(desc.getColorType());
      } else {
        b = Images.newRaster(desc.getColorType());
      }
      break;
    case TEXTURE_1D_ARRAY:
    case TEXTURE_2D_ARRAY:
    case TEXTURE_CUBE:
    case TEXTURE_CUBE_ARRAY:
      // These texture types map to RasterArray or MipmapArray
      if (desc.isMipmapped()) {
        b = Images.newMipmapArray(desc.getColorType()).layers(desc.getImageCount());
      } else {
        b = Images.newRasterArray(desc.getColorType()).layers(desc.getImageCount());
      }
      break;
    case TEXTURE_3D:
      if (desc.isMipmapped()) {
        b = Images.newMipmapVolume(desc.getColorType()).depth(desc.getDepth());
      } else {
        b = Images.newVolume(desc.getColorType()).depth(desc.getDepth());
      }
      break;
    default:
      // This should not happen
      throw new InvalidImageException("Unknown texture target: " + desc.getTextureType());
    }
    // TODO: the generate mipmaps flag is ignored currently

    // Common configuration for all builders
    b.width(desc.getWidth()).height(desc.getHeight())
        .format(desc.getPixelFormat(), desc.isPackedFormat());

    // Now read in the image data, slicing it up for each 2D image contained within
    readImageData(in, desc, b);
    return b.build();
  }


  private long getTotalImageDataPixels(TextureDescription desc) {
    // The descriptions redundant dimensions (possible depth or height) will be set to 1 prior to
    // this, so there is no need to create a 1, 2, or 3 element array; the math is the same
    int[] dimensions = new int[] { desc.getWidth(), desc.getHeight(), desc.getDepth() };
    long pixels = 0;
    if (desc.isMipmapped()) {
      int mipmapCount = Images.getMaxMipmaps(dimensions);
      for (int i = 0; i < mipmapCount; i++) {
        pixels += Images.getUncompressedImageSize(Images.getMipmapDimensions(dimensions, i));
      }
    } else {
      pixels = Images.getUncompressedImageSize(dimensions);
    }

    // Multiply by image Count since there are that many duplicates of that image stack
    return pixels * desc.getImageCount();
  }

  private void readImageData(
      SeekableByteChannel in, TextureDescription desc, ImageBuilder<?, ?, ?> builder) throws
      IOException {
    long pixels = getTotalImageDataPixels(desc);
    // Regardless of whether or not the format is packed, the total bit size is the same and correct
    long bytes = pixels * (desc.getPixelFormat().getTotalBitSize() / Byte.SIZE);

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

    // Because each pixel array uses its own data buffer, and they don't get offsets, we loop
    // over array, mipmap, and then depth and set each buffer into the builder as appropriate.
    for (int i = 0; i < desc.getImageCount(); i++) {
      if (desc.isMipmapped()) {
        int mipmapCount = Images.getMaxMipmaps(desc.getWidth(), desc.getHeight(), desc.getDepth());
        for (int j = 0; j < mipmapCount; j++) {
          int mipWidth = Images.getMipmapDimension(desc.getWidth(), j);
          int mipHeight = Images.getMipmapDimension(desc.getHeight(), j);

          if (desc.getTextureType() == TextureType.TEXTURE_3D) {
            // Each depth slice has to be extracted as well, and since 3D arrays are not supported,
            // assume i == 0 and ignore it, using depth for the layer sent to the builder.
            int mipDepth = Images.getMipmapDimension(desc.getDepth(), j);
            for (int z = 0; z < mipDepth; z++) {
              builder.existingDataFor(z, j, read2DLayer(mipWidth, mipHeight, desc, mappedData));
            }
          } else {
            // 2D slice
            builder.existingDataFor(i, j, read2DLayer(mipWidth, mipHeight, desc, mappedData));
          }
        }
      } else {
        if (desc.getTextureType() == TextureType.TEXTURE_3D) {
          // Extract each depth layer as well
          for (int z = 0; z < desc.getDepth(); z++) {
            builder.existingDataForLayer(z,
                read2DLayer(desc.getWidth(), desc.getHeight(), desc, mappedData));
          }
        } else {
          builder.existingDataForLayer(i,
              read2DLayer(desc.getWidth(), desc.getHeight(), desc, mappedData));
        }
      }
    }
  }

  private DataBuffer read2DLayer(
      int width, int height, TextureDescription desc, MappedByteBuffer fileData) throws
      InvalidImageException, UnsupportedImageFormatException {
    int bytesForLayer = width * height * desc.getPixelFormat().getTotalBitSize() / Byte.SIZE;
    if (fileData.remaining() < bytesForLayer) {
      throw new InvalidImageException("Insufficient data in file to read remaining image data");
    }

    // Get an independent slice of the file data from current position for layer's required bytes
    int layerEnd = fileData.position() + bytesForLayer;
    fileData.limit(layerEnd);
    ByteBuffer layerData = fileData.slice();
    // Then advance the file data's position to the end of this slice
    fileData.limit(fileData.capacity()).position(layerEnd);

    int bitSize;
    if (desc.isPackedFormat()) {
      bitSize = desc.getPixelFormat().getTotalBitSize();
    } else {
      // Assumes every color channel has the same number of bits
      bitSize = desc.getPixelFormat().getColorChannelBitSize(0);
      if (desc.getPixelFormat().getColorChannelType(0) == PixelFormat.Type.SFLOAT) {
        // Possibly use native float or double support
        if (bitSize == 32) {
          // FloatData and mapped as a FloatBuffer
          FloatData data = dataFactory.newFloatData(bytesForLayer / Float.BYTES);
          data.setValues(0, layerData.asFloatBuffer());
          return data;
        } else if (bitSize == 64) {
          // DoubleData and mapped as a DoubleBuffer
          DoubleData data = dataFactory.newDoubleData(bytesForLayer / Double.BYTES);
          data.setValues(0, layerData.asDoubleBuffer());
          return data;
        }
        // Otherwise fall through and just use the bit representation
      }
    }

    switch (bitSize) {
    case 64: {
      // LongData and mapped as a LongBuffer
      LongData data = dataFactory.newLongData(bytesForLayer / Long.BYTES);
      data.set(0, layerData.asLongBuffer());
      return data;
    }
    case 32: {
      // IntData and mapped as a IntBuffer
      IntData data = dataFactory.newIntData(bytesForLayer / Integer.BYTES);
      data.set(0, layerData.asIntBuffer());
      return data;
    }
    case 16: {
      // ShortData and mapped as a ShortBuffer
      ShortData data = dataFactory.newShortData(bytesForLayer / Short.BYTES);
      data.set(0, layerData.asShortBuffer());
      return data;
    }
    case 8: {
      // ByteData and mapped as a ByteBuffer
      ByteData data = dataFactory.newByteData(bytesForLayer);
      data.set(0, layerData);
      return data;
    }
    default:
      throw new UnsupportedImageFormatException("Unsupported bit size for data: " + bitSize);
    }
  }
}
