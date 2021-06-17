package com.hubspot.jinjava.el.ext;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.TemplateError.ErrorType;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class AstDictTest {
  private JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
  }

  @Test
  public void itGetsDictValues() {
    interpreter.getContext().put("foo", ImmutableMap.of("bar", "test"));
    assertThat(interpreter.resolveELExpression("foo.bar", -1)).isEqualTo("test");
  }

  @Test
  public void itGetsDictValuesWithEnumKeys() {
    interpreter.getContext().put("foo", ImmutableMap.of(ErrorType.FATAL, "test"));
    assertThat(interpreter.resolveELExpression("foo.FATAL", -1)).isEqualTo("test");
  }

  @Test
  public void itGetsDictValuesWithEnumKeysUsingToString() {
    interpreter.getContext().put("foo", ImmutableMap.of(TestEnum.BAR, "test"));
    assertThat(interpreter.resolveELExpression("foo.barName", -1)).isEqualTo("test");
  }

  @Test
  public void itHandlesEmptyMaps() {
    interpreter.getContext().put("foo", ImmutableMap.of());
    assertThat(interpreter.resolveELExpression("foo.FATAL", -1)).isNull();
    assertThat(interpreter.getErrors()).isEmpty();
  }

  @Test
  public void itGetsDictValuesWithEnumKeysInObjects() {
    interpreter
      .getContext()
      .put("test", new TestClass(ImmutableMap.of(ErrorType.FATAL, "test")));
    assertThat(interpreter.resolveELExpression("test.my_map.FATAL", -1))
      .isEqualTo("test");
  }

  public class TestClass {
    private Map<ErrorType, String> myMap;

    public TestClass(Map<ErrorType, String> myMap) {
      this.myMap = myMap;
    }

    public Map<ErrorType, String> getMyMap() {
      return myMap;
    }
  }

  public enum TestEnum {
    FOO("fooName"),
    BAR("barName");

    private String name;

    TestEnum(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name;
    }
  }
}
