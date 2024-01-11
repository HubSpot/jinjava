package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class IndentFilterTest extends BaseJinjavaTest {

  private final Map<String, Object> VARS = new HashMap<>();

  @Before
  public void setup() {
    jinjava.getGlobalContext().registerClasses(IndentFilter.class);

    VARS.put("multiLine", "1\n2\n3");
  }

  @Test
  public void itDoesntIndentFirstlineByDefault() {
    assertThat(jinjava.render("{% set d=multiLine | indent %}{{ d }}", VARS))
      .isEqualTo("1\n" + "    2\n" + "    3");
  }

  @Test
  public void itIndentsFirstline() {
    assertThat(
      jinjava.render(
        "{% set d=multiLine | indent(indentfirst= True, width=1) %}{{ d }}",
        VARS
      )
    )
      .isEqualTo(" 1\n" + " 2\n" + " 3");
  }
}
