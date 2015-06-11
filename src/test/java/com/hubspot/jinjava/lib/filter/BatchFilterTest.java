package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;

public class BatchFilterTest {

  Jinjava jinjava;

  @Before
  public void setup() {
    jinjava = new Jinjava();
  }

  @Test
  public void batchFilterNoBackfill() {
    Map<String, Object> context = ImmutableMap.of("items", (Object) Lists.newArrayList(
        "1", "2", "3", "4", "5", "6"));

    Document dom = Jsoup.parseBodyFragment(render("batch-filter", context));
    assertThat(dom.select("tr")).hasSize(2);

    Elements trs = dom.select("tr");
    assertThat(trs.get(0).select("td")).hasSize(3);
    assertThat(trs.get(0).select("td").get(0).text()).isEqualTo("1");
    assertThat(trs.get(0).select("td").get(1).text()).isEqualTo("2");
    assertThat(trs.get(0).select("td").get(2).text()).isEqualTo("3");
    assertThat(trs.get(1).select("td")).hasSize(3);
    assertThat(trs.get(1).select("td").get(0).text()).isEqualTo("4");
    assertThat(trs.get(1).select("td").get(1).text()).isEqualTo("5");
    assertThat(trs.get(1).select("td").get(2).text()).isEqualTo("6");
  }

  @Test
  public void batchFilterFillMissing() {
    Map<String, Object> context = ImmutableMap.of("items", (Object) Lists.newArrayList(
        "1", "2", "3", "4"));

    Document dom = Jsoup.parseBodyFragment(render("batch-filter", context));
    assertThat(dom.select("tr")).hasSize(2);

    Elements trs = dom.select("tr");
    assertThat(trs.get(0).select("td")).hasSize(3);
    assertThat(trs.get(0).select("td").get(0).text()).isEqualTo("1");
    assertThat(trs.get(0).select("td").get(1).text()).isEqualTo("2");
    assertThat(trs.get(0).select("td").get(2).text()).isEqualTo("3");
    assertThat(trs.get(1).select("td")).hasSize(3);
    assertThat(trs.get(1).select("td").get(0).text()).isEqualTo("4");
    assertThat(trs.get(1).select("td").get(1).text()).isEqualTo("foo");
    assertThat(trs.get(1).select("td").get(2).text()).isEqualTo("foo");
  }

  private String render(String template, Map<String, Object> context) {
    try {
      return jinjava.render(Resources.toString(Resources.getResource(String.format("filter/%s.jinja", template)), StandardCharsets.UTF_8), context);
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }

}
