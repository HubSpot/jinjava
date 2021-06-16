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
    assertThat(interpreter.resolveELExpression("foo.fatal", -1)).isEqualTo("test");
  }

  @Test
  public void itGetsDictValuesWithEnumKeysInObjects() {
    interpreter
      .getContext()
      .put("test", new TestClass(ImmutableMap.of(ErrorType.FATAL, "test")));
    assertThat(interpreter.resolveELExpression("test.my_map.fatal", -1))
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
}
