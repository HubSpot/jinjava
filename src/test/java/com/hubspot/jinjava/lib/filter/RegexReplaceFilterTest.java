package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hubspot.jinjava.BaseInterpretingTest;
import com.hubspot.jinjava.interpret.InvalidArgumentException;
import com.hubspot.jinjava.objects.SafeString;
import org.junit.Before;
import org.junit.Test;

public class RegexReplaceFilterTest extends BaseInterpretingTest {
  RegexReplaceFilter filter;

  @Before
  public void setup() {
    filter = new RegexReplaceFilter();
  }

  @Test
  public void expects2Args() {
    assertThatThrownBy(() -> filter.filter("foo", interpreter))
      .hasMessageContaining("Argument named 'regex' is required but missing");
  }

  @Test
  public void expectsNotNullArgs() {
    assertThatThrownBy(
        () -> filter.filter("foo", interpreter, new String[] { null, null })
      )
      .hasMessageContaining("Argument named 'regex' is required but missing");
  }

  public void noopOnNullExpr() {
    assertThat(filter.filter(null, interpreter, "foo", "bar")).isNull();
  }

  @Test
  public void itMatchesRegexAndReplacesString() {
    assertThat(filter.filter("It costs $300", interpreter, "[^a-zA-Z]", ""))
      .isEqualTo("Itcosts");
  }

  @Test(expected = InvalidArgumentException.class)
  public void isThrowsExceptionOnInvalidRegex() {
    filter.filter("It costs $300", interpreter, "[", "");
  }

  @Test
  public void itMatchesRegexAndReplacesStringForSafeString() {
    assertThat(
        filter
          .filter(new SafeString("It costs $300"), interpreter, "[^a-zA-Z]", "")
          .toString()
      )
      .isEqualTo("Itcosts");
  }
}
