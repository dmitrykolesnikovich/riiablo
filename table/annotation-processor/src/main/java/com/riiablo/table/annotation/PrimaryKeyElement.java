package com.riiablo.table.annotation;

import java.util.Collection;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.apache.commons.lang3.builder.ToStringBuilder;

final class PrimaryKeyElement {
  static PrimaryKeyElement find(
      Context context,
      TypeElement element,
      Collection<VariableElement> elements
  ) {
    VariableElement firstAcceptableElement = null, primaryKeyElement = null;
    for (VariableElement e : elements) {
      if (firstAcceptableElement == null && Constants.isPrimaryKey(e)) {
        firstAcceptableElement = e;
      }

      PrimaryKey annotation = e.getAnnotation(PrimaryKey.class);
      if (annotation != null) {
        if (primaryKeyElement == null) {
          primaryKeyElement = e;
        } else {
          context.error(
              e, context.getAnnotationMirror(e, Constants.PRIMARY_KEY),
              "{} already declared as {}", primaryKeyElement, PrimaryKey.class);
        }
      }
    }

    if (primaryKeyElement == null) {
      if (firstAcceptableElement == null) {
        context.error(element, "{element} did not declare any {}", PrimaryKey.class);
        return null;
      }

      context.warn(element, "{element} did not declare any {}, using {}",
          PrimaryKey.class, firstAcceptableElement);
      primaryKeyElement = firstAcceptableElement;
    }

    AnnotationMirror primaryKeyMirror = context.getAnnotationMirror(primaryKeyElement, Constants.PRIMARY_KEY);
    return new PrimaryKeyElement(primaryKeyElement, primaryKeyMirror);
  }

  final VariableElement element;
  final AnnotationMirror mirror;

  PrimaryKeyElement(VariableElement element, AnnotationMirror mirror) {
    this.element = element;
    this.mirror = mirror;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("element", element)
        .append("mirror", mirror)
        .toString();
  }
}
