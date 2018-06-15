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
package com.lhkbob.imaje.util;

import com.lhkbob.imaje.color.space.rgb.Illuminant;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 */
@SupportedAnnotationTypes("com.lhkbob.imaje.color.annot.Illuminant")
public class IlluminantValidator extends AbstractProcessor {
  @Override
  public boolean process(
      Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    TypeElement illumAnnot = processingEnv.getElementUtils().getTypeElement("com.lhkbob.imaje.color.annot.Illuminant");
    if (!annotations.contains(illumAnnot)) {
      // No processing to be done
      return false;
    }

    processingEnv.getMessager()
        .printMessage(Diagnostic.Kind.NOTE, "Validating @Illuminant declarations");
    for (Element e : roundEnv.getElementsAnnotatedWith(illumAnnot)) {
      Illuminant illum = e.getAnnotation(Illuminant.class);
      if (illum.luminance() < 0.0) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Luminance cannot be negative: " + illum, e);
      }
      if (illum.temperature() < 0.0) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Color temperature cannot be negative: " + illum, e);
      }
      if (illum.type() != Illuminant.Type.TEMPERATURE && illum.temperature() != 5000.0) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Color temperature value will be ignored because type is not TEMPERATURE: " + illum, e);
      }
      if (illum.type() != Illuminant.Type.CHROMATICITY) {
        if (illum.chromaticity().x() != 0.3333 || illum.chromaticity().y() != 0.3333) {
          processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "Chromaticity values will be ignored because type is not CHROMATICITY: " + illum, e);
        }
      }
    }

    return true;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}
