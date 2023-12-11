package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.JinjavaConfig;
import com.hubspot.jinjava.interpret.Context;
import com.hubspot.jinjava.interpret.JinjavaInterpreter;
import com.hubspot.jinjava.objects.date.PyishDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.Before;
import org.junit.Test;

public class PrettyPrintFilterTest {
  JinjavaInterpreter i;
  PrettyPrintFilter f;

  @Before
  public void setup() {
    JinjavaConfig config = JinjavaConfig.newBuilder().build();
    Jinjava jinjava = new Jinjava(config);
    Context context = jinjava.getGlobalContext();
    i = new JinjavaInterpreter(jinjava, context, config);
    f = new PrettyPrintFilter();
  }

  @Test
  public void ppString() {
    assertThat(f.filter("foobar", i)).isEqualTo("{% raw %}(String: foobar){% endraw %}");
  }

  @Test
  public void ppInt() {
    assertThat(f.filter(123, null)).isEqualTo("{% raw %}(Integer: 123){% endraw %}");
  }

  @Test
  public void ppPyDate() {
    assertThat(
        f.filter(
          new PyishDate(ZonedDateTime.of(2014, 8, 4, 0, 0, 0, 0, ZoneOffset.UTC)),
          null
        )
      )
      .isEqualTo("{% raw %}(PyishDate: 2014-08-04 00:00:00){% endraw %}");
  }

  @Test
  public void ppMap() {
    assertThat(f.filter(ImmutableMap.of("a", "foo", "b", "bar"), null))
      .isEqualTo("{% raw %}(RegularImmutableMap: {a=foo, b=bar}){% endraw %}");
  }

  @Test
  public void ppList() {
    assertThat(f.filter(Lists.newArrayList("foo", "bar"), null))
      .isEqualTo("{% raw %}(ArrayList: [foo, bar]){% endraw %}");
  }

  @Test
  public void ppObject() {
    MyClass myClass = new MyClass();

    assertThat(f.filter(myClass, i))
      .isEqualTo(
        String.format(
          "{%% raw %%}(MyClass: {\n" +
          "  &quot;foo&quot; : &quot;%s&quot;,\n" +
          "  &quot;bar&quot; : %d,\n" +
          "  &quot;nestedClass&quot; : {\n" +
          "    &quot;fooField&quot; : &quot;%s&quot;,\n" +
          "    &quot;barField&quot; : %d\n" +
          "  },\n" +
          "  &quot;date&quot; : 1702252800.000000000\n" +
          "}){%% endraw %%}",
          myClass.getFoo(),
          myClass.getBar(),
          myClass.getNestedClass().getFooField(),
          myClass.getNestedClass().getBarField()
        )
      );
  }

  @JsonPropertyOrder({ "foo", "bar", "nestedClass" })
  public static class MyClass {

    public String getFoo() {
      return "foofoo";
    }

    public int getBar() {
      return 123;
    }

    public ZonedDateTime getDate() {
      return ZonedDateTime.of(2023, 12, 11, 0, 0, 0, 0, ZonedDateTime.now().getZone());
    }

    public MyNestedClass getNestedClass() {
      return new MyNestedClass();
    }
  }

  @JsonPropertyOrder({ "fooField", "barField" })
  public static class MyNestedClass {

    public String getFooField() {
      return "foofieldfoofield";
    }

    public int getBarField() {
      return 123;
    }
  }
}
