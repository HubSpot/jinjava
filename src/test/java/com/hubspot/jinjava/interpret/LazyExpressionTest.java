package com.hubspot.jinjava.interpret;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.hubspot.jinjava.interpret.LazyExpression.MEMOIZATION;
import java.util.List;
import org.junit.Test;

public class LazyExpressionTest {

  @Test
  public void itSerializesUnderlyingValue() throws JsonProcessingException {
    LazyExpression expression = LazyExpression.of(
      () -> ImmutableMap.of("test", "hello", "test2", "hello2"),
      "{}"
    );
    Object evaluated = expression.get();
    assertThat(evaluated).isNotNull();
    assertThat(new ObjectMapper().writeValueAsString(expression))
      .isEqualTo("{\"test\":\"hello\",\"test2\":\"hello2\"}");
  }

  @Test
  public void itSerializesNonEvaluatedValueToEmpty() throws JsonProcessingException {
    LazyExpression expression = LazyExpression.of(
      () -> ImmutableMap.of("test", "hello", "test2", "hello2"),
      "{}"
    );
    assertThat(new ObjectMapper().writeValueAsString(expression)).isEqualTo("\"\"");
  }

  @Test
  public void itMemoizesByDefault() {
    List mock = mock(List.class);
    LazyExpression expression = LazyExpression.of(mock::isEmpty, "");
    expression.get();
    expression.get();
    verify(mock, times(1)).isEmpty();
  }

  @Test
  public void itAllowsMemoizationToBeDisabled() {
    List mock = mock(List.class);
    LazyExpression expression = LazyExpression.of(mock::isEmpty, "", MEMOIZATION.OFF);
    expression.get();
    expression.get();
    verify(mock, times(2)).isEmpty();
  }

  @Test
  public void itAllowsMemoizationToBeEnabled() {
    List mock = mock(List.class);
    LazyExpression expression = LazyExpression.of(mock::isEmpty, "", MEMOIZATION.ON);
    expression.get();
    expression.get();
    verify(mock, times(1)).isEmpty();
  }
}
