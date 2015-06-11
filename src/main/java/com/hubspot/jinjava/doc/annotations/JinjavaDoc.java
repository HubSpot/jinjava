package com.hubspot.jinjava.doc.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface JinjavaDoc {

  String value() default "";

  JinjavaParam[] params() default {};

  JinjavaSnippet[] snippets() default {};

  JinjavaMetaValue[] meta() default {};

  String aliasOf() default "";

  boolean deprecated() default false;

  boolean hidden() default false;

}
