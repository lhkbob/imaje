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
package com.lhkbob.imaje.color.annot;

import java.util.Collection;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 *
 */
@SupportedAnnotationTypes("com.lhkbob.imaje.color.annot.Channels")
public class ChannelsValidator extends AbstractProcessor {
  @Override
  public boolean process(
      Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    TypeElement channelsAnnot = processingEnv.getElementUtils().getTypeElement("com.lhkbob.imaje.color.annot.Channels");
    if (!annotations.contains(channelsAnnot)) {
      // No processing to be done
      return false;
    }

    // Avoid duplicate process() calls with different annotation sets
    if (!annotations.isEmpty()) {
      TypeElement colorClass = processingEnv.getElementUtils().getTypeElement("com.lhkbob.imaje.color.Color");

      // Find all definitions of Color and make sure they have a Channels annotation present
      processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
          "Checking all Color subclasses for @Channels annotation");
      checkChannelsDefinition(roundEnv.getRootElements(), colorClass.asType());
      return true;
    } else {
      return false;
    }
  }

  private void checkChannelsDefinition(
      Collection<? extends Element> elements, TypeMirror colorClass) {
    // Iterate over every element, and check if it is a subclass of Color.
    // We cannot just iterate over everything with the Channels annotation since we explicitly are hunting
    // for Colors that do not declare a Channels annotation.
    Types types = processingEnv.getTypeUtils();
    for (Element e : elements) {
      if (e.getKind() == ElementKind.CLASS && types.isAssignable(e.asType(), colorClass)) {
        // If the color type is declared abstract then it is not required to have @Channels since it
        // forces subclasses of itself to then specify the annotation
        Channels annot = e.getAnnotation(Channels.class);
        if (!e.getModifiers().contains(Modifier.ABSTRACT)
            && annot == null) {
          processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
              "Color subclass does not specify a @Channels annotation: " + e, e);
        } else if (annot != null) {
          // Validate definition of @Channels annotation
          if (annot.unnamedChannelCount() > 0) {
            // If this is a channel definition without names, then names should not be defined
            if (annot.value().length != 0 || annot.shortNames().length != 0) {
              processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                  "Using @Channels with positive unnamed count must have empty names values: " + annot, e);
            }
          } else {
            // It's a named channels definition, so make sure that names are provided
            if (annot.value().length == 0) {
              processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                  "Must provide at least one channel name, unless unnamed count is set: " + annot, e);
            }
            // If short names are provided, they must have the same number as long names
            if (annot.shortNames().length != 0 && annot.shortNames().length != annot.value().length) {
              processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                  "Must provide same number as long names if short names are specified: " + annot, e);
            }
          }
        } // else abstract class with no @Channels definition
      }

      // Recurse to any nested classes
      checkChannelsDefinition(ElementFilter.typesIn(e.getEnclosedElements()), colorClass);
    }
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}
