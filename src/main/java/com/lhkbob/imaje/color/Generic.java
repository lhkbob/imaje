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

/**
 *
 */
public abstract class Generic extends Color {
  @Channels(value = {}, unnamedChannelCount = 1)
  public static class C1 extends Generic {
    public C1() {
      this(0.0);
    }

    public C1(double v) {
      setChannel(v);
    }

    @Override
    public C1 clone() {
      return (C1) super.clone();
    }

    public double getChannel() {
      return get(0);
    }

    public void setChannel(double v) {
      set(0, v);
    }

    public void c1(double v) {
      setChannel(v);
    }

    public double c1() {
      return getChannel();
    }
  }

  @Channels(value = { }, unnamedChannelCount = 2)
  public static class C2 extends Generic {
    public C2() {
      this(0.0, 0.0);
    }

    public C2(double v1, double v2) {
      setChannel1(v1);
      setChannel2(v2);
    }

    @Override
    public C2 clone() {
      return (C2) super.clone();
    }

    public double getChannel1() {
      return get(0);
    }

    public void setChannel1(double v) {
      set(0, v);
    }

    public double getChannel2() {
      return get(1);
    }

    public void setChannel2(double v) {
      set(1, v);
    }

    public void c1(double v) {
      setChannel1(v);
    }

    public double c1() {
      return getChannel1();
    }

    public void c2(double v) {
      setChannel2(v);
    }

    public double c2() {
      return getChannel2();
    }
  }

  @Channels(value = { }, unnamedChannelCount = 3)
  public static class C3 extends Generic {
    public C3() {
      this(0.0, 0.0, 0.0);
    }

    public C3(double v1, double v2, double v3) {
      setChannel1(v1);
      setChannel2(v2);
      setChannel3(v3);
    }

    @Override
    public C3 clone() {
      return (C3) super.clone();
    }

    public double getChannel1() {
      return get(0);
    }

    public void setChannel1(double v) {
      set(0, v);
    }

    public double getChannel2() {
      return get(1);
    }

    public void setChannel2(double v) {
      set(1, v);
    }

    public double getChannel3() {
      return get(2);
    }

    public void setChannel3(double v) {
      set(2, v);
    }

    public void c1(double v) {
      setChannel1(v);
    }

    public double c1() {
      return getChannel1();
    }

    public void c2(double v) {
      setChannel2(v);
    }

    public double c2() {
      return getChannel2();
    }

    public void c3(double v) {
      setChannel3(v);
    }

    public double c3() {
      return getChannel3();
    }
  }

  @Channels(value = { }, unnamedChannelCount = 4)
  public static class C4 extends Generic {
    public C4() {
      this(0.0, 0.0, 0.0, 0.0);
    }

    public C4(double v1, double v2, double v3, double v4) {
      setChannel1(v1);
      setChannel2(v2);
      setChannel3(v3);
      setChannel4(v4);
    }

    @Override
    public C4 clone() {
      return (C4) super.clone();
    }

    public double getChannel1() {
      return get(0);
    }

    public void setChannel1(double v) {
      set(0, v);
    }

    public double getChannel2() {
      return get(1);
    }

    public void setChannel2(double v) {
      set(1, v);
    }

    public double getChannel3() {
      return get(2);
    }

    public void setChannel3(double v) {
      set(2, v);
    }

    public double getChannel4() {
      return get(3);
    }

    public void setChannel4(double v) {
      set(3, v);
    }

    public void c1(double v) {
      setChannel1(v);
    }

    public double c1() {
      return getChannel1();
    }

    public void c2(double v) {
      setChannel2(v);
    }

    public double c2() {
      return getChannel2();
    }

    public void c3(double v) {
      setChannel3(v);
    }

    public double c3() {
      return getChannel3();
    }

    public void c4(double v) {
      setChannel4(v);
    }

    public double c4() {
      return getChannel4();
    }
  }
}
