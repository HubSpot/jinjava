package com.hubspot.jinjava.el.ext;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class AllowlistGroupTest extends BaseJinjavaTest {

  @Test
  public void itResolvesNamedParameterNameThroughAllowlist() {
    Map<String, Object> context = new HashMap<>();
    context.put("np", new NamedParameter("greeting", "hello"));
    String result = jinjava.render("{{ np.name }}", context);
    assertThat(result).isEqualTo("greeting");
  }

  @Test
  public void itResolvesNamedParameterValueThroughAllowlist() {
    Map<String, Object> context = new HashMap<>();
    context.put("np", new NamedParameter("greeting", "hello"));
    String result = jinjava.render("{{ np.value }}", context);
    assertThat(result).isEqualTo("hello");
  }
}
