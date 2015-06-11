package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.hubspot.jinjava.Jinjava;

@SuppressWarnings("unchecked")
public class SumFilterTest {

  Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
  }

  @Test
  public void sumWithAttr() {
    Map<String, Object> context = new HashMap<>();
    context.put("items", Lists.newArrayList(new Item(12), new Item(30.50)));

    assertThat(jinjava.render("{{ items|sum(attribute='price') }}", context)).isEqualTo("42.5");
  }

  @Test
  public void sumWithAttrAndStart() {
    Map<String, Object> context = new HashMap<>();
    context.put("items", Lists.newArrayList(new Item(12), new Item(30.50)));

    assertThat(jinjava.render("{{ items|sum(attribute='price', 10) }}", context)).isEqualTo("52.5");
  }

  @Test
  public void sumOfSeq() {
    Map<String, Object> context = new HashMap<>();
    context.put("items", Lists.newArrayList(12, 30.50));

    assertThat(jinjava.render("{{ items|sum }}", context)).isEqualTo("42.5");
  }

  public static class Item {
    private Number price;

    public Item(Number price) {
      this.price = price;
    }

    public Number getPrice() {
      return price;
    }
  }
}
