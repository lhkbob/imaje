package com.lhkbob.imaje.color.icc;

import java.util.Arrays;

/**
 *
 */
public final class ProfileID {
  private final byte[] id;

  public ProfileID(byte[] id) {
    if (id.length != 16) {
      throw new IllegalArgumentException("Profile ID is made of 16 bytes, not " + id.length);
    }
    this.id = Arrays.copyOf(id, id.length);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    return o instanceof ProfileID && Arrays.equals(((ProfileID) o).id, id);
  }

  public byte getByte(int idx) {
    return id[idx];
  }

  public byte[] getBytes() {
    return Arrays.copyOf(id, id.length);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(id);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(id.length * 2);
    for (int i = 0; i < id.length; i++) {
      sb.append(Integer.toHexString(id[i]));
    }
    return sb.toString();
  }
}
