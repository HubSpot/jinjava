package com.hubspot.jinjava.el.ext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.el.JinjavaELContext;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.ArrayList;
import java.util.List;
import javax.el.ELContext;
import javax.el.MethodNotFoundException;
import javax.el.PropertyNotFoundException;
import org.junit.Before;
import org.junit.Test;

public class JinjavaBeanELResolverTest {

  private JinjavaBeanELResolver jinjavaBeanELResolver;
  private ELContext elContext;

  JinjavaInterpreter interpreter = mock(JinjavaInterpreter.class);
  JinjavaConfig config = mock(JinjavaConfig.class);

  @Before
  public void setUp() throws Exception {
    jinjavaBeanELResolver = new JinjavaBeanELResolver();
    elContext = new JinjavaELContext();
    when(interpreter.getConfig()).thenReturn(config);
  }

  @Test
  public void itInvokesProperStringReplace() {
    assertThat(
      jinjavaBeanELResolver.invoke(
        elContext,
        "abcd",
        "replace",
        null,
        new Object[] { "abcd", "efgh" }
      )
    )
      .isEqualTo("efgh");
    assertThat(
      jinjavaBeanELResolver.invoke(
        elContext,
        "abcd",
        "replace",
        null,
        new Object[] { 'a', 'e' }
      )
    )
      .isEqualTo("ebcd");
  }

  @Test
  public void itInvokesBestMethodWithSingleParam() {
    class Temp {

      public String getResult(int a) {
        return "int";
      }

      public String getResult(String a) {
        return "String";
      }

      public String getResult(Object a) {
        return "Object";
      }

      public String getResult(CharSequence a) {
        return "CharSequence";
      }
    }
    Temp var = new Temp();
    assertThat(
      jinjavaBeanELResolver.invoke(elContext, var, "getResult", null, new Object[] { 1 })
    )
      .isEqualTo("int");
    assertThat(
      jinjavaBeanELResolver.invoke(
        elContext,
        var,
        "getResult",
        null,
        new Object[] { "1" }
      )
    )
      .isEqualTo("String");
    assertThat(
      jinjavaBeanELResolver.invoke(
        elContext,
        var,
        "getResult",
        null,
        new Object[] { new Object() }
      )
    )
      .isEqualTo("Object");
  }

  @Test
  public void itPrefersPrimitives() {
    class Temp {

      public String getResult(int a, Integer b) {
        return "int Integer";
      }

      public String getResult(int a, Object b) {
        return "int Object";
      }

      public String getResult(Number a, int b) {
        return "Number int";
      }
    }
    Temp var = new Temp();
    assertThat(
      jinjavaBeanELResolver.invoke(
        elContext,
        var,
        "getResult",
        null,
        new Object[] { 1, 2 }
      )
    )
      .isEqualTo("int Integer");
    assertThat(
      jinjavaBeanELResolver.invoke(
        elContext,
        var,
        "getResult",
        null,
        new Object[] { 1, Integer.valueOf(2) }
      )
    )
      .isEqualTo("int Integer"); // should be "int object", but we can't figure that out
    assertThat(
      jinjavaBeanELResolver.invoke(
        elContext,
        var,
        "getResult",
        null,
        new Object[] { Integer.valueOf(1), 2 }
      )
    )
      .isEqualTo("int Integer"); // should be "Number int", but we can't figure that out
  }

  @Test
  public void itThrowsExceptionWhenMethodIsRestrictedFromConfig() {
    JinjavaInterpreter.pushCurrent(interpreter);
    when(config.getRestrictedMethods()).thenReturn(ImmutableSet.of("foo"));
    assertThatThrownBy(() ->
        jinjavaBeanELResolver.invoke(elContext, "abcd", "foo", null, new Object[] { 1 })
      )
      .isInstanceOf(MethodNotFoundException.class)
      .hasMessageStartingWith("Cannot find method 'foo'");
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itThrowsExceptionWhenPropertyIsRestrictedFromConfig() {
    JinjavaInterpreter.pushCurrent(interpreter);
    when(config.getRestrictedProperties()).thenReturn(ImmutableSet.of("property1"));
    assertThatThrownBy(() ->
        jinjavaBeanELResolver.getValue(elContext, "abcd", "property1")
      )
      .isInstanceOf(PropertyNotFoundException.class)
      .hasMessageStartingWith("Could not find property");
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itDoesNotAllowAccessingPropertiesOfInterpreter() {
    JinjavaInterpreter.pushCurrent(interpreter);
    try {
      assertThat(jinjavaBeanELResolver.getValue(elContext, interpreter, "config"))
        .isNull();
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }

  @Test
  public void itDoesNotGettingFromObjectMapper() {
    JinjavaInterpreter.pushCurrent(interpreter);
    try {
      assertThat(
        jinjavaBeanELResolver.getValue(elContext, new ObjectMapper(), "dateFormat")
      )
        .isNull();
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }

  @Test
  public void itDoesNotAllowInvokingFromObjectMapper() throws NoSuchMethodException {
    JinjavaInterpreter.pushCurrent(interpreter);
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      assertThatThrownBy(() ->
          jinjavaBeanELResolver.invoke(
            elContext,
            objectMapper,
            "getDateFormat",
            new Class[] {},
            new Object[] {}
          )
        )
        .isInstanceOf(MethodNotFoundException.class);
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }

  @Test
  public void itDoesNotAllowInvokingFromMethod() throws NoSuchMethodException {
    JinjavaInterpreter.pushCurrent(interpreter);
    try {
      List<String> list = new ArrayList<>();
      list.add("foo");
      assertThatThrownBy(() ->
          jinjavaBeanELResolver.invoke(
            elContext,
            list.getClass().getMethod("get", int.class),
            "invoke",
            new Class[] { Object.class, Object[].class },
            new Object[] { list, 0 }
          )
        )
        .isInstanceOf(MethodNotFoundException.class);
    } finally {
      JinjavaInterpreter.popCurrent();
    }
  }
}
