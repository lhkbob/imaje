package com.lhkbob.imaje.layout;

import com.lhkbob.imaje.data.DataSource;

import java.util.function.Predicate;

/**
 *
 */
public abstract class PixelEncoder {
  public static final int ALPHA_CHANNEL = -1;
  // Really any negative number that isn't ALPHA_CHANNEL would work, but this provides a constant
  // so code that relies on skipping a data channel is more readable.
  public static final int SKIP_CHANNEL = -2;

  protected final int[] logicalToDataChannelMap;
  protected final int alphaDataChannelIndex;

  protected final PixelLayout layout;

  public PixelEncoder(int[] dataToLogicalChannelMap, PixelLayout layout) {
    // Validate the data to logical channel map, where the logic is dependent on having an alpha channel
    int alphaIndex = -1;
    for (int i = 0; i < dataToLogicalChannelMap.length; i++) {
      if (dataToLogicalChannelMap[i] == ALPHA_CHANNEL) {
        if (alphaIndex >= 0) {
          // Duplicate alpha index specified, which is illegal
          throw new IllegalArgumentException(
              "Duplicate alpha channels specified in data to logical map");
        }
        alphaIndex = i;
      }
    }

    int logicalChannelsCount;
    if (alphaIndex < 0) {
      // Has no alpha, so the map length should be color channels max
      logicalChannelsCount = dataToLogicalChannelMap.length;
    } else {
      // Has an alpha, so the map length should be 1+color channels max
      logicalChannelsCount = dataToLogicalChannelMap.length - 1;
    }
    // However, to support formats and adapters that want to store a color that needs fewer channels
    // than what the data can hold (e.g. an XY normal map in an RGBA texture), or even to cleverly
    // encode two images into the same data where one is set to RG and the other stores to BA,
    // the dataToLogicalMap can reference fewer logical channels.
    int maxReferencedLogicalChannel = -1;
    for (int aDataToLogicalChannelMap : dataToLogicalChannelMap) {
      if (aDataToLogicalChannelMap >= logicalChannelsCount) {
        throw new IllegalArgumentException(
            "Cannot reference more logical color channels than available in data map");
      }
      if (aDataToLogicalChannelMap > maxReferencedLogicalChannel) {
        maxReferencedLogicalChannel = aDataToLogicalChannelMap;
      }
    }
    logicalChannelsCount = maxReferencedLogicalChannel + 1;

    // 0 to color channels max - 1 must be present in any order, detect and build up the
    // inverse map simultaneously
    int[] logicalToData = new int[logicalChannelsCount];
    for (int i = 0; i < logicalChannelsCount; i++) {
      // Search for i within dataToLogicalMap. Although not necessary to check for duplicates, it
      // allows for improved error reporting (instead of specifying a different value is missing).
      int index = -1;
      for (int j = 0; j < dataToLogicalChannelMap.length; j++) {
        if (dataToLogicalChannelMap[j] == i) {
          if (index >= 0) {
            // Duplicate logical index
            throw new IllegalArgumentException("Duplicate logical color channel provided: " + i);
          }
          index = j;
        }
      }

      if (index < 0) {
        throw new IllegalArgumentException("No data channel maps to logical color channel: " + i);
      }
      logicalToData[i] = index;
    }

    this.layout = layout;

    logicalToDataChannelMap = logicalToData;
    alphaDataChannelIndex = alphaIndex;
  }

  public int getColorChannelCount() {
    return logicalToDataChannelMap.length;
  }

  public PixelLayout getLayout() {
    return layout;
  }

  public abstract double get(int x, int y, double[] channelValues, int offset);

  public abstract double get(int x, int y, double[] channelValues, int offset, long[] channels);

  public abstract double getAlpha(int x, int y);

  public abstract void set(int x, int y, double[] channelValues, int offset, double a);

  public abstract void set(int x, int y, double[] channelValues, int offset, double a, long[] channels);

  public abstract void setAlpha(int x, int y, double alpha);

  public abstract DataSource getData();

  public Predicate<GPUFormat> getFormatFilter() {
    // FIXME this doesn't handle the cases where some data channels are not mapped.
    GPUFormat.Channel[] channels = new GPUFormat.Channel[layout.getChannelCount()];
    for (int i = 0; i < logicalToDataChannelMap.length; i++) {
      // 0 = R, 1 = G, 2 = B, 3 = A if there's no alpha, otherwise 3+ = X
      // (To support CMYK being represented as an RGBA format on the GPU).
      GPUFormat.Channel c;
      if (i > 3) {
        c = GPUFormat.Channel.X;
      } else if (i == 3) {
        if (hasAlphaChannel())
          c = GPUFormat.Channel.X;
        else
          c = GPUFormat.Channel.A;
      } else if (i == 2) {
        c = GPUFormat.Channel.B;
      } else if (i == 1) {
        c = GPUFormat.Channel.G;
      } else { // i == 0
        c = GPUFormat.Channel.R;
      }

      int dataChannel = logicalToDataChannelMap[i];
      channels[dataChannel] = c;
    }

    if (hasAlphaChannel()) {
      channels[alphaDataChannelIndex] = GPUFormat.Channel.A;
    }

    return GPUFormatFilters.channelLayout(channels);
  }

  public boolean hasAlphaChannel() {
    return alphaDataChannelIndex >= 0;
  }
}
