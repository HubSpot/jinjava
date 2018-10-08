package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.Jinjava;

public class RejectAttrFilterTest {

  Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
    jinjava.getGlobalContext().put("users", Lists.newArrayList(
        new User(0, false, "foo@bar.com", new Option(0, "option0")),
        new User(1, true, "bar@bar.com", new Option(1, "option1")),
        new User(2, false, null, new Option(2, "option2"))));
  }

  @Test
  public void rejectAttrWithNoExp() {
    assertThat(jinjava.render("{{ users|rejectattr('is_active') }}", new HashMap<String, Object>()))
        .isEqualTo("[0, 2]");
  }

  @Test
  public void rejectAttrWithExp() {
    assertThat(jinjava.render("{{ users|rejectattr('email', 'none') }}", new HashMap<String, Object>()))
        .isEqualTo("[0, 1]");
  }

  @Test
  public void rejectAttrWithIsEqualToExp() {
    assertThat(jinjava.render("{{ users|rejectattr('email', 'equalto', 'bar@bar.com') }}", new HashMap<String, Object>()))
        .isEqualTo("[0, 2]");
  }

  @Test
  public void rejectAttrWithNestedProperty() {
    assertThat(jinjava.render("{{ users|rejectattr('option.id', 'equalto', 1) }}", new HashMap<String, Object>()))
        .isEqualTo("[0, 2]");

    assertThat(jinjava.render("{{ users|rejectattr('option.name', 'equalto', 'option0') }}", new HashMap<String, Object>()))
        .isEqualTo("[1, 2]");
  }

  public static class User {
    private int num;
    private boolean isActive;
    private String email;
    private Option option;

    public User(int num, boolean isActive, String email, Option option) {
      this.num = num;
      this.isActive = isActive;
      this.email = email;
      this.option = option;
    }

    public int getNum() {
      return num;
    }

    public String getEmail() {
      return email;
    }

    public boolean getIsActive() {
      return isActive;
    }

    public Option getOption() {
      return option;
    }

    @Override
    public String toString() {
      return num + "";
    }
  }

  public static class Option {
    private long id;
    private String name;

    public Option(long id, String name) {
      this.id = id;
      this.name = name;
    }

    public long getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return id + "";
    }
  }
}
