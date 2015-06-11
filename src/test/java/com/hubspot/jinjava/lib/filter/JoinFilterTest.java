package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.Jinjava;

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
