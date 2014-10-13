package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.hubspot.jinjava.Jinjava;


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
  
  public static class MyFoo {
    public String getBar() {
      return "mybar";
    }
  }

}
