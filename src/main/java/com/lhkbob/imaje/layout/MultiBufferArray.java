package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.data.DataBuffer;
import com.lhkbob.imaje.data.NumericData;
import com.lhkbob.imaje.util.Arguments;

import java.util.Iterator;
import java.util.Spliterator;

/**
 * MultiBufferArray
 * ================
 *
 * A RootPixelArray implementation that stores values for each data field in a PixelFormat in its
 * own NumericData instance. Each field can have a data buffer implemented by a different class, in
 * order to match the format's configured bit size and type. Pixels are arranged in each field's
 * buffer in the same way, as described by a shared DataLayout. This layout must only have a single
 * band, although the layout returned by MultiBufferArray's {@link #getLayout()} returns a layout
 * that copies the shared layout's computed index across the number of data fields required by the
 * format.
 *
 * @author Michael Ludwig
 */
public class MultiBufferArray extends RootPixelArray {
  private final NumericData<?>[] data;
  private final MultiBufferLayout layout;
  private final PixelFormat format;

  /**
   * Create a new MultiBufferArray that contains data described by the given `format`, arranged in
   * each buffer as per `sharedLayout`, and has a separate data buffer per band, contained in
   * `data`. The shared layout must have a single band, since it represents the way in which a band
   * is organized in its own NumericData instance. This shared layout is wrapped to report the
   * actual band count of the array (based on `data`'s length) when {@link #getLayout()} is called.
   *
   * The format's data field count must equal the length of `data`, and each element of `data`
   * corresponds to a field based on its order within `data`. Each NumericData instance in `data`
   * must have a length equal to the required elements specified by `sharedLayout`. The format must
   * not have any skipped channels or custom data channels. For each data field, the corresponding
   * NumericData instance must be compatible with the configured type and bit size (as specified in
   * `format`), determined by {@link PixelArrays#checkBufferCompatible(DataBuffer, PixelFormat.Type,
   * int)}.
   *
   * @param format
   *     The pixel format of this array
   * @param sharedLayout
   *     The 1-band data layout reused for each band's data buffer
   * @param data
   *     The array of NumericData buffers, 1 per band or field
   * @throws IllegalArgumentException
   *     if the constraints described above are not met
   */
  public MultiBufferArray(PixelFormat format, DataLayout sharedLayout, NumericData<?>... data) {
    Arguments.equals("data field count", format.getDataFieldCount(), data.length);
    Arguments.equals("custom channel count", 0, format.getCustomChannelCount());

    layout = new MultiBufferLayout(sharedLayout, data.length);
    this.format = format;

    this.data = new NumericData[data.length];
    for (int i = 0; i < data.length; i++) {
      if (format.isDataFieldSkipped(i)) {
        throw new IllegalArgumentException("Format cannot have any skipped fields");
      }

      // Each databuffer must have the same number of elements as described by the layout
      Arguments.equals("data length", sharedLayout.getRequiredDataElements(), data[i].getLength());
      PixelArrays.checkBufferCompatible(data[i], format.getDataFieldType(i),
          format.getDataFieldBitSize(i));
      this.data[i] = data[i];
    }
  }

  @Override
  public double get(int x, int y, double[] channelValues) {
    long offset = layout.getBandOffset(x, y, 0);
    for (int i = 0; i < format.getColorChannelCount(); i++) {
      int dataChannel = format.getColorChannelDataField(i);
      channelValues[i] = data[dataChannel].getValue(offset);
    }

    if (format.hasAlphaChannel()) {
      return data[format.getAlphaChannelDataField()].getValue(offset);
    } else {
      return 1.0;
    }
  }

  @Override
  public double get(int x, int y, double[] channelValues, long[] bandOffsets) {
    layout.getBandOffsets(x, y, bandOffsets);
    for (int i = 0; i < format.getColorChannelCount(); i++) {
      int dataChannel = format.getColorChannelDataField(i);
      channelValues[i] = data[dataChannel].getValue(bandOffsets[0]);
    }

    if (format.hasAlphaChannel()) {
      return data[format.getAlphaChannelDataField()].getValue(bandOffsets[0]);
    } else {
      return 1.0;
    }
  }

  @Override
  public double getAlpha(int x, int y) {
    if (format.hasAlphaChannel()) {
      return data[format.getAlphaChannelDataField()].getValue(layout.getBandOffset(x, y, 0));
    } else {
      return 1.0;
    }
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a) {
    long offset = layout.getBandOffset(x, y, 0);
    for (int i = 0; i < format.getColorChannelCount(); i++) {
      int dataChannel = format.getColorChannelDataField(i);
      data[dataChannel].setValue(offset, channelValues[i]);
    }

    if (format.hasAlphaChannel()) {
      data[format.getAlphaChannelDataField()].setValue(offset, a);
    }
  }

  @Override
  public void set(int x, int y, double[] channelValues, double a, long[] bandOffsets) {
    layout.getBandOffsets(x, y, bandOffsets);
    for (int i = 0; i < format.getColorChannelCount(); i++) {
      int dataChannel = format.getColorChannelDataField(i);
      data[dataChannel].setValue(bandOffsets[0], channelValues[i]);
    }

    if (format.hasAlphaChannel()) {
      data[format.getAlphaChannelDataField()].setValue(bandOffsets[0], a);
    }
  }

  @Override
  public void setAlpha(int x, int y, double alpha) {
    data[format.getAlphaChannelDataField()].setValue(layout.getBandOffset(x, y, 0), alpha);
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public DataLayout getLayout() {
    return layout;
  }

  @Override
  public NumericData<?> getData(int band) {
    return data[band];
  }

  @Override
  public PixelFormat getFormat() {
    return format;
  }

  private static class MultiBufferLayout implements DataLayout {
    private final DataLayout layout;
    private final int bufferCount;

    MultiBufferLayout(DataLayout layout, int bufferCount) {
      Arguments.equals("layout.getBandCount()", 1, layout.getBandCount());
      Arguments.isPositive("bufferCount", bufferCount);
      this.layout = layout;
      this.bufferCount = bufferCount;
    }

    @Override
    public int getHeight() {
      return layout.getHeight();
    }

    @Override
    public void getBandOffsets(int x, int y, long[] bandOffsets) {
      Arguments.equals("bandOffsets.length", bufferCount, bandOffsets.length);
      long offset = layout.getBandOffset(x, y, 0);
      for (int i = 0; i < bandOffsets.length; i++) {
        bandOffsets[i] = offset;
      }
    }

    @Override
    public long getBandOffset(int x, int y, int band) {
      Arguments.checkIndex("band", bufferCount, band);
      return layout.getBandOffset(x, y, 0);
    }

    @Override
    public int getBandCount() {
      return bufferCount;
    }

    @Override
    public boolean isGPUCompatible() {
      // This layout spreads data across multiple buffers, so it's not GPU compatible
      return false;
    }

    @Override
    public int getWidth() {
      return layout.getWidth();
    }

    @Override
    public void iterateWindow(
        int x, int y, int width, int height, BlockVisitor receiver) {
      long[] bufferOffsets = new long[bufferCount];
      layout.iterateWindow(x, y, width, height, (cx, cy, stride, len, offsets) -> {
        for (int i = 0; i < bufferOffsets.length; i++) {
          bufferOffsets[i] = offsets[0];
        }
        receiver.visit(cx, cy, stride, len, bufferOffsets);
      });
    }

    @Override
    public Iterator<ImageCoordinate> iterator(int x, int y, int width, int height) {
      return layout.iterator(x, y, width, height);
    }

    @Override
    public Spliterator<ImageCoordinate> spliterator(int x, int y, int width, int height) {
      return layout.spliterator(x, y, width, height);
    }
  }
}
