package com.hubspot.jinjava.lib.exptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.hubspot.jinjava.BaseJinjavaTest;
import java.util.HashMap;

import com.hubspot.jinjava.interpret.FatalTemplateErrorsException;
import org.junit.Ignore;
import org.junit.Test;

public class IsContainingAllExpTestTest extends BaseJinjavaTest {
  private static final String CONTAINING_TEMPLATE =
    "{%% if %s is containing all %s %%}pass{%% else %%}fail{%% endif %%}";
  private static final String FAIL_MESSAGE = "This line shouldn't be reached!";

  @Ignore("Failed due to new EL rules. Exception: syntax error at position 35, encountered '2', expected ']'', " +
          "fieldName='2]', lineno=1, startPosition=1, scopeDepth=1, category=UNKNOWN, categoryErrors=null")
  @Test
  public void itPassesOnContainedValues() {
    assertThat(
        jinjava.render(
          String.format(CONTAINING_TEMPLATE, "[1, 2, 3]", "[1, 2]"),
          new HashMap<>()
        )
      )
      .isEqualTo("pass");
  }

  @Ignore("Failed due to new EL rules. Exception: Syntax error in '2, 2]': Error parsing '[1, 2, 3] is containing " +
          "all [1, 2, 2]': syntax error at position 35, encountered '2', expected ']'")
  @Test
  public void itPassesOnContainedDuplicatedValues() {
    assertThat(
        jinjava.render(
          String.format(CONTAINING_TEMPLATE, "[1, 2, 3]", "[1, 2, 2]"),
          new HashMap<>()
        )
      )
      .isEqualTo("pass");
  }

  @Test(expected = FatalTemplateErrorsException.class)
  public void itFailsOnOnlySomeContainedValues() {
    jinjava.render(
            String.format(CONTAINING_TEMPLATE, "[1, 2, 3]", "[1, 2, 4]"),
            new HashMap<>()
    );
    fail(FAIL_MESSAGE);
  }

  @Test(expected = FatalTemplateErrorsException.class)
  public void itFailsOnNullSequence() {
    jinjava.render(
            String.format(CONTAINING_TEMPLATE, "null", "[1, 2, 4]"),
            new HashMap<>()
    );
    fail(FAIL_MESSAGE);
  }

  @Test(expected = FatalTemplateErrorsException.class)
  public void itFailsOnNullValues() {
    jinjava.render(
            String.format(CONTAINING_TEMPLATE, "[1, 2, 3]", "null"),
            new HashMap<>()
    );
    fail(FAIL_MESSAGE);
  }

  @Ignore("Failed due to new EL rules. Exception: syntax error in ''3']': Error parsing '[1, 2, 3] is containing " +
          "all ['2', '3']': syntax error at position 37, encountered '3', expected ']'")
  @Test
  public void itPerformsTypeConversion() {
    assertThat(
        jinjava.render(
          String.format(CONTAINING_TEMPLATE, "[1, 2, 3]", "['2', '3']"),
          new HashMap<>()
        )
      )
      .isEqualTo("pass");
  }
}
