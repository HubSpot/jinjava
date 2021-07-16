package com.hubspot.jinjava.el.ext;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import org.junit.Before;
import org.junit.Test;

public class CollectionNonMembershipOperatorTest {
  private JinjavaInterpreter interpreter;

  @Before
  public void setup() {
    interpreter = new Jinjava().newInterpreter();
  }

  @Test
  public void itChecksIfStringDoesntContainChar() {
    assertThat(interpreter.resolveELExpression("'a' not in 'pastrami'", -1))
      .isEqualTo(false);
    assertThat(interpreter.resolveELExpression("'o' not in 'pastrami'", -1))
      .isEqualTo(true);
  }

  @Test
  public void itChecksIfArrayDoesntContainValue() {
    assertThat(interpreter.resolveELExpression("11 not in [11, 12, 13]", -1))
      .isEqualTo(false);
    assertThat(interpreter.resolveELExpression("14 not in [11, 12, 13]", -1))
      .isEqualTo(true);
  }

  @Test
  public void itChecksIfDictionaryDoesntContainKey() {
    assertThat(interpreter.resolveELExpression("'a' not in {'a': 1, 'b': 2}", -1))
      .isEqualTo(false);
    assertThat(interpreter.resolveELExpression("'c' not in {'a': 1, 'b': 2}", -1))
      .isEqualTo(true);
  }
}
