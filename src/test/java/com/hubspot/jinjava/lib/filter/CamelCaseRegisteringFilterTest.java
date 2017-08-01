package com.hubspot.jinjava.lib.filter;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CamelCaseRegisteringFilterTest {

  Jinjava jinjava;

  @Test
  public void itAllowsCamelCasedFilterNames() {
    jinjava = new Jinjava();
    jinjava.getGlobalContext().registerFilter(new ReturnHelloFilter());

    assertThat(jinjava.render("{{ 'test'|returnHello }}", new HashMap<>())).isEqualTo("Hello");
  }

  private static class ReturnHelloFilter implements AdvancedFilter {
    @Override
    public String getName() {
      return "returnHello";
    }

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, Object[] args, Map<String, Object> kwargs) {
      return "Hello";
    }
  }

}
