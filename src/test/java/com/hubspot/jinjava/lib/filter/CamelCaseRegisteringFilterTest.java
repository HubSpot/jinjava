package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class CamelCaseRegisteringFilterTest extends BaseJinjavaTest {

  @Test
  public void itAllowsCamelCasedFilterNames() {
    jinjava.getGlobalContext().registerFilter(new ReturnHelloFilter());

    assertThat(jinjava.render("{{ 'test'|returnHello }}", new HashMap<>()))
      .isEqualTo("Hello");
  }

  private static class ReturnHelloFilter implements AdvancedFilter {

    @Override
    public String getName() {
      return "returnHello";
    }

    @Override
    public Object filter(
      Object var,
      JinjavaInterpreter interpreter,
      Object[] args,
      Map<String, Object> kwargs
    ) {
      return "Hello";
    }
  }
}
