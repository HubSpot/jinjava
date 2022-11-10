package com.hubspot.jinjava;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.lib.filter.Filter;
import java.util.HashMap;
import org.junit.Test;

public class FilterOverrideTest {

  @Test
  public void itAllowsUsersToOverrideBuiltInFilters() {
    Jinjava jinjava = new Jinjava();
    String template = "{{ 5 | add(6) }}";

    assertThat(jinjava.render(template, new HashMap<>())).isEqualTo("11");

    jinjava.getGlobalContext().registerClasses(DescriptiveAddFilter.class);
    assertThat(jinjava.render(template, new HashMap<>())).isEqualTo("5 + 6 = 11");
  }

  public static class DescriptiveAddFilter implements Filter {

    @Override
    public String getName() {
      return "add";
    }

    @Override
    public Object filter(Object var, JinjavaInterpreter interpreter, String... args) {
      return (
        var +
        " + " +
        args[0] +
        " = " +
        (Integer.parseInt(var.toString()) + Integer.parseInt(args[0]))
      );
    }
  }
}
