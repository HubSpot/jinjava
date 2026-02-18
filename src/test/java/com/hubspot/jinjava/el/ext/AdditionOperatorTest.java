package com.hubspot.jinjava.el.ext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.interpret.OutputTooBigException;
import java.util.Collection;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class AdditionOperatorTest {

  private static final long MAX_STRING_LENGTH = 10_000;
  private JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    interpreter =
      new Jinjava(
        JinjavaConfig
          .newBuilder()
          .withMethodValidator(BaseJinjavaTest.METHOD_VALIDATOR)
          .withReturnTypeValidator(BaseJinjavaTest.RETURN_TYPE_VALIDATOR)
          .withMaxOutputSize(MAX_STRING_LENGTH)
          .build()
      )
        .newInterpreter();
    JinjavaInterpreter.pushCurrent(interpreter);
  }

  @After
  public void teardown() {
    JinjavaInterpreter.popCurrent();
  }

  @Test
  public void itConcatsStrings() {
    assertThat(interpreter.resolveELExpression("'foo' + 'bar'", -1)).isEqualTo("foobar");
  }

  @Test
  public void itLimitsLengthOfStrings() {
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 0; i < MAX_STRING_LENGTH; i++) {
      stringBuilder.append("0");
    }

    String first = stringBuilder.toString();
    assertThat(interpreter.resolveELExpression("'" + first + "' + ''", -1))
      .isEqualTo(first);
    assertThat(interpreter.getErrors()).isEmpty();

    assertThat(interpreter.resolveELExpression("'" + first + "' + 'TOOBIG'", -1))
      .isNull();

    assertThat(interpreter.getErrors().get(0).getMessage())
      .contains(OutputTooBigException.class.getSimpleName());
  }

  @Test
  public void itAddsNumbers() {
    assertThat(interpreter.resolveELExpression("1 + 2", -1)).isEqualTo(3L);
  }

  @Test
  public void itConcatsNumberWithString() {
    assertThat(interpreter.resolveELExpression("'1' + 2", -1)).isEqualTo("12");
    assertThat(interpreter.resolveELExpression("1 + '2'", -1)).isEqualTo("12");
  }

  @Test
  public void itCombinesTwoLists() {
    assertThat(
      (Collection<Object>) interpreter.resolveELExpression(
        "['foo', 'bar'] + ['other', 'one']",
        -1
      )
    )
      .containsExactly("foo", "bar", "other", "one");
  }

  @Test
  public void itAddsToList() {
    assertThat(
      (Collection<Object>) interpreter.resolveELExpression("['foo'] + 'bar'", -1)
    )
      .containsExactly("foo", "bar");
  }

  @Test
  public void itCombinesTwoDicts() {
    assertThat(
      (Map<Object, Object>) interpreter.resolveELExpression(
        "{'k1':'v1'} + {'k2':'v2'}",
        -1
      )
    )
      .containsOnly(entry("k1", "v1"), entry("k2", "v2"));
  }
}
