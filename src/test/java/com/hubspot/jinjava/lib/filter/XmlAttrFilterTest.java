package com.hubspot.jinjava.lib.filter;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.hubspot.jinjava.BaseJinjavaTest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

public class XmlAttrFilterTest extends BaseJinjavaTest {

  @Test
  public void testXmlAttr() {
    Map<String, Object> context = new HashMap<>();
    context.put("variable", 42);

    Document dom = Jsoup.parseBodyFragment(
      jinjava.render(
        "<ul{{ {'class': 'my_list', 'missing': none, 'id': 'list-' ~ variable}|xmlattr }}></ul>",
        context
      )
    );

    assertThat(dom.select("ul").attr("class")).isEqualTo("my_list");
    assertThat(dom.select("ul").attr("id")).isEqualTo("list-42");
    assertThat(dom.select("ul").attr("missing")).isEmpty();
  }

  @Test
  public void itDoesNotAllowInvalidKeys() {
    List<String> invalidStrings = ImmutableList.of("\t", "\n", "\f", " ", "/", ">", "=");
    invalidStrings.forEach(invalidString ->
      assertThat(
        jinjava
          .renderForResult(
            String.format("{{ {'%s': 'foo'}|xmlattr }}", invalidString),
            Collections.emptyMap()
          )
          .getErrors()
      )
        .matches(templateErrors ->
          templateErrors.size() == 1 &&
          templateErrors
            .get(0)
            .getException()
            .getCause()
            .getCause() instanceof IllegalArgumentException
        )
    );
  }
}
