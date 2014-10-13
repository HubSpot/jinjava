package com.hubspot.jinjava.lib.fn;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines that this public static method should be exposed to the expression resolver, with the given name and optional namespace.
 * 
 * Note that you can specify a JinjavaInterpreter or Context parameter in your method as the first argument, and it will be passed in.
 * 
 * @author jstehler
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ELFunction {
  /**
   * The namespace to define the function in (e.g. 'fn' would expose 'foo' as: 'fn:foo()')
   */
  String namespace() default "";
  /**
   * The local name to expose this function as
   */
  String value();
}
