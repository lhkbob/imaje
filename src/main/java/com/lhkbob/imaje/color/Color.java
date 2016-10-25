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
package com.lhkbob.imaje.color;

import com.lhkbob.imaje.color.annot.Channels;
import com.lhkbob.imaje.util.Arguments;

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
      // First time instantiating this color, so lookup the Channels annotation
      channelSize = getChannelCountFromAnnotation(getClass());
      if (channelSize == null) {
        throw new IllegalStateException("Color subclasses must be annotated with @Channels");
      }

      // Cache for future allocations; there is no important race condition between puts with the
      // same key since the channel size is fixed for a given class, and even if multiple threads
      // calculate the channel size from scratch, the stored value will be the same.
      channelCountCache.put(getClass(), channelSize);
    }

    channels = new double[channelSize];
  }

  private static Integer getChannelCountFromAnnotation(Class<? extends Color> color) {
    Channels channelDef = color.getAnnotation(Channels.class);
    if (channelDef == null) {
      return null;
    }
    if (channelDef.unnamedChannelCount() >= 0) {
      // Assert that no names are provided and use reported channel count as the final size
      if (channelDef.value().length > 0) {
        throw new IllegalStateException(
            "Cannot specify channel names when unnamedChannelCount is positive");
      }
      return channelDef.unnamedChannelCount();
    } else {
      // Number of channels is equal to the length of the provided channel names array
      return channelDef.value().length;
    }
  }

  public static int getChannelCount(Class<? extends Color> color) {
    Arguments.notNull("color", color);

    Integer size = channelCountCache.get(color);
    if (size != null) {
      return size;
    } else {
      // We could instantiate an instance for concrete color classes, but to support querying on
      // abstract classes like RGB, the annotation is queried every time
      size = getChannelCountFromAnnotation(color);
      if (size == null) {
        throw new IllegalArgumentException(
            "Abstract color does not define a concrete channel size: " + color);
      }
      channelCountCache.put(color, size);
      return size;
    }
  }

  public static <T extends Color> T newInstance(Class<T> color) {
    Arguments.notNull("color", color);

    try {
      return color.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException("Color not instantiable with reflection: " + color, e);
    }
  }

  @Override
  public Color clone() {
    try {
      Color c = (Color) super.clone();
      // Make a deep clone of the channels array
      c.channels = Arrays.copyOf(channels, channels.length);
      return c;
    } catch (CloneNotSupportedException e) {
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
    Arguments.equals("channel count", channels.length, values.length);

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
    if (o == this) {
      return true;
    }
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
