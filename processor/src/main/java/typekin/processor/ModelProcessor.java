package typekin.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.code.Symbol;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
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

      createModelInterface(typeElement, roundEnvironment);
      createRefInterface(typeElement, roundEnvironment);
    }
    return true;
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

  private static TypeMirror getClassParam(StructuralType annotation) {
    try {
      annotation.clazz();
    } catch (MirroredTypeException mte) {
      return mte.getTypeMirror();
    }
    return null;
  }

  private static TypeMirror getClassParam(Model annotation) {
    try {
      annotation.clazz();
    } catch (MirroredTypeException mte) {
      return mte.getTypeMirror();
    }
    return null;
  }

  private static TypeMirror getClassParam(TypeOf annotation) {
    try {
      annotation.clazz();
    } catch (MirroredTypeException mte) {
      return mte.getTypeMirror();
    }
    return null;
  }
}