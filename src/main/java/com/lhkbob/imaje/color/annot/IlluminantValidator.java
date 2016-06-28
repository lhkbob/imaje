package com.lhkbob.imaje.color.annot;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 *
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
