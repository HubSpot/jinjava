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
  public void itAddsResolvedValuesFromAnotherContextObject() {
    Context donor = new Context();
    donor.addResolvedValue(RESOLVED_VALUE);
    donor.addResolvedFunction(RESOLVED_FUNCTION);
    donor.addResolvedExpression(RESOLVED_EXPRESSION);

    assertThat(context.getResolvedValues()).doesNotContain(RESOLVED_VALUE);
    assertThat(context.getResolvedFunctions()).doesNotContain(RESOLVED_FUNCTION);
    assertThat(context.getResolvedExpressions()).doesNotContain(RESOLVED_EXPRESSION);

    context.addResolvedFrom(donor);

    assertThat(context.getResolvedValues()).contains(RESOLVED_VALUE);
    assertThat(context.getResolvedFunctions()).contains(RESOLVED_FUNCTION);
    assertThat(context.getResolvedExpressions()).contains(RESOLVED_EXPRESSION);
  }

  @Test
  public void itRecursivelyAddsValuesUpTheContextChain() {
    Context child = new Context(context);
    child.addResolvedValue(RESOLVED_VALUE);
    child.addResolvedFunction(RESOLVED_FUNCTION);
    child.addResolvedExpression(RESOLVED_EXPRESSION);

    assertThat(context.getResolvedValues()).contains(RESOLVED_VALUE);
    assertThat(context.getResolvedFunctions()).contains(RESOLVED_FUNCTION);
    assertThat(context.getResolvedExpressions()).contains(RESOLVED_EXPRESSION);
  }
}
