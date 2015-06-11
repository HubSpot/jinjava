package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.lib.filter.JoinFilterTest.User;

public class MapFilterTest {

  Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
  }

  @Test
  public void mapAttr() {
    assertThat(jinjava.render("{{ users|map(attribute='username')|join(', ') }}",
        ImmutableMap.of("users", (Object) Lists.newArrayList(new User("foo"), new User("bar")))))
        .isEqualTo("foo, bar");
  }

  @Test
  public void mapFilter() {
    assertThat(jinjava.render("{{ titles|map('lower')|join(', ') }}",
        ImmutableMap.of("titles", (Object) Lists.newArrayList("Happy Day", "FOO", "bar"))))
        .isEqualTo("happy day, foo, bar");
  }

}
