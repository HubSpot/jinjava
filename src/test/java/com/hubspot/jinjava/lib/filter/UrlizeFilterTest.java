package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Resources;
import com.hubspot.jinjava.Jinjava;

public class UrlizeFilterTest {

  Jinjava jinjava;

  @Before
  public void setup() throws Exception {
    jinjava = new Jinjava();
    jinjava.getGlobalContext().put("txt", Resources.toString(Resources.getResource("filter/urlize.txt"), StandardCharsets.UTF_8));
  }

  @Test
  public void urlizeText() {
    Document dom = Jsoup.parseBodyFragment(jinjava.render("{{ txt|urlize }}", new HashMap<String, Object>()));
    assertThat(dom.select("a")).hasSize(3);
    assertThat(dom.select("a").get(0).attr("href")).isEqualTo("http://www.espn.com");
    assertThat(dom.select("a").get(1).attr("href")).isEqualTo("http://yahoo.com");
    assertThat(dom.select("a").get(2).attr("href")).isEqualTo("https://hubspot.com");
  }

}
