package typekin.processor;

import static typekin.processor.util.Conversions.getTypeMirror;
import static typekin.processor.util.Conversions.toSuperInterface;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import typekin.annotation.StructuralType;
import typekin.annotation.TypeOf;

@AutoService(Processor.class)
public class StructuralTypeProcessor extends AbstractProcessor {

  private Elements elementUtils;
  private Messager messager;
  private Filer filer;
  private Map<Element, Set<TypeCompare>> typeOfMap = new HashMap<>();
  private Map<Element, Set<TypeCompare>> structuralTypeMap = new HashMap<>();

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    elementUtils = processingEnv.getElementUtils();
    messager = processingEnv.getMessager();
    filer = processingEnv.getFiler();
  }

  @Override
  public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
    for (Element element : roundEnvironment.getElementsAnnotatedWith(StructuralType.class)) {
      structuralTypeMap.put(element, allMethods((TypeElement) element));
    }
    for (Element element : roundEnvironment.getElementsAnnotatedWith(TypeOf.class)) {
      typeOfMap.put(element, allMethods((TypeElement) element));
    }

    for (Element element : roundEnvironment.getElementsAnnotatedWith(StructuralType.class)) {
      TypeElement typeElement = (TypeElement) element;
      TypeSpec typeSpec = generateTypeSpec(typeElement, roundEnvironment);
      writeFile(typeElement, typeSpec);
    }

    return true;
  }

  private Set<TypeCompare> allMethods(TypeElement element) {
    Set<TypeCompare> set = new HashSet<>();
    for (Element member : elementUtils.getAllMembers(element)) {
      if (member.getKind() != ElementKind.METHOD) {
        continue;
      }
      MethodSymbol method = (Symbol.MethodSymbol) member;
      TypeCompare typeCompare = new TypeCompare();
      typeCompare.simpleName = method.getSimpleName();
      typeCompare.parameters = method.getParameters();
      set.add(typeCompare);
    }
    return set;
  }

  private TypeSpec generateTypeSpec(TypeElement typeElement, RoundEnvironment roundEnvironment) {
    String nameFormat = typeElement.getAnnotation(StructuralType.class).name();
    String name = String.format(nameFormat, typeElement.getSimpleName().toString());

    return TypeSpec.interfaceBuilder(name)
        .addModifiers(Modifier.PUBLIC)
        .addSuperinterfaces(toSuperInterface(
            getMatchingElements(typeElement, roundEnvironment)))
        .build();
  }

  public Set<? extends Element> getMatchingElements(TypeElement structuralTypeElement,
      RoundEnvironment roundEnvironment) {
    TypeMirror typeMirror = getTypeMirror(structuralTypeElement.getAnnotation(StructuralType.class));
    Set<TypeElement> set = new LinkedHashSet<>();
    for (Element element : roundEnvironment.getElementsAnnotatedWith(TypeOf.class)) {
      TypeElement typeOfElement = (TypeElement) element;
      if (typeMirror != getTypeMirror(typeOfElement.getAnnotation(TypeOf.class))) {
        continue;
      }

      if (!structurallyEquivalent(typeOfElement, structuralTypeElement)) {
        continue;
      }

      set.add(typeOfElement);
    }
    return set;
  }

  private boolean structurallyEquivalent(TypeElement typeOfElement,
      TypeElement structuralTypeElement) {
    Set<TypeCompare> typeOf = typeOfMap.get(typeOfElement);
    Set<TypeCompare> structural = structuralTypeMap.get(structuralTypeElement);

    return structural.containsAll(typeOf);
  }

  private void writeFile(TypeElement typeElement, TypeSpec... typeSpecs) {
    try {
      for (TypeSpec typeSpec : typeSpecs) {
        JavaFile.builder(((Symbol.ClassSymbol) typeElement).packge().fullname.toString(), typeSpec)
            .build()
            .writeTo(filer);
      }
    } catch (IOException e) {
      messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
    }
  }

  class TypeCompare {
    public Name simpleName;
    public List<VarSymbol> parameters;

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      TypeCompare that = (TypeCompare) o;
      return simpleName.equals(that.simpleName) && parameters.equals(that.parameters);
    }

    @Override
    public int hashCode() {
      return Objects.hash(simpleName, parameters);
    }
  }

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    Set<String> types = new HashSet<>();
    types.add(StructuralType.class.getCanonicalName());
    return types;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }
}