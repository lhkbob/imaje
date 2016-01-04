package com.lhkbob.imaje.color.icc.reader;

import com.lhkbob.imaje.color.icc.Signature;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextPositionNumber;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextSignature;
import static com.lhkbob.imaje.color.icc.reader.ICCDataTypeUtil.nextUInt32Number;

/**
 *
 */
public class TagTable {
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
  public <T> Tag<? extends T> getTag(Tag.Definition<T> tagDef) {
    Tag<?> tag = tags.get(tagDef.getSignature());
    if (tag != null) {
      return (Tag<? extends T>) tag;
    } else {
      // Tag was not present, or for some reason wasn't of the expected type
      return null;
    }
  }

  public <T> T getTagValue(Tag.Definition<T> tag) {
    return getTagValue(tag, null);
  }

  public <T> T getTagValue(Tag.Definition<T> tag, T dflt) {
    Tag<? extends T> value = getTag(tag);
    return (value != null ? value.getData() : dflt);
  }

  public Map<Signature, Tag<?>> getTags() {
    return tags;
  }

  @Override
  public int hashCode() {
    return tags.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof TagTable)) {
      return false;
    }

    return ((TagTable) o).tags.equals(tags);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Tags:\n");
    for (Tag<?> t : tags.values()) {
      sb.append(t.toString()).append('\n');
    }
    return sb.toString();
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
}
