package com.hubspot.jinjava.el.ext;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.testobjects.OperationEnum;
import com.hubspot.jinjava.testobjects.SimpleColorEnum;
import java.lang.reflect.Method;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class AllowlistEnumTest extends BaseJinjavaTest {

  private static final AllowlistReturnTypeValidator ENUM_RETURN_TYPE_VALIDATOR =
    AllowlistReturnTypeValidator.create(
      ReturnTypeValidatorConfig
        .builder()
        .addAllowedCanonicalClassNames(
          SimpleColorEnum.class.getCanonicalName(),
          OperationEnum.class.getCanonicalName()
        )
        .build()
    );

  private static final AllowlistMethodValidator ENUM_METHOD_VALIDATOR =
    AllowlistMethodValidator.create(
      MethodValidatorConfig
        .builder()
        .addAllowedDeclaredMethodsFromCanonicalClassNames(
          SimpleColorEnum.class.getCanonicalName(),
          OperationEnum.class.getCanonicalName()
        )
        .build()
    );

  @Test
  public void itAllowsReturningSimpleEnumValue() {
    assertThat(ENUM_RETURN_TYPE_VALIDATOR.validateReturnType(SimpleColorEnum.RED))
      .isEqualTo(SimpleColorEnum.RED);
  }

  @Test
  public void itAllowsReturningConstantSpecificBodyEnumValue() {
    assertThat(ENUM_RETURN_TYPE_VALIDATOR.validateReturnType(OperationEnum.PLUS))
      .isEqualTo(OperationEnum.PLUS);
  }

  @Test
  public void itRejectsReturningNonAllowlistedEnumValue() {
    assertThat(ENUM_RETURN_TYPE_VALIDATOR.validateReturnType(Month.JANUARY)).isNull();
  }

  @Test
  public void itAllowsInvokingSimpleEnumGetter() throws NoSuchMethodException {
    Method getLabel = SimpleColorEnum.class.getMethod("getLabel");
    assertThat(ENUM_METHOD_VALIDATOR.validateMethod(getLabel)).isEqualTo(getLabel);
  }

  @Test
  public void itAllowsInvokingConstantSpecificBodyEnumMethod()
    throws NoSuchMethodException {
    Method apply = OperationEnum.PLUS.getClass().getMethod("apply", int.class, int.class);
    assertThat(apply.getDeclaringClass().getCanonicalName()).isNull();
    assertThat(ENUM_METHOD_VALIDATOR.validateMethod(apply)).isEqualTo(apply);
  }

  @Test
  public void itRejectsInvokingNonAllowlistedEnumMethod() throws NoSuchMethodException {
    Method getValue = Month.class.getMethod("getValue");
    assertThat(ENUM_METHOD_VALIDATOR.validateMethod(getValue)).isNull();
  }

  @Test
  public void itRendersSimpleEnumGetter() {
    Map<String, Object> context = new HashMap<>();
    context.put("color", SimpleColorEnum.RED);
    assertThat(jinjava.render("{{ color.label }}", context)).isEqualTo("red-label");
  }

  @Test
  public void itRendersConstantSpecificBodyEnumMethodInvocation() {
    Map<String, Object> context = new HashMap<>();
    context.put("op", OperationEnum.PLUS);
    assertThat(jinjava.render("{{ op.apply(2, 3) }}", context)).isEqualTo("5");
  }

  @Test
  public void itRendersEnumValueAsName() {
    Map<String, Object> context = new HashMap<>();
    context.put("op", OperationEnum.TIMES);
    assertThat(jinjava.render("{{ op }}", context)).isEqualTo("TIMES");
  }
}
