package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;

public class UniqueFilterTest {

  Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
  }

  @Test
  public void itDoesntFilterWhenNoDuplicateItemsInSeq() {
    assertThat(render(1, 2, 3)).isEqualTo("123");
  }

  @Test
  public void itFiltersDuplicatesFromSeq() {
    assertThat(render(1, 4, 3, 2, 3, 1, 4, 2, 6, 7, 8)).isEqualTo("1432678");
  }

  @Test
  public void itFiltersDuplicatesFromSeqByAttr() {
    assertThat(render("name", new MyClass("a"), new MyClass("b"), new MyClass("a"), new MyClass("c")))
        .isEqualTo("[Name:a][Name:b][Name:c]");
  }

  String render(Object... items) {
    return render("", items);
  }

  String render(String attr, Object... items) {
    Map<String, Object> context = new HashMap<>();
    context.put("iterable", items);

    String attrExtra = "";
    if (StringUtils.isNotBlank(attr)) {
      attrExtra = "(attr='" + attr + "')";
    }

    return jinjava.render("{% for item in iterable|unique" + attrExtra + " %}{{ item }}{% endfor %}", context);
  }

  public static class MyClass {
    private final String name;

    public MyClass(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    @Override
    public String toString() {
      return "[Name:" + name + "]";
    }
  }

}
