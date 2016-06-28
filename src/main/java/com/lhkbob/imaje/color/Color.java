package com.lhkbob.imaje.color;

import com.lhkbob.imaje.color.annot.Channels;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Document that all Color implementations must provide a public default constructor.
 */
public abstract class Color implements Cloneable {
  // The number of channels in a color is declared by annotating the subclass of Color. Color
  // reads this required annotation and thus enforces the behavior that all instance shave the
  // exact same number of channels. To avoid excessive reflection performance, the channel count
  // is cached here. Microbenchmarks suggest that the map lookup is only slightly slower than
  // a regular instance allocation cost.
  private static final ConcurrentHashMap<Class<? extends Color>, Integer> channelCountCache = new ConcurrentHashMap<>();

  private double[] channels;

  public Color() {
    Integer channelSize = channelCountCache.get(getClass());
    if (channelSize == null) {
      // First time instantiating this cost, so lookup the Channels annotation
      Channels channelDef = getClass().getAnnotation(Channels.class);
      if (channelDef == null) {
        throw new IllegalStateException("Color subclasses must be annotated with @Channels");
      }
      if (channelDef.unnamedChannelCount() >= 0) {
        // Assert that no names are provided and use reported channel count as the final size
        if (channelDef.value().length > 0) {
          throw new IllegalStateException("Cannot specify channel names when unnamedChannelCount is positive");
        }
        channelSize = channelDef.unnamedChannelCount();
      } else {
        // Number of channels is equal to the length of the provided channel names array
        channelSize = channelDef.value().length;
      }

      // Cache for future allocations; there is no important race condition between puts with the
      // same key since the channel size is fixed for a given class, and even if multiple threads
      // calculate the channel size from scratch, the stored value will be the same.
      channelCountCache.put(getClass(), channelSize);
    }

    channels = new double[channelSize];
  }

  @Override
  public Color clone() {
    try {
      Color c = (Color) super.clone();
      // Make a deep clone of the channels array
      c.channels = Arrays.copyOf(channels, channels.length);
      return c;
    } catch(CloneNotSupportedException e) {
      throw new RuntimeException("Should not happen");
    }
  }

  public final double get(int channel) {
    return channels[channel];
  }

  public final void set(int channel, double value) {
    channels[channel] = value;
  }

  public final int getChannelCount() {
    return channels.length;
  }

  public final void set(double... values) {
    if (values.length != channels.length) {
      throw new IllegalArgumentException("Incorrect number of channel values provided, requires " + channels.length + " but received " + values.length);
    }
    System.arraycopy(values, 0, channels, 0, channels.length);
  }

  public final double[] getChannels() {
    return channels;
  }

  @Override
  public final int hashCode() {
    return getClass().hashCode() ^ Arrays.hashCode(channels);
  }

  @Override
  public final boolean equals(Object o) {
    if (o == this)
      return true;
    if (!getClass().isInstance(o)) {
      return false;
    }
    Color c = (Color) o;
    return Arrays.equals(c.channels, channels);
  }

  @Override
  public String toString() {
    return String.format("%s %s", getClass().getSimpleName(), Arrays.toString(channels));
  }
}
