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
              "Color subclass does not specify a @Channels annotation: " + e.toString(), e);
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
