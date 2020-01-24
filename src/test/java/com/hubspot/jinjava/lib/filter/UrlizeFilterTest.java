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
  private String inputText;

  @Before
  public void setup() throws Exception {
    jinjava = new Jinjava();
    jinjava.getGlobalContext().setAutoEscape(true); // Ensure that autoescape does not affect generated HTML
    inputText = Resources.toString(Resources.getResource("filter/urlize.txt"), StandardCharsets.UTF_8);
    jinjava.getGlobalContext().put("txt", inputText);
  }

  @Test
  public void urlizeTextWorksWhenSafe() {
    Document dom = Jsoup.parseBodyFragment(jinjava.render("{{ txt|urlize|safe }}", new HashMap<String, Object>()));
    assertThat(dom.select("a")).hasSize(3);
    assertThat(dom.select("a").get(0).attr("href")).isEqualTo("http://www.espn.com");
    assertThat(dom.select("a").get(1).attr("href")).isEqualTo("http://yahoo.com");
    assertThat(dom.select("a").get(2).attr("href")).isEqualTo("https://hubspot.com");
  }

  @Test
  public void urlizeTextIsEscaped() {
    String escapedHtml = "This is some text. Go to &lt;a href=&quot;http://www.espn.com&quot;&gt;http://www.espn.com&lt;/a&gt; if you like sports. Check out &lt;a href=&quot;http://yahoo.com&quot;&gt;http://yahoo.com&lt;/a&gt; if you like news. I like &lt;a href=&quot;https://hubspot.com&quot;&gt;https://hubspot.com&lt;/a&gt;.";
    String rendered = jinjava.render("{{ txt|urlize }}", new HashMap<String, Object>());
    assertThat(rendered).isEqualTo(escapedHtml);
  }
}
