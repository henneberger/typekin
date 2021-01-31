package typekin.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import typekin.annotation.Model;
import typekin.annotation.StructuralType;
import typekin.annotation.TypeOf;

@AutoService(Processor.class)
public class ModelProcessor extends AbstractProcessor {

  private Filer filer;
  private Messager messager;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    filer = processingEnv.getFiler();
    messager = processingEnv.getMessager();
  }

  @Override
  public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
    for (Element element : roundEnvironment.getElementsAnnotatedWith(Model.class)) {
      TypeElement typeElement = (TypeElement) element;

      createConcreteModel(typeElement, roundEnvironment);
      createModelInterface(typeElement, roundEnvironment);
      createRefInterface(typeElement, roundEnvironment);
    }
    return true;
  }

  private void createConcreteModel(TypeElement typeElement, RoundEnvironment roundEnvironment) {
    String name = "Concrete" + typeElement.getSimpleName().toString() + "Model";

    TypeSpec typeSpec = TypeSpec.classBuilder(name)
        .addModifiers(Modifier.PUBLIC)
        .addSuperinterface(buildConcreteModelInterface(typeElement))
        .addMethods(generateFields(typeElement))
        .build();

    writeFile(typeElement, typeSpec);
  }

  private Iterable<MethodSpec> generateFields(TypeElement typeElement) {
    List<MethodSpec> methodSpecs = new ArrayList<>();
    for (Element element : typeElement.getEnclosedElements()) {
      if (element.getModifiers().contains(Modifier.ABSTRACT) && element.getKind() == ElementKind.METHOD) {
        MethodSymbol symbol = (MethodSymbol)element;
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
      parameterSpecs.add(toParameter(symbol));
    }
    return parameterSpecs;
  }

  private ParameterSpec toParameter(VarSymbol symbol) {
    return ParameterSpec.builder(toTypeName(symbol), symbol.getQualifiedName().toString(), Modifier.FINAL)
        .build();
  }

  private TypeName toTypeName(VarSymbol symbol) {
    return TypeName.get(symbol.asType());
  }

  private TypeName toReturnType(MethodSymbol element) {
    return TypeName.get(element.getReturnType());
  }

  private TypeName buildConcreteModelInterface(TypeElement element) {
    return ClassName.get(((Symbol.ClassSymbol) element).packge().fullname.toString(),
        "St" + element.getSimpleName().toString() + "Model");
  }

  public void createModelInterface(TypeElement typeElement,
    RoundEnvironment roundEnvironment) {
    String name = "St" + typeElement.getSimpleName().toString() + "Model";

    TypeSpec typeSpec = TypeSpec.interfaceBuilder(name)
        .addModifiers(Modifier.PUBLIC)
        .addSuperinterfaces(buildSuperInterface(
            getMatchingTypeOfElements(typeElement, roundEnvironment)))
        .build();

    writeFile(typeElement, typeSpec);
  }

  private Set<? extends Element> getMatchingTypeOfElements(TypeElement typeElement,
      RoundEnvironment roundEnvironment) {
    TypeMirror modelType = getClassParam(typeElement.getAnnotation(Model.class));
    Set<TypeElement> set = new LinkedHashSet<>();
    for (Object element : roundEnvironment.getElementsAnnotatedWith(TypeOf.class)) {
      TypeElement typeOfElement = (TypeElement) element;
      if (modelType != getClassParam(typeOfElement.getAnnotation(TypeOf.class))) {
        continue;
      }
      set.add(typeOfElement);
    }
    return set;
  }

  private List<? extends TypeName> buildSuperInterface(Set<? extends Element> elements) {
    List<TypeName> supers = new ArrayList<>();
    for (Element element : elements) {
      supers.add(TypeName.get(element.asType()));
    }
    return supers;
  }

  public void createRefInterface(TypeElement typeElement,
      RoundEnvironment roundEnvironment) {
    String name = "St" + typeElement.getSimpleName().toString() + "Ref";

    TypeSpec typeSpec = TypeSpec.interfaceBuilder(name)
        .addModifiers(Modifier.PUBLIC)
        .addSuperinterfaces(buildRefSuperInterface(
            getMatchingStructuralElements(typeElement, roundEnvironment)))
        .build();

    writeFile(typeElement, typeSpec);
  }

  private Set<? extends Element> getMatchingStructuralElements(TypeElement typeElement,
      RoundEnvironment roundEnvironment) {
    TypeMirror modelType = getClassParam(typeElement.getAnnotation(Model.class));
    Set<TypeElement> set = new LinkedHashSet<>();
    for (Object element : roundEnvironment.getElementsAnnotatedWith(StructuralType.class)) {
      TypeElement typeOfElement = (TypeElement) element;
      if (modelType != getClassParam(typeOfElement.getAnnotation(StructuralType.class))) {
        continue;
      }
      set.add(typeOfElement);
    }
    return set;
  }

  private List<? extends TypeName> buildRefSuperInterface(Set<? extends Element> elements) {
    List<TypeName> supers = new ArrayList<>();
    for (Element element : elements) {
      TypeName typeName = ClassName.get(((Symbol.ClassSymbol) element).packge().fullname.toString(),
          "St" + element.getSimpleName().toString());
      supers.add(typeName);
    }
    return supers;
  }

  private void writeFile(TypeElement typeElement, TypeSpec typeSpec) {
    try {
      JavaFile.builder(((Symbol.ClassSymbol) typeElement).packge().fullname.toString(), typeSpec)
          .build()
          .writeTo(filer);
    } catch (IOException e) {
      messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
    }
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> types = new HashSet<>();
    types.add(Model.class.getCanonicalName());
    return types;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  public static TypeMirror getClassParam(StructuralType annotation) {
    try {
      annotation.clazz();
    } catch (MirroredTypeException mte) {
      return mte.getTypeMirror();
    }
    return null;
  }

  public static TypeMirror getClassParam(Model annotation) {
    try {
      annotation.clazz();
    } catch (MirroredTypeException mte) {
      return mte.getTypeMirror();
    }
    return null;
  }

  public static TypeMirror getClassParam(TypeOf annotation) {
    try {
      annotation.clazz();
    } catch (MirroredTypeException mte) {
      return mte.getTypeMirror();
    }
    return null;
  }
}