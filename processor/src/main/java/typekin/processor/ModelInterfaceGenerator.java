package typekin.processor;

import static typekin.processor.Conversions.toSuperInterface;
import static typekin.processor.Conversions.getTypeMirror;

import com.squareup.javapoet.TypeSpec;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import typekin.annotation.Model;
import typekin.annotation.TypeOf;

public class ModelInterfaceGenerator {
  public static TypeSpec generate(TypeElement typeElement, RoundEnvironment roundEnvironment) {
    return new ModelInterfaceGenerator().generateTypeSpec(typeElement, roundEnvironment);
  }

  private TypeSpec generateTypeSpec(TypeElement typeElement, RoundEnvironment roundEnvironment) {
    String nameFormat = typeElement.getAnnotation(Model.class).name();
    String name = String.format(nameFormat, typeElement.getSimpleName().toString());

    TypeSpec typeSpec = TypeSpec.interfaceBuilder(name)
        .addModifiers(Modifier.PUBLIC)
        .addSuperinterfaces(toSuperInterface(
            getMatchingTypeOfElements(typeElement, roundEnvironment)))
        .build();

    return typeSpec;
  }

  private Set<? extends Element> getMatchingTypeOfElements(TypeElement typeElement,
      RoundEnvironment roundEnvironment) {
    TypeMirror modelType = typeElement.asType();
    Set<TypeElement> set = new LinkedHashSet<>();
    for (Object element : roundEnvironment.getElementsAnnotatedWith(TypeOf.class)) {
      TypeElement typeOfElement = (TypeElement) element;
      if (modelType != getTypeMirror(typeOfElement.getAnnotation(TypeOf.class))) {
        continue;
      }
      set.add(typeOfElement);
    }
    return set;
  }
}
