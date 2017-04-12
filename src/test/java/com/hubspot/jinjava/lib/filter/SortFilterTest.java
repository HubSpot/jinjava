package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;

public class SortFilterTest {

  Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
  }

  @Test
  public void sortAsc() {
    assertThat(render(4, 2, 1, 3)).isEqualTo("1234");
  }

  @Test
  public void sortDesc() {
    assertThat(render("(true)", 4, 2, 1, 3)).isEqualTo("4321");
  }

  @Test
  public void sortStringsCaseSensitive() {
    assertThat(render("(false, true)", "foo", "Foo", "bar")).isEqualTo("Foobarfoo");
  }

  @Test
  public void sortWithNamedAttributes() throws Exception {
    // even if named attributes were never supported for this filter, ensure parameters are passed in order and it works
    assertThat(render("(reverse=false, case_sensitive=false, attribute='foo.date')",
        new MyBar(new MyFoo(new Date(250L))), new MyBar(new MyFoo(new Date(0L))), new MyBar(new MyFoo(new Date(100000000L))))).isEqualTo("0250100000000");
  }

  @Test
  public void sortStringsCaseInsensitive() {
    assertThat(render("()", "foo", "Foo", "bar")).isEqualTo("barfooFoo");
  }

  @Test
  public void sortWithAttr() {
    assertThat(render("(false, false, 'date')", new MyFoo(new Date(250L)), new MyFoo(new Date(0L)), new MyFoo(new Date(100000000L)))).isEqualTo("0250100000000");
  }

  @Test
  public void sortWithNestedAttr() {
    assertThat(render("(false, false, 'foo.date')",
        new MyBar(new MyFoo(new Date(250L))), new MyBar(new MyFoo(new Date(0L))), new MyBar(new MyFoo(new Date(100000000L))))).isEqualTo("0250100000000");
  }

  String render(Object... items) {
    return render("", items);
  }

  String render(String sortExtra, Object... items) {
    Map<String, Object> context = new HashMap<>();
    context.put("iterable", items);

    return jinjava.render("{% for item in iterable|sort" + sortExtra + " %}{{ item }}{% endfor %}", context);
  }

  public static class MyFoo {
    private Date date;

    MyFoo(Date date) {
      this.date = date;
    }

    public Date getDate() {
      return date;
    }

    @Override
    public String toString() {
      return "" + date.getTime();
    }
  }

  public static class MyBar {
    private MyFoo foo;

    MyBar(MyFoo foo) {
      this.foo = foo;
    }

    public MyFoo getFoo() {
      return foo;
    }

    @Override
    public String toString() {
      return foo.toString();
    }
  }
}
