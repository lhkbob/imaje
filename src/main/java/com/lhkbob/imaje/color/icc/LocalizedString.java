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

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public final class LocalizedString {
  private final Locale defaultKey;
  private final Map<Locale, String> localizedText;

  public LocalizedString() {
    this(Collections.singletonMap(Locale.US, ""), Locale.US);
  }

  public LocalizedString(Map<Locale, String> variants, Locale dflt) {
    Arguments.notNull("variants", variants);
    Arguments.notNull("dflt", dflt);

    if (!variants.containsKey(dflt)) {
      throw new IllegalArgumentException("Default locale must be present in the variants map");
    }
    localizedText = Collections.unmodifiableMap(new HashMap<>(variants));
    defaultKey = dflt;
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof LocalizedString)) {
      return false;
    }
    LocalizedString s = (LocalizedString) o;
    return Objects.equals(s.defaultKey, defaultKey) && Objects.equals(s.localizedText, localizedText);
  }

  public Map<Locale, String> getAllLocalizations() {
    return localizedText;
  }

  public Locale getDefaultLocale() {
    return defaultKey;
  }

  public String getLocalizedString(Locale key) {
    String localized = localizedText.get(key);
    if (localized != null) {
      return localized;
    }

    // Otherwise search for any record with the same language <code></code>
    for (Locale k : localizedText.keySet()) {
      if (Objects.equals(k.getLanguage(), key.getLanguage())) {
        return localizedText.get(k);
      }
    }

    // Otherwise return the default locale's text
    return localizedText.get(defaultKey);
  }

  @Override
  public int hashCode() {
    return defaultKey.hashCode() ^ localizedText.hashCode();
  }

  @Override
  public String toString() {
    return getLocalizedString(Locale.getDefault());
  }
}
