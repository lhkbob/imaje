package com.lhkbob.imaje.io.exr;

import com.lhkbob.imaje.Images;
import com.lhkbob.imaje.data.Bytes;
import com.lhkbob.imaje.data.Data;
import com.lhkbob.imaje.data.NumericData;
import com.lhkbob.imaje.io.IO;
import com.lhkbob.imaje.io.InvalidImageException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class ScanlineChunkReader extends AbstractChunkReader {

  public ScanlineChunkReader(Data.Factory factory, ChannelMapping mapping) {
    super(factory, mapping);
  }

  @Override
  public void readNextChunk(SeekableByteChannel in, ByteBuffer work) throws IOException {
    if (!IO.read(in, work, 8)) {
      throw new InvalidImageException("Unable to read chunk Y and date size");
    }
    int chunkY = Bytes.bytesToIntLE(work);
    int dataSize = Bytes.bytesToIntLE(work);

    // Calculate the data window that is going to be read in
    Box2Int imageWindow = getHeader().getDataWindow();
    int linesInChunk = getHeader().getCompression().getLinesInBuffer();
    if (chunkY + linesInChunk > imageWindow.getMaxY()) {
      linesInChunk = imageWindow.getMaxY() - chunkY + 1;
    }
    Box2Int dataWindow = new Box2Int(imageWindow.getMinX(), chunkY, imageWindow.getMaxX(),
        chunkY + linesInChunk - 1);

    readChunk(
        dataSize, dataWindow, getDataForLevel(0), getHeader().getLayoutForMipmap(0), in, work);
  }

  @Override
  public OffsetTable readOffsetTable(SeekableByteChannel in, ByteBuffer work) throws IOException {
    return ScanLineOffsetTable.read(getHeader(), in, work);
  }

  @Override
  protected int getMaxUncompressedDataSize() {
    // The chunk block holds rows of data where compression determines the line height
    // and is the full width of the image
    int chunkWidth = getHeader().getDataWindow().width();
    int chunkHeight = getHeader().getCompression().getLinesInBuffer();

    return chunkWidth * chunkHeight * getHeader().getBytesPerPixel();
  }

  @Override
  protected List<NumericData<?>> createBackingData() {
    // Only one level to create
    long length = Images.getUncompressedImageSize(getHeader().getDataWindow().width(),
        getHeader().getDataWindow().height());
    return Collections.singletonList(createData(length));
  }
}
