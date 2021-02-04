package com.github.henneberger.typekin.processor.util;

import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import com.github.henneberger.typekin.annotation.StructuralType;
import com.github.henneberger.typekin.annotation.TypeOf;

public class Conversions {

  public static ParameterSpec toParameter(VarSymbol symbol, Modifier... modifiers) {
    return ParameterSpec.builder(toTypeName(symbol), symbol.getQualifiedName().toString(), modifiers)
        .build();
  }

  public static TypeName toTypeName(VarSymbol symbol) {
    return TypeName.get(symbol.asType());
  }

  public static TypeName toReturnType(MethodSymbol element) {
    return TypeName.get(element.getReturnType());
  }

  public static List<? extends TypeName> toSuperInterface(Set<? extends Element> elements) {
    List<TypeName> supers = new ArrayList<>();
    for (Element element : elements) {
      supers.add(TypeName.get(element.asType()));
    }
    return supers;
  }

  public static TypeMirror getTypeMirror(StructuralType annotation) {
    try {
      annotation.model();
    } catch (MirroredTypeException mte) {
      return mte.getTypeMirror();
    }
    return null;
  }

  public static TypeMirror getTypeMirror(TypeOf annotation) {
    try {
      annotation.model();
    } catch (MirroredTypeException mte) {
      return mte.getTypeMirror();
    }
    return null;
  }
}
