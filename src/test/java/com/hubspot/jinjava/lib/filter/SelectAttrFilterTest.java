package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.Lists;
import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.objects.serialization.PyishSerializable;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;

public class SelectAttrFilterTest extends BaseJinjavaTest {

  @Before
  public void setup() {
    jinjava
      .getGlobalContext()
      .put(
        "users",
        Lists.newArrayList(
          new User(0, false, "foo@bar.com", new Option(0, "option0")),
          new User(1, true, "bar@bar.com", new Option(1, "option1")),
          new User(2, false, null, new Option(2, "option2"))
        )
      );
  }

  @Test
  public void selectAttrWithNoExp() {
    assertThat(
        jinjava.render(
          "{{ users|selectattr('is_active') }}",
          new HashMap<String, Object>()
        )
      )
      .isEqualTo("[1]");
  }

  @Test
  public void selectAttrWithExp() {
    assertThat(
        jinjava.render(
          "{{ users|selectattr('email', 'none') }}",
          new HashMap<String, Object>()
        )
      )
      .isEqualTo("[2]");
  }

  @Test
  public void selectAttrWithSymbolicExp() {
    assertThat(
        jinjava.render(
          "{{ users|selectattr('isActive', '==', 'true') }}",
          new HashMap<String, Object>()
        )
      )
      .isEqualTo("[1]");
  }

  @Test
  public void selectAttrWithIsEqualToExp() {
    assertThat(
        jinjava.render(
          "{{ users|selectattr('email', 'equalto', 'bar@bar.com') }}",
          new HashMap<String, Object>()
        )
      )
      .isEqualTo("[1]");
  }

  @Test
  public void selectAttrWithNumericIsEqualToExp() {
    assertThat(
        jinjava.render(
          "{{ users|selectattr('num', 'equalto', 1) }}",
          new HashMap<String, Object>()
        )
      )
      .isEqualTo("[1]");
  }

  @Test
  public void selectAttrWithNestedProperty() {
    assertThat(
        jinjava.render(
          "{{ users|selectattr('option.id', 'equalto', 1) }}",
          new HashMap<String, Object>()
        )
      )
      .isEqualTo("[1]");

    assertThat(
        jinjava.render(
          "{{ users|selectattr('option.name', 'equalto', 'option2') }}",
          new HashMap<String, Object>()
        )
      )
      .isEqualTo("[2]");
  }

  public static class User implements PyishSerializable {
    private long num;
    private boolean isActive;
    private String email;
    private Option option;

    public User(long num, boolean isActive, String email, Option option) {
      this.num = num;
      this.isActive = isActive;
      this.email = email;
      this.option = option;
    }

    public long getNum() {
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

    @Override
    public String toPyishString() {
      return toString();
    }
  }

  public static class Option implements PyishSerializable {
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

    @JsonValue
    @Override
    public String toString() {
      return id + "";
    }

    @Override
    public String toPyishString() {
      return toString();
    }
  }
}
