package typekin.processor;

import static typekin.processor.Conversions.toParameter;
import static typekin.processor.Conversions.toReturnType;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import typekin.annotation.Model;

public class ModelImplGenerator {

  public static TypeSpec generate(TypeElement typeElement, RoundEnvironment roundEnvironment) {
    return new ModelImplGenerator().generateTypeSpec(typeElement, roundEnvironment);
  }

  private TypeSpec generateTypeSpec(TypeElement typeElement, RoundEnvironment roundEnvironment) {
    String nameFormat = typeElement.getAnnotation(Model.class).implName();
    String name = String.format(nameFormat, typeElement.getSimpleName().toString());

    TypeSpec typeSpec = TypeSpec.classBuilder(name)
        .addModifiers(Modifier.PUBLIC)
        .addSuperinterface(buildConcreteModelInterface(typeElement))
        .addMethods(generateFields(typeElement))
        .build();

    return typeSpec;
  }

  private Iterable<MethodSpec> generateFields(TypeElement typeElement) {
    List<MethodSpec> methodSpecs = new ArrayList<>();
    for (Element element : typeElement.getEnclosedElements()) {
      if (element.getModifiers().contains(Modifier.ABSTRACT)
          && element.getKind() == ElementKind.METHOD) {
        MethodSymbol symbol = (MethodSymbol) element;
        if (symbol.getReturnType().getKind() == TypeKind.VOID) {
          methodSpecs.add(generateVoidConcreteField(symbol));
        } else {
          methodSpecs.add(generateConcreteField(symbol));
        }
      }
    }

    return methodSpecs;
  }

  private MethodSpec generateConcreteField(MethodSymbol element) {
    return MethodSpec.methodBuilder(element.getQualifiedName().toString())
        .returns(toReturnType(element))
        .addModifiers(Modifier.PUBLIC)
        .addParameters(toParameters(element))
        .addCode("return null;")
        .build();
  }

  private MethodSpec generateVoidConcreteField(MethodSymbol element) {
    return MethodSpec.methodBuilder(element.getQualifiedName().toString())
        .returns(toReturnType(element))
        .addModifiers(Modifier.PUBLIC)
        .addParameters(toParameters(element))
        .build();
  }

  private Iterable<ParameterSpec> toParameters(MethodSymbol element) {
    List<ParameterSpec> parameterSpecs = new ArrayList<>();
    for (VarSymbol symbol : element.getParameters()) {
      parameterSpecs.add(toParameter(symbol, Modifier.FINAL));
    }
    return parameterSpecs;
  }

  private TypeName buildConcreteModelInterface(TypeElement typeElement) {
    String nameFormat = typeElement.getAnnotation(Model.class).name();
    String name = String.format(nameFormat, typeElement.getSimpleName().toString());

    return ClassName.get(((Symbol.ClassSymbol) typeElement).packge().fullname.toString(),
        name);
  }
}
