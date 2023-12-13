package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.InterpretException;
import com.hubspot.jinjava.interpret.InvalidInputException;
import com.hubspot.jinjava.objects.SafeString;
import org.junit.Before;
import org.junit.Test;

public class ReplaceFilterTest extends BaseInterpretingTest {
  ReplaceFilter filter;

  @Before
  public void setup() {
    filter = new ReplaceFilter();
  }

  @Test(expected = InterpretException.class)
  public void expectsAtLeast2Args() {
    filter.filter("foo", interpreter);
  }

  public void noopOnNullExpr() {
    assertThat(filter.filter(null, interpreter, "foo", "bar")).isNull();
  }

  @Test
  public void replaceString() {
    assertThat(filter.filter("hello world", interpreter, "hello", "goodbye"))
      .isEqualTo("goodbye world");
  }

  @Test
  public void replaceWithCount() {
    assertThat(filter.filter("aaaaargh", interpreter, "a", "d'oh, ", "2"))
      .isEqualTo("d'oh, d'oh, aaargh");
  }

  @Test
  public void replaceSafeStringWithCount() {
    assertThat(
        filter
          .filter(new SafeString("aaaaargh"), interpreter, "a", "d'oh, ", "2")
          .toString()
      )
      .isEqualTo("d'oh, d'oh, aaargh");
  }

  @Test
  public void replaceBoolean() {
    assertThat(filter.filter(true, interpreter, "true", "TRUEEE").toString())
      .isEqualTo("TRUEEE");
  }

  @Test
  public void itLimitsLongInput() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 101; i++) {
      sb.append('a');
    }
    assertThatThrownBy(
        () ->
          filter.filter(
            sb.toString(),
            new Jinjava(JinjavaConfig.newBuilder().withMaxStringLength(10).build())
            .newInterpreter(),
            "O",
            "0"
          )
      )
      .isInstanceOf(InvalidInputException.class)
      .hasMessageContaining(
        "Invalid input for 'replace': input with length '101' exceeds maximum allowed length of '10'"
      );
  }
}
