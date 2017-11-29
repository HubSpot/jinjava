package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;

public class GroupByFilterTest {

  Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
  }

  @Test
  public void testGroupByAttr() throws Exception {
    Document dom = Jsoup.parseBodyFragment(
        jinjava.render(
            Resources.toString(Resources.getResource("filter/groupby-attr.jinja"), StandardCharsets.UTF_8),
            ImmutableMap.of("persons", (Object) Lists.newArrayList(
                new Person("male", "jared", "stehler"),
                new Person("male", "foo", "bar"),
                new Person("female", "sarah", "jones"),
                new Person("male", "jim", "jones"),
                new Person("female", "barb", "smith")
                ))));
    
    assertThat(dom.select("ul.root > li")).hasSize(2);
    assertThat(dom.select("ul.root > li").get(0).text()).contains("male jared");
    assertThat(dom.select("ul.root > li.male > ul > li")).hasSize(3);
    assertThat(dom.select("ul.root > li.female > ul > li")).hasSize(2);
  }

  public static class Person {
    private String gender;
    private String firstName;
    private String lastName;

    public Person(String gender, String firstName, String lastName) {
      this.gender = gender;
      this.firstName = firstName;
      this.lastName = lastName;
    }

    public String getFirstName() {
      return firstName;
    }

    public String getGender() {
      return gender;
    }

    public String getLastName() {
      return lastName;
    }
  }

}
