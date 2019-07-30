package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

  @Test
  public void itUsesAttributeIfAttributeNameClashesWithFilter() {
    assertThat(jinjava.render("{{ titles|map(attribute='date')|join(' ') }}",
        ImmutableMap.of("titles", (Object) Lists.newArrayList(new TestClass(12345)))))
        .isEqualTo("12345");
  }

  @Test
  public void itMapsFirstArgumentToFilterIfFilterExists() {
    assertThatThrownBy(() -> jinjava.render("{{ titles|map('date')|join(' ') }}",
        ImmutableMap.of("titles", (Object) Lists.newArrayList(new TestClass(12345)))))
        .hasMessageContaining("Input to function must be a date object");
  }

  @Test
  public void itAddsErrorIfFirstArgumentIsNull() {
    assertThatThrownBy(() -> jinjava.render("{{ titles|map(null)|join(' ') }}",
        ImmutableMap.of("titles", (Object) Lists.newArrayList(new TestClass(12345)))))
        .hasMessageContaining("1st argument cannot be null");
  }

  @Test
  public void itAddsErrorIfAttributeArgumentIsNull() {
    assertThatThrownBy(() -> jinjava.render("{{ titles|map(attribute=null)|join(' ') }}",
        ImmutableMap.of("titles", (Object) Lists.newArrayList(new TestClass(12345)))))
        .hasMessageContaining("'attribute' argument cannot be null");
  }

  @Test
  public void itAddsErrorIfNoArgumentsAreProvided() {
    assertThatThrownBy(() -> jinjava.render("{{ titles|map()|join(' ') }}",
        ImmutableMap.of("titles", (Object) Lists.newArrayList(new TestClass(12345)))))
        .hasMessageContaining("requires 1 argument");
  }

  public class TestClass {

    private final long date;

    public TestClass(long date) {
      this.date = date;
    }

    public long getDate() {
      return date;
    }
  }

}
