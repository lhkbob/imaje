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
package com.lhkbob.imaje.io.exr;

import com.lhkbob.imaje.color.Alpha;
import com.lhkbob.imaje.color.Color;
import com.lhkbob.imaje.color.Depth;
import com.lhkbob.imaje.color.Generic;
import com.lhkbob.imaje.color.Luminance;
import com.lhkbob.imaje.color.Normal;
import com.lhkbob.imaje.color.RGB;
import com.lhkbob.imaje.color.XYZ;
import com.lhkbob.imaje.io.UnsupportedImageFormatException;
import com.lhkbob.imaje.layout.PixelFormat;
import com.lhkbob.imaje.layout.PixelFormatBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Things this needs to do:
 * - map every channel in a header to a pixel format
 * - group channels into assigned pixel formats
 * - map the pixel formats to a color
 * - name the pixel format based on its layer hierarchy from the channel name
 * - layer name + color do not necessarily uniquely identify a pixel format
 * - a channel belongs to a layer (implicit), color group (same as the pixel format + color type pair),
 * the color channel of the pixel format, and the data channel of the format (implicit == index
 * of channel within header channels list)
 */
public class ChannelMapping {
  private final Map<String, Map<PixelFormat, Class<? extends Color>>> formats;

  private final Map<Channel, Integer> channelMapping;
  private final Map<Channel, PixelFormat> formatMapping;

  private final OpenEXRHeader header;

  public ChannelMapping(OpenEXRHeader header) throws UnsupportedImageFormatException {
    Map<Channel, Class<? extends Color>> colorMapping = new HashMap<>();
    Map<Channel, Integer> channelMapping = new HashMap<>();
    Map<Channel, PixelFormat> formatMapping = new HashMap<>();

    formats = buildColorMapping(header, channelMapping, formatMapping);

    this.channelMapping = Collections.unmodifiableMap(channelMapping);
    this.formatMapping = Collections.unmodifiableMap(formatMapping);
    this.header = header;
  }

  public OpenEXRHeader getHeader() {
    return header;
  }

  private static PixelFormat makePixelFormat(
      List<Channel> logicalChannels, Channel alphaChannel, List<Channel> allChannels) {
    PixelFormatBuilder pf = new PixelFormatBuilder();

    // The pixel format will have data channels equal to allChannels.size()
    for (Channel c : allChannels) {
      // If c is equal to the alpha channel, add an alpha channel at current position
      // otherwise if it's one of the logical channels, add channel with that index,
      // otherwise add a skip channel.
      if (alphaChannel == c) {
        pf.addChannel(
            PixelFormat.ALPHA_CHANNEL, c.getFormat().getBits(), c.getFormat().getPixelType());
      } else {
        int index = logicalChannels.indexOf(c);
        if (index >= 0) {
          pf.addChannel(index, c.getFormat().getBits(), c.getFormat().getPixelType());
        } else {
          pf.addChannel(
              PixelFormat.SKIP_CHANNEL, c.getFormat().getBits(), c.getFormat().getPixelType());
        }
      }
    }

    return pf.build();
  }

  private static void updateChannelMapping(
      List<Channel> logicalChannels, Map<Channel, Integer> channelMapping) {
    for (int i = 0; i < logicalChannels.size(); i++) {
      channelMapping.put(logicalChannels.get(i), i);
    }
  }

  private static void updateFormatMapping(
      List<Channel> logicalChannels, PixelFormat format, Map<Channel, PixelFormat> formatMapping) {
    for (Channel c : logicalChannels) {
      formatMapping.put(c, format);
    }
  }

  private static void updateFormats(
      List<Channel> logicalChannels, PixelFormat format, Class<? extends Color> color,
      Map<PixelFormat, Class<? extends Color>> formats, Map<Channel, Integer> channelMapping,
      Map<Channel, PixelFormat> formatMapping) {
    formats.put(format, color);
    updateChannelMapping(logicalChannels, channelMapping);
    updateFormatMapping(logicalChannels, format, formatMapping);
  }

  private static List<Channel> getMatchedChannels(
      Map<String, Channel> channelsByName, List<Channel> remainingChannels,
      String... channelNames) {
    List<Channel> logical = new ArrayList<>();
    for (String name : channelNames) {
      if (!channelsByName.containsKey(name)) {
        // Can't match the channel name pattern so abort the whole match
        return null;
      }

      // Otherwise collect the channel in the name order
      logical.add(channelsByName.get(name));
    }

    // Remove the matched channels from the map and list
    for (int i = 0; i < channelNames.length; i++) {
      channelsByName.remove(channelNames[i]);
      remainingChannels.remove(logical.get(i));
    }

    return logical;
  }

  public static Map<String, Map<PixelFormat, Class<? extends Color>>> buildColorMapping(
      OpenEXRHeader header, Map<Channel, Integer> channelMapping,
      Map<Channel, PixelFormat> formatMapping) throws UnsupportedImageFormatException {
    // First group channels by their group name
    Map<String, List<Channel>> byLayer = new HashMap<>();
    for (Channel c : header.getChannels()) {
      String layer = c.getGroupName();
      List<Channel> forLayer = byLayer.get(layer);
      if (forLayer == null) {
        forLayer = new ArrayList<>();
        byLayer.put(layer, forLayer);
      }
      forLayer.add(c);
    }

    // For each detected layer, match the channel names to known conventions (after discarding the
    // layer name). If the layer does not match a known color type it maps to one of the generic
    // channel color types.
    Map<String, Map<PixelFormat, Class<? extends Color>>> formats = new HashMap<>();
    for (Map.Entry<String, List<Channel>> layer : byLayer.entrySet()) {
      List<Channel> layerChannels = layer.getValue();

      Map<String, Channel> channelsByName = new HashMap<>();
      // Validate channels and collect names without layer prefix
      for (Channel c : layerChannels) {
        if (channelsByName.put(c.getName(), c) != null) {
          throw new UnsupportedImageFormatException(
              "Duplicate channel name present in layer: " + c.getName());
        }
      }

      // The channels organized as detected color types and pixel formats
      Map<PixelFormat, Class<? extends Color>> layerFormats = new HashMap<>();
      List<Channel> logicalChannels;

      // Channel names are stored lexigraphically, so if there is an alpha channel it will be the
      // first in the list.
      Channel alphaChannel = null;
      if (channelsByName.containsKey("a")) {
        alphaChannel = channelsByName.remove("a");
        layerChannels.remove(alphaChannel);
      }

      // If there are no other channels, then make this a logical alpha image
      if (channelsByName.isEmpty()) {
        logicalChannels = Collections.singletonList(alphaChannel);
        PixelFormat pf = makePixelFormat(logicalChannels, null, header.getChannels());
        updateFormats(
            logicalChannels, pf, Alpha.class, layerFormats, channelMapping, formatMapping);
      }

      // RGB match
      logicalChannels = getMatchedChannels(channelsByName, layerChannels, "r", "g", "b");
      if (logicalChannels != null) {
        // RGB.Linear or XYZ depending on chromaticity, which is addressed later since the
        // chromaticity is a header property and does not depend on the particular layer group.

        PixelFormat pf = makePixelFormat(logicalChannels, alphaChannel, header.getChannels());
        updateFormats(
            logicalChannels, pf, RGB.Linear.class, layerFormats, channelMapping, formatMapping);
      }

      // Normal match
      logicalChannels = getMatchedChannels(channelsByName, layerChannels, "x", "y", "z");
      if (logicalChannels != null) {
        PixelFormat pf = makePixelFormat(logicalChannels, alphaChannel, header.getChannels());
        updateFormats(logicalChannels, pf, Normal.class, layerFormats, channelMapping,
            formatMapping);
      }

      // Alternate normal match
      logicalChannels = getMatchedChannels(channelsByName, layerChannels, "nx", "ny", "nz");
      if (logicalChannels != null) {
        PixelFormat pf = makePixelFormat(logicalChannels, alphaChannel, header.getChannels());
        updateFormats(logicalChannels, pf, Normal.class, layerFormats, channelMapping,
            formatMapping);
      }

      // Luminance (must come after vector colors since y can be matched there too)
      logicalChannels = getMatchedChannels(channelsByName, layerChannels, "y");
      if (logicalChannels != null) {
        PixelFormat pf = makePixelFormat(logicalChannels, alphaChannel, header.getChannels());
        updateFormats(logicalChannels, pf, Luminance.class, layerFormats, channelMapping,
            formatMapping);
      }

      // Depth (must come after vector colors since z can be matched there too)
      logicalChannels = getMatchedChannels(channelsByName, layerChannels, "z");
      if (logicalChannels != null) {
        PixelFormat pf = makePixelFormat(logicalChannels, alphaChannel, header.getChannels());
        updateFormats(logicalChannels, pf, Depth.Linear.class, layerFormats, channelMapping,
            formatMapping);
      }

      // Alternate depth match
      logicalChannels = getMatchedChannels(channelsByName, layerChannels, "zback");
      if (logicalChannels != null) {
        PixelFormat pf = makePixelFormat(logicalChannels, alphaChannel, header.getChannels());
        updateFormats(logicalChannels, pf, Depth.Linear.class, layerFormats, channelMapping,
            formatMapping);
      }

      // Known color types have been removed, so only generic colors can be mapped now;
      // and since channels have also been removed from layerChannels, that list can be used as-is
      // for the logicalChannels ordering in makePixelFormat().
      if (channelsByName.size() == 1) {
        PixelFormat pf = makePixelFormat(layerChannels, alphaChannel, header.getChannels());
        updateFormats(
            layerChannels, pf, Generic.C1.class, layerFormats, channelMapping, formatMapping);
      } else if (channelsByName.size() == 2) {
        PixelFormat pf = makePixelFormat(layerChannels, alphaChannel, header.getChannels());
        updateFormats(layerChannels, pf, Generic.C2.class, layerFormats, channelMapping,
            formatMapping);
      } else if (channelsByName.size() == 3) {
        PixelFormat pf = makePixelFormat(layerChannels, alphaChannel, header.getChannels());
        updateFormats(layerChannels, pf, Generic.C3.class, layerFormats, channelMapping,
            formatMapping);
      } else if (channelsByName.size() == 4) {
        PixelFormat pf = makePixelFormat(layerChannels, alphaChannel, header.getChannels());
        updateFormats(layerChannels, pf, Generic.C4.class, layerFormats, channelMapping,
            formatMapping);
      } else {
        // Unmappable color with currently defined Color classes
        throw new UnsupportedImageFormatException(
            "Unable to map channels to color class: " + channelsByName.keySet());
      }

      // The layer is completed
      formats.put(layer.getKey(), layerFormats);
    }

    // Possibly convert all formats and mapped colors from RGB to XYZ if the chromaticity is
    // a special signal value.
    // TODO: better support generic RGB linear color spaces based on the provided chromaticities.
    if (header.getChromaticity() != null && header.getChromaticity().isCIEXYZ()) {
      for (Map<PixelFormat, Class<? extends Color>> f : formats.values()) {
        replaceRGBWithXYZ(f);
      }
    }

    return makeUnmodifiable(formats);
  }

  private static <T> void replaceRGBWithXYZ(Map<T, Class<? extends Color>> toModify) {
    List<T> keysToChange = new ArrayList<>();
    for (Map.Entry<T, Class<? extends Color>> e : toModify.entrySet()) {
      if (RGB.Linear.class.equals(e.getValue())) {
        keysToChange.add(e.getKey());
      }
    }

    for (T c : keysToChange) {
      toModify.put(c, XYZ.class);
    }
  }

  private static Map<String, Map<PixelFormat, Class<? extends Color>>> makeUnmodifiable(
      Map<String, Map<PixelFormat, Class<? extends Color>>> formats) {
    Map<String, Map<PixelFormat, Class<? extends Color>>> fixedGroups = new HashMap<>();
    for (Map.Entry<String, Map<PixelFormat, Class<? extends Color>>> byGroup : formats
        .entrySet()) {
      // The entry's map values are immutable so only the map itself has to be wrapped in
      // an unmodifiable wrapper.
      fixedGroups.put(byGroup.getKey(), Collections.unmodifiableMap(byGroup.getValue()));
    }

    // Lastly, make the outer map unmodifiable
    return Collections.unmodifiableMap(fixedGroups);
  }

  public int getChannelDataIndex(Channel channel) {
    return header.getChannels().indexOf(channel);
  }

  public int getChannelColorIndex(Channel channel) {
    return channelMapping.get(channel);
  }

  public PixelFormat getFormatForChannel(Channel channel) {
    return formatMapping.get(channel);
  }

  public Class<? extends Color> getColorForChannel(Channel channel) {
    return getFormatsForGroup(channel.getGroupName()).get(getFormatForChannel(channel));
  }

  public Map<PixelFormat, Class<? extends Color>> getFormatsForGroup(String name) {
    return formats.get(name);
  }

  public Map<PixelFormat, Class<? extends Color>> getAllFormats() {
    Map<PixelFormat, Class<? extends Color>> flat = new HashMap<>();
    for (Map<PixelFormat, Class<? extends Color>> forGroup : formats.values()) {
      flat.putAll(forGroup);
    }

    return flat;
  }

  public Set<String> getGroups() {
    return formats.keySet();
  }
}
