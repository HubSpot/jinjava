package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.RenderResult;

public class JoinFilterTest {

  Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
    jinjava.getGlobalContext().put("users", Lists.newArrayList(
        new User("foo"), new User("bar")));
  }

  @Test
  public void testJoinVals() {
    assertThat(jinjava.render("{{ [1, 2, 3]|join('|') }}", new HashMap<String, Object>())).isEqualTo("1|2|3");
  }

  @Test
  public void testJoinAttrs() {
    assertThat(jinjava.render("{{ users|join(', ', attribute='username') }}", new HashMap<String, Object>()))
        .isEqualTo("foo, bar");
  }

  @Test
  public void itTruncatesStringToConfigLimit() {
    jinjava = new Jinjava(JinjavaConfig.newBuilder()
        .withMaxStringLength(5)
        .build());

    RenderResult result = jinjava.renderForResult("{{ [1, 2, 3, 4, 5]|join('|') }}", new HashMap<String, Object>());
    assertThat(result.getOutput()).isEqualTo("1|2|3");
    assertThat(result.getErrors().size()).isEqualTo(1);
    assertThat(result.getErrors().get(0).getMessage()).contains("filter has been truncated to the max String length");
  }

  public static class User {
    private String username;

    public User(String username) {
      this.username = username;
    }

    public String getUsername() {
      return username;
    }
  }
}
