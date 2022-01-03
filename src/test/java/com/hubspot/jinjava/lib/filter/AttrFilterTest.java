package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.hubspot.jinjava.BaseJinjavaTest;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class AttrFilterTest extends BaseJinjavaTest {

  @Test
  public void testAttr() {
    Map<String, Object> context = new HashMap<>();
    context.put("foo", new MyFoo());

    assertThat(jinjava.render("{{ foo|attr(\"bar\") }}", context)).isEqualTo("mybar");
  }

  @Test
  public void testAttrNotFound() {
    Map<String, Object> context = new HashMap<>();
    context.put("foo", new MyFoo());

    RenderResult renderResult = jinjava.renderForResult(
      "{{ foo|attr(\"barf\") }}",
      context
    );
    assertThat(renderResult.getOutput()).isEmpty();
    assertThat(renderResult.getErrors()).hasSize(1);
    assertThat(renderResult.getErrors().get(0).getReason())
      .isEqualTo(ErrorReason.UNKNOWN);
    assertThat(renderResult.getErrors().get(0).getFieldName()).isEqualTo("barf");
  }

  @Test
  public void testAttrNull() {
    Map<String, Object> context = new HashMap<>();
    context.put("foo", new MyFoo());

    assertThat(jinjava.render("{{ foo|attr(\"null_val\") }}", context)).isEqualTo("");
  }

  @Test
  public void itAddsErrorOnNullAttribute() {
    Map<String, Object> context = new HashMap<>();
    context.put("foo", new MyFoo());
    context.put("filter", null);

    RenderResult result = jinjava.renderForResult("{{ foo|attr(filter) }}", context);
    assertThat(result.getErrors()).hasSize(1);
    assertThat(result.getErrors().get(0).getMessage())
      .contains("'name' argument cannot be null");
  }

  public static class MyFoo {

    public String getBar() {
      return "mybar";
    }

    public String getNullVal() {
      return null;
    }
  }
}
