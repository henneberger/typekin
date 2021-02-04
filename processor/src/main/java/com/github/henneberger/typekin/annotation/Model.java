package com.github.henneberger.typekin.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Model {
  String name() default "St%sModel";
  String refName() default "St%sRef";
  String implName() default "St%sModelImpl";
}
