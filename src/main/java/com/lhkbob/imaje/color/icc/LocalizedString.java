package com.lhkbob.imaje.color.icc;

import com.lhkbob.imaje.util.Arguments;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
    return s.defaultKey.equals(defaultKey) && s.localizedText.equals(localizedText);
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
      if (k.getLanguage().equals(key.getLanguage())) {
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
