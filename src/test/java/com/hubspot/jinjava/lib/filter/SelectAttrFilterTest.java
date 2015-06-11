package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.Jinjava;

public class SelectAttrFilterTest {

  Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
    jinjava.getGlobalContext().put("users", Lists.newArrayList(
        new User(0, false, "foo@bar.com"), new User(1, true, "bar@bar.com"), new User(2, false, null)));
  }

  @Test
  public void selectAttrWithNoExp() {
    assertThat(jinjava.render("{{ users|selectattr('is_active') }}", new HashMap<String, Object>()))
        .isEqualTo("[1]");
  }

  @Test
  public void selectAttrWithExp() {
    assertThat(jinjava.render("{{ users|selectattr('email', 'none') }}", new HashMap<String, Object>()))
        .isEqualTo("[2]");
  }

  public static class User {
    private int num;
    private boolean isActive;
    private String email;

    public User(int num, boolean isActive, String email) {
      this.num = num;
      this.isActive = isActive;
      this.email = email;
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

    @Override
    public String toString() {
      return num + "";
    }
  }

}
