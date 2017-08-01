package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class ContextTest {
  private static final String RESOLVED_EXPRESSION = "exp" ;
  private static final String RESOLVED_FUNCTION = "func" ;
  private static final String RESOLVED_VALUE = "val" ;

  private Context context;

  @Before
  public void setUp() {
    context = new Context();
  }

  @Test
  public void itAppliesResolvedValuesFromAnotherContextObject() {
    Context appliedFrom = new Context();
    appliedFrom.addResolvedValue(RESOLVED_VALUE);
    appliedFrom.addResolvedFunction(RESOLVED_FUNCTION);
    appliedFrom.addResolvedExpression(RESOLVED_EXPRESSION);

    assertThat(context.getResolvedValues()).doesNotContain(RESOLVED_VALUE);
    assertThat(context.getResolvedFunctions()).doesNotContain(RESOLVED_FUNCTION);
    assertThat(context.getResolvedExpressions()).doesNotContain(RESOLVED_EXPRESSION);

    context.addResolvedFrom(appliedFrom);

    assertThat(context.getResolvedValues()).contains(RESOLVED_VALUE);
    assertThat(context.getResolvedFunctions()).contains(RESOLVED_FUNCTION);
    assertThat(context.getResolvedExpressions()).contains(RESOLVED_EXPRESSION);
  }
}
