package com.hubspot.jinjava.el.ext;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.lang.reflect.Method;
import org.junit.Test;

public class ValidatorConfigBannedConstructsTest {

  // MethodValidatorConfig: allowedMethods() path

  @Test
  public void itRejectsObjectMethodInAllowedMethods() throws NoSuchMethodException {
    Method toStringMethod = Object.class.getMethod("toString");
    assertThatThrownBy(
      () -> MethodValidatorConfig.builder().addAllowedMethods(toStringMethod).build()
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Banned classes or prefixes");
  }

  @Test
  public void itRejectsClassMethodInAllowedMethods() throws NoSuchMethodException {
    Method getNameMethod = Class.class.getMethod("getName");
    assertThatThrownBy(
      () -> MethodValidatorConfig.builder().addAllowedMethods(getNameMethod).build()
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Banned classes or prefixes");
  }

  // MethodValidatorConfig: allowedDeclaredMethodsFromCanonicalClassNames() path

  @Test
  public void itRejectsObjectClassInAllowedDeclaredMethodClassNames() {
    assertThatThrownBy(
      () ->
        MethodValidatorConfig
          .builder()
          .addAllowedDeclaredMethodsFromCanonicalClassNames(
            Object.class.getCanonicalName()
          )
          .build()
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Banned classes or prefixes");
  }

  @Test
  public void itRejectsClassClassInAllowedDeclaredMethodClassNames() {
    assertThatThrownBy(
      () ->
        MethodValidatorConfig
          .builder()
          .addAllowedDeclaredMethodsFromCanonicalClassNames(
            Class.class.getCanonicalName()
          )
          .build()
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Banned classes or prefixes");
  }

  @Test
  public void itRejectsObjectMapperInAllowedDeclaredMethodClassNames() {
    assertThatThrownBy(
      () ->
        MethodValidatorConfig
          .builder()
          .addAllowedDeclaredMethodsFromCanonicalClassNames(
            ObjectMapper.class.getCanonicalName()
          )
          .build()
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Banned classes or prefixes");
  }

  @Test
  public void itRejectsJinjavaInterpreterInAllowedDeclaredMethodClassNames() {
    assertThatThrownBy(
      () ->
        MethodValidatorConfig
          .builder()
          .addAllowedDeclaredMethodsFromCanonicalClassNames(
            JinjavaInterpreter.class.getCanonicalName()
          )
          .build()
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Banned classes or prefixes");
  }

  // MethodValidatorConfig: allowedDeclaredMethodsFromCanonicalClassPrefixes() path

  @Test
  public void itRejectsReflectPackageInAllowedDeclaredMethodPrefixes() {
    assertThatThrownBy(
      () ->
        MethodValidatorConfig
          .builder()
          .addAllowedDeclaredMethodsFromCanonicalClassPrefixes(
            Method.class.getPackageName()
          )
          .build()
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Banned classes or prefixes");
  }

  @Test
  public void itRejectsJacksonDatabindPackageInAllowedDeclaredMethodPrefixes() {
    assertThatThrownBy(
      () ->
        MethodValidatorConfig
          .builder()
          .addAllowedDeclaredMethodsFromCanonicalClassPrefixes(
            ObjectMapper.class.getPackageName()
          )
          .build()
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Banned classes or prefixes");
  }

  // ReturnTypeValidatorConfig: allowedCanonicalClassNames() path

  @Test
  public void itRejectsObjectClassInAllowedReturnTypeClassNames() {
    assertThatThrownBy(
      () ->
        ReturnTypeValidatorConfig
          .builder()
          .addAllowedCanonicalClassNames(Object.class.getCanonicalName())
          .build()
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Banned classes or prefixes");
  }

  @Test
  public void itRejectsClassClassInAllowedReturnTypeClassNames() {
    assertThatThrownBy(
      () ->
        ReturnTypeValidatorConfig
          .builder()
          .addAllowedCanonicalClassNames(Class.class.getCanonicalName())
          .build()
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Banned classes or prefixes");
  }

  @Test
  public void itRejectsObjectMapperInAllowedReturnTypeClassNames() {
    assertThatThrownBy(
      () ->
        ReturnTypeValidatorConfig
          .builder()
          .addAllowedCanonicalClassNames(ObjectMapper.class.getCanonicalName())
          .build()
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Banned classes or prefixes");
  }

  @Test
  public void itRejectsJinjavaInterpreterInAllowedReturnTypeClassNames() {
    assertThatThrownBy(
      () ->
        ReturnTypeValidatorConfig
          .builder()
          .addAllowedCanonicalClassNames(JinjavaInterpreter.class.getCanonicalName())
          .build()
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Banned classes or prefixes");
  }

  // ReturnTypeValidatorConfig: allowedCanonicalClassPrefixes() path

  @Test
  public void itRejectsReflectPackageInAllowedReturnTypePrefixes() {
    assertThatThrownBy(
      () ->
        ReturnTypeValidatorConfig
          .builder()
          .addAllowedCanonicalClassPrefixes(Method.class.getPackageName())
          .build()
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Banned classes or prefixes");
  }

  @Test
  public void itRejectsJacksonDatabindPackageInAllowedReturnTypePrefixes() {
    assertThatThrownBy(
      () ->
        ReturnTypeValidatorConfig
          .builder()
          .addAllowedCanonicalClassPrefixes(ObjectMapper.class.getPackageName())
          .build()
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Banned classes or prefixes");
  }
}
