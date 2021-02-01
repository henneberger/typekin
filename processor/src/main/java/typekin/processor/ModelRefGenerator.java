package typekin.processor;

import static typekin.processor.Conversions.getTypeMirror;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.code.Symbol;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import typekin.annotation.Model;
import typekin.annotation.StructuralType;

public class ModelRefGenerator {

  public static TypeSpec generate(TypeElement typeElement, RoundEnvironment roundEnvironment) {
    return new ModelRefGenerator().generateTypeSpec(typeElement, roundEnvironment);
  }

  public TypeSpec generateTypeSpec(TypeElement typeElement, RoundEnvironment roundEnvironment) {
    String nameFormat = typeElement.getAnnotation(Model.class).refName();
    String name = String.format(nameFormat, typeElement.getSimpleName().toString());

    return TypeSpec.interfaceBuilder(name)
        .addModifiers(Modifier.PUBLIC)
        .addSuperinterfaces(buildRefSuperInterface(
            getMatchingStructuralElements(typeElement, roundEnvironment)))
        .build();
  }

  private Set<? extends Element> getMatchingStructuralElements(TypeElement typeElement,
      RoundEnvironment roundEnvironment) {
    TypeMirror modelType = typeElement.asType();
    Set<TypeElement> set = new LinkedHashSet<>();
    for (Object element : roundEnvironment.getElementsAnnotatedWith(StructuralType.class)) {
      TypeElement typeOfElement = (TypeElement) element;
      if (modelType != getTypeMirror(typeOfElement.getAnnotation(StructuralType.class))) {
        continue;
      }
      set.add(typeOfElement);
    }
    return set;
  }

  private List<? extends TypeName> buildRefSuperInterface(Set<? extends Element> elements) {
    List<TypeName> supers = new ArrayList<>();
    for (Element element : elements) {
      String nameFormat = element.getAnnotation(StructuralType.class).name();
      String name = String.format(nameFormat, element.getSimpleName().toString());

      TypeName typeName = ClassName.get(((Symbol.ClassSymbol) element).packge().fullname.toString(),
          name);
      supers.add(typeName);
    }
    return supers;
  }
}
