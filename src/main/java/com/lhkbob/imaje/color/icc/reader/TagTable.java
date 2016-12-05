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
package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextPositionNumber;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextSignature;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt32Number;

/**
 *
 */
public final class TagTable {
  private final Map<Signature, Tag<?>> tags;

  public TagTable(List<Tag<?>> tags) {
    LinkedHashMap<Signature, Tag<?>> mapped = new LinkedHashMap<>();
    for (Tag<?> t : tags) {
      if (mapped.containsKey(t.getSignature())) {
        throw new IllegalArgumentException("Duplicate tag signature: " + t.getSignature());
      }
      mapped.put(t.getSignature(), t);
    }
    this.tags = Collections.unmodifiableMap(mapped);
  }

  @SuppressWarnings("unchecked")
  public static TagTable fromBytes(Header header, ByteBuffer data) {
    int profileStart = data.position() - 128;

    int tagCount = Math.toIntExact(nextUInt32Number(data));
    Signature[] tagSigs = new Signature[tagCount];
    ICCDataTypeUtil.PositionNumber[] tagData = new ICCDataTypeUtil.PositionNumber[tagCount];

    for (int i = 0; i < tagCount; i++) {
      tagSigs[i] = nextSignature(data);
      // The ICC spec actually describes the tag table as two separate uint32's as offset and
      // then size, but this is exactly what a PositionNumber is.
      tagData[i] = nextPositionNumber(data);
    }

    List<Tag<?>> tags = new ArrayList<>();
    for (int i = 0; i < tagCount; i++) {
      Tag.Definition<?> def = Tag.fromSignature(tagSigs[i]);
      if (def == null) {
        System.out.println("Unknown tag: " + tagSigs[i]);
        continue;
      }

      tagData[i].configureBuffer(data, profileStart);
      Tag<?> tag = def.parseTag(header, data);
      if (tag == null) {
        continue;
      }

      tags.add(tag);
    }

    return new TagTable(tags);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof TagTable)) {
      return false;
    }

    return Objects.equals(((TagTable) o).tags, tags);
  }

  @SuppressWarnings("unchecked")
  public <T> Tag<? extends T> getTag(Tag.Definition<T> tagDef) {
    Tag<?> tag = tags.get(tagDef.getSignature());
    if (tag != null) {
      return (Tag<? extends T>) tag;
    } else {
      // Tag was not present, or for some reason wasn't of the expected type
      return null;
    }
  }

  public <T> T getTagValue(Tag.Definition<T> tag, T dflt) {
    Tag<? extends T> value = getTag(tag);
    return (value != null ? value.getData() : dflt);
  }

  public <T> T getTagValue(Tag.Definition<T> tag) {
    return getTagValue(tag, null);
  }

  public Map<Signature, Tag<?>> getTags() {
    return tags;
  }

  @Override
  public int hashCode() {
    return tags.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Tags:\n");
    for (Tag<?> t : tags.values()) {
      sb.append(t).append('\n');
    }
    return sb.toString();
  }
}
