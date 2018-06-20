package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;
import com.hubspot.jinjava.interpret.RenderResult;
import com.hubspot.jinjava.interpret.TemplateError.ErrorReason;

public class AttrFilterTest {

  Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
  }

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

    RenderResult renderResult = jinjava.renderForResult("{{ foo|attr(\"barf\") }}", context);
    assertThat(renderResult.getOutput()).isEmpty();
    assertThat(renderResult.getErrors()).hasSize(1);
    assertThat(renderResult.getErrors().get(0).getReason()).isEqualTo(ErrorReason.UNKNOWN);
    assertThat(renderResult.getErrors().get(0).getFieldName()).isEqualTo("barf");
  }

  @Test
  public void testAttrNull() {
    Map<String, Object> context = new HashMap<>();
    context.put("foo", new MyFoo());

    assertThat(jinjava.render("{{ foo|attr(\"null_val\") }}", context)).isEqualTo("");
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
