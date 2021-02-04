package com.github.henneberger.typekin.processor;

import com.github.henneberger.typekin.processor.generators.ModelImplGenerator;
import com.github.henneberger.typekin.processor.generators.ModelInterfaceGenerator;
import com.github.henneberger.typekin.processor.generators.ModelRefGenerator;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.sun.tools.javac.code.Symbol;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import com.github.henneberger.typekin.annotation.Model;

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

      TypeSpec modelImplTypeSpec = ModelImplGenerator.generate(typeElement, roundEnvironment);
      TypeSpec modelInterfaceTypeSpec = ModelInterfaceGenerator
          .generate(typeElement, roundEnvironment);
      TypeSpec modelRefTypeSpec = ModelRefGenerator.generate(typeElement, roundEnvironment);
      writeFile(typeElement, modelImplTypeSpec, modelInterfaceTypeSpec, modelRefTypeSpec);
    }
    return true;
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
}