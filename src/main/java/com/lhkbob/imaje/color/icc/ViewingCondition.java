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
package com.lhkbob.imaje.color.icc;

import com.lhkbob.imaje.util.Arguments;

import java.util.Objects;

/**
 *
 */
public final class ViewingCondition {
  private final LocalizedString description;
  private final GenericColorValue illuminant;
  private final StandardIlluminant illuminantType;
  private final GenericColorValue surround;

  public ViewingCondition(
      GenericColorValue illuminant, StandardIlluminant illuminantType, GenericColorValue surround,
      LocalizedString description) {
    Arguments.notNull("description", description);
    Arguments.equals("illuminant.getType()", GenericColorValue.ColorType.CIEXYZ, illuminant.getType());
    Arguments.equals("surround.getType()", GenericColorValue.ColorType.CIEXYZ, surround.getType());

    this.description = description;
    this.illuminant = illuminant;
    this.illuminantType = illuminantType;
    this.surround = surround;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof ViewingCondition)) {
      return false;
    }
    ViewingCondition c = (ViewingCondition) o;
    return Objects.equals(c.description, description) && Objects.equals(c.illuminantType, illuminantType)
        && Objects.equals(c.illuminant, illuminant) && Objects.equals(c.surround, surround);
  }

  public GenericColorValue getIlluminant() {
    return illuminant;
  }

  public StandardIlluminant getIlluminantType() {
    return illuminantType;
  }

  public GenericColorValue getSurround() {
    return surround;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + illuminantType.hashCode();
    result = 31 * result + illuminant.hashCode();
    result = 31 * result + surround.hashCode();
    result = 31 * result + description.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return String
        .format("ViewingCondition (desc: %s, illuminant: %s (%s), surround: %s)", description,
            illuminant, illuminantType, surround);
  }
}
