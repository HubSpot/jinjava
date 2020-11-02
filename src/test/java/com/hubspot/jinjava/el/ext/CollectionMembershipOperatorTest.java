package com.hubspot.jinjava.el.ext;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class CollectionMembershipOperatorTest {
  private JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
  }

  @Test
  public void itChecksIfStringContainsChar() {
    assertThat(interpreter.resolveELExpression("'a' in 'pastrami'", -1)).isEqualTo(true);
    assertThat(interpreter.resolveELExpression("'o' in 'pastrami'", -1)).isEqualTo(false);
  }

  @Test
  public void itChecksIfArrayContainsValue() {
    assertThat(interpreter.resolveELExpression("11 in [11, 12, 13]", -1)).isEqualTo(true);
    assertThat(interpreter.resolveELExpression("14 in [11, 12, 13]", -1))
      .isEqualTo(false);
  }

  @Test
  public void itChecksIfDictionaryContainsKey() {
    assertThat(interpreter.resolveELExpression("'a' in {'a': 1, 'b': 2}", -1))
      .isEqualTo(true);
    assertThat(interpreter.resolveELExpression("'c' in {'a': 1, 'b': 2}", -1))
      .isEqualTo(false);
  }
}
