package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.hubspot.jinjava.objects.date.PyishDate;

public class PrettyPrintFilterTest {

  PrettyPrintFilter f;

  @Before
  public void setup() {
    f = new PrettyPrintFilter();
  }

  @Test
  public void ppString() {
    assertThat(f.filter("foobar", null)).isEqualTo("{% raw %}(String: foobar){% endraw %}");
  }

  @Test
  public void ppInt() {
    assertThat(f.filter(123, null)).isEqualTo("{% raw %}(Integer: 123){% endraw %}");
  }

  @Test
  public void ppPyDate() {
    assertThat(f.filter(new PyishDate(ZonedDateTime.of(2014, 8, 4, 0, 0, 0, 0, ZoneOffset.UTC)), null)).isEqualTo("{% raw %}(PyishDate: 2014-08-04 00:00:00){% endraw %}");
  }

  @Test
  public void ppMap() {
    assertThat(f.filter(ImmutableMap.of("a", "foo", "b", "bar"), null))
        .isEqualTo("{% raw %}(RegularImmutableMap: {a=foo, b=bar}){% endraw %}");
  }

  @Test
  public void ppList() {
    assertThat(f.filter(Lists.newArrayList("foo", "bar"), null)).isEqualTo("{% raw %}(ArrayList: [foo, bar]){% endraw %}");
  }

  @Test
  public void ppObject() {
    assertThat(f.filter(new MyClass(), null)).isEqualTo("{% raw %}(MyClass: {bar=123, foo=foofoo}){% endraw %}");
  }

  public static class MyClass {
    public String getFoo() {
      return "foofoo";
    }

    public int getBar() {
      return 123;
    }
  }

}
