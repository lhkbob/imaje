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
@SupportedAnnotationTypes("com.lhkbob.imaje.color.annot.SpectrumRange")
public class SpectrumRangeValidator extends AbstractProcessor {
  @Override
  public boolean process(
      Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    TypeElement spectrumAnnot = processingEnv.getElementUtils().getTypeElement("com.lhkbob.imaje.color.annot.SpectrumRange");
    if (!annotations.contains(spectrumAnnot)) {
      // No processing to be done
      return false;
    }

    processingEnv.getMessager()
        .printMessage(Diagnostic.Kind.NOTE, "Validating @SpectrumRange declarations");
    TypeElement spectrumClass = processingEnv.getElementUtils().getTypeElement("com.lhkbob.imaje.color.Spectrum");
    for (Element e : roundEnv.getElementsAnnotatedWith(spectrumAnnot)) {
      // Only validate @SpectrumRange applied to Spectrum colors
      if (processingEnv.getTypeUtils().isAssignable(e.asType(), spectrumClass.asType())) {
        SpectrumRange s = e.getAnnotation(SpectrumRange.class);
        if (s.highWavelength() <= s.lowWavelength()) {
          processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "High wavelength must be greater than low wavelength: " + s, e);
        }
        if (s.highWavelength() < 0 || s.lowWavelength() < 0) {
          processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Wavelengths must be positive: " + s, e);
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
